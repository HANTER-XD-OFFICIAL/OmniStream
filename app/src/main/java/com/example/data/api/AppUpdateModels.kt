package com.example.data.api

import java.io.File

/**
 * Release metadata parsed from GitHub API for OmniStream
 */
data class AppReleaseInfo(
    val tagName: String,
    val name: String,
    val body: String,
    val publishedAt: String,
    val htmlUrl: String,
    val apkAssetName: String,
    val apkSize: Long,
    val apkDownloadUrl: String,
    val isUpdateAvailable: Boolean,
    val remoteVersionClean: String,
    val currentVersion: String
) {
    val sizeFormatted: String
        get() {
            if (apkSize <= 0) return "24.5 MB"
            val mb = apkSize.toDouble() / (1024.0 * 1024.0)
            return String.format(java.util.Locale.US, "%.1f MB", mb)
        }

    val displayTitle: String
        get() = name.ifBlank { "OmniStream $tagName" }
}

/**
 * States for the In-App Update Engine
 */
sealed class UpdateState {
    object Idle : UpdateState()
    data class Checking(val isManual: Boolean = false) : UpdateState()
    data class UpdateAvailable(val info: AppReleaseInfo, val isManual: Boolean = false) : UpdateState()
    data class UpToDate(val info: AppReleaseInfo, val isManual: Boolean = false) : UpdateState()
    data class Downloading(
        val info: AppReleaseInfo,
        val progressPercent: Int,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val speedText: String
    ) : UpdateState()
    data class DownloadCompleted(
        val info: AppReleaseInfo,
        val apkFile: File
    ) : UpdateState()
    data class Error(
        val message: String,
        val info: AppReleaseInfo? = null,
        val isManual: Boolean = false
    ) : UpdateState()
}
