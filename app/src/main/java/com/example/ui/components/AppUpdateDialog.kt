package com.example.ui.components

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.api.AppReleaseInfo
import com.example.data.api.AppUpdateManager
import com.example.data.api.UpdateState
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyberBlack
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
import java.io.File

/**
 * Modern Material 3 In-App Update Dialog for OmniStream
 * - Displays update details & changelog
 * - Shows in-app real-time download progress bar, speed, and percentage
 * - Direct one-tap install trigger without redirecting to external websites
 */
@Composable
fun AppUpdateDialog(
    updateState: UpdateState,
    onStartDownload: (AppReleaseInfo) -> Unit,
    onCancelDownload: () -> Unit,
    onInstallApk: (File) -> Unit,
    onDismiss: (rememberDismissal: Boolean) -> Unit
) {
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "update_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(
        onDismissRequest = {
            if (updateState !is UpdateState.Downloading) {
                onDismiss(true)
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = updateState !is UpdateState.Downloading,
            dismissOnClickOutside = updateState !is UpdateState.Downloading,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 500.dp)
                .padding(vertical = 24.dp)
                .testTag("update_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = CyberDarkSurface,
            border = BorderStroke(1.5.dp, CyanBright.copy(alpha = 0.7f)),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row with Icon & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .scale(if (updateState is UpdateState.Downloading) pulseScale else 1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(CyanBright.copy(alpha = 0.25f), NeonPurple.copy(alpha = 0.25f)))
                                )
                                .border(1.dp, CyanBright, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (updateState) {
                                    is UpdateState.DownloadCompleted -> Icons.Default.CheckCircle
                                    is UpdateState.Downloading -> Icons.Default.CloudDownload
                                    is UpdateState.UpToDate -> Icons.Default.CheckCircle
                                    else -> Icons.Default.RocketLaunch
                                },
                                contentDescription = null,
                                tint = when (updateState) {
                                    is UpdateState.DownloadCompleted -> EmeraldSuccess
                                    is UpdateState.UpToDate -> EmeraldSuccess
                                    else -> CyanBright
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = when (updateState) {
                                    is UpdateState.DownloadCompleted -> "DOWNLOAD COMPLETE"
                                    is UpdateState.Downloading -> "UPDATING OMNISTREAM"
                                    is UpdateState.UpToDate -> "UP TO DATE"
                                    else -> "NEW UPDATE AVAILABLE"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                ),
                                color = when (updateState) {
                                    is UpdateState.DownloadCompleted -> EmeraldSuccess
                                    is UpdateState.UpToDate -> EmeraldSuccess
                                    else -> CyanBright
                                }
                            )
                            Text(
                                text = "Official OmniStream App",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                        }
                    }

                    if (updateState !is UpdateState.Downloading) {
                        IconButton(
                            onClick = { onDismiss(true) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content Based on State
                when (updateState) {
                    is UpdateState.Checking -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(38.dp),
                                    color = CyanBright,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    "Checking GitHub Releases...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    is UpdateState.Downloading -> {
                        DownloadingProgressSection(
                            state = updateState,
                            onCancel = onCancelDownload
                        )
                    }

                    is UpdateState.DownloadCompleted -> {
                        DownloadCompletedSection(
                            state = updateState,
                            onInstall = { onInstallApk(updateState.apkFile) }
                        )
                    }

                    is UpdateState.UpdateAvailable -> {
                        UpdateDetailsSection(
                            info = updateState.info,
                            onUpdateNow = { onStartDownload(updateState.info) },
                            onLater = { onDismiss(true) }
                        )
                    }

                    is UpdateState.UpToDate -> {
                        UpToDateSection(
                            info = updateState.info,
                            onReinstall = { onStartDownload(updateState.info) },
                            onClose = { onDismiss(false) }
                        )
                    }

                    is UpdateState.Error -> {
                        ErrorSection(
                            state = updateState,
                            onRetry = {
                                if (updateState.info != null) {
                                    onStartDownload(updateState.info)
                                } else {
                                    AppUpdateManager.getInstance(context).checkForUpdates(isManual = true)
                                }
                            },
                            onDismiss = { onDismiss(false) }
                        )
                    }

                    UpdateState.Idle -> {
                        // Empty or fallback
                    }
                }
            }
        }
    }
}

/**
 * Section displaying update info, version pills, and changelog
 */
@Composable
private fun UpdateDetailsSection(
    info: AppReleaseInfo,
    onUpdateNow: () -> Unit,
    onLater: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Version Comparison & Size Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Version Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CyanBright.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, CyanBright.copy(alpha = 0.4f)),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "v${info.currentVersion}",
                        fontSize = 11.5.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = CyanBright,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = info.tagName,
                        fontSize = 12.sp,
                        color = CyanBright,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Size Pill
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = NeonPurple.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.4f))
            ) {
                Text(
                    text = info.sizeFormatted,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonPurple
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Changelog Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 180.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
            border = BorderStroke(1.dp, CyberBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WHAT'S NEW",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyanAccent,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val changelogText = if (info.body.isNotBlank()) info.body else "• Upgraded high-speed multi-threaded download engine\n• 4K Video and 320kbps Audio resolution stability\n• Fixed Telegram bot integration & background delivery\n• Native Android in-app auto-update system"

                Text(
                    text = changelogText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    ),
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onLater,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberBorder),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("update_later_button")
            ) {
                Text(
                    text = "Later",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = onUpdateNow,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanBright,
                    contentColor = CyberBlack
                ),
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp)
                    .testTag("update_now_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Update Now",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

/**
 * Live download progress section
 */
@Composable
private fun DownloadingProgressSection(
    state: UpdateState.Downloading,
    onCancel: () -> Unit
) {
    val downloadedMb = String.format(java.util.Locale.US, "%.1f", state.downloadedBytes.toDouble() / (1024 * 1024))
    val totalMb = String.format(java.util.Locale.US, "%.1f", state.totalBytes.toDouble() / (1024 * 1024))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Percentage Display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "DOWNLOADING UPDATE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = CyanBright
                )
                Text(
                    text = "$downloadedMb MB / $totalMb MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Text(
                text = "${state.progressPercent}%",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                ),
                color = CyanBright
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress Bar
        LinearProgressIndicator(
            progress = { (state.progressPercent / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .testTag("update_progress_bar"),
            color = CyanBright,
            trackColor = CyberCardSurface,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Speed & Info Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = NeonPurple,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = state.speedText,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonPurple
                )
            }

            Text(
                text = "Direct In-App Delivery",
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onCancel,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, RoseError.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
        ) {
            Text(
                text = "Cancel Download",
                color = RoseError,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Section displayed once APK download completes
 */
@Composable
private fun DownloadCompletedSection(
    state: UpdateState.DownloadCompleted,
    onInstall: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(EmeraldSuccess.copy(alpha = 0.15f))
                .border(1.5.dp, EmeraldSuccess, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = EmeraldSuccess,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Package Downloaded!",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "The official installer has been launched automatically. Tap below if you need to re-open the installation prompt.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onInstall,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldSuccess,
                contentColor = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("update_install_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.InstallMobile,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Install Update Now",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

/**
 * Section displayed when the app is already on the latest version
 */
@Composable
private fun UpToDateSection(
    info: AppReleaseInfo,
    onReinstall: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = EmeraldSuccess.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "You're on the latest version!",
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Installed Version: v${info.currentVersion}\nGitHub Latest Tag: ${info.tagName}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onReinstall,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CyberBorder),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Re-Download", fontSize = 11.5.sp, color = TextSecondary)
                }
            }

            Button(
                onClick = onClose,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = CyberBlack),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Text("Got It", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Error Section with retry button
 */
@Composable
private fun ErrorSection(
    state: UpdateState.Error,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = RoseError.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, RoseError.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = RoseError, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Update Check Notice",
                        fontWeight = FontWeight.Bold,
                        color = RoseError,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CyberBorder),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Text("Dismiss", color = TextSecondary, fontSize = 12.sp)
            }

            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = CyberBlack),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
