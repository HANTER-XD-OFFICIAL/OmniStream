package com.example.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class TelegramBotInfo(
    val id: Long,
    val isBot: Boolean,
    val firstName: String,
    val username: String,
    val canJoinGroups: Boolean = false,
    val canReadAllGroupMessages: Boolean = false,
    val isOnline: Boolean = true
)

sealed class TelegramBotResult {
    data class Success(val botInfo: TelegramBotInfo) : TelegramBotResult()
    data class Error(val message: String) : TelegramBotResult()
}

class TelegramBotClient {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    companion object {
        const val DEFAULT_BOT_TOKEN = "8451030732:AAEK2MnsTmdJbhqQVMtUik4s58TuNZFHo18"
        const val DEFAULT_BOT_USERNAME = "OmniStream34_bot"
        const val DEFAULT_BOT_NAME = "OmniStream"
        const val BOT_TELEGRAM_URL = "https://t.me/OmniStream34_bot"
    }

    suspend fun verifyBotToken(token: String): TelegramBotResult = withContext(Dispatchers.IO) {
        val cleanToken = token.trim()
        if (cleanToken.isBlank()) {
            return@withContext TelegramBotResult.Error("Bot token cannot be empty.")
        }

        try {
            val url = "https://api.telegram.org/bot$cleanToken/getMe"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful || body.isBlank()) {
                return@withContext TelegramBotResult.Error("HTTP error ${response.code}: Invalid bot token or connection issue.")
            }

            val json = JSONObject(body)
            val ok = json.optBoolean("ok", false)
            if (!ok) {
                val description = json.optString("description", "Unknown Telegram API error")
                return@withContext TelegramBotResult.Error(description)
            }

            val result = json.getJSONObject("result")
            val botInfo = TelegramBotInfo(
                id = result.optLong("id"),
                isBot = result.optBoolean("is_bot", true),
                firstName = result.optString("first_name", "OmniStream Bot"),
                username = result.optString("username", ""),
                canJoinGroups = result.optBoolean("can_join_groups", false),
                canReadAllGroupMessages = result.optBoolean("can_read_all_group_messages", false),
                isOnline = true
            )

            TelegramBotResult.Success(botInfo)
        } catch (e: Exception) {
            TelegramBotResult.Error(e.localizedMessage ?: "Failed to connect to Telegram Bot API.")
        }
    }

    suspend fun sendMessage(
        token: String,
        chatId: String,
        text: String,
        parseMode: String = "HTML"
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val cleanToken = token.trim()
        val cleanChatId = chatId.trim()
        if (cleanToken.isBlank() || cleanChatId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Token and Chat ID are required."))
        }

        try {
            val url = "https://api.telegram.org/bot$cleanToken/sendMessage"
            val jsonBody = JSONObject().apply {
                put("chat_id", cleanChatId)
                put("text", text)
                put("parse_mode", parseMode)
                put("disable_web_page_preview", false)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()

            if (response.isSuccessful && body.contains("\"ok\":true")) {
                Result.success(true)
            } else {
                Result.failure(Exception("Telegram API rejected message: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
