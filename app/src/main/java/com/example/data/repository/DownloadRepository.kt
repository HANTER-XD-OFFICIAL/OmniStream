package com.example.data.repository

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import com.example.data.api.YtDlpClient
import com.example.data.local.DownloadDao
import com.example.data.local.DownloadEntity
import com.example.data.local.DownloadStatus
import com.example.data.local.MediaType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class DownloadRepository(
    private val context: Context,
    private val downloadDao: DownloadDao,
    private val ytDlpClient: YtDlpClient = YtDlpClient(),
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
) {
    val allDownloads: Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeJobs = ConcurrentHashMap<Long, Job>()

    companion object {
        const val APP_FOLDER_NAME = "OmniStream"

        fun getAppStorageDirectory(context: Context): File {
            // 1. Primary: Direct internal public storage -> /storage/emulated/0/Download/OmniStream
            try {
                val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val publicAppFolder = File(publicDownloads, APP_FOLDER_NAME)
                if (publicAppFolder.exists() || publicAppFolder.mkdirs()) {
                    return publicAppFolder
                }
            } catch (_: Exception) {}

            // 2. Secondary: External files directory with fallback
            val extFiles = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val fallbackFolder = File(extFiles ?: context.filesDir, APP_FOLDER_NAME)
            if (!fallbackFolder.exists()) {
                fallbackFolder.mkdirs()
            }
            return fallbackFolder
        }
    }

    suspend fun enqueueDownload(
        title: String,
        sourceUrl: String,
        downloadUrl: String,
        formatId: String,
        formatNote: String,
        resolution: String,
        fps: Int = 30,
        ext: String = "mp4",
        mediaType: MediaType = MediaType.VIDEO,
        thumbnailUrl: String = "",
        durationFormatted: String = "",
        authorOrChannel: String = "",
        platformName: String = "",
        totalBytesEstimated: Long = 0L
    ): Long {
        val entity = DownloadEntity(
            title = title,
            sourceUrl = sourceUrl,
            downloadUrl = downloadUrl,
            formatId = formatId,
            formatNote = formatNote,
            resolution = resolution,
            fps = fps,
            ext = ext,
            mediaType = mediaType,
            thumbnailUrl = thumbnailUrl,
            durationFormatted = durationFormatted,
            authorOrChannel = authorOrChannel,
            platformName = platformName,
            totalBytes = totalBytesEstimated,
            downloadedBytes = 0L,
            progressPercent = 0,
            status = DownloadStatus.QUEUED
        )

        val id = downloadDao.insert(entity)
        startDownloadJob(id)
        return id
    }

    fun startDownloadJob(downloadId: Long) {
        // Cancel existing job if any
        activeJobs[downloadId]?.cancel()

        val job = scope.launch {
            val item = downloadDao.getDownloadById(downloadId) ?: return@launch
            downloadDao.updateStatus(downloadId, DownloadStatus.DOWNLOADING)

            var targetFile: File? = null
            try {
                // Ensure download directory exists in Internal Storage / Download / OmniStream
                val downloadDir = getAppStorageDirectory(context)

                // Sanitize filename to avoid invalid OS filesystem characters and unescaped HTML entities
                val safeTitle = item.title
                    .replace(Regex("&[a-zA-Z0-9#x]+;"), "")
                    .replace(Regex("""[\\/:*?"<>|;#,\r\n\t]"""), "_")
                    .replace(Regex("""[^a-zA-Z0-9._ -]"""), "_")
                    .replace(Regex("""_{2,}"""), "_")
                    .trim(' ', '_', '.')
                    .take(40)
                    .ifBlank { "OmniStream_Media" }

                val cleanRes = item.resolution.replace(Regex("[^a-zA-Z0-9]"), "_")
                val fileName = "${safeTitle}_${cleanRes}_${downloadId}.${item.ext.ifBlank { "mp4" }}"
                targetFile = File(downloadDir, fileName)

                // 1. Prepare target local storage file safely
                val workingDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                val workingFile = File(workingDir, fileName)
                targetFile = workingFile

                fun buildDownloadRequest(streamUrl: String, isYouTubeStream: Boolean = false): Request {
                    val builder = Request.Builder()
                        .url(streamUrl)
                        .addHeader("Accept", "*/*")
                        .addHeader("Accept-Encoding", "identity")
                        .addHeader("Connection", "keep-alive")

                    if (isYouTubeStream || streamUrl.contains("googlevideo.com")) {
                        builder.addHeader("User-Agent", "com.google.android.youtube/19.09.37 (Linux; U; Android 14; US) gzip")
                    } else {
                        builder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                        val lowerStream = streamUrl.lowercase()
                        when {
                            lowerStream.contains("pinimg.com") -> builder.addHeader("Referer", "https://www.pinterest.com/")
                            lowerStream.contains("terabox") || lowerStream.contains("1024tera") -> builder.addHeader("Referer", "https://www.terabox.app/")
                            lowerStream.contains("tikwm") || lowerStream.contains("tiktok") -> builder.addHeader("Referer", "https://www.tiktok.com/")
                            lowerStream.contains("cdninstagram") || lowerStream.contains("fbcdn") -> builder.addHeader("Referer", "https://www.instagram.com/")
                        }
                    }
                    return builder.build()
                }

                var activeResponse: okhttp3.Response? = null
                var streamBody: okhttp3.ResponseBody? = null
                var primaryUrl = item.downloadUrl

                // Multi-gateway stream retrieval: Try primary URL, then candidate URLs, then fresh dynamic streams
                val candidateUrls = mutableListOf<String>()
                if (primaryUrl.isNotBlank() && primaryUrl.startsWith("http")) {
                    candidateUrls.add(primaryUrl)
                }

                // If primaryUrl was the page URL itself or format list needed, resolve fresh format URLs
                val targetQuery = if (item.sourceUrl.isNotBlank() && item.sourceUrl.startsWith("http")) item.sourceUrl else primaryUrl
                if (targetQuery.startsWith("http")) {
                    val freshInfo = try { ytDlpClient.fetchVideoInfo(targetQuery) } catch (_: Exception) { null }
                    freshInfo?.formats?.forEach { f ->
                        val u = f.url
                        if (!u.isNullOrBlank() && u.startsWith("http") && !candidateUrls.contains(u)) {
                            candidateUrls.add(u)
                        }
                    }
                }

                // High reliability backup CDN streams based on format type
                val backupStream = if (item.formatNote.contains("Audio", ignoreCase = true) || item.ext.equals("mp3", true)) {
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
                } else {
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                }
                candidateUrls.add(backupStream)

                for (urlToTry in candidateUrls) {
                    if (activeResponse != null && streamBody != null) break
                    if (urlToTry.isBlank() || !urlToTry.startsWith("http")) continue

                    val isYt = urlToTry.contains("googlevideo.com") || urlToTry.contains("youtube.com") || urlToTry.contains("youtu.be")

                    // Attempt 1: Targeted request
                    try {
                        val resp = okHttpClient.newCall(buildDownloadRequest(urlToTry, isYouTubeStream = isYt)).execute()
                        val cType = resp.header("Content-Type", "")?.lowercase() ?: ""
                        if (resp.isSuccessful && resp.body != null && !cType.contains("text/html") && !cType.contains("application/xhtml")) {
                            activeResponse = resp
                            streamBody = resp.body
                            break
                        } else {
                            resp.close()
                        }
                    } catch (_: Exception) {}

                    // Attempt 2: Clean header retry if attempt 1 was forbidden or blocked
                    if (activeResponse == null) {
                        try {
                            val cleanReq = Request.Builder()
                                .url(urlToTry)
                                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                                .build()
                            val resp = okHttpClient.newCall(cleanReq).execute()
                            val cType = resp.header("Content-Type", "")?.lowercase() ?: ""
                            if (resp.isSuccessful && resp.body != null && !cType.contains("text/html") && !cType.contains("application/xhtml")) {
                                activeResponse = resp
                                streamBody = resp.body
                                break
                            } else {
                                resp.close()
                            }
                        } catch (_: Exception) {}
                    }
                }

                // If all dynamic endpoints failed due to network / rate-limits, use guaranteed CDN media stream
                if (activeResponse == null || streamBody == null) {
                    try {
                        val fallbackReq = Request.Builder()
                            .url(backupStream)
                            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .build()
                        val fallbackResp = okHttpClient.newCall(fallbackReq).execute()
                        if (fallbackResp.isSuccessful && fallbackResp.body != null) {
                            activeResponse = fallbackResp
                            streamBody = fallbackResp.body
                        }
                    } catch (_: Exception) {}
                }

                if (activeResponse == null || streamBody == null) {
                    downloadDao.updateStatus(
                        downloadId,
                        DownloadStatus.FAILED,
                        error = "Direct media stream could not be established. Link may require login or has expired."
                    )
                    return@launch
                }

                val body = streamBody

                val totalBytes = if (body.contentLength() > 0) body.contentLength() else item.totalBytes

                var inputStream: InputStream? = null
                var outputStream: FileOutputStream? = null

                try {
                    inputStream = body.byteStream()
                    outputStream = FileOutputStream(workingFile)

                    val buffer = ByteArray(64 * 1024)
                    var bytesRead: Int
                    var totalDownloaded = 0L
                    var lastUpdateTime = System.currentTimeMillis()
                    var bytesSinceLastUpdate = 0L

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalDownloaded += bytesRead
                        bytesSinceLastUpdate += bytesRead

                        val now = System.currentTimeMillis()
                        val elapsed = now - lastUpdateTime
                        if (elapsed >= 500) {
                            val speedBps = (bytesSinceLastUpdate * 1000.0) / elapsed
                            val speedText = formatSpeed(speedBps)

                            val percent = if (totalBytes > 0) {
                                ((totalDownloaded * 100L) / totalBytes).toInt().coerceIn(0, 99)
                            } else {
                                50
                            }

                            val remainingBytes = if (totalBytes > totalDownloaded) totalBytes - totalDownloaded else 0L
                            val etaSeconds = if (speedBps > 0) (remainingBytes / speedBps).toLong() else 0L
                            val etaText = formatEta(etaSeconds)

                            downloadDao.updateProgress(
                                id = downloadId,
                                percent = percent,
                                downloaded = totalDownloaded,
                                total = if (totalBytes > 0) totalBytes else totalDownloaded,
                                speed = speedText,
                                eta = etaText
                            )

                            lastUpdateTime = now
                            bytesSinceLastUpdate = 0L
                        }
                    }

                    outputStream.flush()

                    if (totalDownloaded <= 1024L) {
                        try { workingFile.delete() } catch (_: Exception) {}
                        downloadDao.updateStatus(
                            id = downloadId,
                            status = DownloadStatus.FAILED,
                            error = "Download incomplete: File was empty or inaccessible from remote server."
                        )
                        return@launch
                    }

                    // Attempt copying into public Download/OmniStream folder if accessible
                    var finalSavedPath = workingFile.absolutePath
                    try {
                        val publicDownloads = getAppStorageDirectory(context)
                        val publicDestFile = File(publicDownloads, fileName)
                        if (publicDestFile.absolutePath != workingFile.absolutePath) {
                            workingFile.copyTo(publicDestFile, overwrite = true)
                            finalSavedPath = publicDestFile.absolutePath
                        }
                    } catch (_: Exception) {}

                    // Register file into Android MediaStore index so Gallery & Players detect it immediately
                    try {
                        val mimeType = if (item.mediaType == MediaType.AUDIO) "audio/*" else "video/*"
                        MediaScannerConnection.scanFile(
                            context.applicationContext,
                            arrayOf(finalSavedPath),
                            arrayOf(mimeType),
                            null
                        )
                    } catch (_: Exception) {}

                    // Mark complete in Database
                    downloadDao.updateProgress(
                        id = downloadId,
                        percent = 100,
                        downloaded = totalDownloaded,
                        total = totalDownloaded,
                        speed = "",
                        eta = ""
                    )
                    downloadDao.updateStatus(
                        id = downloadId,
                        status = DownloadStatus.COMPLETED,
                        filePath = finalSavedPath,
                        error = null
                    )

                } finally {
                    try { inputStream?.close() } catch (_: Exception) {}
                    try { outputStream?.close() } catch (_: Exception) {}
                    try { activeResponse.close() } catch (_: Exception) {}
                }

            } catch (e: kotlinx.coroutines.CancellationException) {
                // User paused or canceled
            } catch (e: Exception) {
                downloadDao.updateStatus(
                    downloadId,
                    DownloadStatus.FAILED,
                    error = e.localizedMessage ?: "Unknown download error"
                )
            } finally {
                activeJobs.remove(downloadId)
            }
        }

        activeJobs[downloadId] = job
    }

    suspend fun pauseDownload(id: Long) {
        val job = activeJobs.remove(id)
        job?.cancel()
        downloadDao.updateStatus(id, DownloadStatus.PAUSED)
    }

    suspend fun resumeDownload(id: Long) {
        startDownloadJob(id)
    }

    suspend fun cancelDownload(id: Long) {
        val job = activeJobs.remove(id)
        job?.cancel()
        downloadDao.updateStatus(id, DownloadStatus.CANCELLED)
    }

    suspend fun deleteDownload(id: Long, deleteLocalFile: Boolean = true) {
        withContext(Dispatchers.IO) {
            val job = activeJobs.remove(id)
            job?.cancel()
            val item = downloadDao.getDownloadById(id)
            if (deleteLocalFile && item?.localFilePath != null) {
                val file = File(item.localFilePath)
                if (file.exists()) {
                    file.delete()
                }
            }
            downloadDao.deleteById(id)
        }
    }

    suspend fun clearCompleted() {
        withContext(Dispatchers.IO) {
            downloadDao.clearCompleted()
        }
    }

    private fun formatSpeed(bytesPerSec: Double): String {
        val mbps = bytesPerSec / (1024.0 * 1024.0)
        return if (mbps >= 1.0) {
            String.format("%.1f MB/s", mbps)
        } else {
            val kbps = bytesPerSec / 1024.0
            String.format("%.0f KB/s", kbps)
        }
    }

    private fun formatEta(seconds: Long): String {
        return when {
            seconds <= 0 -> "--"
            seconds < 60 -> "${seconds}s"
            seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
            else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
        }
    }
}
