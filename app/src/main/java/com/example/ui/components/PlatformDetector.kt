package com.example.ui.components

import androidx.compose.ui.graphics.Color

data class PlatformMeta(
    val name: String,
    val brandColor: Color,
    val badgeText: String,
    val category: PlatformCategory = PlatformCategory.VIDEO_SHARING
)

enum class PlatformCategory(val displayName: String) {
    ALL("All 21 Services"),
    SOCIAL("Social & Reels"),
    VIDEO_SHARING("Video Platforms"),
    LIVE_CLIPS("Live & Clips"),
    AUDIO_HOSTS("Audio & Media")
}

data class SupportedPlatformItem(
    val name: String,
    val domainPattern: String,
    val brandColor: Color,
    val features: String,
    val category: PlatformCategory,
    val sampleUrl: String,
    val isPopular: Boolean = false
)

object PlatformDetector {

    /**
     * Exact 21 Official Supported Download Services:
     * bilibili, bluesky, dailymotion, facebook, instagram, loom, ok.ru, pinterest,
     * newgrounds, reddit, rutube, snapchat, soundcloud, streamable, tiktok, tumblr,
     * twitch clips, twitter (X), vimeo, vk, youtube.
     */
    val SUPPORTED_PLATFORMS_CATALOG = listOf(
        // 1. YouTube
        SupportedPlatformItem(
            name = "YouTube",
            domainPattern = "youtube.com, youtu.be",
            brandColor = Color(0xFFFF0000),
            features = "Videos, Shorts, 8K/4K/1080p, 320k MP3 Audio",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            isPopular = true
        ),
        // 2. TikTok
        SupportedPlatformItem(
            name = "TikTok",
            domainPattern = "tiktok.com, douyin.com",
            brandColor = Color(0xFF00F2FE),
            features = "HD Videos without watermark, Audio Tracks",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://www.tiktok.com/@creator/video/1234567890",
            isPopular = true
        ),
        // 3. Facebook
        SupportedPlatformItem(
            name = "Facebook",
            domainPattern = "facebook.com, fb.watch, fb.com",
            brandColor = Color(0xFF1877F2),
            features = "Public Videos, Reels, Watch, Live archives HD",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://www.facebook.com/watch/?v=1234567890",
            isPopular = true
        ),
        // 4. Instagram
        SupportedPlatformItem(
            name = "Instagram",
            domainPattern = "instagram.com, instagr.am",
            brandColor = Color(0xFFE1306C),
            features = "Reels, Video Posts, Stories, IGTV 1080p",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://www.instagram.com/reel/Cx123456789/",
            isPopular = true
        ),
        // 5. Twitter / X
        SupportedPlatformItem(
            name = "Twitter / X",
            domainPattern = "x.com, twitter.com",
            brandColor = Color(0xFF1DA1F2),
            features = "Video tweets, Clips, High Bitrate Media HD",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://twitter.com/user/status/1234567890",
            isPopular = true
        ),
        // 6. Pinterest
        SupportedPlatformItem(
            name = "Pinterest",
            domainPattern = "pinterest.com, pin.it",
            brandColor = Color(0xFFE60023),
            features = "Idea Pins, Video Pins, 1080p Direct MP4",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://www.pinterest.com/pin/123456789012345678/",
            isPopular = true
        ),
        // 7. Reddit
        SupportedPlatformItem(
            name = "Reddit",
            domainPattern = "reddit.com, v.redd.it",
            brandColor = Color(0xFFFF4500),
            features = "Native videos with merged audio stream HD",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://www.reddit.com/r/videos/comments/sample123/",
            isPopular = true
        ),
        // 8. SoundCloud
        SupportedPlatformItem(
            name = "SoundCloud",
            domainPattern = "soundcloud.com",
            brandColor = Color(0xFFFF7700),
            features = "High quality 320kbps MP3 & FLAC master audio",
            category = PlatformCategory.AUDIO_HOSTS,
            sampleUrl = "https://soundcloud.com/artist/sample-track",
            isPopular = true
        ),
        // 9. Bilibili
        SupportedPlatformItem(
            name = "Bilibili",
            domainPattern = "bilibili.com, bilibili.tv, b23.tv",
            brandColor = Color(0xFF00A1D6),
            features = "Main site & Bilibili TV, 1080p/4K 60FPS",
            category = PlatformCategory.VIDEO_SHARING,
            sampleUrl = "https://www.bilibili.com/video/BV1xx411c7mD",
            isPopular = true
        ),
        // 10. Dailymotion
        SupportedPlatformItem(
            name = "Dailymotion",
            domainPattern = "dailymotion.com, dai.ly",
            brandColor = Color(0xFF0066DC),
            features = "Full HD video streams, Official channel clips",
            category = PlatformCategory.VIDEO_SHARING,
            sampleUrl = "https://www.dailymotion.com/video/x8abcdef",
            isPopular = true
        ),
        // 11. Snapchat
        SupportedPlatformItem(
            name = "Snapchat",
            domainPattern = "snapchat.com",
            brandColor = Color(0xFFFFFC00),
            features = "Public Spotlight videos, Stories HD",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://www.snapchat.com/spotlight/W7_ED1nYR9",
            isPopular = true
        ),
        // 12. Vimeo
        SupportedPlatformItem(
            name = "Vimeo",
            domainPattern = "vimeo.com",
            brandColor = Color(0xFF1AB7EA),
            features = "Standard & password videos, 4K/60FPS master",
            category = PlatformCategory.VIDEO_SHARING,
            sampleUrl = "https://vimeo.com/76979871",
            isPopular = true
        ),
        // 13. Bluesky
        SupportedPlatformItem(
            name = "Bluesky",
            domainPattern = "bsky.app",
            brandColor = Color(0xFF0085FF),
            features = "Decentralized video and audio posts HD",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://bsky.app/profile/user.bsky.social/post/123456"
        ),
        // 14. Loom
        SupportedPlatformItem(
            name = "Loom",
            domainPattern = "loom.com",
            brandColor = Color(0xFF625DF5),
            features = "High-definition screen & camera video recordings",
            category = PlatformCategory.VIDEO_SHARING,
            sampleUrl = "https://www.loom.com/share/abc123def456"
        ),
        // 15. OK.ru (Odnoklassniki)
        SupportedPlatformItem(
            name = "OK.ru",
            domainPattern = "ok.ru",
            brandColor = Color(0xFFEE8208),
            features = "Public video clips & community movie streams",
            category = PlatformCategory.VIDEO_SHARING,
            sampleUrl = "https://ok.ru/video/1234567890"
        ),
        // 16. Newgrounds
        SupportedPlatformItem(
            name = "Newgrounds",
            domainPattern = "newgrounds.com",
            brandColor = Color(0xFFFFA500),
            features = "Animations, music, movies & video stream",
            category = PlatformCategory.AUDIO_HOSTS,
            sampleUrl = "https://www.newgrounds.com/portal/view/123456"
        ),
        // 17. Rutube
        SupportedPlatformItem(
            name = "Rutube",
            domainPattern = "rutube.ru",
            brandColor = Color(0xFF0055FF),
            features = "HD video streams & creator channel videos",
            category = PlatformCategory.VIDEO_SHARING,
            sampleUrl = "https://rutube.ru/video/1234567890abcdef/"
        ),
        // 18. Streamable
        SupportedPlatformItem(
            name = "Streamable",
            domainPattern = "streamable.com",
            brandColor = Color(0xFF0F86FF),
            features = "Instant direct 1080p MP4 download",
            category = PlatformCategory.AUDIO_HOSTS,
            sampleUrl = "https://streamable.com/moo7b"
        ),
        // 19. Tumblr
        SupportedPlatformItem(
            name = "Tumblr",
            domainPattern = "tumblr.com",
            brandColor = Color(0xFF36465D),
            features = "High-res video posts & animated media clips",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://creator.tumblr.com/post/1234567890"
        ),
        // 20. Twitch Clips
        SupportedPlatformItem(
            name = "Twitch Clips",
            domainPattern = "twitch.tv, clips.twitch.tv",
            brandColor = Color(0xFF9146FF),
            features = "Stream clips, highlights, 1080p 60FPS",
            category = PlatformCategory.LIVE_CLIPS,
            sampleUrl = "https://clips.twitch.tv/GloriousSampleClip"
        ),
        // 21. VK (VKontakte)
        SupportedPlatformItem(
            name = "VK (VKontakte)",
            domainPattern = "vk.com, vk.ru",
            brandColor = Color(0xFF0077FF),
            features = "VK Videos, Clips, Community HD media",
            category = PlatformCategory.VIDEO_SHARING,
            sampleUrl = "https://vk.com/video-123456_789012"
        )
    )

    fun detect(url: String): PlatformMeta {
        val lower = url.lowercase().trim()
        return when {
            // 1. YouTube
            "youtube.com" in lower || "youtu.be" in lower ->
                PlatformMeta("YouTube", Color(0xFFFF0000), "YouTube 8K/4K/HD", PlatformCategory.SOCIAL)

            // 2. TikTok
            "tiktok.com" in lower || "douyin.com" in lower ->
                PlatformMeta("TikTok", Color(0xFF00F2FE), "TikTok No-Watermark", PlatformCategory.SOCIAL)

            // 3. Facebook
            "facebook.com" in lower || "fb.watch" in lower || "fb.com" in lower ->
                PlatformMeta("Facebook", Color(0xFF1877F2), "Facebook HD/Reels", PlatformCategory.SOCIAL)

            // 4. Instagram
            "instagram.com" in lower || "instagr.am" in lower ->
                PlatformMeta("Instagram", Color(0xFFE1306C), "Instagram Reels/Posts", PlatformCategory.SOCIAL)

            // 5. Twitter / X
            "twitter.com" in lower || "://x.com" in lower || ".x.com" in lower || lower.startsWith("x.com") ->
                PlatformMeta("Twitter / X", Color(0xFF1DA1F2), "X Media HD", PlatformCategory.SOCIAL)

            // 6. Pinterest
            "pinterest." in lower || "pin.it" in lower ->
                PlatformMeta("Pinterest", Color(0xFFE60023), "Pinterest 1080p MP4", PlatformCategory.SOCIAL)

            // 7. Reddit
            "reddit.com" in lower || "v.redd.it" in lower ->
                PlatformMeta("Reddit", Color(0xFFFF4500), "Reddit Video+Audio", PlatformCategory.SOCIAL)

            // 8. SoundCloud
            "soundcloud.com" in lower ->
                PlatformMeta("SoundCloud", Color(0xFFFF7700), "SoundCloud 320kbps", PlatformCategory.AUDIO_HOSTS)

            // 9. Bilibili
            "bilibili.com" in lower || "bilibili.tv" in lower || "b23.tv" in lower ->
                PlatformMeta("Bilibili", Color(0xFF00A1D6), "Bilibili HD", PlatformCategory.VIDEO_SHARING)

            // 10. Dailymotion
            "dailymotion.com" in lower || "dai.ly" in lower ->
                PlatformMeta("Dailymotion", Color(0xFF0066DC), "Dailymotion HD", PlatformCategory.VIDEO_SHARING)

            // 11. Snapchat
            "snapchat.com" in lower ->
                PlatformMeta("Snapchat", Color(0xFFFFFC00), "Snapchat Spotlight", PlatformCategory.SOCIAL)

            // 12. Vimeo
            "vimeo.com" in lower ->
                PlatformMeta("Vimeo", Color(0xFF1AB7EA), "Vimeo 4K/60FPS", PlatformCategory.VIDEO_SHARING)

            // 13. Bluesky
            "bsky.app" in lower || "bluesky" in lower ->
                PlatformMeta("Bluesky", Color(0xFF0085FF), "Bluesky Video", PlatformCategory.SOCIAL)

            // 14. Loom
            "loom.com" in lower ->
                PlatformMeta("Loom", Color(0xFF625DF5), "Loom HD Video", PlatformCategory.VIDEO_SHARING)

            // 15. OK.ru (Odnoklassniki)
            "ok.ru" in lower ->
                PlatformMeta("OK.ru", Color(0xFFEE8208), "OK.ru Media", PlatformCategory.VIDEO_SHARING)

            // 16. Newgrounds
            "newgrounds.com" in lower ->
                PlatformMeta("Newgrounds", Color(0xFFFFA500), "Newgrounds Media", PlatformCategory.AUDIO_HOSTS)

            // 17. Rutube
            "rutube.ru" in lower ->
                PlatformMeta("Rutube", Color(0xFF0055FF), "Rutube Video", PlatformCategory.VIDEO_SHARING)

            // 18. Streamable
            "streamable.com" in lower ->
                PlatformMeta("Streamable", Color(0xFF0F86FF), "Streamable 1080p", PlatformCategory.AUDIO_HOSTS)

            // 19. Tumblr
            "tumblr.com" in lower ->
                PlatformMeta("Tumblr", Color(0xFF36465D), "Tumblr Video", PlatformCategory.SOCIAL)

            // 20. Twitch Clips
            "twitch.tv" in lower || "clips.twitch.tv" in lower ->
                PlatformMeta("Twitch Clips", Color(0xFF9146FF), "Twitch 1080p 60FPS", PlatformCategory.LIVE_CLIPS)

            // 21. VK (VKontakte)
            "vk.com" in lower || "vk.ru" in lower ->
                PlatformMeta("VKontakte", Color(0xFF0077FF), "VK Video", PlatformCategory.VIDEO_SHARING)

            // Direct Media Streams
            lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".webm") ||
            lower.endsWith(".mp3") || lower.endsWith(".m4a") || lower.endsWith(".flac") ->
                PlatformMeta("Direct Media Stream", Color(0xFF10B981), "Direct Master Stream", PlatformCategory.AUDIO_HOSTS)

            else -> PlatformMeta("Universal Web Stream", Color(0xFF00F2FE), "OmniStream Engine", PlatformCategory.VIDEO_SHARING)
        }
    }
}
