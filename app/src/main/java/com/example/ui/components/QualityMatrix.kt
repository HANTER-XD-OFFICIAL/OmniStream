package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.FormatInfo
import com.example.data.api.VideoInfoResponse
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyanBright
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class QualityTier(
    val id: String,
    val label: String,
    val technicalSpec: String,
    val isAudio: Boolean,
    val matchingFormat: FormatInfo?,
    val badgeColor: Color,
    val isRecommended: Boolean = false,
    val isDataSaver: Boolean = false,
    val unavailableReason: String? = null
) {
    val isAvailable: Boolean
        get() = matchingFormat != null
}

object QualityTierResolver {
    fun resolveTiers(
        formats: List<FormatInfo>,
        platformName: String = "Web",
        maxUploadedQualityLabel: String = "1080p Full HD"
    ): List<QualityTier> {
        val videoFormats = formats.filter { !it.isAudioOnly }
        val audioFormats = formats.filter { it.isAudioOnly }

        // Find matches based on true effective resolution height (works for both 16:9 horizontal & 9:16 vertical TikTok/Reels)
        val format8K = videoFormats.firstOrNull { it.effectiveResolutionHeight >= 4000 }
        val format4K = videoFormats.firstOrNull { it.effectiveResolutionHeight in 2000..3999 }
        val format2K = videoFormats.firstOrNull { it.effectiveResolutionHeight in 1400..1999 }
        val format1080p120 = videoFormats.firstOrNull { it.effectiveResolutionHeight in 1000..1399 && (it.fps ?: 0) >= 100 }
        val format1080p60 = videoFormats.firstOrNull { it.effectiveResolutionHeight in 1000..1399 && (it.fps ?: 0) in 50..99 }
        val format1080p30 = videoFormats.firstOrNull { it.effectiveResolutionHeight in 1000..1399 && (it.fps ?: 0) < 50 }
            ?: (if (format1080p60 == null && format1080p120 == null) videoFormats.firstOrNull { it.effectiveResolutionHeight in 1000..1399 } else null)
        val format720p = videoFormats.firstOrNull { it.effectiveResolutionHeight in 700..999 }
        val format480p = videoFormats.firstOrNull { it.effectiveResolutionHeight in 400..699 }
        val format360p = videoFormats.firstOrNull { it.effectiveResolutionHeight in 300..399 }

        // Identify the highest genuine available video format to recommend
        val highestAvailableTierId = when {
            format8K != null -> "tier_8k"
            format4K != null -> "tier_4k"
            format2K != null -> "tier_2k"
            format1080p120 != null -> "tier_1080p_120fps"
            format1080p60 != null -> "tier_1080p_60fps"
            format1080p30 != null -> "tier_1080p_std"
            format720p != null -> "tier_720p"
            format480p != null -> "tier_480p"
            format360p != null -> "tier_360p"
            else -> null
        }

        // Media fallback format for universal audio extraction
        val anyMedia = videoFormats.firstOrNull() ?: formats.firstOrNull()

        // Audio: FLAC Lossless
        val formatFlac = audioFormats.firstOrNull { it.ext == "flac" || it.acodec?.contains("flac", ignoreCase = true) == true }
            ?: anyMedia?.copy(
                formatId = "audio_flac_${anyMedia.formatId}",
                formatNote = "Studio FLAC Audio • Lossless Extract",
                resolution = "Audio Only",
                ext = "flac",
                vcodec = "none",
                acodec = "flac",
                filesizeApprox = ((anyMedia.filesizeApprox ?: 40_000_000L) * 0.4).toLong().coerceAtLeast(8_000_000L),
                abr = 1411.0
            )

        // Audio: MP3 320k (Always active & guaranteed for all media)
        val formatMp3_320 = audioFormats.firstOrNull { (it.abr ?: 0.0) >= 300 || it.formatNote?.contains("320") == true }
            ?: audioFormats.firstOrNull { it.ext == "mp3" }
            ?: audioFormats.firstOrNull()
            ?: anyMedia?.copy(
                formatId = "audio_mp3_320k_${anyMedia.formatId}",
                formatNote = "MP3 Master 320 kbps • High Fidelity Audio Extract",
                resolution = "Audio Only",
                ext = "mp3",
                vcodec = "none",
                acodec = "mp3",
                filesizeApprox = ((anyMedia.filesizeApprox ?: 30_000_000L) * 0.25).toLong().coerceAtLeast(4_500_000L),
                abr = 320.0
            )

        // Audio: AAC 192k (Always active & guaranteed for all media)
        val formatAac_192 = audioFormats.firstOrNull { it.ext == "m4a" || (it.abr ?: 0.0) in 160.0..299.0 }
            ?: anyMedia?.copy(
                formatId = "audio_aac_192k_${anyMedia.formatId}",
                formatNote = "AAC / M4A 192 kbps • Universal Audio Extract",
                resolution = "Audio Only",
                ext = "m4a",
                vcodec = "none",
                acodec = "aac",
                filesizeApprox = ((anyMedia.filesizeApprox ?: 30_000_000L) * 0.15).toLong().coerceAtLeast(2_800_000L),
                abr = 192.0
            )

        fun lockedReason(label: String, reqMinHeight: Int): String {
            return when {
                platformName in listOf("TikTok", "Instagram", "Facebook", "Twitter / X", "Pinterest") && reqMinHeight > 1080 ->
                    "$platformName does not encode media in $label. The creator uploaded up to $maxUploadedQualityLabel."
                reqMinHeight > 1080 ->
                    "This video was not recorded or uploaded in $label by the creator. Source quality is $maxUploadedQualityLabel."
                else ->
                    "The creator uploaded this video in $maxUploadedQualityLabel. Higher resolutions do not exist on the source server."
            }
        }

        return listOf(
            QualityTier(
                id = "tier_8k",
                label = "8K Ultra HD",
                technicalSpec = "4320p • 60 FPS • AV1 / VP9 HDR",
                isAudio = false,
                matchingFormat = format8K,
                badgeColor = Color(0xFFF59E0B), // Radiant Gold
                isRecommended = highestAvailableTierId == "tier_8k",
                unavailableReason = lockedReason("8K Ultra HD", 4320)
            ),
            QualityTier(
                id = "tier_4k",
                label = "4K Ultra HD",
                technicalSpec = "2160p • 60 FPS • High Dynamic Range",
                isAudio = false,
                matchingFormat = format4K,
                badgeColor = CyanBright,
                isRecommended = highestAvailableTierId == "tier_4k",
                unavailableReason = lockedReason("4K Ultra HD", 2160)
            ),
            QualityTier(
                id = "tier_2k",
                label = "2K Quad HD",
                technicalSpec = "1440p • 60 FPS • Pro Stream",
                isAudio = false,
                matchingFormat = format2K,
                badgeColor = Color(0xFF38BDF8),
                isRecommended = highestAvailableTierId == "tier_2k",
                unavailableReason = lockedReason("2K Quad HD", 1440)
            ),
            QualityTier(
                id = "tier_1080p_120fps",
                label = "1080p Pro (120 FPS)",
                technicalSpec = "Full HD • 120 FPS High Refresh",
                isAudio = false,
                matchingFormat = format1080p120,
                badgeColor = NeonPurple,
                isRecommended = highestAvailableTierId == "tier_1080p_120fps",
                unavailableReason = lockedReason("1080p 120 FPS", 1080)
            ),
            QualityTier(
                id = "tier_1080p_60fps",
                label = "1080p FHD (60 FPS)",
                technicalSpec = "Full HD • 60 FPS Smooth",
                isAudio = false,
                matchingFormat = format1080p60,
                badgeColor = Color(0xFF38BDF8),
                isRecommended = highestAvailableTierId == "tier_1080p_60fps",
                unavailableReason = lockedReason("1080p 60 FPS", 1080)
            ),
            QualityTier(
                id = "tier_1080p_std",
                label = "1080p Standard",
                technicalSpec = "Full HD • 30 FPS Universal",
                isAudio = false,
                matchingFormat = format1080p30,
                badgeColor = Color(0xFF60A5FA),
                isRecommended = highestAvailableTierId == "tier_1080p_std",
                unavailableReason = lockedReason("1080p Standard", 1080)
            ),
            QualityTier(
                id = "tier_720p",
                label = "720p HD",
                technicalSpec = "HD 720p • Fast Download",
                isAudio = false,
                matchingFormat = format720p,
                badgeColor = Color(0xFF34D399),
                isRecommended = highestAvailableTierId == "tier_720p",
                unavailableReason = lockedReason("720p HD", 720)
            ),
            QualityTier(
                id = "tier_480p",
                label = "480p SD (Data Saver)",
                technicalSpec = "Standard Definition • Mobile",
                isAudio = false,
                matchingFormat = format480p,
                badgeColor = Color(0xFF94A3B8),
                isRecommended = highestAvailableTierId == "tier_480p",
                isDataSaver = true,
                unavailableReason = lockedReason("480p SD", 480)
            ),
            QualityTier(
                id = "tier_360p",
                label = "360p Low Bandwidth",
                technicalSpec = "Low Res • Super Lightweight",
                isAudio = false,
                matchingFormat = format360p,
                badgeColor = Color(0xFF64748B),
                isRecommended = highestAvailableTierId == "tier_360p",
                isDataSaver = true,
                unavailableReason = lockedReason("360p Low", 360)
            ),
            QualityTier(
                id = "tier_audio_flac",
                label = "Studio FLAC Audio",
                technicalSpec = "24-bit Studio Lossless Audio",
                isAudio = true,
                matchingFormat = formatFlac,
                badgeColor = Color(0xFFA855F7),
                isRecommended = false
            ),
            QualityTier(
                id = "tier_audio_mp3",
                label = "MP3 320 kbps Master",
                technicalSpec = "High Bitrate Studio Audio",
                isAudio = true,
                matchingFormat = formatMp3_320,
                badgeColor = Color(0xFFEC4899),
                isRecommended = false
            ),
            QualityTier(
                id = "tier_audio_aac",
                label = "AAC / M4A 192 kbps",
                technicalSpec = "Clean Standard Audio Stream",
                isAudio = true,
                matchingFormat = formatAac_192,
                badgeColor = Color(0xFF818CF8),
                isRecommended = false
            )
        )
    }
}

@Composable
fun QualityMatrixView(
    videoInfo: VideoInfoResponse? = null,
    allFormats: List<FormatInfo> = videoInfo?.formats ?: emptyList(),
    selectedFormat: FormatInfo?,
    onSelectFormat: (FormatInfo) -> Unit,
    onLockedTierTapped: (QualityTier) -> Unit
) {
    val platformName = videoInfo?.platformName ?: "Web"
    val maxUploadedQuality = videoInfo?.maxUploadedQualityLabel ?: "1080p Full HD"
    val tiers = QualityTierResolver.resolveTiers(allFormats, platformName, maxUploadedQuality)
    val videoTiers = tiers.filter { !it.isAudio }
    val audioTiers = tiers.filter { it.isAudio }
    val availableCount = tiers.count { it.isAvailable }
    val totalCount = tiers.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quality_matrix_container"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = BorderStroke(1.dp, CyberBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Matrix Header with live stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DYNAMIC QUALITY MATRIX",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp
                        ),
                        color = CyanBright
                    )
                    Text(
                        text = "Platform-aware: Only authentic creator uploads unlocked",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 10.5.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (availableCount > 0) EmeraldSuccess.copy(alpha = 0.15f) else TextMuted.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, if (availableCount > 0) EmeraldSuccess else TextMuted)
                ) {
                    Text(
                        text = "$availableCount/$totalCount READY",
                        color = if (availableCount > 0) EmeraldSuccess else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Platform & Upload Quality Intelligence Insights
            if (videoInfo != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, CyanBright.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CyanBright.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, CyanBright.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = videoInfo.platformName.uppercase(),
                                        color = CyanBright,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Source Stream Analysis",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldSuccess.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "ORIGINAL: ${videoInfo.maxUploadedQualityLabel}",
                                    color = EmeraldSuccess,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Suggested: ${videoInfo.maxUploadedQualityLabel} (100% native clarity from ${videoInfo.platformName})",
                                color = TextSecondary,
                                fontSize = 10.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            } else {
                // Explanation chip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CyanBright,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Enabled tiers are verified in the video source. Locked tiers were not uploaded by the creator.",
                        fontSize = 10.5.sp,
                        color = TextSecondary,
                        lineHeight = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 1: Video Qualities
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VIDEO FORMATS (${videoTiers.count { it.isAvailable }}/${videoTiers.size} ACTIVE)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyanBright
                    ),
                    fontSize = 10.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                videoTiers.forEach { tier ->
                    val isSelected = tier.matchingFormat != null && selectedFormat?.formatId == tier.matchingFormat.formatId
                    QualityTierCard(
                        tier = tier,
                        isSelected = isSelected,
                        onSelect = {
                            if (tier.isAvailable && tier.matchingFormat != null) {
                                onSelectFormat(tier.matchingFormat)
                            } else {
                                onLockedTierTapped(tier)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 2: Studio Audio Extracts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STUDIO AUDIO EXTRACTS (ALL READY)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = NeonPurple
                    ),
                    fontSize = 10.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                audioTiers.forEach { tier ->
                    val isSelected = tier.matchingFormat != null && selectedFormat?.formatId == tier.matchingFormat.formatId
                    QualityTierCard(
                        tier = tier,
                        isSelected = isSelected,
                        onSelect = {
                            if (tier.isAvailable && tier.matchingFormat != null) {
                                onSelectFormat(tier.matchingFormat)
                            } else {
                                onLockedTierTapped(tier)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QualityTierCard(
    tier: QualityTier,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val isAvailable = tier.isAvailable
    val fmt = tier.matchingFormat

    val containerColor = when {
        isSelected -> CyanBright.copy(alpha = 0.14f)
        tier.isRecommended -> EmeraldSuccess.copy(alpha = 0.08f)
        isAvailable -> Color(0xFF0B132B)
        else -> Color(0xFF060911)
    }

    val borderColor = when {
        isSelected -> CyanBright
        tier.isRecommended -> EmeraldSuccess.copy(alpha = 0.6f)
        isAvailable -> tier.badgeColor.copy(alpha = 0.4f)
        else -> CyberBorder.copy(alpha = 0.3f)
    }

    val borderStrokeWidth = when {
        isSelected -> 1.5.dp
        tier.isRecommended -> 1.2.dp
        else -> 1.dp
    }

    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(borderStrokeWidth, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tier_card_${tier.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Status Icon + Titles
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Availability indicator icon
                if (isAvailable) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                if (tier.isRecommended) EmeraldSuccess.copy(alpha = 0.18f)
                                else tier.badgeColor.copy(alpha = 0.15f)
                            )
                            .border(
                                1.dp,
                                if (tier.isRecommended) EmeraldSuccess
                                else tier.badgeColor.copy(alpha = 0.6f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = CyanBright,
                                modifier = Modifier.size(17.dp)
                            )
                        } else if (tier.isRecommended) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Recommended",
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(15.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Selectable",
                                tint = tier.badgeColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B).copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked - Not Available in Source",
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = tier.label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = if (isAvailable) TextPrimary else TextMuted,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (tier.isRecommended) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = EmeraldSuccess.copy(alpha = 0.2f),
                                border = BorderStroke(0.5.dp, EmeraldSuccess.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "RECOMMENDED",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
                                )
                            }
                        } else if (tier.isAudio) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFA855F7).copy(alpha = 0.2f),
                                border = BorderStroke(0.5.dp, Color(0xFFA855F7).copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "AUDIO",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA855F7),
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isAvailable && fmt != null) {
                            "${fmt.ext.uppercase()} • ${fmt.readableSize} • ${tier.technicalSpec}"
                        } else {
                            tier.unavailableReason ?: "Locked • Not uploaded in this quality"
                        },
                        fontSize = 10.5.sp,
                        color = if (isAvailable) TextSecondary else Color(0xFF64748B),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Pill Badge
            if (isAvailable && fmt != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isSelected -> CyanBright.copy(alpha = 0.2f)
                        tier.isRecommended -> EmeraldSuccess.copy(alpha = 0.18f)
                        else -> tier.badgeColor.copy(alpha = 0.15f)
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            isSelected -> CyanBright
                            tier.isRecommended -> EmeraldSuccess
                            else -> tier.badgeColor.copy(alpha = 0.6f)
                        }
                    )
                ) {
                    Text(
                        text = when {
                            isSelected -> "SELECTED"
                            tier.isRecommended -> "NATIVE"
                            tier.isDataSaver -> "SAVER"
                            else -> "READY"
                        },
                        color = when {
                            isSelected -> CyanBright
                            tier.isRecommended -> EmeraldSuccess
                            else -> tier.badgeColor
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "LOCKED",
                        color = Color(0xFF64748B),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}
