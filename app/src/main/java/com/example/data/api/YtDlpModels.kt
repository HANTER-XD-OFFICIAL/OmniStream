package com.example.data.api

data class VideoInfoResponse(
    val id: String = "",
    val title: String = "",
    val thumbnail: String? = null,
    val duration: Long? = null,
    val durationString: String? = null,
    val uploader: String? = null,
    val channel: String? = null,
    val viewCount: Long? = null,
    val webpageUrl: String? = null,
    val extractor: String? = null,
    val description: String? = null,
    val formats: List<FormatInfo> = emptyList()
) {
    val author: String
        get() = uploader ?: channel ?: "Unknown Creator"

    val platformName: String
        get() = when {
            webpageUrl?.contains("youtube.com", true) == true || webpageUrl?.contains("youtu.be", true) == true -> "YouTube"
            webpageUrl?.contains("tiktok.com", true) == true -> "TikTok"
            webpageUrl?.contains("facebook.com", true) == true || webpageUrl?.contains("fb.watch", true) == true -> "Facebook"
            webpageUrl?.contains("instagram.com", true) == true -> "Instagram"
            webpageUrl?.contains("twitter.com", true) == true || webpageUrl?.contains("x.com", true) == true -> "Twitter / X"
            webpageUrl?.contains("pinterest.com", true) == true || webpageUrl?.contains("pin.it", true) == true -> "Pinterest"
            webpageUrl?.contains("terabox.com", true) == true || webpageUrl?.contains("1024tera", true) == true -> "TeraBox"
            webpageUrl?.contains("reddit.com", true) == true -> "Reddit"
            !extractor.isNullOrBlank() -> extractor
            else -> "Web Platform"
        }

    val maxUploadedHeight: Int
        get() {
            val videoHeights = formats.filter { !it.isAudioOnly }.map { it.effectiveResolutionHeight }
            return videoHeights.maxOrNull() ?: 720
        }

    val maxUploadedQualityLabel: String
        get() = when {
            maxUploadedHeight >= 4000 -> "8K Ultra HD (4320p)"
            maxUploadedHeight >= 2000 -> "4K Ultra HD (2160p)"
            maxUploadedHeight >= 1400 -> "2K Quad HD (1440p)"
            maxUploadedHeight >= 1000 -> "1080p Full HD"
            maxUploadedHeight >= 700 -> "720p HD"
            maxUploadedHeight >= 400 -> "480p SD"
            else -> "360p Standard"
        }

    val platformMaxSupportedLabel: String
        get() = when (platformName) {
            "TikTok" -> "1080p Full HD (No Watermark)"
            "Instagram" -> "1080p Full HD"
            "Facebook" -> "1080p HD"
            "Twitter / X" -> "1080p HD"
            "Pinterest" -> "1080p HD"
            "YouTube" -> "4K / 8K Ultra HD"
            "TeraBox" -> "Source Quality"
            "Reddit" -> "1080p HD"
            else -> "Source Quality"
        }

    val suggestedQualityLabel: String
        get() = "$maxUploadedQualityLabel (Native Creator Stream)"

    val displayDuration: String
        get() {
            if (!durationString.isNullOrEmpty()) return durationString
            val sec = duration ?: 0L
            if (sec <= 0) return "--:--"
            val hours = sec / 3600
            val minutes = (sec % 3600) / 60
            val seconds = sec % 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
}

data class FormatInfo(
    val formatId: String = "",
    val formatNote: String? = null,
    val resolution: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val fps: Int? = null,
    val ext: String = "mp4",
    val vcodec: String? = null,
    val acodec: String? = null,
    val filesize: Long? = null,
    val filesizeApprox: Long? = null,
    val tbr: Double? = null,
    val abr: Double? = null,
    val url: String? = null
) {
    val effectiveResolutionHeight: Int
        get() {
            val w = width ?: 0
            val h = height ?: 0
            return if (w > 0 && h > 0) minOf(w, h) else (h.takeIf { it > 0 } ?: w)
        }
    val isAudioOnly: Boolean
        get() = ((vcodec == "none" || vcodec == null) && (acodec != null && acodec != "none")) ||
                resolution.equals("Audio Only", ignoreCase = true) ||
                (ext in listOf("mp3", "m4a", "flac", "wav", "opus", "ogg", "aac") && (vcodec == null || vcodec == "none"))

    val isVideoOnly: Boolean
        get() = (vcodec != null && vcodec != "none") && (acodec == "none" || acodec == null)

    val isCombined: Boolean
        get() = (vcodec != null && vcodec != "none") && (acodec != null && acodec != "none")

    val estimatedBytes: Long
        get() = filesize ?: filesizeApprox ?: 0L

    val readableSize: String
        get() {
            val bytes = estimatedBytes
            if (bytes <= 0) return "Dynamic / Stream"
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.2f GB", gb)
                mb >= 1.0 -> String.format("%.1f MB", mb)
                else -> String.format("%.0f KB", kb)
            }
        }

    val displayQualityBadge: String
        get() {
            val h = height ?: 0
            val f = fps ?: 30
            return when {
                h >= 4320 -> "8K $f FPS"
                h >= 2160 -> "4K $f FPS"
                h >= 1440 -> "2K $f FPS"
                h >= 1080 -> if (f >= 60) "1080p $f FPS" else "1080p"
                h >= 720 -> if (f >= 60) "720p $f FPS" else "720p"
                h >= 480 -> "480p"
                h >= 360 -> "360p"
                isAudioOnly -> {
                    val rate = abr?.toInt() ?: 192
                    "Audio ${rate}k"
                }
                else -> resolution ?: (formatNote ?: "Standard")
            }
        }
}

data class ApiHealthResponse(
    val status: String = "ok",
    val ytdlpVersion: String = "2026.08.01",
    val latencyMs: Long = 0L,
    val message: String = "yt-dlp engine online"
)
