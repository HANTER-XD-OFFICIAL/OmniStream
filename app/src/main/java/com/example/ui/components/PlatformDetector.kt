package com.example.ui.components

import androidx.compose.ui.graphics.Color

data class PlatformMeta(
    val name: String,
    val brandColor: Color,
    val badgeText: String
)

object PlatformDetector {
    fun detect(url: String): PlatformMeta {
        val lower = url.lowercase().trim()
        return when {
            "terabox.com" in lower || "teraboxapp.com" in lower || "1024tera.com" in lower ||
            "teraboxlink.com" in lower || "terasharelink.com" in lower || "terafileshare.com" in lower ||
            "mirrobox.com" in lower || "nephobox.com" in lower || "freeterabox.com" in lower ||
            "tibibox.com" in lower || "4funbox.com" in lower -> PlatformMeta(
                name = "TeraBox Cloud",
                brandColor = Color(0xFF0284C7),
                badgeText = "TeraBox Cloud HD"
            )
            "youtube.com" in lower || "youtu.be" in lower -> PlatformMeta(
                name = "YouTube",
                brandColor = Color(0xFFFF0000),
                badgeText = "YouTube 4K/8K"
            )
            "facebook.com" in lower || "fb.watch" in lower -> PlatformMeta(
                name = "Facebook",
                brandColor = Color(0xFF1877F2),
                badgeText = "Facebook HD"
            )
            "tiktok.com" in lower -> PlatformMeta(
                name = "TikTok",
                brandColor = Color(0xFF00F2FE),
                badgeText = "TikTok No-WM"
            )
            "instagram.com" in lower -> PlatformMeta(
                name = "Instagram",
                brandColor = Color(0xFFE1306C),
                badgeText = "Instagram Reels"
            )
            "twitter.com" in lower || "://x.com" in lower || ".x.com" in lower || lower.startsWith("x.com") -> PlatformMeta(
                name = "X (Twitter)",
                brandColor = Color(0xFF1DA1F2),
                badgeText = "X Media"
            )
            "reddit.com" in lower || "v.redd.it" in lower -> PlatformMeta(
                name = "Reddit",
                brandColor = Color(0xFFFF4500),
                badgeText = "Reddit Video"
            )
            "vimeo.com" in lower -> PlatformMeta(
                name = "Vimeo",
                brandColor = Color(0xFF1AB7EA),
                badgeText = "Vimeo 4K"
            )
            "soundcloud.com" in lower -> PlatformMeta(
                name = "SoundCloud",
                brandColor = Color(0xFFFF7700),
                badgeText = "SoundCloud HQ"
            )
            "twitch.tv" in lower -> PlatformMeta(
                name = "Twitch",
                brandColor = Color(0xFF9146FF),
                badgeText = "Twitch Clips"
            )
            else -> PlatformMeta(
                name = "Universal Any-Web",
                brandColor = Color(0xFF00F2FE),
                badgeText = "OmniStream Core"
            )
        }
    }
}
