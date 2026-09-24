package com.example.data.api

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Robust GitHub In-App Update Engine for OmniStream
 * - Automatic startup check for newer releases
 * - In-app direct APK download with real-time speed & progress
 * - Seamless automatic package installer launch without redirecting to external websites
 */
class AppUpdateManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val prefs = appContext.getSharedPreferences("omnistream_updates", Context.MODE_PRIVATE)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()

    private var activeDownloadJob: Job? = null

    companion object {
        private const val TAG = "OmniStreamUpdater"
        const val GITHUB_REPO = "HANTER-XD-OFFICIAL/OmniStream"
        private const val PREF_DISMISSED_TAG = "dismissed_tag"

        @Volatile
        private var INSTANCE: AppUpdateManager? = null

        fun getInstance(context: Context): AppUpdateManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppUpdateManager(context).also { INSTANCE = it }
            }
        }

        /**
         * Compare remote version vs local version
         * Supports semver like 2.0.0, 2.0.1, 2.1.0, tags like v2.0.0-beta, v2.0.0-beta_OmniStream
         */
        fun isNewerVersion(currentVersion: String, remoteTag: String): Boolean {
            try {
                val currentClean = cleanVersionString(currentVersion)
                val remoteClean = cleanVersionString(remoteTag)

                val currentParts = extractVersionParts(currentClean)
                val remoteParts = extractVersionParts(remoteClean)

                for (i in 0 until 3) {
                    val r = remoteParts.getOrElse(i) { 0 }
                    val c = currentParts.getOrElse(i) { 0 }
                    if (r > c) return true
                    if (r < c) return false
                }

                // If major.minor.patch are equal, check if current is beta/alpha while remote is stable
                val currentIsPre = currentClean.contains("beta", ignoreCase = true) || currentClean.contains("alpha", ignoreCase = true)
                val remoteIsPre = remoteClean.contains("beta", ignoreCase = true) || remoteClean.contains("alpha", ignoreCase = true)

                if (currentIsPre && !remoteIsPre) {
                    return true // Stable is newer than beta
                }

                // If both are pre-release, compare pre-release numbers if present (e.g. beta2 vs beta1)
                if (currentIsPre && remoteIsPre) {
                    val currentPreNum = extractTrailingNumber(currentClean)
                    val remotePreNum = extractTrailingNumber(remoteClean)
                    if (remotePreNum > currentPreNum) return true
                }

                return false
            } catch (e: Exception) {
                Log.w(TAG, "Version comparison error: ${e.message}")
                return false
            }
        }

        private fun cleanVersionString(version: String): String {
            var v = version.trim()
            if (v.startsWith("v", ignoreCase = true)) {
                v = v.substring(1)
            }
            return v
        }

        private fun extractVersionParts(version: String): List<Int> {
            val matcher = Pattern.compile("(\\d+)").matcher(version)
            val parts = mutableListOf<Int>()
            while (matcher.find() && parts.size < 3) {
                parts.add(matcher.group(1)?.toIntOrNull() ?: 0)
            }
            return parts
        }

        private fun extractTrailingNumber(str: String): Int {
            val matcher = Pattern.compile("(\\d+)$").matcher(str)
            return if (matcher.find()) matcher.group(1)?.toIntOrNull() ?: 0 else 0
        }
    }

    /**
     * Checks GitHub for updates
     * @param isManual True if clicked from Settings or Developer hub; false if automatic app start
     */
    fun checkForUpdates(isManual: Boolean = false) {
        scope.launch {
            _updateState.value = UpdateState.Checking(isManual = isManual)

            try {
                val releaseInfo = fetchLatestGitHubRelease()

                if (releaseInfo == null) {
                    if (isManual) {
                        _updateState.value = UpdateState.Error("No release packages found on GitHub.", isManual = isManual)
                    } else {
                        _updateState.value = UpdateState.Idle
                    }
                    return@launch
                }

                val currentVersion = BuildConfig.VERSION_NAME
                val isNewer = isNewerVersion(currentVersion, releaseInfo.tagName)
                val dismissedTag = prefs.getString(PREF_DISMISSED_TAG, null)

                val updatedInfo = releaseInfo.copy(
                    isUpdateAvailable = isNewer,
                    currentVersion = currentVersion
                )

                if (isNewer) {
                    _updateState.value = UpdateState.UpdateAvailable(updatedInfo, isManual = isManual)
                    // If automatic start check, only show if user hasn't dismissed this exact tag in this session
                    if (isManual || dismissedTag != releaseInfo.tagName) {
                        _showUpdateDialog.value = true
                    }
                } else {
                    _updateState.value = UpdateState.UpToDate(updatedInfo, isManual = isManual)
                    if (isManual) {
                        // In manual mode, we also allow the user to view the release dialog and reinstall if desired
                        _showUpdateDialog.value = true
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check for updates: ${e.message}", e)
                if (isManual) {
                    _updateState.value = UpdateState.Error(
                        e.localizedMessage ?: "Network connection error while checking GitHub Releases.",
                        isManual = true
                    )
                } else {
                    _updateState.value = UpdateState.Idle
                }
            }
        }
    }

    private suspend fun fetchLatestGitHubRelease(): AppReleaseInfo? = withContext(Dispatchers.IO) {
        // Strategy 1: Fetch releases list (up to 5 releases) to find the best APK release
        val urlsToTry = listOf(
            "https://api.github.com/repos/$GITHUB_REPO/releases?per_page=5",
            "https://api.github.com/repos/$GITHUB_REPO/releases/latest"
        )

        for (url in urlsToTry) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Android; OmniStream-App)")
                    .header("Accept", "application/vnd.github.v3+json")
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use

                    val jsonStr = response.body?.string() ?: return@use

                    if (jsonStr.trim().startsWith("[")) {
                        val array = JSONArray(jsonStr)
                        for (i in 0 until array.length()) {
                            val relObj = array.getJSONObject(i)
                            val info = parseReleaseObject(relObj)
                            if (info != null) return@withContext info
                        }
                    } else if (jsonStr.trim().startsWith("{")) {
                        val relObj = JSONObject(jsonStr)
                        val info = parseReleaseObject(relObj)
                        if (info != null) return@withContext info
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error fetching from $url: ${e.message}")
            }
        }

        null
    }

    private fun parseReleaseObject(obj: JSONObject): AppReleaseInfo? {
        val tagName = obj.optString("tag_name", "")
        if (tagName.isBlank()) return null

        val name = obj.optString("name", tagName)
        val body = obj.optString("body", "Official OmniStream Android release with latest high-speed media downloader engine.")
        val publishedAt = obj.optString("published_at", "")
        val htmlUrl = obj.optString("html_url", "https://github.com/$GITHUB_REPO/releases")

        val assetsArray = obj.optJSONArray("assets") ?: return null

        var apkName = ""
        var apkSize: Long = 0
        var apkDownloadUrl = ""

        for (j in 0 until assetsArray.length()) {
            val asset = assetsArray.getJSONObject(j)
            val assetName = asset.optString("name", "")
            if (assetName.endsWith(".apk", ignoreCase = true)) {
                apkName = assetName
                apkSize = asset.optLong("size", 0L)
                apkDownloadUrl = asset.optString("browser_download_url", "")
                break
            }
        }

        if (apkDownloadUrl.isBlank()) {
            return null
        }

        val cleanTag = cleanVersionString(tagName)

        return AppReleaseInfo(
            tagName = tagName,
            name = name,
            body = body,
            publishedAt = publishedAt,
            htmlUrl = htmlUrl,
            apkAssetName = apkName,
            apkSize = apkSize,
            apkDownloadUrl = apkDownloadUrl,
            isUpdateAvailable = false,
            remoteVersionClean = cleanTag,
            currentVersion = BuildConfig.VERSION_NAME
        )
    }

    /**
     * Downloads APK directly inside app with live progress reporting
     */
    fun startDownload(info: AppReleaseInfo) {
        activeDownloadJob?.cancel()
        activeDownloadJob = scope.launch {
            _updateState.value = UpdateState.Downloading(
                info = info,
                progressPercent = 0,
                downloadedBytes = 0,
                totalBytes = info.apkSize,
                speedText = "0 KB/s"
            )

            try {
                val targetFile = getApkDownloadTarget(info.apkAssetName)
                if (targetFile.exists()) {
                    targetFile.delete()
                }

                val request = Request.Builder()
                    .url(info.apkDownloadUrl)
                    .header("User-Agent", "Mozilla/5.0 (Android; OmniStream-Updater)")
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("HTTP ${response.code}: ${response.message}")
                    }

                    val responseBody = response.body ?: throw IOException("Empty response body")
                    val totalContentLength = if (responseBody.contentLength() > 0) responseBody.contentLength() else info.apkSize

                    val inputStream = responseBody.byteStream()
                    val outputStream = FileOutputStream(targetFile)

                    val buffer = ByteArray(16384)
                    var downloaded: Long = 0
                    var lastUpdateTime = System.currentTimeMillis()
                    var lastDownloadedBytes: Long = 0

                    outputStream.use { out ->
                        inputStream.use { inp ->
                            while (true) {
                                val read = inp.read(buffer)
                                if (read == -1) break
                                out.write(buffer, 0, read)
                                downloaded += read

                                val now = System.currentTimeMillis()
                                if (now - lastUpdateTime >= 250 || downloaded == totalContentLength) {
                                    val progress = if (totalContentLength > 0) {
                                        ((downloaded * 100f) / totalContentLength).coerceIn(0f, 100f).toInt()
                                    } else 0

                                    val deltaBytes = downloaded - lastDownloadedBytes
                                    val deltaTimeMs = (now - lastUpdateTime).coerceAtLeast(1)
                                    val speedBps = (deltaBytes * 1000.0) / deltaTimeMs
                                    val speedFormatted = formatSpeed(speedBps)

                                    lastUpdateTime = now
                                    lastDownloadedBytes = downloaded

                                    _updateState.value = UpdateState.Downloading(
                                        info = info,
                                        progressPercent = progress,
                                        downloadedBytes = downloaded,
                                        totalBytes = totalContentLength,
                                        speedText = speedFormatted
                                    )
                                }
                            }
                        }
                    }

                    if (targetFile.length() < 1000000) {
                        targetFile.delete()
                        throw IOException("Downloaded APK file is incomplete or corrupted.")
                    }

                    Log.i(TAG, "APK download finished: ${targetFile.absolutePath} (${targetFile.length()} bytes)")
                    _updateState.value = UpdateState.DownloadCompleted(info, targetFile)

                    // Automatically trigger the package installer
                    withContext(Dispatchers.Main) {
                        installApk(appContext, targetFile)
                    }
                }
            } catch (e: CancellationException) {
                Log.i(TAG, "Download was cancelled.")
                _updateState.value = UpdateState.UpdateAvailable(info)
            } catch (e: Exception) {
                Log.e(TAG, "APK download failed: ${e.message}", e)
                _updateState.value = UpdateState.Error("Download failed: ${e.localizedMessage}", info = info)
            }
        }
    }

    fun cancelDownload() {
        activeDownloadJob?.cancel()
        activeDownloadJob = null
    }

    private fun getApkDownloadTarget(apkName: String): File {
        val safeName = if (apkName.endsWith(".apk", ignoreCase = true)) apkName else "OmniStream_Update.apk"
        val extDir = appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        return if (extDir != null && (extDir.exists() || extDir.mkdirs())) {
            File(extDir, safeName)
        } else {
            val cacheUpdatesDir = File(appContext.cacheDir, "updates")
            if (!cacheUpdatesDir.exists()) cacheUpdatesDir.mkdirs()
            File(cacheUpdatesDir, safeName)
        }
    }

    /**
     * Launches Android Package Installer directly without opening external browser
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists() || apkFile.length() == 0L) {
                Log.e(TAG, "APK file does not exist: ${apkFile.absolutePath}")
                return
            }

            // Check if app has permission to install unknown apps (Android 8.0+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                    return
                }
            }

            val authority = "${context.packageName}.provider"
            val apkUri = FileProvider.getUriForFile(context, authority, apkFile)

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
            Log.i(TAG, "Package Installer launched successfully for: $apkUri")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch package installer: ${e.message}", e)
        }
    }

    fun dismissDialog(rememberDismissal: Boolean = false) {
        _showUpdateDialog.value = false
        if (rememberDismissal) {
            val current = _updateState.value
            val tag = when (current) {
                is UpdateState.UpdateAvailable -> current.info.tagName
                is UpdateState.UpToDate -> current.info.tagName
                else -> null
            }
            if (tag != null) {
                prefs.edit().putString(PREF_DISMISSED_TAG, tag).apply()
            }
        }
    }

    fun showDialog() {
        _showUpdateDialog.value = true
    }

    private fun formatSpeed(bytesPerSec: Double): String {
        return when {
            bytesPerSec >= 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f MB/s", bytesPerSec / (1024 * 1024))
            bytesPerSec >= 1024 -> String.format(java.util.Locale.US, "%.0f KB/s", bytesPerSec / 1024)
            else -> String.format(java.util.Locale.US, "%.0f B/s", bytesPerSec)
        }
    }
}
