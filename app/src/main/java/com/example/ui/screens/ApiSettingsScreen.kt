package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
fun ApiSettingsScreen(viewModel: DownloadViewModel) {
    val context = LocalContext.current
    val currentSettings by viewModel.settings.collectAsStateWithLifecycle()
    val apiHealth by viewModel.apiHealth.collectAsStateWithLifecycle()
    val isTestingApi by viewModel.isTestingApi.collectAsStateWithLifecycle()

    val telegramBotInfo by viewModel.telegramBotInfo.collectAsStateWithLifecycle()
    val isVerifyingBot by viewModel.isVerifyingBot.collectAsStateWithLifecycle()
    val botVerificationStatus by viewModel.botVerificationStatus.collectAsStateWithLifecycle()

    var apiUrl by remember(currentSettings) { mutableStateOf(currentSettings.customApiUrl) }
    var authToken by remember(currentSettings) { mutableStateOf(currentSettings.authToken) }
    var selectedVideoQuality by remember(currentSettings) { mutableStateOf(currentSettings.defaultVideoQuality) }
    var selectedAudioFormat by remember(currentSettings) { mutableStateOf(currentSettings.defaultAudioFormat) }
    var embedSubs by remember(currentSettings) { mutableStateOf(currentSettings.embedSubtitles) }
    var embedThumb by remember(currentSettings) { mutableStateOf(currentSettings.embedThumbnail) }
    var cliFlags by remember(currentSettings) { mutableStateOf(currentSettings.extraCliFlags) }

    var telegramChatId by remember(currentSettings) { mutableStateOf(currentSettings.telegramChatId) }
    var telegramSyncEnabled by remember(currentSettings) { mutableStateOf(currentSettings.telegramSyncEnabled) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("settings_screen_list"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Master API & Engine Hub",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Single Master API powering all 21 platforms (YouTube, TikTok, Facebook, Instagram, Twitter/X, Pinterest, SoundCloud, etc.)",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // Custom API URL Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Dns, contentDescription = null, tint = CyanBright, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
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
                        placeholder = { Text("https://muddy-scene-0ff7.alexraselchodhury.workers.dev", color = TextMuted) },
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

                    // Presets
                    Text("Official API Engine:", fontSize = 11.sp, color = TextMuted)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip("Cloudflare Worker API (Active)") { apiUrl = "https://muddy-scene-0ff7.alexraselchodhury.workers.dev" }
                        PresetChip("Render VIP Mirror") { apiUrl = "https://cobalt-latest-a04h.onrender.com" }
                        PresetChip("Cobalt Official") { apiUrl = "https://api.cobalt.tools" }
                    }

                    // Ping / Test API Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.testApiHealth() },
                            modifier = Modifier.height(40.dp).testTag("test_api_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberDarkSurface, contentColor = CyanBright),
                            border = BorderStroke(1.dp, CyanBright),
                            enabled = !isTestingApi
                        ) {
                            if (isTestingApi) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyanBright, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ping & Check Health", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Status result pill
                        val health = apiHealth
                        if (health != null) {
                            val isConnected = health.status == "connected"
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isConnected) EmeraldSuccess else CyanBright)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${health.latencyMs} ms • ${health.ytdlpVersion}",
                                    color = if (isConnected) EmeraldSuccess else CyanBright,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = CyanBright, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Telegram Bot Integration",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        // Verified Status Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldSuccess.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldSuccess)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "LINKED & READY",
                                    color = EmeraldSuccess,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "Official Connected Bot: OmniStream (@OmniStream34_bot)",
                        fontSize = 11.sp,
                        color = CyanAccent,
                        fontWeight = FontWeight.Medium
                    )

                    // Bot API Token (Completely Hidden & Protected in Internal Vault)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = CyberDarkSurface,
                        border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(EmeraldSuccess.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Key,
                                        contentDescription = "Vault Protected",
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "Telegram Bot API Token",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Protected Vault • Hidden from view for security",
                                        color = EmeraldSuccess,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldSuccess.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "HIDDEN & SAFE",
                                        color = EmeraldSuccess,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Real-time Verified Bot Status Card
                    val info = telegramBotInfo
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = CyberDarkSurface,
                        border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(info?.firstName ?: "OmniStream", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Text("@${info?.username ?: "OmniStream34_bot"} • Official Verified Downloader", color = TextSecondary, fontSize = 11.sp)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldSuccess.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "Online",
                                    color = EmeraldSuccess,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    if (botVerificationStatus != null && botVerificationStatus?.startsWith("Error") == true) {
                        Text(
                            text = botVerificationStatus.orEmpty(),
                            fontSize = 11.sp,
                            color = RoseError
                        )
                    }

                    // 24/7 Downloader Engine Status Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = CyanBright.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, CyanBright.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldSuccess)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "24/7 Media Downloader Active",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text("No Watermarks", color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                "Send any video link to @OmniStream34_bot from TikTok, Facebook, Instagram, YouTube, or TeraBox to get HD MP4 & MP3 directly inside Telegram!",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Test & Launch Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.verifyTelegramBot() },
                            modifier = Modifier.weight(1f).height(40.dp).testTag("verify_telegram_bot_button"),
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
                            modifier = Modifier.weight(1f).height(40.dp).testTag("open_telegram_bot_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanBright.copy(alpha = 0.15f), contentColor = CyanBright),
                            border = BorderStroke(1.dp, CyanBright.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
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
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
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
                    val newSettings = AppSettings(
                        customApiUrl = apiUrl.trim(),
                        authToken = authToken.trim(),
                        defaultVideoQuality = selectedVideoQuality,
                        defaultAudioFormat = selectedAudioFormat,
                        embedSubtitles = embedSubs,
                        embedThumbnail = embedThumb,
                        extraCliFlags = cliFlags.trim(),
                        telegramBotToken = currentSettings.telegramBotToken,
                        telegramBotUsername = currentSettings.telegramBotUsername,
                        telegramBotName = currentSettings.telegramBotName,
                        telegramChatId = telegramChatId.trim(),
                        telegramSyncEnabled = telegramSyncEnabled
                    )
                    viewModel.updateSettings(newSettings)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_settings_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanBright,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Settings & Apply", fontWeight = FontWeight.Bold)
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun PresetChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = CyberDarkSurface,
        border = BorderStroke(1.dp, CyberBorder)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = CyanAccent,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
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
