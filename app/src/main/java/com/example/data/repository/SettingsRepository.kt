package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val customApiUrl: String = "https://muddy-scene-0ff7.alexraselchodhury.workers.dev",
    val authToken: String = "",
    val defaultVideoQuality: String = "4K 60 FPS",
    val defaultAudioFormat: String = "MP3 320 kbps",
    val embedSubtitles: Boolean = true,
    val embedThumbnail: Boolean = true,
    val extraCliFlags: String = "--embed-metadata --embed-thumbnail",
    val maxConcurrentDownloads: Int = 3,
    val telegramBotToken: String = "8451030732:AAEK2MnsTmdJbhqQVMtUik4s58TuNZFHo18",
    val telegramBotUsername: String = "OmniStream34_bot",
    val telegramBotName: String = "OmniStream",
    val telegramChatId: String = "",
    val telegramSyncEnabled: Boolean = true
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ytdlp_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val savedUrl = prefs.getString("custom_api_url", "https://muddy-scene-0ff7.alexraselchodhury.workers.dev")
        val effectiveUrl = if (savedUrl.isNullOrBlank() || savedUrl.contains(".local") || savedUrl.contains("192.168.") || savedUrl == "https://cobalt-latest-a04h.onrender.com") {
            "https://muddy-scene-0ff7.alexraselchodhury.workers.dev"
        } else {
            savedUrl
        }
        return AppSettings(
            customApiUrl = effectiveUrl,
            authToken = prefs.getString("auth_token", "") ?: "",
            defaultVideoQuality = prefs.getString("default_video_quality", "4K 60 FPS")
                ?: "4K 60 FPS",
            defaultAudioFormat = prefs.getString("default_audio_format", "MP3 320 kbps")
                ?: "MP3 320 kbps",
            embedSubtitles = prefs.getBoolean("embed_subtitles", true),
            embedThumbnail = prefs.getBoolean("embed_thumbnail", true),
            extraCliFlags = prefs.getString("extra_cli_flags", "--embed-metadata --embed-thumbnail")
                ?: "--embed-metadata --embed-thumbnail",
            maxConcurrentDownloads = prefs.getInt("max_concurrent", 3),
            telegramBotToken = run {
                val saved = prefs.getString("telegram_bot_token", "8451030732:AAEK2MnsTmdJbhqQVMtUik4s58TuNZFHo18")
                if (saved.isNullOrBlank() || saved.contains("8523953940")) {
                    prefs.edit().putString("telegram_bot_token", "8451030732:AAEK2MnsTmdJbhqQVMtUik4s58TuNZFHo18").apply()
                    "8451030732:AAEK2MnsTmdJbhqQVMtUik4s58TuNZFHo18"
                } else {
                    saved
                }
            },
            telegramBotUsername = run {
                val saved = prefs.getString("telegram_bot_username", "OmniStream34_bot")
                if (saved.isNullOrBlank() || saved == "downloadallinonebot") {
                    prefs.edit().putString("telegram_bot_username", "OmniStream34_bot").apply()
                    "OmniStream34_bot"
                } else {
                    saved
                }
            },
            telegramBotName = "OmniStream",
            telegramChatId = prefs.getString("telegram_chat_id", "") ?: "",
            telegramSyncEnabled = prefs.getBoolean("telegram_sync_enabled", true)
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
            putString("telegram_bot_token", newSettings.telegramBotToken)
            putString("telegram_bot_username", newSettings.telegramBotUsername)
            putString("telegram_bot_name", newSettings.telegramBotName)
            putString("telegram_chat_id", newSettings.telegramChatId)
            putBoolean("telegram_sync_enabled", newSettings.telegramSyncEnabled)
            apply()
        }
        _settings.value = newSettings
    }
}
