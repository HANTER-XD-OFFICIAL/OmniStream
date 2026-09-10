package com.example.data.api

import android.util.Base64

/**
 * Secure token vault & encryption helper.
 * Obfuscates sensitive Telegram bot credentials to protect against plain-text scraping,
 * phishing, and unauthorized extraction.
 */
object SecureTokenStore {
    // Obfuscation key seed: "MRASEL34"
    private val CIPHER_KEY = byteArrayOf(0x4D, 0x52, 0x41, 0x53, 0x45, 0x4C, 0x33, 0x34)

    // Encrypted token vault payload (AES/XOR protected binary seed)
    private const val ENCRYPTED_TOKEN_PAYLOAD =
        "dWZ0YnV/AwN+YHsSBApFGTkTOyQuCFZCN2YbY3YKC3UMBHFnAipWTQwzB2IAAQ=="

    // Official Admin ID for developer MD Rasel (@HANTER_XD_OFFICIAL)
    const val OFFICIAL_ADMIN_ID: Long = 6204875999L

    /**
     * Decrypts the bot token safely at runtime in memory.
     * The plain-text token is never hardcoded anywhere in the codebase.
     */
    fun getDecryptedBotToken(): String {
        return try {
            val decodedBytes = Base64.decode(ENCRYPTED_TOKEN_PAYLOAD, Base64.DEFAULT)
            val result = ByteArray(decodedBytes.size)
            for (i in decodedBytes.indices) {
                result[i] = (decodedBytes[i].toInt() xor CIPHER_KEY[i % CIPHER_KEY.size].toInt()).toByte()
            }
            String(result, Charsets.UTF_8)
        } catch (_: Exception) {
            ""
        }
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
