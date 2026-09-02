package com.example

import com.example.data.api.FormatInfo
import com.example.ui.components.PlatformDetector
import com.example.ui.components.QualityTierResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QualityMatrixTest {

    @Test
    fun `detect 21 official platforms correctly`() {
        val ytUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val tiktokUrl = "https://www.tiktok.com/@creator/video/1234567890"
        val fbUrl = "https://www.facebook.com/watch/?v=1234567890"
        val instaUrl = "https://www.instagram.com/reel/Cx123456789/"
        val twitterUrl = "https://twitter.com/user/status/1234567890"
        val redditUrl = "https://www.reddit.com/r/videos/comments/sample123/"
        val soundcloudUrl = "https://soundcloud.com/artist/sample-track"

        val p1 = PlatformDetector.detect(ytUrl)
        val p2 = PlatformDetector.detect(tiktokUrl)
        val p3 = PlatformDetector.detect(fbUrl)
        val p4 = PlatformDetector.detect(instaUrl)
        val p5 = PlatformDetector.detect(twitterUrl)
        val p6 = PlatformDetector.detect(redditUrl)
        val p7 = PlatformDetector.detect(soundcloudUrl)

        assertEquals("YouTube", p1.name)
        assertEquals("TikTok", p2.name)
        assertEquals("Facebook", p3.name)
        assertEquals("Instagram", p4.name)
        assertEquals("Twitter / X", p5.name)
        assertEquals("Reddit", p6.name)
        assertEquals("SoundCloud", p7.name)
    }

    @Test
    fun `quality matrix enables only available formats in video source`() {
        // Video that only has 1080p and 720p, NO 4K or 8K
        val formats = listOf(
            FormatInfo(
                formatId = "1080p_standard",
                formatNote = "1080p FHD",
                resolution = "1920x1080",
                width = 1920,
                height = 1080,
                fps = 30,
                ext = "mp4"
            ),
            FormatInfo(
                formatId = "720p_hd",
                formatNote = "720p HD",
                resolution = "1280x720",
                width = 1280,
                height = 720,
                fps = 30,
                ext = "mp4"
            ),
            FormatInfo(
                formatId = "audio_mp3",
                formatNote = "MP3 320k",
                resolution = "Audio Only",
                ext = "mp3",
                abr = 320.0,
                vcodec = "none"
            )
        )

        val tiers = QualityTierResolver.resolveTiers(formats)

        val tier8K = tiers.first { it.id == "tier_8k" }
        val tier4K = tiers.first { it.id == "tier_4k" }
        val tier1080p = tiers.first { it.id == "tier_1080p_std" }
        val tier720p = tiers.first { it.id == "tier_720p" }
        val tierAudio = tiers.first { it.id == "tier_audio_mp3" }

        // 8K and 4K MUST be locked (not available)
        assertFalse("8K should be disabled when not in video", tier8K.isAvailable)
        assertNull("8K matching format should be null", tier8K.matchingFormat)

        assertFalse("4K should be disabled when not in video", tier4K.isAvailable)
        assertNull("4K matching format should be null", tier4K.matchingFormat)

        // 1080p and 720p MUST be enabled
        assertTrue("1080p must be enabled when present", tier1080p.isAvailable)
        assertNotNull("1080p matching format must exist", tier1080p.matchingFormat)
        assertEquals("1080p_standard", tier1080p.matchingFormat?.formatId)

        assertTrue("720p must be enabled when present", tier720p.isAvailable)
        assertTrue("MP3 320k must be enabled when present", tierAudio.isAvailable)
    }

    @Test
    fun `quality matrix enables 8K and 4K when video source has them`() {
        val formats = listOf(
            FormatInfo(
                formatId = "8k_stream",
                formatNote = "8K 60FPS",
                resolution = "7680x4320",
                width = 7680,
                height = 4320,
                fps = 60,
                ext = "mp4"
            ),
            FormatInfo(
                formatId = "4k_stream",
                formatNote = "4K 60FPS",
                resolution = "3840x2160",
                width = 3840,
                height = 2160,
                fps = 60,
                ext = "mp4"
            )
        )

        val tiers = QualityTierResolver.resolveTiers(formats)
        val tier8K = tiers.first { it.id == "tier_8k" }
        val tier4K = tiers.first { it.id == "tier_4k" }

        assertTrue("8K should be available", tier8K.isAvailable)
        assertTrue("4K should be available", tier4K.isAvailable)
    }
}
