package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.data.api.AppUpdateManager
import com.example.data.api.UpdateState
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.api.TelegramBotClient
import com.example.data.repository.AppSettings
import com.example.ui.DownloadViewModel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CobaltBlue
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

@Composable
fun ApiSettingsScreen(
    viewModel: DownloadViewModel,
    onCheckForUpdates: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentSettings by viewModel.settings.collectAsStateWithLifecycle()
    val apiHealth by viewModel.apiHealth.collectAsStateWithLifecycle()
    val isTestingApi by viewModel.isTestingApi.collectAsStateWithLifecycle()

    val telegramBotInfo by viewModel.telegramBotInfo.collectAsStateWithLifecycle()
    val isVerifyingBot by viewModel.isVerifyingBot.collectAsStateWithLifecycle()
    val botVerificationStatus by viewModel.botVerificationStatus.collectAsStateWithLifecycle()

    val appUpdateManager = remember { AppUpdateManager.getInstance(context) }
    val updateState by appUpdateManager.updateState.collectAsStateWithLifecycle()

    var apiUrl by remember(currentSettings) { mutableStateOf(currentSettings.customApiUrl) }
    var authToken by remember(currentSettings) { mutableStateOf(currentSettings.authToken) }
    var selectedVideoQuality by remember(currentSettings) { mutableStateOf(currentSettings.defaultVideoQuality) }
    var selectedAudioFormat by remember(currentSettings) { mutableStateOf(currentSettings.defaultAudioFormat) }
    var embedSubs by remember(currentSettings) { mutableStateOf(currentSettings.embedSubtitles) }
    var embedThumb by remember(currentSettings) { mutableStateOf(currentSettings.embedThumbnail) }
    var cliFlags by remember(currentSettings) { mutableStateOf(currentSettings.extraCliFlags) }

    var telegramChatId by remember(currentSettings) { mutableStateOf(currentSettings.telegramChatId) }
    var telegramSyncEnabled by remember(currentSettings) { mutableStateOf(currentSettings.telegramSyncEnabled) }
    var workerApiSecret by remember(currentSettings) { mutableStateOf(currentSettings.workerApiSecret) }
    var manualBotToken by remember(currentSettings) { mutableStateOf("") }
    var showSecretPassword by remember { mutableStateOf(false) }
    var showManualTokenField by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("settings_screen_list"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Cyber Engine Command Banner
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
                border = BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(
                            CyanBright.copy(alpha = 0.55f),
                            NeonPurple.copy(alpha = 0.35f),
                            CyberBorder
                        )
                    )
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    CyanBright.copy(alpha = 0.09f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    CyanBright.copy(alpha = 0.25f),
                                                    NeonPurple.copy(alpha = 0.25f)
                                                )
                                            )
                                        )
                                        .border(1.dp, CyanBright.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Dns,
                                        contentDescription = null,
                                        tint = CyanBright,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "OmniStream Engine Hub",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.2.sp
                                        ),
                                        color = TextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "OmniStream Core Network",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = CyanAccent
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Fixed, sleek Version Badge that NEVER breaks or wraps
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CyanBright.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, CyanBright.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(CyanBright)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "v2.0.0-beta",
                                        color = CyanBright,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Single Master API powering all 21 platforms (YouTube, TikTok, Facebook, Instagram, Twitter/X, Pinterest, SoundCloud, etc.) with real-time yt-dlp & Cobalt extraction.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )

                        // High-tech status telemetry badges - strictly equal width & single-line
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f).height(30.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = CyberDarkSurface,
                                border = BorderStroke(1.dp, CyberBorder)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Cobalt 10.x", fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f).height(30.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = CyberDarkSurface,
                                border = BorderStroke(1.dp, CyberBorder)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("SSL Secure", fontSize = 10.sp, color = EmeraldSuccess, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f).height(30.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = CyberDarkSurface,
                                border = BorderStroke(1.dp, CyberBorder)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Public, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("21+ Sites", fontSize = 10.sp, color = CyanAccent, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                }
            }
        }

        // In-App Updates & Releases (GitHub OTA Engine) Card - Developer Standard UI
        item {
            val isChecking = updateState is UpdateState.Checking
            val isUpdateAvailable = updateState is UpdateState.UpdateAvailable
            val isUpToDate = updateState is UpdateState.UpToDate

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("in_app_update_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
                border = BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(
                            if (isUpdateAvailable) NeonPurple else CyanBright.copy(alpha = 0.5f),
                            CyberBorder
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Row: Icon + Title/Subtitle + Status Chip
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
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isUpdateAvailable) NeonPurple.copy(alpha = 0.2f)
                                        else CyanBright.copy(alpha = 0.15f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isUpdateAvailable) NeonPurple else CyanBright.copy(alpha = 0.45f),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = null,
                                    tint = if (isUpdateAvailable) NeonPurple else CyanBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "In-App Updates & Releases",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.1.sp
                                    ),
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Automated OTA GitHub Engine",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = CyanAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Dynamic Status Badge on Top Right
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                isChecking -> CyanBright.copy(alpha = 0.15f)
                                isUpdateAvailable -> EmeraldSuccess.copy(alpha = 0.15f)
                                isUpToDate -> EmeraldSuccess.copy(alpha = 0.12f)
                                else -> CyberDarkSurface
                            },
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isChecking -> CyanBright.copy(alpha = 0.5f)
                                    isUpdateAvailable -> EmeraldSuccess
                                    isUpToDate -> EmeraldSuccess.copy(alpha = 0.6f)
                                    else -> CyberBorder
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isChecking) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(10.dp),
                                        color = CyanBright,
                                        strokeWidth = 1.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Checking...", fontSize = 10.sp, color = CyanBright, fontWeight = FontWeight.Bold)
                                } else if (isUpdateAvailable) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldSuccess)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Update Available", fontSize = 10.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                } else if (isUpToDate) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Up to Date", fontSize = 10.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(CyanBright)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("v${com.example.BuildConfig.VERSION_NAME}", fontSize = 10.sp, color = CyanBright, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Description
                    Text(
                        text = "Automated version tracking directly syncing GitHub Releases with direct in-app package installation without opening external browsers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    // 3 High-Tech Status Telemetry Badges (Harmonious with Top Card!)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f).height(30.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = CyberDarkSurface,
                            border = BorderStroke(1.dp, CyberBorder)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Dns, contentDescription = null, tint = CyanBright, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("GitHub API", fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f).height(30.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = CyberDarkSurface,
                            border = BorderStroke(1.dp, CyberBorder)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("OTA Direct", fontSize = 10.sp, color = AmberWarning, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f).height(30.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = CyberDarkSurface,
                            border = BorderStroke(1.dp, CyberBorder)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Auto Install", fontSize = 10.sp, color = EmeraldSuccess, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                            }
                        }
                    }

                    // Action & Version Footer Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CURRENT VERSION",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "v${com.example.BuildConfig.VERSION_NAME} (Build ${com.example.BuildConfig.VERSION_CODE})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanBright
                            )
                        }

                        Button(
                            onClick = {
                                if (isUpdateAvailable) {
                                    appUpdateManager.showDialog()
                                } else {
                                    onCheckForUpdates()
                                }
                            },
                            enabled = !isChecking,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isUpdateAvailable) EmeraldSuccess else CyanBright,
                                contentColor = CyberBlack
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            if (isChecking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = CyberBlack,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Checking...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            } else if (isUpdateAvailable) {
                                Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("View Update", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Check Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Custom API URL Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
                border = BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(CyanBright.copy(alpha = 0.45f), CyberBorder)
                    )
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyanBright.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Dns, contentDescription = null, tint = CyanBright, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Official Master API Endpoint",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                    }

                    OutlinedTextField(
                        value = apiUrl,
                        onValueChange = { apiUrl = it },
                        modifier = Modifier.fillMaxWidth().testTag("api_url_input"),
                        label = { Text("Master Cobalt API URL") },
                        placeholder = { Text("https://omnistream-api.alexraselchodhury.workers.dev", color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanBright,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = authToken,
                        onValueChange = { authToken = it },
                        modifier = Modifier.fillMaxWidth().testTag("api_token_input"),
                        label = { Text("Bearer / Auth Token (Optional)") },
                        placeholder = { Text("Leave blank for direct access", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanBright,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Presets with Active Highlighting
                    Text("Official API Engine Mirrors:", fontSize = 11.sp, color = TextMuted)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip(
                            label = "Cloudflare Worker (Recommended)",
                            isActive = apiUrl.trim() == "https://omnistream-api.alexraselchodhury.workers.dev"
                        ) {
                            apiUrl = "https://omnistream-api.alexraselchodhury.workers.dev"
                        }
                        PresetChip(
                            label = "Render VIP Mirror",
                            isActive = apiUrl.trim() == "https://cobalt-latest-a04h.onrender.com"
                        ) {
                            apiUrl = "https://cobalt-latest-a04h.onrender.com"
                        }
                        PresetChip(
                            label = "Cobalt Official",
                            isActive = apiUrl.trim() == "https://api.cobalt.tools"
                        ) {
                            apiUrl = "https://api.cobalt.tools"
                        }
                    }

                    // Ping / Test API Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.testApiHealth() },
                            modifier = Modifier
                                .then(if (apiHealth != null) Modifier.weight(1f) else Modifier.fillMaxWidth())
                                .height(40.dp)
                                .testTag("test_api_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberDarkSurface, contentColor = CyanBright),
                            border = BorderStroke(1.dp, CyanBright.copy(alpha = 0.8f)),
                            enabled = !isTestingApi,
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            if (isTestingApi) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyanBright, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (apiHealth != null) "Ping Health" else "Ping & Check Health",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        // Sleek status result pill matching button height & clean 2-line telemetry
                        val health = apiHealth
                        if (health != null) {
                            val isConnected = health.status == "connected"
                            val statusColor = if (isConnected) EmeraldSuccess else CyanBright
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(40.dp),
                                color = statusColor.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(statusColor)
                                    )
                                    Spacer(modifier = Modifier.width(7.dp))
                                    Column(verticalArrangement = Arrangement.Center) {
                                        Text(
                                            text = "${health.latencyMs} ms",
                                            color = statusColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Text(
                                            text = "v${health.ytdlpVersion}",
                                            color = TextMuted,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Telegram Bot Hub Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Header Row: Bot Identity + Live Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(CyanBright.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = CyanBright,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Telegram Bot Integration",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = "@OmniStream34_bot",
                                    color = CyanAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Verified Status Badge (Single line, strictly no wrap)
                        val isOnline = telegramBotInfo?.isOnline == true && botVerificationStatus?.startsWith("Error") != true
                        val badgeColor = if (isOnline) EmeraldSuccess else RoseError
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = badgeColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(badgeColor)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isOnline) "ONLINE" else "OFFLINE",
                                    color = badgeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // Unified Clean Status & Vault Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = CyberDarkSurface,
                        border = BorderStroke(1.dp, CyberBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Vault Protection Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Key,
                                        contentDescription = null,
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Bot API Credentials",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = EmeraldSuccess.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        "VAULT PROTECTED",
                                        color = EmeraldSuccess,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Clean description
                            Text(
                                text = "Send any video or audio link directly to @OmniStream34_bot in Telegram to receive Full HD media with zero watermarks.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )

                            // Feature Chips Row - strictly equal width & single-line
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f).height(28.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    color = CyanBright.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, CyanBright.copy(alpha = 0.25f))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            "⚡ 24/7 Active",
                                            color = CyanAccent,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                                Surface(
                                    modifier = Modifier.weight(1f).height(28.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    color = CyanBright.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, CyanBright.copy(alpha = 0.25f))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            "✨ Clean HD",
                                            color = CyanAccent,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                                Surface(
                                    modifier = Modifier.weight(1f).height(28.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    color = EmeraldSuccess.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.25f))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            "🔒 Private",
                                            color = EmeraldSuccess,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Secure Worker Authorization Bridge (Hidden to prevent unauthorized exposure)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberDarkSurface.copy(alpha = 0.5f))
                            .border(BorderStroke(1.dp, CyberBorder.copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Worker Security Shield",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldSuccess.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "ENCRYPTED",
                                        color = EmeraldSuccess,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                        Text(
                            "Authorization key is securely hidden to safeguard your private Cloudflare Worker API. Requests are automatically authenticated via encrypted headers.",
                            color = TextMuted,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )

                        // Discreet expandable option for authorized admin to edit password if needed
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSecretPassword = !showSecretPassword }
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (showSecretPassword) "Hide Password Editor" else "Change Secret Key (Admin Only)",
                                fontSize = 11.sp,
                                color = CyanBright,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = if (showSecretPassword) Icons.Default.VisibilityOff else Icons.Default.Key,
                                contentDescription = null,
                                tint = CyanBright,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        if (showSecretPassword) {
                            OutlinedTextField(
                                value = workerApiSecret,
                                onValueChange = { workerApiSecret = it },
                                modifier = Modifier.fillMaxWidth().testTag("worker_api_secret_input"),
                                label = { Text("Worker Secret Key", fontSize = 11.sp) },
                                placeholder = { Text("••••••••", color = TextMuted) },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanBright,
                                    unfocusedBorderColor = CyberBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }
                    }

                    // Direct Bot Token Override toggle & input
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showManualTokenField = !showManualTokenField }
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showManualTokenField) "▲ Hide Direct Bot Token Override" else "▼ Direct Bot Token Override (Optional)",
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (showManualTokenField) {
                        OutlinedTextField(
                            value = manualBotToken,
                            onValueChange = { manualBotToken = it },
                            modifier = Modifier.fillMaxWidth().testTag("manual_bot_token_input"),
                            label = { Text("Direct Bot Token") },
                            placeholder = { Text("Paste token e.g. 123456789:AAFv...", color = TextMuted) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanBright,
                                unfocusedBorderColor = CyberBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    if (botVerificationStatus != null && botVerificationStatus?.startsWith("Error") == true) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = RoseError.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, RoseError.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = botVerificationStatus.orEmpty(),
                                    fontSize = 11.sp,
                                    color = RoseError,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "💡 Tip: If HTTP 401 Unauthorized, make sure API_SECRET in Cloudflare Worker matches your password, or enter the Telegram Bot Token directly above.",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Test & Launch Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.verifyTelegramBot(
                                    customToken = manualBotToken.trim().ifBlank { null },
                                    customSecret = workerApiSecret.trim()
                                )
                            },
                            modifier = Modifier.weight(1f).height(42.dp).testTag("verify_telegram_bot_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberDarkSurface, contentColor = CyanBright),
                            border = BorderStroke(1.dp, CyanBright),
                            enabled = !isVerifyingBot
                        ) {
                            if (isVerifyingBot) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyanBright, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Verify Bot", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TelegramBotClient.BOT_TELEGRAM_URL)).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.weight(1f).height(42.dp).testTag("open_telegram_bot_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanBright.copy(alpha = 0.15f), contentColor = CyanBright),
                            border = BorderStroke(1.dp, CyanBright.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open in TG", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Telegram Push Alerts & Sync Configuration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Send Alerts to Telegram", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("Receive completed download alerts on TG", color = TextMuted, fontSize = 10.sp)
                            }
                        }
                        Switch(
                            checked = telegramSyncEnabled,
                            onCheckedChange = { telegramSyncEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = CyanBright,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = CyberDarkSurface
                            )
                        )
                    }

                    if (telegramSyncEnabled) {
                        OutlinedTextField(
                            value = telegramChatId,
                            onValueChange = { telegramChatId = it },
                            modifier = Modifier.fillMaxWidth().testTag("telegram_chat_id_input"),
                            label = { Text("Your Telegram Chat ID / User ID") },
                            placeholder = { Text("e.g. 123456789 (optional)", color = TextMuted) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanBright,
                                unfocusedBorderColor = CyberBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        if (telegramChatId.isNotBlank()) {
                            Button(
                                onClick = { viewModel.sendTestTelegramMessage(telegramChatId) },
                                modifier = Modifier.fillMaxWidth().height(36.dp).testTag("send_telegram_test_msg_button"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberDarkSurface, contentColor = CyanAccent),
                                border = BorderStroke(1.dp, CyberBorder)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send Test Message via Bot", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Quality Preferences Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = CyanBright, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Default Quality Presets",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }

                    Text("Default Video Resolution:", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val videoOptions = listOf("8K 60 FPS", "4K 60 FPS", "1080p 120 FPS", "1080p 60 FPS", "720p", "Best Available")
                        videoOptions.forEach { opt ->
                            QualitySelectionChip(
                                label = opt,
                                isSelected = selectedVideoQuality == opt,
                                onSelect = { selectedVideoQuality = opt }
                            )
                        }
                    }

                    Text("Default Audio Format:", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val audioOptions = listOf("FLAC 24-bit", "MP3 320 kbps", "MP3 256 kbps", "AAC 192 kbps", "Opus 160 kbps")
                        audioOptions.forEach { opt ->
                            QualitySelectionChip(
                                label = opt,
                                isSelected = selectedAudioFormat == opt,
                                onSelect = { selectedAudioFormat = opt },
                                accentColor = NeonPurple
                            )
                        }
                    }
                }
            }
        }

        // Pro yt-dlp CLI Flags Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = CyanBright, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "yt-dlp Engine Flags",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }

                    OutlinedTextField(
                        value = cliFlags,
                        onValueChange = { cliFlags = it },
                        modifier = Modifier.fillMaxWidth().testTag("cli_flags_input"),
                        label = { Text("Extra yt-dlp Arguments") },
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanBright,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Quick Flag Presets
                    Text("Recommended Engine Flags:", fontSize = 11.sp, color = TextMuted)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip("Bypass Bot Challenge (Android Client)") {
                            cliFlags = "--extractor-args \"youtube:player_client=android,ios,web\""
                        }
                        PresetChip("Embed Meta & Art") {
                            cliFlags = "--embed-metadata --embed-thumbnail"
                        }
                        PresetChip("Best Multi-Stream") {
                            cliFlags = "-f \"bestvideo+bestaudio/best\" --no-check-certificates"
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto-embed Subtitles (bn/en)", color = TextPrimary, fontSize = 13.sp)
                        Switch(
                            checked = embedSubs,
                            onCheckedChange = { embedSubs = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanBright)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Embed Media Thumbnail Art", color = TextPrimary, fontSize = 13.sp)
                        Switch(
                            checked = embedThumb,
                            onCheckedChange = { embedThumb = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanBright)
                        )
                    }
                }
            }
        }

        // Storage Directory Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Download Storage Location", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("App / Android Downloads Directory", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    val manualTokenClean = manualBotToken.trim()
                    val tokenToSave = if (manualTokenClean.isNotBlank()) manualTokenClean else currentSettings.telegramBotToken
                    val newSettings = AppSettings(
                        customApiUrl = apiUrl.trim(),
                        authToken = authToken.trim(),
                        defaultVideoQuality = selectedVideoQuality,
                        defaultAudioFormat = selectedAudioFormat,
                        embedSubtitles = embedSubs,
                        embedThumbnail = embedThumb,
                        extraCliFlags = cliFlags.trim(),
                        telegramBotToken = tokenToSave,
                        telegramBotUsername = currentSettings.telegramBotUsername,
                        telegramBotName = currentSettings.telegramBotName,
                        telegramChatId = telegramChatId.trim(),
                        telegramSyncEnabled = telegramSyncEnabled,
                        workerApiSecret = workerApiSecret.trim()
                    )
                    viewModel.updateSettings(newSettings)
                    Toast.makeText(context, "Settings saved & applied successfully!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_settings_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanBright,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Settings & Apply", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun PresetChip(
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) CyanBright.copy(alpha = 0.18f) else CyberDarkSurface,
        border = BorderStroke(
            1.dp,
            if (isActive) CyanBright else CyberBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isActive) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = CyanBright,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) CyanBright else CyanAccent
            )
        }
    }
}

@Composable
fun QualitySelectionChip(
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    accentColor: Color = CyanBright
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.2f) else CyberDarkSurface,
        border = BorderStroke(1.dp, if (isSelected) accentColor else CyberBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) accentColor else TextSecondary
            )
        }
    }
}
