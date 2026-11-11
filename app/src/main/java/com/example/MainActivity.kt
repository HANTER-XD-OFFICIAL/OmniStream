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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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

private fun LiquidGlassNavButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    badgeCount: Int = 0,
    accentColor: Color = CyanBright,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.94f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "nav_btn_scale"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = modifier
            .testTag(testTag)
            .scale(scale)
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .then(
                    if (isSelected) {
                        Modifier
                            .drawBehind {
                                val r = size.maxDimension * 0.50f
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            accentColor.copy(alpha = 0.12f),
                                            NeonPurple.copy(alpha = 0.05f),
                                            Color.Transparent
                                        ),
                                        center = Offset(size.width * 0.5f, size.height * 0.7f),
                                        radius = r
                                    ),
                                    radius = r,
                                    center = Offset(size.width * 0.5f, size.height * 0.7f)
                                )
                            }
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x4DFFFFFF), // Crisp specular dome reflection
                                        accentColor.copy(alpha = 0.22f),
                                        Color(0x38121E38)
                                    )
                                )
                            )
                            .border(
                                width = 1.2.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color.White,
                                        accentColor,
                                        Color.White.copy(alpha = 0.60f)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                    } else {
                        Modifier.background(Color.Transparent)
                    }
                )
                .padding(vertical = 7.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isSelected) accentColor else Color(0xFFE2E8F0),
                        modifier = Modifier.size(22.dp)
                    )

                    if (badgeCount > 0) {
                        Box(
                            modifier = Modifier
                                .padding(start = 13.dp, bottom = 8.dp)
                                .clip(CircleShape)
                                .background(CyanBright)
                                .padding(horizontal = 4.5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "$badgeCount",
                                color = Color.Black,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = title,
                    fontSize = 9.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) Color.White else Color(0xFFE2E8F0),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )

                if (isSelected) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp, 2.5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        accentColor.copy(alpha = 0.5f),
                                        accentColor,
                                        accentColor.copy(alpha = 0.5f)
                                    )
                                )
                            )
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.5.dp))
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    // Refined subtle rim elevation aura underneath floating dock (significantly reduced ambient glow)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        CyanBright.copy(alpha = 0.10f),
                                        NeonPurple.copy(alpha = 0.08f),
                                        Color(0xFFFB7185).copy(alpha = 0.06f),
                                        CyanBright.copy(alpha = 0.08f)
                                    )
                                )
                            )
                    )

                    // Liquid Glass Island Dock Frame with elevated frosted glass
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.4.dp, LiquidGlassBrushes.PrismaticCausticBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bottom_nav_bar")
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x4DFFFFFF), // Crisp frosted specular light edge highlight
                                        Color(0xF008101E), // Deep high-opacity frosted obsidian glass
                                        Color(0xFA040810)  // Solid true-black anchor
                                    )
                                )
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Tab 0: Downloader
                            LiquidGlassNavButton(
                                title = "Downloader",
                                icon = Icons.Default.Download,
                                isSelected = currentTab == 0,
                                testTag = "nav_tab_downloader",
                                accentColor = CyanBright,
                                onClick = { currentTab = 0 },
                                modifier = Modifier.weight(1f)
                            )

                            // Tab 1: Downloads
                            LiquidGlassNavButton(
                                title = "Downloads",
                                icon = Icons.Default.Folder,
                                isSelected = currentTab == 1,
                                testTag = "nav_tab_downloads",
                                badgeCount = activeCount,
                                accentColor = CyanBright,
                                onClick = { currentTab = 1 },
                                modifier = Modifier.weight(1f)
                            )

                            // Tab 2: API & Engine
                            LiquidGlassNavButton(
                                title = "API & Engine",
                                icon = Icons.Default.Dns,
                                isSelected = currentTab == 2,
                                testTag = "nav_tab_settings",
                                accentColor = CyanBright,
                                onClick = { currentTab = 2 },
                                modifier = Modifier.weight(1f)
                            )

                            // Tab 3: Developer Hub (MD RASEL)
                            LiquidGlassNavButton(
                                title = "Developer",
                                icon = Icons.Default.Code,
                                isSelected = currentTab == 3,
                                testTag = "nav_tab_developer",
                                accentColor = NeonPurple,
                                onClick = { currentTab = 3 },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
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
