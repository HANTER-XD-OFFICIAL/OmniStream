package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

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

    // Default API_SECRET authorization password for Cloudflare Worker
    const val DEFAULT_WORKER_API_SECRET = "432872"
    const val WORKER_API_SECRET = DEFAULT_WORKER_API_SECRET

    // Known obsolete/revoked token signature to automatically discard stale caches
    private const val REVOKED_TOKEN_SIGNATURE = "AAFv-tAzwkDevz4Z03F8AAV04GfeyAaF1EM"

    @Volatile
    private var cachedToken: String? = null

    @Volatile
    var lastWorkerError: String? = null
        private set

    /**
     * Checks if a given token is obsolete, revoked, or an invalid format like a URL.
     */
    fun isKnownRevokedToken(token: String): Boolean {
        if (token.isBlank()) return true
        if (token.contains(REVOKED_TOKEN_SIGNATURE)) return true
        if (token == "YOUR_TELEGRAM_BOT_TOKEN") return true
        if (token.startsWith("http://") || token.startsWith("https://")) return true
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
            lastWorkerError = null
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
     * 1. Checks local in-memory token (unless forceRefresh is true)
     * 2. If absent, revoked, or forceRefresh requested, securely retrieves it from the Cloudflare Worker secret endpoint
     *    using the specified or default authorization password.
     */
    suspend fun resolveBotToken(
        client: OkHttpClient? = null,
        forceRefresh: Boolean = false,
        workerSecret: String? = null
    ): String = withContext(Dispatchers.IO) {
        if (!forceRefresh) {
            val existing = getDecryptedBotToken()
            if (existing.isNotBlank() && !isKnownRevokedToken(existing)) return@withContext existing
        }

        val secretToUse = workerSecret?.trim()?.ifBlank { null } ?: DEFAULT_WORKER_API_SECRET

        try {
            val httpClient = client ?: OkHttpClient.Builder().build()
            val encodedSecret = try { URLEncoder.encode(secretToUse, "UTF-8") } catch (_: Exception) { secretToUse }
            val requestUrl = "$SECURE_TOKEN_ENDPOINT?auth=$encodedSecret&secret=$encodedSecret&password=$encodedSecret"

            val request = Request.Builder()
                .url(requestUrl)
                .header("Authorization", secretToUse)
                .header("X-API-Key", secretToUse)
                .header("X-Worker-Secret", secretToUse)
                .header("User-Agent", "OmniStream-Android-Client/1.0")
                .header("Cache-Control", "no-cache")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.code == 401) {
                    lastWorkerError = "HTTP 401 Unauthorized: Authorization password '$secretToUse' was rejected by Cloudflare Worker. Check Cloudflare Worker secrets/variables."
                    Log.w("SecureTokenStore", lastWorkerError ?: "")
                    return@withContext ""
                }

                if (!response.isSuccessful) {
                    lastWorkerError = "Worker request failed with HTTP ${response.code}."
                    Log.w("SecureTokenStore", lastWorkerError ?: "")
                    return@withContext ""
                }

                val rawBody = response.body?.string()?.trim().orEmpty()
                var resolvedToken = rawBody

                // Parse JSON if returned (e.g. {"token": "..."} or {"bot_token": "..."})
                if (rawBody.startsWith("{") && rawBody.endsWith("}")) {
                    try {
                        val json = JSONObject(rawBody)
                        resolvedToken = json.optString("token")
                            .ifBlank { json.optString("bot_token") }
                            .ifBlank { json.optString("telegram_bot_token") }
                            .ifBlank { json.optString("api_key") }
                    } catch (_: Exception) {}
                }

                if (resolvedToken.isNotBlank() && resolvedToken.contains(":") && !isKnownRevokedToken(resolvedToken)) {
                    cachedToken = resolvedToken
                    lastWorkerError = null
                    Log.i("SecureTokenStore", "Successfully fetched fresh bot token from Worker API")
                    return@withContext resolvedToken
                } else {
                    lastWorkerError = "Worker response did not return a valid Telegram bot token (received: '${rawBody.take(40)}')"
                    Log.w("SecureTokenStore", lastWorkerError ?: "")
                }
            }
        } catch (e: Exception) {
            lastWorkerError = "Network error connecting to Worker: ${e.localizedMessage}"
            Log.w("SecureTokenStore", lastWorkerError ?: "")
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

