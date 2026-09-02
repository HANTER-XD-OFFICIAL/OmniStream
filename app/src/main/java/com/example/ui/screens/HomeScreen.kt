package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.api.FormatInfo
import com.example.ui.DownloadViewModel
import com.example.ui.components.PlatformDetector
import com.example.ui.components.QualityMatrixView
import com.example.ui.components.QualityTier
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanBright
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

import com.example.ui.components.StoragePermissionDialog
import com.example.ui.components.StoragePermissionHelper

@Composable
fun HomeScreen(
    viewModel: DownloadViewModel,
    onNavigateToDownloads: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val urlInput by viewModel.urlInput.collectAsStateWithLifecycle()
    val isFetching by viewModel.isFetching.collectAsStateWithLifecycle()
    val fetchError by viewModel.fetchError.collectAsStateWithLifecycle()
    val videoInfo by viewModel.videoInfo.collectAsStateWithLifecycle()
    val selectedFormat by viewModel.selectedFormat.collectAsStateWithLifecycle()
    val apiHealth by viewModel.apiHealth.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var lockedTierInfo by remember { mutableStateOf<QualityTier?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showSupportedPlatformsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showSupportHubModal by remember { mutableStateOf(false) }
    var pendingDownloadAudioOnly by remember { mutableStateOf<Boolean?>(null) }

    val detectedPlatform = remember(urlInput) { PlatformDetector.detect(urlInput) }
    val isTeraBoxUrl = remember(urlInput) {
        val lower = urlInput.lowercase()
        "terabox" in lower || "1024tera" in lower || "terasharelink" in lower
    }

    if (showSupportHubModal) {
        com.example.ui.components.DeveloperSupportHubDialog(
            onDismiss = { showSupportHubModal = false }
        )
    }

    if (showPrivacyDialog) {
        com.example.ui.components.UserPrivacyHubDialog(
            onDismiss = { showPrivacyDialog = false }
        )
    }

    if (showSupportedPlatformsDialog) {
        com.example.ui.components.SupportedPlatformsDialog(
            onDismiss = { showSupportedPlatformsDialog = false },
            onSelectSample = { sampleUrl ->
                viewModel.loadSampleUrl(sampleUrl)
            }
        )
    }

    if (showPermissionDialog) {
        StoragePermissionDialog(
            onDismiss = {
                showPermissionDialog = false
                pendingDownloadAudioOnly = null
            },
            onPermissionGranted = {
                showPermissionDialog = false
                val audioOnly = pendingDownloadAudioOnly ?: (selectedFormat?.isAudioOnly == true)
                viewModel.startDownload(audioOnlyOverride = audioOnly)
                pendingDownloadAudioOnly = null
                onNavigateToDownloads()
            }
        )
    }

    // Locked tier alert dialog explaining why a quality cannot be downloaded for this video
    lockedTierInfo?.let { tier ->
        AlertDialog(
            onDismissRequest = { lockedTierInfo = null },
            containerColor = CyberDarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${tier.label} Unavailable",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This media was not uploaded or encoded in ${tier.label} (${tier.technicalSpec}) by the content creator.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "OmniStream only enables genuine, authentic streams available directly from the source to guarantee native quality without artificial upscaling.",
                            color = CyanAccent,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { lockedTierInfo = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Color.Black)
                ) {
                    Text("Understood", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("home_screen_list"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Ultra-Sleek Futuristic Command Unit Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_command_header"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(CyanBright.copy(alpha = 0.7f), NeonPurple.copy(alpha = 0.5f))))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Brand Unit
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(CyanBright.copy(alpha = 0.25f), NeonPurple.copy(alpha = 0.25f))))
                                .border(1.dp, CyanBright.copy(alpha = 0.8f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "OmniStream Logo",
                                tint = CyanBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "OMNISTREAM",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.2.sp,
                                    fontSize = 16.sp
                                ),
                                color = TextPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "Universal 8K & MP3 Downloader",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = CyanAccent,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right Status Badges: Privacy & Live Engine
                    val isOnline = apiHealth?.status == "connected"
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 100% Privacy Shield Badge
                        Surface(
                            onClick = { showPrivacyDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldSuccess.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.65f)),
                            modifier = Modifier.testTag("privacy_status_indicator")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Privacy Shield",
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "PRIVACY",
                                    color = EmeraldSuccess,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // Engine Status Badge
                        Surface(
                            onClick = onNavigateToSettings,
                            shape = RoundedCornerShape(8.dp),
                            color = if (isOnline) EmeraldSuccess.copy(alpha = 0.12f) else CyanBright.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, if (isOnline) EmeraldSuccess.copy(alpha = 0.7f) else CyanBright.copy(alpha = 0.7f)),
                            modifier = Modifier.testTag("api_status_indicator")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isOnline) EmeraldSuccess else CyanBright)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOnline) "LIVE" else "CORE",
                                    color = if (isOnline) EmeraldSuccess else CyanBright,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }

        // High-Quality Link Input Studio Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_url_input_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                border = BorderStroke(1.2.dp, Brush.verticalGradient(listOf(CyberBorder, CyanBright.copy(alpha = 0.35f))))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Bar of Input Box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MEDIA STREAM URL",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp,
                                    fontSize = 11.sp
                                ),
                                color = TextPrimary
                            )
                        }

                        // Live Platform Detection Badge
                        if (urlInput.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = detectedPlatform.brandColor.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, detectedPlatform.brandColor.copy(alpha = 0.8f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(detectedPlatform.brandColor)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = detectedPlatform.badgeText,
                                        color = detectedPlatform.brandColor,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Next-Gen High-Tech Input Box
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { viewModel.setUrlInput(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("url_input_field"),
                        placeholder = {
                            Text(
                                "Paste video or audio link here...",
                                color = TextMuted,
                                fontSize = 12.5.sp
                            )
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyanBright.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = "Link Icon",
                                    tint = CyanBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        trailingIcon = {
                            Row(
                                modifier = Modifier.padding(end = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (urlInput.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.setUrlInput("") },
                                        modifier = Modifier.size(30.dp).testTag("clear_url_button")
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Quick High-Tech PASTE Button
                                Surface(
                                    onClick = { viewModel.pasteFromClipboard() },
                                    shape = RoundedCornerShape(8.dp),
                                    color = CyanBright.copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, CyanBright.copy(alpha = 0.7f)),
                                    modifier = Modifier.testTag("paste_clipboard_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.ContentPaste,
                                            contentDescription = "Paste",
                                            tint = CyanBright,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "PASTE",
                                            color = CyanBright,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CyberBlack.copy(alpha = 0.7f),
                            unfocusedContainerColor = CyberBlack.copy(alpha = 0.5f),
                            focusedBorderColor = CyanBright,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = CyanBright
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            focusManager.clearFocus()
                            viewModel.fetchInfo()
                        })
                    )

                    // Platform Detection Info Card
                    AnimatedVisibility(visible = urlInput.isNotBlank()) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = detectedPlatform.brandColor.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, detectedPlatform.brandColor.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(detectedPlatform.brandColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${detectedPlatform.name} Stream Detected • Ready for lossless download",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // High-Impact Gradient Analyze Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.fetchInfo()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("fetch_formats_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(),
                        enabled = !isFetching
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(CyanBright, Color(0xFF06B6D4), NeonPurple)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isFetching) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.Black,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Analyzing Media Streams...",
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.5.sp,
                                        color = Color.Black
                                    )
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "ANALYZE & EXTRACT QUALITIES",
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.5.sp,
                                        letterSpacing = 0.5.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Platform Triggers Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUICK PLATFORMS (21 SERVICES)",
                            fontSize = 10.5.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )

                        Surface(
                            onClick = { showSupportedPlatformsDialog = true },
                            shape = RoundedCornerShape(6.dp),
                            color = CyanBright.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, CyanBright.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "21 PORTALS",
                                    color = CyanBright,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = CyanBright,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal Quick Platform Triggers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DemoChip("🎬 YouTube") {
                            viewModel.loadSampleUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                        }
                        DemoChip("📱 TikTok") {
                            viewModel.loadSampleUrl("https://www.tiktok.com/@creator/video/1234567890")
                        }
                        DemoChip("📸 Instagram") {
                            viewModel.loadSampleUrl("https://www.instagram.com/reel/Cx123456789/")
                        }
                        DemoChip("🌐 Facebook") {
                            viewModel.loadSampleUrl("https://www.facebook.com/watch/?v=1234567890")
                        }
                        DemoChip("🐦 Twitter / X") {
                            viewModel.loadSampleUrl("https://twitter.com/user/status/1234567890")
                        }
                        DemoChip("📌 Pinterest") {
                            viewModel.loadSampleUrl("https://www.pinterest.com/pin/123456789012345678/")
                        }
                        DemoChip("🤖 Reddit") {
                            viewModel.loadSampleUrl("https://www.reddit.com/r/videos/comments/sample123/")
                        }
                        DemoChip("🎧 SoundCloud") {
                            viewModel.loadSampleUrl("https://soundcloud.com/artist/sample-track")
                        }
                        DemoChip("📺 Bilibili") {
                            viewModel.loadSampleUrl("https://www.bilibili.com/video/BV1xx411c7mD")
                        }
                        DemoChip("👻 Snapchat") {
                            viewModel.loadSampleUrl("https://www.snapchat.com/spotlight/W7_ED1nYR9")
                        }
                        DemoChip("🎥 Dailymotion") {
                            viewModel.loadSampleUrl("https://www.dailymotion.com/video/x8abcdef")
                        }
                        DemoChip("🎬 Vimeo") {
                            viewModel.loadSampleUrl("https://vimeo.com/76979871")
                        }
                        DemoChip("🦋 Bluesky") {
                            viewModel.loadSampleUrl("https://bsky.app/profile/user.bsky.social/post/123456")
                        }
                        DemoChip("📹 Loom") {
                            viewModel.loadSampleUrl("https://www.loom.com/share/abc123def456")
                        }
                        DemoChip("🟠 OK.ru") {
                            viewModel.loadSampleUrl("https://ok.ru/video/1234567890")
                        }
                        DemoChip("🇷🇺 Rutube") {
                            viewModel.loadSampleUrl("https://rutube.ru/video/1234567890abcdef/")
                        }
                        DemoChip("⚡ Streamable") {
                            viewModel.loadSampleUrl("https://streamable.com/moo7b")
                        }
                        DemoChip("📝 Tumblr") {
                            viewModel.loadSampleUrl("https://creator.tumblr.com/post/1234567890")
                        }
                        DemoChip("🎮 Twitch Clips") {
                            viewModel.loadSampleUrl("https://clips.twitch.tv/GloriousSampleClip")
                        }
                        DemoChip("💙 VK") {
                            viewModel.loadSampleUrl("https://vk.com/video-123456_789012")
                        }
                        DemoChip("👾 Newgrounds") {
                            viewModel.loadSampleUrl("https://www.newgrounds.com/portal/view/123456")
                        }
                    }
                }
            }
        }

        // Error message if any
        if (fetchError != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4C0519)),
                    border = BorderStroke(1.dp, Color(0xFFBE123C))
                ) {
                    Text(
                        text = fetchError ?: "",
                        color = Color(0xFFFECDD3),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Extracted Media Info Card & Format Controller
        val info = videoInfo
        if (info != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
                    border = BorderStroke(1.dp, CyanBright.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Thumbnail & Meta
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF070D1E))
                                .clickable {
                                    val previewUrl = selectedFormat?.url ?: info.formats.firstOrNull { !it.url.isNullOrBlank() }?.url ?: info.webpageUrl
                                    if (!previewUrl.isNullOrBlank()) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(previewUrl))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!info.thumbnail.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(info.thumbnail)
                                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                                        .addHeader("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8")
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = info.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Dark gradient scrim for legibility
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                            )
                                        )
                                )

                                // Center Play Action Button
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .border(1.5.dp, CyanBright, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Video Preview",
                                        tint = CyanBright,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(CyanBright.copy(alpha = 0.15f))
                                            .border(1.dp, CyanBright.copy(alpha = 0.5f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = CyanBright,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${info.extractor ?: "Media"} Stream Verified",
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                            // Duration badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.85f))
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = info.displayDuration,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Platform pill
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(10.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberBlack.copy(alpha = 0.85f))
                                    .border(1.dp, CyanBright.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = info.extractor ?: "Media Stream",
                                    color = CyanBright,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = info.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${info.author} • ${info.formats.size} stream tracks",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Category Tabs: Dynamic Quality Matrix vs Raw Streams vs Controller
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = CyberDarkSurface,
                            contentColor = CyanBright,
                            modifier = Modifier.clip(RoundedCornerShape(10.dp)),
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = CyanBright
                                )
                            }
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.HighQuality, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Quality Matrix", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                    }
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Raw Streams", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                    }
                                }
                            )
                            Tab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Options", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Tab 0: DYNAMIC QUALITY AVAILABILITY MATRIX
            // Shows all standard qualities, enabling only those present in the video source!
            if (selectedTab == 0) {
                item {
                    QualityMatrixView(
                        allFormats = info.formats,
                        selectedFormat = selectedFormat,
                        onSelectFormat = { fmt -> viewModel.selectFormat(fmt) },
                        onLockedTierTapped = { tier -> lockedTierInfo = tier }
                    )
                }
            }

            // Tab 1: Detailed Raw Streams List (yt-dlp format manifest)
            if (selectedTab == 1) {
                item {
                    Text(
                        text = "ALL EXTRACTED RAW STREAMS (${info.formats.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                items(info.formats) { format ->
                    val isSelected = selectedFormat?.formatId == format.formatId
                    FormatItemCard(
                        format = format,
                        isSelected = isSelected,
                        onSelect = { viewModel.selectFormat(format) },
                        isAudioMode = format.isAudioOnly
                    )
                }
            }

            // Tab 2: Advanced Controller Settings
            if (selectedTab == 2) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                        border = BorderStroke(1.dp, CyberBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "yt-dlp Engine Controller",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Embed Subtitles (CC)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text("Extracts multitrack closed captions", color = TextMuted, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = true,
                                    onCheckedChange = {},
                                    colors = SwitchDefaults.colors(checkedThumbColor = CyanBright)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Embed High-Res Cover Art", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text("Saves thumbnail directly inside media file", color = TextMuted, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = true,
                                    onCheckedChange = {},
                                    colors = SwitchDefaults.colors(checkedThumbColor = CyanBright)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("TeraBox Direct Token Resolver", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text("Bypasses speed throttle on TeraBox links", color = TextMuted, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = true,
                                    onCheckedChange = {},
                                    colors = SwitchDefaults.colors(checkedThumbColor = CyanBright)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "CLI: --embed-metadata --embed-thumbnail --no-mtime --merge-output-format mp4",
                                    color = CyanAccent,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // Storage Location Indicator
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = CyberDarkSurface,
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SAVE DIRECTORY: Internal Storage > Download > OmniStream",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Media automatically indexes to Phone Gallery, VLC & Music Players",
                                fontSize = 10.5.sp,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Dual Download CTA Buttons: Video & Audio
            item {
                Spacer(modifier = Modifier.height(4.dp))
                val format = selectedFormat
                val isAudioSelected = format?.isAudioOnly == true
                val badge = format?.displayQualityBadge ?: "Selected Quality"
                val ext = format?.ext?.uppercase() ?: (if (isAudioSelected) "M4A" else "MP4")
                val size = format?.readableSize ?: ""

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Primary Video Download Button
                    Button(
                        onClick = {
                            if (!StoragePermissionHelper.hasStoragePermission(context)) {
                                pendingDownloadAudioOnly = isAudioSelected
                                showPermissionDialog = true
                            } else {
                                viewModel.startDownload(audioOnlyOverride = isAudioSelected)
                                onNavigateToDownloads()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .testTag("start_download_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAudioSelected) NeonPurple else CyanBright,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isAudioSelected) Icons.Default.Audiotrack else Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isAudioSelected) "DOWNLOAD AUDIO STREAM" else "DOWNLOAD VIDEO STREAM",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.5.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Text(
                                    text = "$badge • $ext${if (size.isNotEmpty() && size != "Dynamic / Stream") " • $size" else ""}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = Color.Black.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    // Direct MP3 320k Audio Extract Button (Always Active for Any Video)
                    OutlinedButton(
                        onClick = {
                            if (!StoragePermissionHelper.hasStoragePermission(context)) {
                                pendingDownloadAudioOnly = true
                                showPermissionDialog = true
                            } else {
                                viewModel.startDownload(customExt = "mp3", audioOnlyOverride = true)
                                onNavigateToDownloads()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag("download_audio_mp3_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, NeonPurple),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = NeonPurple.copy(alpha = 0.12f),
                            contentColor = NeonPurple
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = NeonPurple
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "EXTRACT MP3 MASTER AUDIO (320 KBPS)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 0.3.sp
                                    ),
                                    color = NeonPurple
                                )
                                Text(
                                    text = "High Fidelity Studio Sound • Universal Compatibility",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(36.dp))
            }
        } else if (!isFetching && fetchError == null) {
            // High-Tech Cyber Hub Dashboard (Displayed on app launch when no URL is analyzed yet)
            item {
                HomePlatformsShowcaseCard(
                    onSelectPlatformSample = { sampleUrl ->
                        viewModel.loadSampleUrl(sampleUrl)
                    },
                    onViewAllPlatforms = {
                        showSupportedPlatformsDialog = true
                    }
                )
            }

            item {
                HomeEngineCapabilitiesCard(
                    onOpenPrivacy = { showPrivacyDialog = true }
                )
            }

            item {
                HomeHowItWorksCard()
            }

            item {
                HomeDeveloperSupportBanner(
                    onOpenSupportHub = { showSupportHubModal = true }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun FormatItemCard(
    format: FormatInfo,
    isSelected: Boolean,
    onSelect: () -> Unit,
    isAudioMode: Boolean = false
) {
    val h = format.height ?: 0
    val fps = format.fps ?: 30

    val badgeColor = when {
        h >= 4320 -> Color(0xFFF59E0B) // 8K Gold
        h >= 2160 -> CyanBright       // 4K Cyan
        fps >= 120 -> NeonPurple      // 120 FPS Neon
        fps >= 60 -> Color(0xFF38BDF8) // 60 FPS Blue
        isAudioMode -> Color(0xFFA855F7)
        else -> Color(0xFF94A3B8)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect)
            .testTag("format_card_${format.formatId}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CyanBright.copy(alpha = 0.12f) else CyberDarkSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) CyanBright else CyberBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Resolution / FPS badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.7f))
                ) {
                    Text(
                        text = format.displayQualityBadge,
                        color = badgeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = format.formatNote ?: format.resolution ?: "Standard Stream",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${format.ext.uppercase()} • ${if (format.fps != null && format.fps > 30) "${format.fps} FPS • " else ""}${format.readableSize}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Selection indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = CyanBright,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .border(1.dp, TextMuted, CircleShape)
                )
            }
        }
    }
}

@Composable
fun DemoChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = CyberDarkSurface,
        border = BorderStroke(1.dp, CyberBorder),
        modifier = Modifier.testTag("demo_chip_$label")
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = CyanAccent,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

/**
 * 1. Interactive Supported Platforms Showcase Hub Card
 */
@Composable
private fun HomePlatformsShowcaseCard(
    onSelectPlatformSample: (String) -> Unit,
    onViewAllPlatforms: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_platforms_showcase_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = BorderStroke(1.dp, CyberBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyanBright.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = CyanBright, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "SUPPORTED SERVICES",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "21 Official Portals • 1-Tap Instant Load",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    onClick = onViewAllPlatforms,
                    shape = RoundedCornerShape(8.dp),
                    color = CyanBright.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, CyanBright.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ALL (21)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyanBright
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = CyanBright,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Platform Buttons Grid
            val platforms = listOf(
                PlatformQuickItem("YouTube", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", Color(0xFFFF0000), "8K / 4K / MP3"),
                PlatformQuickItem("TikTok", "https://www.tiktok.com/@creator/video/1234567890", Color(0xFF00F2FE), "No Watermark"),
                PlatformQuickItem("Facebook", "https://www.facebook.com/watch/?v=1234567890", Color(0xFF1877F2), "Public HD & Reels"),
                PlatformQuickItem("Instagram", "https://www.instagram.com/reel/Cx123456789/", Color(0xFFE1306C), "Reels & Stories"),
                PlatformQuickItem("Twitter / X", "https://twitter.com/user/status/1234567890", Color(0xFF1DA1F2), "High-Speed Media"),
                PlatformQuickItem("Pinterest", "https://www.pinterest.com/pin/123456789012345678/", Color(0xFFE60023), "1080p MP4 Pins"),
                PlatformQuickItem("Reddit", "https://www.reddit.com/r/videos/comments/sample123/", Color(0xFFFF4500), "Audio+Video Mux"),
                PlatformQuickItem("SoundCloud", "https://soundcloud.com/artist/sample-track", Color(0xFFFF7700), "320kbps MP3 Master"),
                PlatformQuickItem("Bilibili", "https://www.bilibili.com/video/BV1xx411c7mD", Color(0xFF00A1D6), "HD / 4K 60FPS"),
                PlatformQuickItem("Snapchat", "https://www.snapchat.com/spotlight/W7_ED1nYR9", Color(0xFFFFFC00), "Spotlight Video"),
                PlatformQuickItem("Dailymotion", "https://www.dailymotion.com/video/x8abcdef", Color(0xFF0066DC), "Full HD Streams"),
                PlatformQuickItem("Vimeo", "https://vimeo.com/76979871", Color(0xFF1AB7EA), "4K Master Video")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (row in platforms.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (item in row) {
                            Surface(
                                onClick = { onSelectPlatformSample(item.sampleUrl) },
                                shape = RoundedCornerShape(10.dp),
                                color = item.color.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, item.color.copy(alpha = 0.45f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(item.color)
                                    )
                                    Spacer(modifier = Modifier.width(7.dp))
                                    Column {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp
                                            ),
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = item.desc,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 9.5.sp
                                            ),
                                            color = item.color,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class PlatformQuickItem(
    val name: String,
    val sampleUrl: String,
    val color: Color,
    val desc: String
)

/**
 * 2. Next-Gen OmniStream Core Capabilities Grid
 */
@Composable
private fun HomeEngineCapabilitiesCard(
    onOpenPrivacy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_engine_capabilities_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = BorderStroke(1.dp, CyberBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "HARDWARE ENGINE SPECS",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "Native Lossless Pipeline • Android Gallery Integration",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Spec Rows
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EngineSpecRow(
                    icon = Icons.Default.HighQuality,
                    iconTint = CyanBright,
                    title = "Lossless 8K / 4K 60FPS Video",
                    desc = "Preserves native source bitstreams without recompression or downsampling."
                )
                EngineSpecRow(
                    icon = Icons.Default.Audiotrack,
                    iconTint = NeonPurple,
                    title = "Studio Master 320k Audio Extractor",
                    desc = "1-click direct MP3 (320 kbps) & FLAC ripping with embedded ID3 artwork."
                )
                EngineSpecRow(
                    icon = Icons.Default.Speed,
                    iconTint = Color(0xFF38BDF8),
                    title = "TeraBox Cloud Stream Bypass",
                    desc = "Resolves private & shared TeraBox links directly into fast download streams."
                )
                EngineSpecRow(
                    icon = Icons.Default.Shield,
                    iconTint = EmeraldSuccess,
                    title = "100% Zero-Log Privacy Storage",
                    desc = "All processing occurs on-device with zero tracking or remote telemetry logging.",
                    isClickable = true,
                    onClick = onOpenPrivacy
                )
            }
        }
    }
}

@Composable
private fun EngineSpecRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    desc: String,
    isClickable: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = { onClick?.invoke() },
        enabled = isClickable,
        shape = RoundedCornerShape(10.dp),
        color = CyberCardSurface,
        border = BorderStroke(0.8.dp, CyberBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp
                    ),
                    color = TextSecondary,
                    maxLines = 2
                )
            }

            if (isClickable) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

/**
 * 3. Simple 3-Step Guide
 */
@Composable
private fun HomeHowItWorksCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_how_it_works_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = BorderStroke(1.dp, CyberBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "HOW TO DOWNLOAD",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                ),
                color = TextPrimary
            )
            Text(
                text = "Three simple steps to save any media to your phone",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GuideStepTile(
                    stepNum = "1",
                    title = "Copy Link",
                    desc = "From YouTube, TikTok, TeraBox, FB, etc.",
                    accentColor = CyanBright,
                    modifier = Modifier.weight(1f)
                )
                GuideStepTile(
                    stepNum = "2",
                    title = "Analyze",
                    desc = "Select 8K, 4K, 1080p, or 320k MP3 audio.",
                    accentColor = NeonPurple,
                    modifier = Modifier.weight(1f)
                )
                GuideStepTile(
                    stepNum = "3",
                    title = "Save",
                    desc = "1-Tap Download into Android Gallery.",
                    accentColor = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun GuideStepTile(
    stepNum: String,
    title: String,
    desc: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = CyberCardSurface,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNum,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                ),
                color = TextPrimary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 9.5.sp
                ),
                color = TextSecondary,
                maxLines = 3
            )
        }
    }
}

/**
 * 4. Lead Developer & Support Banner
 */
@Composable
private fun HomeDeveloperSupportBanner(
    onOpenSupportHub: () -> Unit
) {
    val context = LocalContext.current
    val devFacebookUrl = "https://www.facebook.com/md.rasel.7.8.2.3.4"
    val devWhatsAppNumber = "+8801882278234"
    val devTelegramUrl = "https://t.me/HANTER_XD_OFFICIAL"

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun openWhatsApp(phone: String) {
        try {
            val cleanPhone = phone.replace("+", "").replace(" ", "").replace("-", "")
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://wa.me/$cleanPhone?text=${Uri.encode("Hello MD RASEL, I need support with OmniStream Downloader.")}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            openUrl("https://wa.me/8801882278234")
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_developer_support_banner"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(CyanBright.copy(alpha = 0.8f), NeonPurple.copy(alpha = 0.6f))))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CyanBright.copy(alpha = 0.15f))
                            .border(1.2.dp, CyanBright, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = CyanBright, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MD RASEL",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = null, tint = CyanBright, modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = "Lead Developer & System Architect",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CyanAccent,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }

                Surface(
                    onClick = onOpenSupportHub,
                    shape = RoundedCornerShape(8.dp),
                    color = NeonPurple.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.HeadsetMic, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SUPPORT HUB",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = NeonPurple
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1-Click Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // WhatsApp
                Button(
                    onClick = { openWhatsApp(devWhatsAppNumber) },
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF064E3B), contentColor = Color(0xFF34D399)),
                    border = BorderStroke(0.8.dp, Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                // Telegram
                Button(
                    onClick = { openUrl(devTelegramUrl) },
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C4A6E), contentColor = Color(0xFF38BDF8)),
                    border = BorderStroke(0.8.dp, Color(0xFF0284C7))
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Telegram", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                // Facebook
                Button(
                    onClick = { openUrl(devFacebookUrl) },
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A), contentColor = Color(0xFF60A5FA)),
                    border = BorderStroke(0.8.dp, Color(0xFF3B82F6))
                ) {
                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Facebook", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}
