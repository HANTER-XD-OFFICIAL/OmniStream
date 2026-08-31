package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.DownloadEntity
import com.example.data.local.DownloadStatus
import com.example.data.local.MediaType
import com.example.ui.DownloadViewModel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanBright
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.RoseError
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DownloadsScreen(
    viewModel: DownloadViewModel,
    onNavigateToHome: () -> Unit
) {
    val downloads by viewModel.filteredDownloads.collectAsStateWithLifecycle()
    val currentFilter by viewModel.downloadsFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("downloads_screen_list"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Download Manager",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "${downloads.size} items in queue & library",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                if (downloads.any { it.status == DownloadStatus.COMPLETED }) {
                    IconButton(
                        onClick = { viewModel.clearCompleted() },
                        modifier = Modifier.testTag("clear_completed_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Completed",
                            tint = CyanBright
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_downloads_input"),
                placeholder = { Text("Search downloads by title or quality...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanBright,
                    unfocusedBorderColor = CyberBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = CyanBright
                )
            )
        }

        // Filter Chips Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterTabChip("All", currentFilter == "ALL") { viewModel.setDownloadsFilter("ALL") }
                FilterTabChip("Active", currentFilter == "DOWNLOADING") { viewModel.setDownloadsFilter("DOWNLOADING") }
                FilterTabChip("Videos", currentFilter == "VIDEO") { viewModel.setDownloadsFilter("VIDEO") }
                FilterTabChip("Audio", currentFilter == "AUDIO") { viewModel.setDownloadsFilter("AUDIO") }
                FilterTabChip("Completed", currentFilter == "COMPLETED") { viewModel.setDownloadsFilter("COMPLETED") }
            }
        }

        // Downloads List or Empty State
        if (downloads.isEmpty()) {
            item {
                EmptyDownloadsState(onNavigateToHome = onNavigateToHome)
            }
        } else {
            items(downloads, key = { it.id }) { item ->
                DownloadItemCard(
                    item = item,
                    onPlay = { viewModel.setPreviewItem(item) },
                    onPause = { viewModel.pauseDownload(item.id) },
                    onResume = { viewModel.resumeDownload(item.id) },
                    onCancel = { viewModel.cancelDownload(item.id) },
                    onDelete = { viewModel.deleteDownload(item.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun DownloadItemCard(
    item: DownloadEntity,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    val isAudio = item.mediaType == MediaType.AUDIO
    val isDownloading = item.status == DownloadStatus.DOWNLOADING || item.status == DownloadStatus.QUEUED
    val isCompleted = item.status == DownloadStatus.COMPLETED
    val isPaused = item.status == DownloadStatus.PAUSED
    val isFailed = item.status == DownloadStatus.FAILED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("download_item_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
        border = BorderStroke(
            1.dp,
            when {
                isDownloading -> CyanBright.copy(alpha = 0.5f)
                isCompleted -> EmeraldSuccess.copy(alpha = 0.3f)
                isFailed -> RoseError.copy(alpha = 0.3f)
                else -> CyberBorder
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Media Type Icon / Thumbnail
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isAudio) NeonPurple.copy(alpha = 0.15f) else CyanBright.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.thumbnailUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.thumbnailUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = if (isAudio) Icons.Default.Audiotrack else Icons.Default.Videocam,
                            contentDescription = null,
                            tint = if (isAudio) NeonPurple else CyanBright,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CyanAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = item.resolution,
                                color = CyanAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }

                        Text(
                            text = "${item.ext.uppercase()} • ${item.authorOrChannel}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Status Badge
                StatusPill(status = item.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar & Speed / ETA during downloading
            if (isDownloading) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { item.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CyanBright,
                        trackColor = CyberDarkSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val downloadedMb = item.downloadedBytes / (1024.0 * 1024.0)
                        val totalMb = item.totalBytes / (1024.0 * 1024.0)
                        val sizeString = if (totalMb > 0) {
                            String.format("%.1f / %.1f MB (%d%%)", downloadedMb, totalMb, item.progressPercent)
                        } else {
                            String.format("%.1f MB downloaded", downloadedMb)
                        }

                        Text(text = sizeString, fontSize = 11.sp, color = TextSecondary)

                        val speedEta = listOfNotNull(
                            item.downloadSpeedText.takeIf { it.isNotBlank() },
                            item.etaText.takeIf { it.isNotBlank() }?.let { "ETA: $it" }
                        ).joinToString(" • ")

                        Text(
                            text = speedEta.ifEmpty { "Streaming..." },
                            fontSize = 11.sp,
                            color = CyanBright,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Error message if failed
            if (isFailed && !item.errorMessage.isNullOrEmpty()) {
                Text(
                    text = item.errorMessage,
                    color = RoseError,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Card Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCompleted) {
                    OutlinedButton(
                        onClick = onPlay,
                        modifier = Modifier.height(34.dp).testTag("play_button_${item.id}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = CyanBright
                        ),
                        border = BorderStroke(1.dp, CyanBright)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Play / Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted)
                    }
                } else if (isDownloading) {
                    IconButton(onClick = onPause, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause", tint = AmberWarning)
                    }
                    IconButton(onClick = onCancel, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = RoseError)
                    }
                } else if (isPaused) {
                    IconButton(onClick = onResume, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = CyanBright)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted)
                    }
                } else if (isFailed) {
                    IconButton(onClick = onResume, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = CyanBright)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusPill(status: DownloadStatus) {
    val (color, text) = when (status) {
        DownloadStatus.COMPLETED -> EmeraldSuccess to "Done"
        DownloadStatus.DOWNLOADING -> CyanBright to "Active"
        DownloadStatus.QUEUED -> CyanAccent to "Queued"
        DownloadStatus.PAUSED -> AmberWarning to "Paused"
        DownloadStatus.FAILED -> RoseError to "Failed"
        DownloadStatus.CANCELLED -> TextMuted to "Cancelled"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.6f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun FilterTabChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) CyanBright.copy(alpha = 0.2f) else CyberDarkSurface,
        border = BorderStroke(1.dp, if (isSelected) CyanBright else CyberBorder)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) CyanBright else TextSecondary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun EmptyDownloadsState(onNavigateToHome: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(CyberDarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DownloadDone,
                    contentDescription = null,
                    tint = CyanBright,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Downloads Found",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Paste any link in the Downloader tab to start downloading 4K, 8K, 60fps & audio formats!",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedButton(
                onClick = onNavigateToHome,
                shape = RoundedCornerShape(10.dp),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = CyanBright),
                border = BorderStroke(1.dp, CyanBright)
            ) {
                Text("Go to Downloader", fontWeight = FontWeight.Bold)
            }
        }
    }
}
