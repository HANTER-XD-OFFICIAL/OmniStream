package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val customApiUrl: String = "https://ytdlp-api.local:5000",
    val authToken: String = "",
    val defaultVideoQuality: String = "4K 60 FPS",
    val defaultAudioFormat: String = "MP3 320 kbps",
    val embedSubtitles: Boolean = true,
    val embedThumbnail: Boolean = true,
    val extraCliFlags: String = "--embed-metadata --embed-thumbnail",
    val maxConcurrentDownloads: Int = 3
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ytdlp_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        return AppSettings(
            customApiUrl = prefs.getString("custom_api_url", "https://ytdlp-api.local:5000")
                ?: "https://ytdlp-api.local:5000",
            authToken = prefs.getString("auth_token", "") ?: "",
            defaultVideoQuality = prefs.getString("default_video_quality", "4K 60 FPS")
                ?: "4K 60 FPS",
            defaultAudioFormat = prefs.getString("default_audio_format", "MP3 320 kbps")
                ?: "MP3 320 kbps",
            embedSubtitles = prefs.getBoolean("embed_subtitles", true),
            embedThumbnail = prefs.getBoolean("embed_thumbnail", true),
            extraCliFlags = prefs.getString("extra_cli_flags", "--embed-metadata --embed-thumbnail")
                ?: "--embed-metadata --embed-thumbnail",
            maxConcurrentDownloads = prefs.getInt("max_concurrent", 3)
        )
    }

    fun updateSettings(newSettings: AppSettings) {
        prefs.edit().apply {
            putString("custom_api_url", newSettings.customApiUrl)
            putString("auth_token", newSettings.authToken)
            putString("default_video_quality", newSettings.defaultVideoQuality)
            putString("default_audio_format", newSettings.defaultAudioFormat)
            putBoolean("embed_subtitles", newSettings.embedSubtitles)
            putBoolean("embed_thumbnail", newSettings.embedThumbnail)
            putString("extra_cli_flags", newSettings.extraCliFlags)
            putInt("max_concurrent", newSettings.maxConcurrentDownloads)
            apply()
        }
        _settings.value = newSettings
    }
}
