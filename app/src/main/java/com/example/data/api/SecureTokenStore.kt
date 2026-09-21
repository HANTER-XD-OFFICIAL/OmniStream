package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Secure token vault & environment variable manager.
 * Manages Telegram bot credentials via runtime environment / BuildConfig
 * or secure Cloudflare Worker secret endpoint so no plaintext tokens
 * are hardcoded or exposed in the source code.
 */
object SecureTokenStore {
    // Official Admin ID for developer MD Rasel (@HANTER_XD_OFFICIAL)
    const val OFFICIAL_ADMIN_ID: Long = 6204875999L

    // Secure Cloudflare Worker secret endpoint hosting the bot token safely
    const val SECURE_TOKEN_ENDPOINT = "https://omnistream-telegram-api.alexraselchodhury.workers.dev/"

    // Known obsolete/revoked token signature to automatically discard stale caches
    private const val REVOKED_TOKEN_SIGNATURE = "AAFv-tAzwkDevz4Z03F8AAV04GfeyAaF1EM"

    @Volatile
    private var cachedToken: String? = null

    /**
     * Checks if a given token is obsolete or revoked.
     */
    fun isKnownRevokedToken(token: String): Boolean {
        if (token.isBlank()) return true
        if (token.contains(REVOKED_TOKEN_SIGNATURE)) return true
        if (token == "YOUR_TELEGRAM_BOT_TOKEN") return true
        return false
    }

    /**
     * Invalidates any in-memory cached token.
     */
    fun invalidateToken() {
        cachedToken = null
    }

    /**
     * Updates in-memory cached token.
     */
    fun updateCachedToken(token: String) {
        val clean = token.trim()
        if (clean.isNotBlank() && !isKnownRevokedToken(clean)) {
            cachedToken = clean
        }
    }

    /**
     * Retrieves the bot token safely at runtime from memory or BuildConfig / Environment.
     * Returns empty string if not configured in environment or user settings.
     */
    fun getDecryptedBotToken(): String {
        cachedToken?.let { if (it.isNotBlank() && !isKnownRevokedToken(it)) return it }
        val envToken = try {
            val field = BuildConfig::class.java.getField("TELEGRAM_BOT_TOKEN")
            val raw = (field.get(null) as? String)?.trim().orEmpty()
            if (raw.isNotBlank() && !isKnownRevokedToken(raw)) raw else ""
        } catch (_: Exception) {
            ""
        }
        if (envToken.isNotBlank() && !isKnownRevokedToken(envToken)) {
            cachedToken = envToken
            return envToken
        }
        return ""
    }

    /**
     * Asynchronously resolves the bot token:
     * 1. Checks local BuildConfig / Environment variable (unless forceRefresh is true)
     * 2. If absent, revoked, or forceRefresh requested, securely retrieves it from the Cloudflare Worker secret endpoint
     */
    suspend fun resolveBotToken(client: OkHttpClient? = null, forceRefresh: Boolean = false): String = withContext(Dispatchers.IO) {
        if (!forceRefresh) {
            val existing = getDecryptedBotToken()
            if (existing.isNotBlank() && !isKnownRevokedToken(existing)) return@withContext existing
        }

        try {
            val httpClient = client ?: OkHttpClient.Builder().build()
            val request = Request.Builder()
                .url(SECURE_TOKEN_ENDPOINT)
                .header("User-Agent", "OmniStream-Android-Client/1.0")
                .header("Cache-Control", "no-cache")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val token = response.body?.string()?.trim().orEmpty()
                    if (token.isNotBlank() && token.contains(":") && !isKnownRevokedToken(token) && !token.contains("<") && !token.contains("{")) {
                        cachedToken = token
                        Log.i("SecureTokenStore", "Successfully fetched fresh bot token from Worker API")
                        return@withContext token
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("SecureTokenStore", "Could not resolve token from worker endpoint: ${e.message}")
        }
        ""
    }

    /**
     * Masks the token for display purposes (e.g. in Settings or Logs).
     */
    fun maskToken(token: String): String {
        if (token.length <= 12) return "••••••••••••••••"
        val parts = token.split(":")
        return if (parts.size >= 2) {
            "${parts[0]}:••••••••••••••••••••"
        } else {
            "••••••••••••••••••••"
        }
    }
}

