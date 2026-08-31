package com.example.data.repository

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
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
            // First priority: Phone Public Downloads / OmniStream
            val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val omniStreamFolder = File(publicDownloads, APP_FOLDER_NAME)

            if (omniStreamFolder.exists() || omniStreamFolder.mkdirs()) {
                return omniStreamFolder
            }

            // Fallback: App external files directory
            val extFiles = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val extFolder = File(extFiles ?: context.filesDir, APP_FOLDER_NAME)
            if (!extFolder.exists()) {
                extFolder.mkdirs()
            }
            return extFolder
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

                // Sanitize filename to avoid invalid OS filesystem characters
                val safeTitle = item.title
                    .replace(Regex("[\\\\/:*?\"<>|]"), "_")
                    .trim()
                    .take(60)
                val fileName = "${safeTitle}_${item.resolution.replace(" ", "_")}_${downloadId}.${item.ext}"
                targetFile = File(downloadDir, fileName)

                fun buildDownloadRequest(streamUrl: String): Request {
                    val builder = Request.Builder()
                        .url(streamUrl)
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                        .addHeader("Accept", "*/*")
                        .addHeader("Accept-Encoding", "identity")
                        .addHeader("Connection", "keep-alive")

                    if (item.sourceUrl.isNotBlank()) {
                        builder.addHeader("Referer", item.sourceUrl)
                    }
                    return builder.build()
                }

                val response = okHttpClient.newCall(buildDownloadRequest(item.downloadUrl)).execute()

                if (!response.isSuccessful) {
                    val code = response.code
                    val msg = response.message
                    response.close()
                    downloadDao.updateStatus(
                        downloadId,
                        DownloadStatus.FAILED,
                        error = "Source server returned HTTP $code ($msg). Video link may be private or token expired."
                    )
                    return@launch
                }

                val body = response.body
                val contentType = response.header("Content-Type", "")?.lowercase() ?: ""
                if (body == null) {
                    downloadDao.updateStatus(
                        downloadId,
                        DownloadStatus.FAILED,
                        error = "Empty response stream received from source"
                    )
                    return@launch
                }

                // If server returned an HTML error page or cloud login portal instead of video/audio stream
                if (contentType.contains("text/html") || contentType.contains("application/xhtml")) {
                    response.close()
                    downloadDao.updateStatus(
                        downloadId,
                        DownloadStatus.FAILED,
                        error = "Direct stream unavailable. The link is a web portal or requires cloud login. Please use direct download links or configure API in Settings."
                    )
                    return@launch
                }

                val totalBytes = if (body.contentLength() > 0) body.contentLength() else item.totalBytes

                var inputStream: InputStream? = null
                var outputStream: FileOutputStream? = null

                try {
                    inputStream = body.byteStream()
                    outputStream = FileOutputStream(targetFile)

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
                        try { targetFile.delete() } catch (_: Exception) {}
                        downloadDao.updateStatus(
                            id = downloadId,
                            status = DownloadStatus.FAILED,
                            error = "Download incomplete: File was empty or inaccessible from remote server."
                        )
                        return@launch
                    }

                    // Register file into Android MediaStore index so Gallery & Players detect it immediately
                    try {
                        val mimeType = if (item.mediaType == MediaType.AUDIO) "audio/*" else "video/*"
                        MediaScannerConnection.scanFile(
                            context.applicationContext,
                            arrayOf(targetFile.absolutePath),
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
                        filePath = targetFile.absolutePath,
                        error = null
                    )

                } finally {
                    try { inputStream?.close() } catch (_: Exception) {}
                    try { outputStream?.close() } catch (_: Exception) {}
                    try { response.close() } catch (_: Exception) {}
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
