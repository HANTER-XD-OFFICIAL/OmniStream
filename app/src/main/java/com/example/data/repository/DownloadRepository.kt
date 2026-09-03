package com.example.data.repository

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.os.PowerManager
import com.example.data.api.YtDlpClient
import com.example.data.local.AppDatabase
import com.example.data.local.DownloadDao
import com.example.data.local.DownloadEntity
import com.example.data.local.DownloadStatus
import com.example.data.local.MediaType
import com.example.service.DownloadForegroundService
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
        .callTimeout(0, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
) {
    val allDownloads: Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeJobs = ConcurrentHashMap<Long, Job>()

    companion object {
        const val APP_FOLDER_NAME = "OmniStream"

        @Volatile
        private var INSTANCE: DownloadRepository? = null

        fun getInstance(context: Context, downloadDao: DownloadDao? = null): DownloadRepository {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                val dao = downloadDao ?: AppDatabase.getInstance(appContext).downloadDao()
                INSTANCE ?: DownloadRepository(appContext, dao).also { INSTANCE = it }
            }
        }

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
        // Ensure background service is running to keep process alive in background & screen off
        DownloadForegroundService.start(context)

        // Cancel existing job if any
        activeJobs[downloadId]?.cancel()

        val job = scope.launch {
            val item = downloadDao.getDownloadById(downloadId) ?: return@launch
            downloadDao.updateStatus(downloadId, DownloadStatus.DOWNLOADING)

            // Hold WakeLock specifically for this active download job to prevent CPU sleep when screen is turned off
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val jobWakeLock = pm?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "OmniStream:DownloadJob_$downloadId"
            )?.apply {
                setReferenceCounted(false)
                try { acquire(60 * 60 * 1000L) } catch (_: Exception) {}
            }

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

                val existingBytes = if (workingFile.exists()) workingFile.length() else 0L

                fun buildDownloadRequest(streamUrl: String, isYouTubeStream: Boolean = false, rangeStart: Long = 0L): Request {
                    val builder = Request.Builder()
                        .url(streamUrl)
                        .addHeader("Accept", "*/*")
                        .addHeader("Accept-Encoding", "identity")
                        .addHeader("Connection", "keep-alive")

                    if (rangeStart > 0L) {
                        builder.addHeader("Range", "bytes=$rangeStart-")
                    }

                    if (streamUrl.contains("savenow.to") || streamUrl.contains("loader.to") || streamUrl.contains("affadaffa.com")) {
                        builder.addHeader("Referer", "https://loader.to/")
                        builder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    } else if (isYouTubeStream || streamUrl.contains("googlevideo.com")) {
                        builder.addHeader("User-Agent", "com.google.android.youtube/19.09.37 (Linux; U; Android 14; US) gzip")
                    } else {
                        builder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    }
                    return builder.build()
                }

                var activeResponse: okhttp3.Response? = null
                var streamBody: okhttp3.ResponseBody? = null
                var primaryUrl = item.downloadUrl

                // Multi-gateway stream retrieval: Try primary URL, then candidate URLs, then fresh dynamic streams
                val candidateUrls = mutableListOf<String>()

                // Check if this is a YouTube stream (or unresolved / Invidious stream), resolve the direct download URL
                val isYouTubeStream = primaryUrl.startsWith("yt_loader:") ||
                        primaryUrl.contains("loader.to") ||
                        primaryUrl.contains("inv.tux.pizza") ||
                        primaryUrl.contains("latest_version") ||
                        primaryUrl.contains("yewtu.be") ||
                        primaryUrl.contains("invidious") ||
                        primaryUrl.contains("googlevideo.com") ||
                        ((item.sourceUrl.contains("youtube.com") || item.sourceUrl.contains("youtu.be")) && !primaryUrl.contains("savenow.to") && !primaryUrl.contains("piped"))

                if (isYouTubeStream) {
                    val directResolved = ytDlpClient.resolveYouTubeDirectStream(
                        targetQueryOrUrl = primaryUrl,
                        sourceUrl = item.sourceUrl,
                        formatPreference = item.resolution ?: "720"
                    )
                    if (!directResolved.isNullOrBlank() && directResolved.startsWith("http")) {
                        primaryUrl = directResolved
                        try {
                            downloadDao.updateDownloadUrl(downloadId, directResolved)
                        } catch (_: Exception) {}
                    }
                }

                if (primaryUrl.isNotBlank() && primaryUrl.startsWith("http") && !primaryUrl.startsWith("yt_loader:")) {
                    candidateUrls.add(primaryUrl)
                }

                val targetQuery = if (item.sourceUrl.isNotBlank() && item.sourceUrl.startsWith("http")) item.sourceUrl else primaryUrl
                if (candidateUrls.isEmpty() && targetQuery.startsWith("http")) {
                    try {
                        val freshInfo = ytDlpClient.fetchVideoInfo(targetQuery)
                        freshInfo.formats.forEach { f ->
                            val u = f.url
                            if (!u.isNullOrBlank() && u.startsWith("http") && !u.startsWith("yt_loader:") && !candidateUrls.contains(u)) {
                                candidateUrls.add(u)
                            }
                        }
                    } catch (_: Exception) {}
                }

                fun isValidMediaContentType(cType: String): Boolean {
                    val lower = cType.lowercase()
                    if (lower.contains("text/html") ||
                        lower.contains("text/plain") ||
                        lower.contains("application/json") ||
                        lower.contains("application/xhtml") ||
                        lower.contains("application/xml")
                    ) {
                        return false
                    }
                    return true
                }

                for (urlToTry in candidateUrls) {
                    if (activeResponse != null && streamBody != null) break
                    if (urlToTry.isBlank() || !urlToTry.startsWith("http")) continue

                    val isYt = urlToTry.contains("googlevideo.com") || urlToTry.contains("youtube.com") || urlToTry.contains("youtu.be")

                    // Attempt 1: Targeted request (with Range if existing partial bytes exist)
                    try {
                        val resp = okHttpClient.newCall(buildDownloadRequest(urlToTry, isYouTubeStream = isYt, rangeStart = existingBytes)).execute()
                        val cType = resp.header("Content-Type", "") ?: ""
                        if (resp.isSuccessful && resp.body != null && isValidMediaContentType(cType)) {
                            activeResponse = resp
                            streamBody = resp.body
                            break
                        } else {
                            resp.close()
                        }
                    } catch (_: Exception) {}

                    // Attempt 2: Clean standard request (reset to 0 if Range was rejected or unsupported)
                    if (activeResponse == null) {
                        try {
                            val cleanReq = buildDownloadRequest(urlToTry, isYouTubeStream = isYt, rangeStart = 0L)
                            val resp = okHttpClient.newCall(cleanReq).execute()
                            val cType = resp.header("Content-Type", "") ?: ""
                            if (resp.isSuccessful && resp.body != null && isValidMediaContentType(cType)) {
                                activeResponse = resp
                                streamBody = resp.body
                                break
                            } else {
                                resp.close()
                            }
                        } catch (_: Exception) {}
                    }
                }

                if (activeResponse == null || streamBody == null) {
                    downloadDao.updateStatus(
                        downloadId,
                        DownloadStatus.FAILED,
                        error = "Direct media stream could not be established. Link may require login or has expired."
                    )
                    return@launch
                }

                val is206 = activeResponse.code == 206
                val appendMode = is206 && existingBytes > 0L
                val body = streamBody

                val remoteLength = body.contentLength()
                val totalBytes = if (appendMode && remoteLength > 0L) {
                    existingBytes + remoteLength
                } else if (remoteLength > 0L) {
                    remoteLength
                } else if (item.totalBytes > 0L) {
                    item.totalBytes
                } else {
                    0L
                }

                var inputStream: InputStream? = null
                var outputStream: FileOutputStream? = null

                try {
                    inputStream = body.byteStream()
                    outputStream = FileOutputStream(workingFile, appendMode)

                    val buffer = ByteArray(64 * 1024)
                    var bytesRead: Int
                    var totalDownloaded = if (appendMode) existingBytes else 0L
                    var lastUpdateTime = System.currentTimeMillis()
                    var bytesSinceLastUpdate = 0L
                    var firstChunkChecked = appendMode

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        if (!firstChunkChecked && bytesRead > 0) {
                            firstChunkChecked = true
                            // Check if the beginning is an error HTML or JSON payload
                            val headerPrefix = String(buffer, 0, minOf(bytesRead, 128))
                            if (headerPrefix.startsWith("<!DOCTYPE", ignoreCase = true) ||
                                headerPrefix.startsWith("<html", ignoreCase = true) ||
                                headerPrefix.startsWith("{\"error\"", ignoreCase = true) ||
                                headerPrefix.startsWith("{\"status\":\"error\"", ignoreCase = true)
                            ) {
                                // Error payload detected
                                outputStream.close()
                                workingFile.delete()
                                downloadDao.updateStatus(
                                    downloadId,
                                    DownloadStatus.FAILED,
                                    error = "Remote media stream returned an error page. Please try another quality format."
                                )
                                return@launch
                            }
                        }

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
                try {
                    if (jobWakeLock?.isHeld == true) {
                        jobWakeLock.release()
                    }
                } catch (_: Exception) {}
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
        DownloadForegroundService.start(context)
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
