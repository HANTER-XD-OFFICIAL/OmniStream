package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class MediaType {
    VIDEO,
    AUDIO
}

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val sourceUrl: String,
    val downloadUrl: String,
    val formatId: String,
    val formatNote: String,
    val resolution: String,
    val fps: Int = 30,
    val ext: String = "mp4",
    val mediaType: MediaType = MediaType.VIDEO,
    val thumbnailUrl: String = "",
    val durationFormatted: String = "",
    val authorOrChannel: String = "",
    val platformName: String = "",
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val progressPercent: Int = 0,
    val downloadSpeedText: String = "",
    val etaText: String = "",
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val localFilePath: String? = null,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
