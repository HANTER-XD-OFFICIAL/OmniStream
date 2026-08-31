package com.example.ui.components

import androidx.compose.ui.graphics.Color

data class PlatformMeta(
    val name: String,
    val brandColor: Color,
    val badgeText: String,
    val category: PlatformCategory = PlatformCategory.UNIVERSAL
)

enum class PlatformCategory(val displayName: String) {
    ALL("All 1000+"),
    SOCIAL("Social Media"),
    VIDEO_SHARING("Video Sharing"),
    LIVE_STREAMING("Live Streaming"),
    NEWS_ENTERTAINMENT("News & Media"),
    CLOUD_HOSTS("Cloud & Hosts"),
    UNIVERSAL("Universal Web")
}

data class SupportedPlatformItem(
    val name: String,
    val domainPattern: String,
    val brandColor: Color,
    val features: String,
    val category: PlatformCategory,
    val sampleUrl: String
)

object PlatformDetector {

    val SUPPORTED_PLATFORMS_CATALOG = listOf(
        // === SOCIAL MEDIA PLATFORMS ===
        SupportedPlatformItem(
            name = "YouTube",
            domainPattern = "youtube.com, youtu.be",
            brandColor = Color(0xFFFF0000),
            features = "Shorts, Videos, Music, 8K/4K/1080p 60FPS",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://www.youtube.com/watch?v=LXb3EKWsInQ"
        ),
        SupportedPlatformItem(
            name = "Facebook",
            domainPattern = "facebook.com, fb.watch, fb.com",
            brandColor = Color(0xFF1877F2),
            features = "Public Videos, Reels, Watch, Live Archive",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://www.facebook.com/watch/?v=10928374"
        ),
        SupportedPlatformItem(
            name = "Instagram",
            domainPattern = "instagram.com",
            brandColor = Color(0xFFE1306C),
            features = "Posts, Reels, Stories, IGTV HD",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://www.instagram.com/reel/C8qW1234567/"
        ),
        SupportedPlatformItem(
            name = "TikTok",
            domainPattern = "tiktok.com, douyin.com",
            brandColor = Color(0xFF00F2FE),
            features = "Videos without watermark, Live Archives",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://www.tiktok.com/@creator/video/72891234"
        ),
        SupportedPlatformItem(
            name = "Twitter / X",
            domainPattern = "x.com, twitter.com",
            brandColor = Color(0xFF1DA1F2),
            features = "Video tweets, Clips, GIFs HD",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://x.com/space/status/1789123456789"
        ),
        SupportedPlatformItem(
            name = "Reddit",
            domainPattern = "reddit.com, v.redd.it",
            brandColor = Color(0xFFFF4500),
            features = "Native videos with merged audio HD",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://www.reddit.com/r/videos/comments/xyz123/"
        ),
        SupportedPlatformItem(
            name = "Pinterest",
            domainPattern = "pinterest.com, pin.it",
            brandColor = Color(0xFFE60023),
            features = "Idea Pins, Video Pins, 1080p Direct MP4",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://www.pinterest.com/pin/123456789012345678/"
        ),
        SupportedPlatformItem(
            name = "Snapchat",
            domainPattern = "snapchat.com",
            brandColor = Color(0xFFFFFC00),
            features = "Public Spotlight videos, Stories HD",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://www.snapchat.com/spotlight/W7_ED1nYR9"
        ),
        SupportedPlatformItem(
            name = "LinkedIn",
            domainPattern = "linkedin.com",
            brandColor = Color(0xFF0A66C2),
            features = "Post videos, Video articles HD",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://www.linkedin.com/posts/activity-123456789"
        ),
        SupportedPlatformItem(
            name = "Threads",
            domainPattern = "threads.net",
            brandColor = Color(0xFFFFFFFF),
            features = "Video posts, High Definition Stream",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://www.threads.net/@user/post/C_abc123"
        ),

        // === SHORT-FORM & VIDEO SHARING ===
        SupportedPlatformItem(
            name = "Vimeo",
            domainPattern = "vimeo.com",
            brandColor = Color(0xFF1AB7EA),
            features = "Standard & Password protected videos, 4K/60FPS",
            category = PlatformCategory.VIDEO_SHARING,
            sampleUrl = "https://vimeo.com/76979871"
        ),
        SupportedPlatformItem(
            name = "Dailymotion",
            domainPattern = "dailymotion.com, dai.ly",
            brandColor = Color(0xFF0066DC),
            features = "Full HD video streams, Official Channels",
            category = PlatformCategory.VIDEO_SHARING,
            sampleUrl = "https://www.dailymotion.com/video/x8abcdef"
        ),
        SupportedPlatformItem(
            name = "Rumble",
            domainPattern = "rumble.com",
            brandColor = Color(0xFF85C742),
            features = "HD Videos, Creator Live Streams",
            category = PlatformCategory.VIDEO_SHARING,
            sampleUrl = "https://rumble.com/v2abcde-sample-video.html"
        ),
        SupportedPlatformItem(
            name = "Bilibili",
            domainPattern = "bilibili.com, bilibili.tv, b23.tv",
            brandColor = Color(0xFF00A1D6),
            features = "Main site & Bilibili TV, 1080p/4K 60FPS",
            category = PlatformCategory.VIDEO_SHARING,
            sampleUrl = "https://www.bilibili.com/video/BV1xx411c7mD"
        ),
        SupportedPlatformItem(
            name = "VK (VKontakte)",
            domainPattern = "vk.com, vk.ru",
            brandColor = Color(0xFF0077FF),
            features = "VK Videos, Clips, Community HD media",
            category = PlatformCategory.VIDEO_SHARING,
            sampleUrl = "https://vk.com/video-123456_789012"
        ),
        SupportedPlatformItem(
            name = "OK.ru (Odnoklassniki)",
            domainPattern = "ok.ru",
            brandColor = Color(0xFFEE8208),
            features = "Public video clips & movie streams",
            category = PlatformCategory.VIDEO_SHARING,
            sampleUrl = "https://ok.ru/video/1234567890"
        ),
        SupportedPlatformItem(
            name = "Likee",
            domainPattern = "likee.video, likee.com",
            brandColor = Color(0xFFFF2442),
            features = "Short videos without watermark",
            category = PlatformCategory.VIDEO_SHARING,
            sampleUrl = "https://likee.video/@creator/video/123456"
        ),

        // === LIVE STREAMING PLATFORMS ===
        SupportedPlatformItem(
            name = "Twitch",
            domainPattern = "twitch.tv, clips.twitch.tv",
            brandColor = Color(0xFF9146FF),
            features = "Clips, VODs, High bitrate 1080p 60FPS",
            category = PlatformCategory.LIVE_STREAMING,
            sampleUrl = "https://clips.twitch.tv/GloriousSampleClip"
        ),
        SupportedPlatformItem(
            name = "Kick",
            domainPattern = "kick.com",
            brandColor = Color(0xFF53FC18),
            features = "VODs, Clips, Stream archives",
            category = PlatformCategory.LIVE_STREAMING,
            sampleUrl = "https://kick.com/creator/clips/clip_123456"
        ),
        SupportedPlatformItem(
            name = "DLive",
            domainPattern = "dlive.tv",
            brandColor = Color(0xFFFFD300),
            features = "Live streams & replay archives",
            category = PlatformCategory.LIVE_STREAMING,
            sampleUrl = "https://dlive.tv/p/creator+12345"
        ),
        SupportedPlatformItem(
            name = "Trovo",
            domainPattern = "trovo.live",
            brandColor = Color(0xFF19D873),
            features = "Clips & past broadcasts",
            category = PlatformCategory.LIVE_STREAMING,
            sampleUrl = "https://trovo.live/clip/sample123"
        ),

        // === NEWS & ENTERTAINMENT ===
        SupportedPlatformItem(
            name = "TED / TED-Ed",
            domainPattern = "ted.com",
            brandColor = Color(0xFFE62B1E),
            features = "Talks with subtitles & 1080p MP4 master",
            category = PlatformCategory.NEWS_ENTERTAINMENT,
            sampleUrl = "https://www.ted.com/talks/sample_talk_future"
        ),
        SupportedPlatformItem(
            name = "BBC / BBC iPlayer",
            domainPattern = "bbc.com, bbc.co.uk",
            brandColor = Color(0xFFFF0033),
            features = "News reports, documentaries, clips",
            category = PlatformCategory.NEWS_ENTERTAINMENT,
            sampleUrl = "https://www.bbc.com/news/videos/c12345678"
        ),
        SupportedPlatformItem(
            name = "CNN",
            domainPattern = "cnn.com",
            brandColor = Color(0xFFCC0000),
            features = "CNN Video reports, Specials, Live clips",
            category = PlatformCategory.NEWS_ENTERTAINMENT,
            sampleUrl = "https://edition.cnn.com/videos/world/sample.cnn"
        ),
        SupportedPlatformItem(
            name = "Al Jazeera",
            domainPattern = "aljazeera.com",
            brandColor = Color(0xFFEAA11F),
            features = "Documentaries, news packages, AJ+ clips",
            category = PlatformCategory.NEWS_ENTERTAINMENT,
            sampleUrl = "https://www.aljazeera.com/program/newsfeed/video"
        ),
        SupportedPlatformItem(
            name = "Reuters",
            domainPattern = "reuters.com",
            brandColor = Color(0xFFFF8000),
            features = "Global video wires, economic & news reports",
            category = PlatformCategory.NEWS_ENTERTAINMENT,
            sampleUrl = "https://www.reuters.com/video/watch/sample-id"
        ),
        SupportedPlatformItem(
            name = "Daily Mail",
            domainPattern = "dailymail.co.uk",
            brandColor = Color(0xFF004D99),
            features = "MailOnline exclusive video clips",
            category = PlatformCategory.NEWS_ENTERTAINMENT,
            sampleUrl = "https://www.dailymail.co.uk/video/news/video-123456.html"
        ),
        SupportedPlatformItem(
            name = "The Guardian",
            domainPattern = "theguardian.com",
            brandColor = Color(0xFF052962),
            features = "Documentaries, explainers, world video",
            category = PlatformCategory.NEWS_ENTERTAINMENT,
            sampleUrl = "https://www.theguardian.com/news/video/sample"
        ),

        // === CLOUD & FILE HOSTS ===
        SupportedPlatformItem(
            name = "TeraBox Cloud",
            domainPattern = "terabox.com, 1024tera.com, terasharelink.com, mirrobox.com, nephobox.com",
            brandColor = Color(0xFF0284C7),
            features = "Direct cloud stream, bypass limits, Full HD",
            category = PlatformCategory.CLOUD_HOSTS,
            sampleUrl = "https://terabox.com/s/1aB2c3d4e5fG6h7i8j"
        ),
        SupportedPlatformItem(
            name = "Google Drive",
            domainPattern = "drive.google.com",
            brandColor = Color(0xFF34A853),
            features = "Public shared video & audio direct streams",
            category = PlatformCategory.CLOUD_HOSTS,
            sampleUrl = "https://drive.google.com/file/d/1a2b3c4d5e6f7g8h9/view"
        ),
        SupportedPlatformItem(
            name = "Dropbox",
            domainPattern = "dropbox.com",
            brandColor = Color(0xFF0061FE),
            features = "Shared media direct stream extraction",
            category = PlatformCategory.CLOUD_HOSTS,
            sampleUrl = "https://www.dropbox.com/s/sample12345/video.mp4?dl=0"
        ),
        SupportedPlatformItem(
            name = "Streamable",
            domainPattern = "streamable.com",
            brandColor = Color(0xFF0F86FF),
            features = "Instant direct 1080p MP4 download",
            category = PlatformCategory.CLOUD_HOSTS,
            sampleUrl = "https://streamable.com/moo7b"
        ),
        SupportedPlatformItem(
            name = "Archive.org",
            domainPattern = "archive.org",
            brandColor = Color(0xFFCCCCCC),
            features = "Internet Archive public domain movies & media",
            category = PlatformCategory.CLOUD_HOSTS,
            sampleUrl = "https://archive.org/details/sample_video_archive"
        ),
        SupportedPlatformItem(
            name = "SoundCloud",
            domainPattern = "soundcloud.com",
            brandColor = Color(0xFFFF7700),
            features = "High quality 320kbps MP3 / FLAC audio stream",
            category = PlatformCategory.SOCIAL,
            sampleUrl = "https://soundcloud.com/artist/high-res-master"
        )
    )

    fun detect(url: String): PlatformMeta {
        val lower = url.lowercase().trim()
        return when {
            // Cloud & Hosts
            "terabox" in lower || "1024tera" in lower || "terasharelink" in lower || "mirrobox" in lower || "nephobox" in lower || "freeterabox" in lower || "tibibox" in lower || "4funbox" in lower ->
                PlatformMeta("TeraBox Cloud", Color(0xFF0284C7), "TeraBox Cloud HD", PlatformCategory.CLOUD_HOSTS)
            "drive.google.com" in lower ->
                PlatformMeta("Google Drive", Color(0xFF34A853), "Google Drive Stream", PlatformCategory.CLOUD_HOSTS)
            "dropbox.com" in lower ->
                PlatformMeta("Dropbox", Color(0xFF0061FE), "Dropbox Direct", PlatformCategory.CLOUD_HOSTS)
            "streamable.com" in lower ->
                PlatformMeta("Streamable", Color(0xFF0F86FF), "Streamable 1080p", PlatformCategory.CLOUD_HOSTS)
            "archive.org" in lower ->
                PlatformMeta("Archive.org", Color(0xFFAAAAAA), "Internet Archive", PlatformCategory.CLOUD_HOSTS)

            // Social Media
            "youtube.com" in lower || "youtu.be" in lower ->
                PlatformMeta("YouTube", Color(0xFFFF0000), "YouTube 8K/4K/HD", PlatformCategory.SOCIAL)
            "facebook.com" in lower || "fb.watch" in lower || "fb.com" in lower ->
                PlatformMeta("Facebook", Color(0xFF1877F2), "Facebook HD/Reels", PlatformCategory.SOCIAL)
            "instagram.com" in lower ->
                PlatformMeta("Instagram", Color(0xFFE1306C), "Instagram Reels/Posts", PlatformCategory.SOCIAL)
            "tiktok.com" in lower || "douyin.com" in lower ->
                PlatformMeta("TikTok", Color(0xFF00F2FE), "TikTok No-Watermark", PlatformCategory.SOCIAL)
            "pinterest." in lower || "pin.it" in lower ->
                PlatformMeta("Pinterest", Color(0xFFE60023), "Pinterest 1080p MP4", PlatformCategory.SOCIAL)
            "snapchat.com" in lower ->
                PlatformMeta("Snapchat", Color(0xFFFFFC00), "Snapchat Spotlight", PlatformCategory.SOCIAL)
            "linkedin.com" in lower ->
                PlatformMeta("LinkedIn", Color(0xFF0A66C2), "LinkedIn Video", PlatformCategory.SOCIAL)
            "threads.net" in lower ->
                PlatformMeta("Threads", Color(0xFFFFFFFF), "Threads Video", PlatformCategory.SOCIAL)
            "twitter.com" in lower || "://x.com" in lower || ".x.com" in lower || lower.startsWith("x.com") ->
                PlatformMeta("X (Twitter)", Color(0xFF1DA1F2), "X Media HD", PlatformCategory.SOCIAL)
            "reddit.com" in lower || "v.redd.it" in lower ->
                PlatformMeta("Reddit", Color(0xFFFF4500), "Reddit Video+Audio", PlatformCategory.SOCIAL)
            "soundcloud.com" in lower ->
                PlatformMeta("SoundCloud", Color(0xFFFF7700), "SoundCloud 320kbps", PlatformCategory.SOCIAL)

            // Video Sharing
            "vimeo.com" in lower ->
                PlatformMeta("Vimeo", Color(0xFF1AB7EA), "Vimeo 4K/60FPS", PlatformCategory.VIDEO_SHARING)
            "dailymotion.com" in lower || "dai.ly" in lower ->
                PlatformMeta("Dailymotion", Color(0xFF0066DC), "Dailymotion HD", PlatformCategory.VIDEO_SHARING)
            "rumble.com" in lower ->
                PlatformMeta("Rumble", Color(0xFF85C742), "Rumble HD", PlatformCategory.VIDEO_SHARING)
            "bilibili.com" in lower || "bilibili.tv" in lower || "b23.tv" in lower ->
                PlatformMeta("Bilibili", Color(0xFF00A1D6), "Bilibili HD", PlatformCategory.VIDEO_SHARING)
            "vk.com" in lower || "vk.ru" in lower ->
                PlatformMeta("VKontakte", Color(0xFF0077FF), "VK Video", PlatformCategory.VIDEO_SHARING)
            "ok.ru" in lower ->
                PlatformMeta("OK.ru", Color(0xFFEE8208), "OK.ru Media", PlatformCategory.VIDEO_SHARING)
            "likee.video" in lower || "likee.com" in lower ->
                PlatformMeta("Likee", Color(0xFFFF2442), "Likee No-WM", PlatformCategory.VIDEO_SHARING)

            // Live Streaming
            "twitch.tv" in lower ->
                PlatformMeta("Twitch", Color(0xFF9146FF), "Twitch 1080p 60FPS", PlatformCategory.LIVE_STREAMING)
            "kick.com" in lower ->
                PlatformMeta("Kick", Color(0xFF53FC18), "Kick VODs/Clips", PlatformCategory.LIVE_STREAMING)
            "dlive.tv" in lower ->
                PlatformMeta("DLive", Color(0xFFFFD300), "DLive Replay", PlatformCategory.LIVE_STREAMING)
            "trovo.live" in lower ->
                PlatformMeta("Trovo", Color(0xFF19D873), "Trovo Clips", PlatformCategory.LIVE_STREAMING)

            // News & Entertainment
            "ted.com" in lower ->
                PlatformMeta("TED Talks", Color(0xFFE62B1E), "TED HD Master", PlatformCategory.NEWS_ENTERTAINMENT)
            "bbc.com" in lower || "bbc.co.uk" in lower ->
                PlatformMeta("BBC News", Color(0xFFFF0033), "BBC Video", PlatformCategory.NEWS_ENTERTAINMENT)
            "cnn.com" in lower ->
                PlatformMeta("CNN", Color(0xFFCC0000), "CNN Video", PlatformCategory.NEWS_ENTERTAINMENT)
            "aljazeera.com" in lower ->
                PlatformMeta("Al Jazeera", Color(0xFFEAA11F), "Al Jazeera HD", PlatformCategory.NEWS_ENTERTAINMENT)
            "reuters.com" in lower ->
                PlatformMeta("Reuters", Color(0xFFFF8000), "Reuters Wire", PlatformCategory.NEWS_ENTERTAINMENT)
            "dailymail.co.uk" in lower ->
                PlatformMeta("Daily Mail", Color(0xFF004D99), "Daily Mail Video", PlatformCategory.NEWS_ENTERTAINMENT)
            "theguardian.com" in lower ->
                PlatformMeta("The Guardian", Color(0xFF052962), "Guardian Video", PlatformCategory.NEWS_ENTERTAINMENT)

            // Direct Media Streams
            lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".webm") ||
            lower.endsWith(".mp3") || lower.endsWith(".m4a") || lower.endsWith(".flac") ->
                PlatformMeta("Direct Media Stream", Color(0xFF10B981), "Direct Master Stream", PlatformCategory.UNIVERSAL)

            else -> PlatformMeta("Universal Any-Web", Color(0xFF00F2FE), "OmniStream 1000+ Core", PlatformCategory.UNIVERSAL)
        }
    }
}
