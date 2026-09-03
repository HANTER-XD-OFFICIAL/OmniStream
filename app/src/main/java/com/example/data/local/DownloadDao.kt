package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY createdAt DESC")
    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'DOWNLOADING' OR status = 'QUEUED' ORDER BY createdAt ASC")
    fun getActiveDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE mediaType = :type ORDER BY createdAt DESC")
    fun getDownloadsByType(type: MediaType): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getDownloadById(id: Long): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE id = :id")
    fun observeDownloadById(id: Long): Flow<DownloadEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: DownloadEntity): Long

    @Update
    suspend fun update(download: DownloadEntity)

    @Query("UPDATE downloads SET downloadUrl = :url WHERE id = :id")
    suspend fun updateDownloadUrl(id: Long, url: String)

    @Query("UPDATE downloads SET progressPercent = :percent, downloadedBytes = :downloaded, totalBytes = :total, downloadSpeedText = :speed, etaText = :eta WHERE id = :id")
    suspend fun updateProgress(
        id: Long,
        percent: Int,
        downloaded: Long,
        total: Long,
        speed: String,
        eta: String
    )

    @Query("UPDATE downloads SET status = :status, localFilePath = :filePath, errorMessage = :error WHERE id = :id")
    suspend fun updateStatus(
        id: Long,
        status: DownloadStatus,
        filePath: String? = null,
        error: String? = null
    )

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM downloads WHERE status = 'COMPLETED'")
    suspend fun clearCompleted()

    @Query("DELETE FROM downloads")
    suspend fun deleteAll()
}
