package com.example.ui

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ApiHealthResponse
import com.example.data.api.FormatInfo
import com.example.data.api.VideoInfoResponse
import com.example.data.api.YtDlpClient
import com.example.data.local.AppDatabase
import com.example.data.local.DownloadEntity
import com.example.data.local.DownloadStatus
import com.example.data.local.MediaType
import com.example.data.repository.AppSettings
import com.example.data.repository.DownloadRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DownloadViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val downloadDao = db.downloadDao()
    private val downloadRepo = DownloadRepository.getInstance(application, downloadDao)
    private val settingsRepo = SettingsRepository(application)
    private val ytDlpClient = YtDlpClient()

    val settings: StateFlow<AppSettings> = settingsRepo.settings

    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _isFetching = MutableStateFlow(false)
    val isFetching: StateFlow<Boolean> = _isFetching.asStateFlow()

    private val _fetchError = MutableStateFlow<String?>(null)
    val fetchError: StateFlow<String?> = _fetchError.asStateFlow()

    private val _videoInfo = MutableStateFlow<VideoInfoResponse?>(null)
    val videoInfo: StateFlow<VideoInfoResponse?> = _videoInfo.asStateFlow()

    private val _selectedFormat = MutableStateFlow<FormatInfo?>(null)
    val selectedFormat: StateFlow<FormatInfo?> = _selectedFormat.asStateFlow()

    private val _apiHealth = MutableStateFlow<ApiHealthResponse?>(null)
    val apiHealth: StateFlow<ApiHealthResponse?> = _apiHealth.asStateFlow()

    private val _isTestingApi = MutableStateFlow(false)
    val isTestingApi: StateFlow<Boolean> = _isTestingApi.asStateFlow()

    private val _downloadsFilter = MutableStateFlow("ALL")
    val downloadsFilter: StateFlow<String> = _downloadsFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _previewItem = MutableStateFlow<DownloadEntity?>(null)
    val previewItem: StateFlow<DownloadEntity?> = _previewItem.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Filtered downloads
    val filteredDownloads: StateFlow<List<DownloadEntity>> = combine(
        downloadRepo.allDownloads,
        _downloadsFilter,
        _searchQuery
    ) { downloads, filter, query ->
        downloads.filter { item ->
            val matchesFilter = when (filter) {
                "DOWNLOADING" -> item.status == DownloadStatus.DOWNLOADING || item.status == DownloadStatus.QUEUED
                "COMPLETED" -> item.status == DownloadStatus.COMPLETED
                "VIDEO" -> item.mediaType == MediaType.VIDEO
                "AUDIO" -> item.mediaType == MediaType.AUDIO
                else -> true
            }
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.authorOrChannel.contains(query, ignoreCase = true) ||
                    item.resolution.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Initial health check of API
        testApiHealth()
    }

    fun setUrlInput(newUrl: String) {
        _urlInput.value = newUrl
        if (_fetchError.value != null) {
            _fetchError.value = null
        }
    }

    fun pasteFromClipboard() {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text?.toString()?.trim()
            if (!text.isNullOrEmpty()) {
                _urlInput.value = text
                _statusMessage.value = "Pasted URL from clipboard"
                fetchInfo()
            }
        }
    }

    fun loadSampleUrl(url: String) {
        _urlInput.value = url
        fetchInfo()
    }

    fun fetchInfo() {
        val url = _urlInput.value.trim()
        if (url.isEmpty()) {
            _fetchError.value = "Please enter or paste a media link"
            return
        }

        viewModelScope.launch {
            _isFetching.value = true
            _fetchError.value = null
            _videoInfo.value = null
            _selectedFormat.value = null

            try {
                val currentSettings = settings.value
                val info = ytDlpClient.fetchVideoInfo(
                    url = url,
                    baseUrl = currentSettings.customApiUrl,
                    authToken = currentSettings.authToken,
                    extraArgs = currentSettings.extraCliFlags
                )
                _videoInfo.value = info

                // Auto-select preferred format based on user settings
                val defaultPref = currentSettings.defaultVideoQuality
                val matching = info.formats.find { it.displayQualityBadge.contains(defaultPref, ignoreCase = true) }
                    ?: info.formats.firstOrNull { !it.isAudioOnly }
                    ?: info.formats.firstOrNull()
                _selectedFormat.value = matching

            } catch (e: Exception) {
                _fetchError.value = e.localizedMessage ?: "Failed to extract media information"
            } finally {
                _isFetching.value = false
            }
        }
    }

    fun selectFormat(format: FormatInfo) {
        _selectedFormat.value = format
    }

    fun getStorageLocationText(): String {
        return "Internal Storage/Download/${DownloadRepository.APP_FOLDER_NAME}"
    }

    fun startDownload(
        customExt: String? = null,
        audioOnlyOverride: Boolean = false
    ) {
        val info = _videoInfo.value ?: return
        val format = _selectedFormat.value ?: return

        viewModelScope.launch {
            val isAudio = audioOnlyOverride || format.isAudioOnly
            val mediaType = if (isAudio) MediaType.AUDIO else MediaType.VIDEO
            val ext = customExt ?: (if (isAudio) "mp3" else format.ext)

            val downloadUrl = format.url ?: info.webpageUrl ?: _urlInput.value.trim()

            downloadRepo.enqueueDownload(
                title = info.title,
                sourceUrl = info.webpageUrl ?: _urlInput.value,
                downloadUrl = downloadUrl,
                formatId = format.formatId,
                formatNote = format.formatNote ?: format.displayQualityBadge,
                resolution = if (isAudio) "Audio HQ" else (format.resolution ?: format.displayQualityBadge),
                fps = format.fps ?: 30,
                ext = ext,
                mediaType = mediaType,
                thumbnailUrl = info.thumbnail ?: "",
                durationFormatted = info.displayDuration,
                authorOrChannel = info.author,
                platformName = info.extractor ?: "Web",
                totalBytesEstimated = format.estimatedBytes
            )

            _statusMessage.value = "Saving to ${DownloadRepository.APP_FOLDER_NAME}: ${info.title.take(28)}..."
        }
    }

    fun pauseDownload(id: Long) {
        viewModelScope.launch {
            downloadRepo.pauseDownload(id)
        }
    }

    fun resumeDownload(id: Long) {
        viewModelScope.launch {
            downloadRepo.resumeDownload(id)
        }
    }

    fun cancelDownload(id: Long) {
        viewModelScope.launch {
            downloadRepo.cancelDownload(id)
        }
    }

    fun deleteDownload(id: Long, deleteFile: Boolean = true) {
        viewModelScope.launch {
            downloadRepo.deleteDownload(id, deleteFile)
            _statusMessage.value = "Download removed"
        }
    }

    fun clearCompleted() {
        viewModelScope.launch {
            downloadRepo.clearCompleted()
            _statusMessage.value = "Cleared completed downloads"
        }
    }

    fun testApiHealth() {
        viewModelScope.launch {
            _isTestingApi.value = true
            try {
                val current = settings.value
                val result = ytDlpClient.testApiHealth(current.customApiUrl, current.authToken)
                _apiHealth.value = result
            } catch (e: Exception) {
                _apiHealth.value = ApiHealthResponse(
                    status = "error",
                    message = "Connection failed: ${e.localizedMessage}"
                )
            } finally {
                _isTestingApi.value = false
            }
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        settingsRepo.updateSettings(newSettings)
        _statusMessage.value = "Settings updated successfully"
        testApiHealth()
    }

    fun setDownloadsFilter(filter: String) {
        _downloadsFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setPreviewItem(item: DownloadEntity?) {
        _previewItem.value = item
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
