package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.ripple

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.api.AppUpdateManager
import com.example.data.local.DownloadStatus
import com.example.ui.DownloadViewModel
import com.example.ui.components.AppLoadingScreen
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.DeveloperSupportHubDialog
import com.example.ui.components.MediaPreviewDialog
import com.example.ui.components.StoragePermissionDialog
import com.example.ui.components.StoragePermissionHelper
import com.example.ui.components.UserPrivacyHubDialog
import com.example.ui.components.WelcomeOnboardingDialog
import com.example.ui.screens.ApiSettingsScreen
import com.example.ui.screens.DeveloperScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyanBright
import com.example.ui.theme.LiquidGlassAtmosphere
import com.example.ui.theme.LiquidGlassBrushes
import com.example.ui.theme.LiquidGlassPillShape
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.liquidGlass
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.fillMaxWidth

class MainActivity : ComponentActivity() {

    private val viewModel: DownloadViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp(viewModel)
            }
        }
    }
}

@Composable
private fun LiquidGlassMorphingDock(
    currentTab: Int,
    activeCount: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember {
        listOf(
            Triple("Downloader", Icons.Default.Download, "nav_tab_downloader"),
            Triple("Downloads", Icons.Default.Folder, "nav_tab_downloads"),
            Triple("API & Engine", Icons.Default.Dns, "nav_tab_settings"),
            Triple("Developer", Icons.Default.Code, "nav_tab_developer")
        )
    }

    val animatedAccentColor by animateColorAsState(
        targetValue = if (currentTab == 3) NeonPurple else CyanBright,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f),
        label = "dock_accent_color"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Refined subtle rim elevation aura directly beneath the dock
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 12.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            animatedAccentColor.copy(alpha = 0.12f),
                            NeonPurple.copy(alpha = 0.08f),
                            Color(0xFFFB7185).copy(alpha = 0.06f),
                            animatedAccentColor.copy(alpha = 0.10f)
                        )
                    )
                )
        )

        // Floating Frosted Crystal Dock Container
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = Color.Transparent,
            border = BorderStroke(1.4.dp, LiquidGlassBrushes.PrismaticCausticBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .testTag("bottom_nav_bar")
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x40FFFFFF), // Specular light edge highlight
                            Color(0xF008101E), // Frosted obsidian deep glass
                            Color(0xFA040810)  // Solid true-black anchor
                        )
                    )
                )
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 5.dp, vertical = 5.dp)
            ) {
                val tabWidth = maxWidth / items.size
                val animatedPillOffset by animateDpAsState(
                    targetValue = tabWidth * currentTab,
                    animationSpec = spring(
                        dampingRatio = 0.74f, // Organic liquid spring bounce
                        stiffness = 380f
                    ),
                    label = "dock_sliding_pill_offset"
                )

                // 1. Fluid Sliding Liquid Glass Capsule Pill (Masterpiece Hero Effect)
                Box(
                    modifier = Modifier
                        .offset(x = animatedPillOffset)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp, vertical = 1.dp)
                        .drawBehind {
                            val r = size.maxDimension * 0.52f
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        animatedAccentColor.copy(alpha = 0.16f),
                                        NeonPurple.copy(alpha = 0.06f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.5f, size.height * 0.75f),
                                    radius = r
                                ),
                                radius = r,
                                center = Offset(size.width * 0.5f, size.height * 0.75f)
                            )
                        }
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x4DFFFFFF), // Top specular dome reflection
                                    animatedAccentColor.copy(alpha = 0.22f), // Translucent liquid tint
                                    Color(0x38121E38)  // Deep obsidian backing
                                )
                            )
                        )
                        .border(
                            width = 1.3.dp,
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color.White,
                                    animatedAccentColor,
                                    Color.White.copy(alpha = 0.65f)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                )

                // 2. Interactive Navigation Tabs Layer
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        val (title, icon, testTag) = item
                        val isSelected = currentTab == index
                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.14f else 0.96f,
                            animationSpec = spring(dampingRatio = 0.68f, stiffness = 420f),
                            label = "tab_icon_scale_$index"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(24.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(
                                        bounded = true,
                                        color = if (index == 3) NeonPurple else CyanBright
                                    )
                                ) {
                                    onTabSelected(index)
                                }
                                .testTag(testTag),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier.size(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = title,
                                        tint = if (isSelected) animatedAccentColor else Color(0xFFCBD5E1),
                                        modifier = Modifier
                                            .size(21.dp)
                                            .scale(iconScale)
                                    )

                                    if (index == 1 && activeCount > 0) {
                                        val badgeText = if (activeCount > 99) "99+" else "$activeCount"
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 7.dp, y = (-3).dp)
                                                .defaultMinSize(minWidth = 15.dp, minHeight = 15.dp)
                                                .height(15.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(
                                                            Color(0xFFA855F7),
                                                            Color(0xFFEC4899)
                                                        )
                                                    )
                                                )
                                                .border(1.4.dp, Color(0xFF060E1C), CircleShape)
                                                .padding(horizontal = if (badgeText.length > 1) 4.dp else 0.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = badgeText,
                                                style = TextStyle(
                                                    color = Color.White,
                                                    fontSize = 8.5.sp,
                                                    lineHeight = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                ),
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = title,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // Micro glowing liquid droplet indicator for active tab
                                Spacer(modifier = Modifier.height(2.dp))
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp, 2.5.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(
                                                        animatedAccentColor.copy(alpha = 0.6f),
                                                        Color.White,
                                                        animatedAccentColor.copy(alpha = 0.6f)
                                                    )
                                                )
                                            )
                                    )
                                } else {
                                    Box(modifier = Modifier.size(10.dp, 2.5.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MainApp(viewModel: DownloadViewModel) {
    val context = LocalContext.current
    var isAppLoading by remember { mutableStateOf(true) }
    var currentTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val previewItem by viewModel.previewItem.collectAsStateWithLifecycle()
    val allDownloads by viewModel.filteredDownloads.collectAsStateWithLifecycle()

    val appUpdateManager = remember { AppUpdateManager.getInstance(context) }
    val updateState by appUpdateManager.updateState.collectAsStateWithLifecycle()
    val showUpdateDialog by appUpdateManager.showUpdateDialog.collectAsStateWithLifecycle()

    var showPermissionDialog by remember {
        mutableStateOf(false)
    }

    var showWelcomeDialog by remember { mutableStateOf(false) }
    var showSupportHubModal by remember { mutableStateOf(false) }

    // When loading screen completes, check permissions, welcome state, and GitHub releases
    fun onBootFinished() {
        isAppLoading = false
        if (!StoragePermissionHelper.hasStoragePermission(context)) {
            showPermissionDialog = true
        }

        val prefs = context.getSharedPreferences("omnistream_prefs", android.content.Context.MODE_PRIVATE)
        val hasSeenWelcome = prefs.getBoolean("has_seen_welcome_v2", false)
        if (!hasSeenWelcome) {
            showWelcomeDialog = true
            prefs.edit().putBoolean("has_seen_welcome_v2", true).apply()
        }

        // Automatic In-App Update check against GitHub Releases on app launch
        appUpdateManager.checkForUpdates(isManual = false)
    }

    if (isAppLoading) {
        AppLoadingScreen(
            onLoadingComplete = { onBootFinished() }
        )
        return
    }

    if (showPermissionDialog) {
        StoragePermissionDialog(
            onDismiss = { showPermissionDialog = false },
            onPermissionGranted = {
                showPermissionDialog = false
            }
        )
    }

    if (showWelcomeDialog) {
        WelcomeOnboardingDialog(
            onDismiss = { showWelcomeDialog = false },
            onOpenSupportHub = {
                showWelcomeDialog = false
                currentTab = 3
            }
        )
    }

    if (showSupportHubModal) {
        DeveloperSupportHubDialog(
            onDismiss = { showSupportHubModal = false }
        )
    }

    if (showUpdateDialog) {
        AppUpdateDialog(
            updateState = updateState,
            onStartDownload = { info -> appUpdateManager.startDownload(info) },
            onCancelDownload = { appUpdateManager.cancelDownload() },
            onInstallApk = { file -> appUpdateManager.installApk(context, file) },
            onDismiss = { rememberDismissal -> appUpdateManager.dismissDialog(rememberDismissal) }
        )
    }

    val activeCount = remember(allDownloads) {
        allDownloads.count { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.QUEUED }
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // Media preview dialog for completed audio & video
    previewItem?.let { item ->
        MediaPreviewDialog(
            item = item,
            onDismiss = { viewModel.setPreviewItem(null) }
        )
    }

    LiquidGlassAtmosphere {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(snackbarHostState) { snackbarData ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xF00A1324),
                        border = BorderStroke(1.2.dp, LiquidGlassBrushes.PrismaticCausticBorder),
                        modifier = Modifier
                            .padding(14.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        Text(
                            text = snackbarData.visuals.message,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            },
            bottomBar = {
                LiquidGlassMorphingDock(
                    currentTab = currentTab,
                    activeCount = activeCount,
                    onTabSelected = { currentTab = it }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                    when (tab) {
                        0 -> HomeScreen(
                            viewModel = viewModel,
                            onNavigateToDownloads = { currentTab = 1 },
                            onNavigateToSettings = { currentTab = 2 }
                        )
                        1 -> DownloadsScreen(
                            viewModel = viewModel,
                            onNavigateToHome = { currentTab = 0 }
                        )
                        2 -> ApiSettingsScreen(
                            viewModel = viewModel,
                            onCheckForUpdates = { appUpdateManager.checkForUpdates(isManual = true) }
                        )
                        3 -> DeveloperScreen(
                            onNavigateToHome = { currentTab = 0 },
                            onCheckForUpdates = { appUpdateManager.checkForUpdates(isManual = true) }
                        )
                    }
                }
            }
        }
    }
}
