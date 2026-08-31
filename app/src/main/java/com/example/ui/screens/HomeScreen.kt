package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
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
    var pendingDownloadAudioOnly by remember { mutableStateOf<Boolean?>(null) }

    val detectedPlatform = remember(urlInput) { PlatformDetector.detect(urlInput) }
    val isTeraBoxUrl = remember(urlInput) {
        val lower = urlInput.lowercase()
        "terabox" in lower || "1024tera" in lower || "terasharelink" in lower
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
            // Futuristic Command Unit Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(CyanBright)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OMNISTREAM",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.5.sp,
                                    color = TextPrimary
                                )
                            )
                        }
                        Text(
                            text = "Any Video Downloader • TeraBox & 8K Core",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyanAccent,
                            fontSize = 11.sp
                        )
                    }

                    // Live Engine Status Chip
                    val isOnline = apiHealth?.status == "connected"
                    Surface(
                        onClick = onNavigateToSettings,
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOnline) EmeraldSuccess.copy(alpha = 0.15f) else CyanBright.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (isOnline) EmeraldSuccess else CyanBright),
                        modifier = Modifier.testTag("api_status_indicator")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) EmeraldSuccess else CyanBright)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOnline) "ENGINE LIVE" else "CORE ACTIVE",
                                color = if (isOnline) EmeraldSuccess else CyanBright,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Link Input Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MEDIA STREAM URL",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            ),
                            color = TextSecondary
                        )

                        // Platform chip
                        if (urlInput.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = detectedPlatform.brandColor.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, detectedPlatform.brandColor.copy(alpha = 0.7f))
                            ) {
                                Text(
                                    text = detectedPlatform.badgeText,
                                    color = detectedPlatform.brandColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { viewModel.setUrlInput(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("url_input_field"),
                        placeholder = {
                            Text(
                                "Paste TeraBox, YouTube, TikTok, FB, Instagram, X, or any website link...",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = CyanAccent)
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (urlInput.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.setUrlInput("") },
                                        modifier = Modifier.size(32.dp).testTag("clear_url_button")
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.pasteFromClipboard() },
                                    modifier = Modifier.size(32.dp).testTag("paste_clipboard_button")
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = CyanBright)
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
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            focusManager.clearFocus()
                            viewModel.fetchInfo()
                        })
                    )

                    // TeraBox Accelerator Banner (shows when TeraBox link is pasted/detected)
                    AnimatedVisibility(visible = isTeraBoxUrl) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "TERABOX CLOUD DETECTED",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF38BDF8),
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Direct share bypass enabled for fast Full HD cloud streaming",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fetch Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.fetchInfo()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("fetch_formats_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanBright,
                            contentColor = Color.Black
                        ),
                        enabled = !isFetching
                    ) {
                        if (isFetching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing Available Streams...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyze & Verify Source Qualities", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick Platform Triggers:",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )

                        Surface(
                            onClick = { showSupportedPlatformsDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            color = CyanBright.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, CyanBright.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = CyanBright,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "1000+ Sites Directory",
                                    color = CyanBright,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DemoChip("📦 TeraBox Cloud") {
                            viewModel.loadSampleUrl("https://terabox.com/s/1aB2c3d4e5fG6h7i8j")
                        }
                        DemoChip("📌 Pinterest 1080p") {
                            viewModel.loadSampleUrl("https://www.pinterest.com/pin/123456789012345678/")
                        }
                        DemoChip("🎬 YouTube 8K/4K") {
                            viewModel.loadSampleUrl("https://www.youtube.com/watch?v=LXb3EKWsInQ")
                        }
                        DemoChip("📱 TikTok No-WM") {
                            viewModel.loadSampleUrl("https://www.tiktok.com/@creator/video/72891234")
                        }
                        DemoChip("📸 Instagram Reels") {
                            viewModel.loadSampleUrl("https://www.instagram.com/reel/C8qW1234567/")
                        }
                        DemoChip("🌐 Facebook Video HD") {
                            viewModel.loadSampleUrl("https://www.facebook.com/watch/?v=10928374")
                        }
                        DemoChip("📁 Google Drive Stream") {
                            viewModel.loadSampleUrl("https://drive.google.com/file/d/1a2b3c4d5e6f7g8h9/view")
                        }
                        DemoChip("👻 Snapchat Spotlight") {
                            viewModel.loadSampleUrl("https://www.snapchat.com/spotlight/W7_ED1nYR9")
                        }
                        DemoChip("🎮 Twitch 60FPS Clip") {
                            viewModel.loadSampleUrl("https://clips.twitch.tv/GloriousSampleClip")
                        }
                        DemoChip("⚡ Kick VOD/Clip") {
                            viewModel.loadSampleUrl("https://kick.com/creator/clips/clip_123456")
                        }
                        DemoChip("💡 TED Talk Master") {
                            viewModel.loadSampleUrl("https://www.ted.com/talks/sample_talk_future")
                        }
                        DemoChip("📰 BBC Video News") {
                            viewModel.loadSampleUrl("https://www.bbc.com/news/videos/c12345678")
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
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!info.thumbnail.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(info.thumbnail)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = info.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = CyanBright.copy(alpha = 0.8f),
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${info.extractor ?: "Media"} Stream Preview",
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                            // Duration badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.85f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = info.displayDuration,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Platform pill
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberBlack.copy(alpha = 0.85f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
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
                            text = "${info.author} • ${info.formats.size} raw stream tracks",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

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
                                        Icon(Icons.Default.HighQuality, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Quality Matrix", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Raw Streams", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            )
                            Tab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Options", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "SAVE DIRECTORY: Internal Storage > Download > OmniStream",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Media automatically indexes to Phone Gallery, VLC, MX Player & Music Players",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Big Start Download CTA Button
            item {
                Spacer(modifier = Modifier.height(2.dp))
                val format = selectedFormat
                val badge = format?.displayQualityBadge ?: "Selected Quality"
                val ext = format?.ext?.uppercase() ?: "MP4"
                val size = format?.readableSize ?: ""

                Button(
                    onClick = {
                        val isAudio = format?.isAudioOnly == true
                        if (!StoragePermissionHelper.hasStoragePermission(context)) {
                            pendingDownloadAudioOnly = isAudio
                            showPermissionDialog = true
                        } else {
                            viewModel.startDownload(audioOnlyOverride = isAudio)
                            onNavigateToDownloads()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_download_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (format?.isAudioOnly == true) NeonPurple else CyanBright,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "DOWNLOAD [$badge • $ext${if (size.isNotEmpty() && size != "Dynamic / Stream") " • $size" else ""}]",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
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
