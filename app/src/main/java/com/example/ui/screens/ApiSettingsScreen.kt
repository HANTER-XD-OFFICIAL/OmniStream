package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val currentSettings by viewModel.settings.collectAsStateWithLifecycle()
    val apiHealth by viewModel.apiHealth.collectAsStateWithLifecycle()
    val isTestingApi by viewModel.isTestingApi.collectAsStateWithLifecycle()

    var apiUrl by remember(currentSettings) { mutableStateOf(currentSettings.customApiUrl) }
    var authToken by remember(currentSettings) { mutableStateOf(currentSettings.authToken) }
    var selectedVideoQuality by remember(currentSettings) { mutableStateOf(currentSettings.defaultVideoQuality) }
    var selectedAudioFormat by remember(currentSettings) { mutableStateOf(currentSettings.defaultAudioFormat) }
    var embedSubs by remember(currentSettings) { mutableStateOf(currentSettings.embedSubtitles) }
    var embedThumb by remember(currentSettings) { mutableStateOf(currentSettings.embedThumbnail) }
    var cliFlags by remember(currentSettings) { mutableStateOf(currentSettings.extraCliFlags) }

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
                text = "API & Engine Controller",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Configure custom yt-dlp API server endpoints, quality defaults & pro flags",
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
                                text = "Custom yt-dlp Server URL",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                    }

                    OutlinedTextField(
                        value = apiUrl,
                        onValueChange = { apiUrl = it },
                        modifier = Modifier.fillMaxWidth().testTag("api_url_input"),
                        label = { Text("API Base URL (HTTP/HTTPS)") },
                        placeholder = { Text("http://192.168.1.100:5000 or https://...", color = TextMuted) },
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
                        placeholder = { Text("Leave blank if no authorization needed", color = TextMuted) },
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
                    Text("Quick Presets:", fontSize = 11.sp, color = TextMuted)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip("TeraBox Cloud Gateway") { apiUrl = "https://terabox-gateway.local/api" }
                        PresetChip("Self-Hosted (:5000)") { apiUrl = "http://192.168.1.100:5000" }
                        PresetChip("Docker Container (:8080)") { apiUrl = "http://localhost:8080" }
                        PresetChip("Cobalt API") { apiUrl = "https://api.cobalt.tools" }
                        PresetChip("OmniStream Core") { apiUrl = "https://omnistream-api.local:5000" }
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
                        extraCliFlags = cliFlags.trim()
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
            Spacer(modifier = Modifier.height(24.dp))
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
