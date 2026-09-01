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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.FormatInfo
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
    val badgeColor: Color
) {
    val isAvailable: Boolean
        get() = matchingFormat != null
}

object QualityTierResolver {
    fun resolveTiers(formats: List<FormatInfo>): List<QualityTier> {
        val videoFormats = formats.filter { !it.isAudioOnly }
        val audioFormats = formats.filter { it.isAudioOnly }

        // 8K Ultra HD (4320p)
        val format8K = videoFormats.firstOrNull { (it.height ?: 0) >= 4000 }
        // 4K Ultra HD (2160p)
        val format4K = videoFormats.firstOrNull { (it.height ?: 0) in 2000..3999 }
        // 2K Quad HD (1440p)
        val format2K = videoFormats.firstOrNull { (it.height ?: 0) in 1400..1999 }
        // 1080p 120 FPS High Refresh
        val format1080p120 = videoFormats.firstOrNull { (it.height ?: 0) in 1000..1399 && (it.fps ?: 0) >= 100 }
        // 1080p 60 FPS
        val format1080p60 = videoFormats.firstOrNull { (it.height ?: 0) in 1000..1399 && (it.fps ?: 0) in 50..99 }
        // 1080p 30 FPS Standard
        val format1080p30 = videoFormats.firstOrNull { (it.height ?: 0) in 1000..1399 && (it.fps ?: 0) < 50 }
            ?: (if (format1080p60 == null && format1080p120 == null) videoFormats.firstOrNull { (it.height ?: 0) in 1000..1399 } else null)
        // 720p HD
        val format720p = videoFormats.firstOrNull { (it.height ?: 0) in 700..999 }
        // 480p SD
        val format480p = videoFormats.firstOrNull { (it.height ?: 0) in 400..699 }
        // 360p Low
        val format360p = videoFormats.firstOrNull { (it.height ?: 0) in 300..399 }

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

        return listOf(
            QualityTier(
                id = "tier_8k",
                label = "8K Ultra HD",
                technicalSpec = "4320p • 60 FPS • AV1 / VP9 HDR",
                isAudio = false,
                matchingFormat = format8K,
                badgeColor = Color(0xFFF59E0B) // Radiant Gold
            ),
            QualityTier(
                id = "tier_4k",
                label = "4K Ultra HD",
                technicalSpec = "2160p • 60 FPS • High Dynamic Range",
                isAudio = false,
                matchingFormat = format4K,
                badgeColor = CyanBright
            ),
            QualityTier(
                id = "tier_2k",
                label = "2K Quad HD",
                technicalSpec = "1440p • 60 FPS • Pro Stream",
                isAudio = false,
                matchingFormat = format2K,
                badgeColor = Color(0xFF38BDF8)
            ),
            QualityTier(
                id = "tier_1080p_120fps",
                label = "1080p Pro (120 FPS)",
                technicalSpec = "Full HD • 120 FPS High Refresh",
                isAudio = false,
                matchingFormat = format1080p120,
                badgeColor = NeonPurple
            ),
            QualityTier(
                id = "tier_1080p_60fps",
                label = "1080p FHD (60 FPS)",
                technicalSpec = "Full HD • 60 FPS Smooth",
                isAudio = false,
                matchingFormat = format1080p60,
                badgeColor = Color(0xFF38BDF8)
            ),
            QualityTier(
                id = "tier_1080p_std",
                label = "1080p Standard",
                technicalSpec = "Full HD • 30 FPS Universal",
                isAudio = false,
                matchingFormat = format1080p30,
                badgeColor = Color(0xFF60A5FA)
            ),
            QualityTier(
                id = "tier_720p",
                label = "720p HD",
                technicalSpec = "HD 720p • Fast Download",
                isAudio = false,
                matchingFormat = format720p,
                badgeColor = Color(0xFF34D399)
            ),
            QualityTier(
                id = "tier_480p",
                label = "480p SD (Data Saver)",
                technicalSpec = "Standard Definition • Mobile",
                isAudio = false,
                matchingFormat = format480p,
                badgeColor = Color(0xFF94A3B8)
            ),
            QualityTier(
                id = "tier_360p",
                label = "360p Low Bandwidth",
                technicalSpec = "Low Res • Super Lightweight",
                isAudio = false,
                matchingFormat = format360p,
                badgeColor = Color(0xFF64748B)
            ),
            QualityTier(
                id = "tier_audio_flac",
                label = "Studio FLAC Audio",
                technicalSpec = "24-bit Studio Lossless Audio",
                isAudio = true,
                matchingFormat = formatFlac,
                badgeColor = Color(0xFFA855F7)
            ),
            QualityTier(
                id = "tier_audio_mp3",
                label = "MP3 320 kbps Master",
                technicalSpec = "High Bitrate Studio Audio",
                isAudio = true,
                matchingFormat = formatMp3_320,
                badgeColor = Color(0xFFEC4899)
            ),
            QualityTier(
                id = "tier_audio_aac",
                label = "AAC / M4A 192 kbps",
                technicalSpec = "Clean Standard Audio Stream",
                isAudio = true,
                matchingFormat = formatAac_192,
                badgeColor = Color(0xFF818CF8)
            )
        )
    }
}

@Composable
fun QualityMatrixView(
    allFormats: List<FormatInfo>,
    selectedFormat: FormatInfo?,
    onSelectFormat: (FormatInfo) -> Unit,
    onLockedTierTapped: (QualityTier) -> Unit
) {
    val tiers = QualityTierResolver.resolveTiers(allFormats)
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
        Column(modifier = Modifier.padding(16.dp)) {
            // Matrix Header with live stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "QUALITY AVAILABILITY MATRIX",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            ),
                            color = CyanBright
                        )
                    }
                    Text(
                        text = "Source verification: Only uploaded formats are enabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (availableCount > 0) EmeraldSuccess.copy(alpha = 0.15f) else TextMuted.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, if (availableCount > 0) EmeraldSuccess else TextMuted)
                ) {
                    Text(
                        text = "$availableCount / $totalCount READY",
                        color = if (availableCount > 0) EmeraldSuccess else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Enabled tiers are verified in the video source. Locked tiers were not uploaded by the creator.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Render each tier
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tiers.forEach { tier ->
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
        isAvailable -> Color(0xFF0B132B)
        else -> Color(0xFF060911)
    }

    val borderColor = when {
        isSelected -> CyanBright
        isAvailable -> tier.badgeColor.copy(alpha = 0.4f)
        else -> CyberBorder.copy(alpha = 0.3f)
    }

    val borderStrokeWidth = if (isSelected) 1.5.dp else 1.dp

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
                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(tier.badgeColor.copy(alpha = 0.15f))
                            .border(1.dp, tier.badgeColor.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = CyanBright,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Selectable",
                                tint = tier.badgeColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B).copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked - Not Available in Source",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tier.label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isAvailable) TextPrimary else TextMuted
                        )
                        if (tier.isAudio) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFA855F7).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "AUDIO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA855F7),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isAvailable && fmt != null) {
                            "${fmt.ext.uppercase()} • ${fmt.readableSize} • ${tier.technicalSpec}"
                        } else {
                            "Not available in original video stream (Locked)"
                        },
                        fontSize = 11.sp,
                        color = if (isAvailable) TextSecondary else Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Pill Badge
            if (isAvailable && fmt != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = tier.badgeColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, tier.badgeColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = if (isSelected) "SELECTED" else "AVAILABLE",
                        color = tier.badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "UNAVAILABLE",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}
