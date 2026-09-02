package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DeveloperSupportHubDialog
import com.example.ui.components.UserPrivacyHubDialog
import com.example.ui.components.WelcomeOnboardingDialog
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanBright
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Dedicated Developer Hub Screen (4th Tab in Navigation Bar)
 * Features Developer Details for MD RASEL:
 * - 100% Privacy Protection: All sensitive contact strings/numbers are completely hidden from UI
 * - 1-Click Action Access: Direct WhatsApp, Telegram, Facebook, Gmail buttons
 * - User Privacy & Data Safety Hub Modal Trigger
 * - High Polish Cyber UI Aesthetics
 */
@Composable
fun DeveloperScreen(
    onNavigateToHome: () -> Unit = {}
) {
    val context = LocalContext.current
    var showSupportHubModal by remember { mutableStateOf(false) }
    var showWelcomeModal by remember { mutableStateOf(false) }
    var showPrivacyHubModal by remember { mutableStateOf(false) }

    val devName = "MD RASEL"
    val devFacebookUrl = "https://www.facebook.com/md.rasel.7.8.2.3.4"
    val devWhatsAppNumber = "+8801882278234"
    val devTelegramUrl = "https://t.me/HANTER_XD_OFFICIAL"
    val devEmail = "alexraselchodhury@gmail.com"

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open link: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsApp(phone: String) {
        try {
            val cleanPhone = phone.replace("+", "").replace(" ", "").replace("-", "")
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://wa.me/$cleanPhone?text=${Uri.encode("Hello MD RASEL, I am reaching out regarding OmniStream App Support.")}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            openUrl("https://wa.me/8801882278234")
        }
    }

    fun openEmail(email: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, "OmniStream - Developer Support Request")
                putExtra(Intent.EXTRA_TEXT, "Hello MD RASEL,\n\nI am contacting you regarding OmniStream Video Downloader.\n\n")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            openUrl("mailto:$email")
        }
    }

    // Glowing animation for hero avatar
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_pulse"
    )

    if (showSupportHubModal) {
        DeveloperSupportHubDialog(
            onDismiss = { showSupportHubModal = false }
        )
    }

    if (showWelcomeModal) {
        WelcomeOnboardingDialog(
            onDismiss = { showWelcomeModal = false },
            onOpenSupportHub = {
                showWelcomeModal = false
                showSupportHubModal = true
            }
        )
    }

    if (showPrivacyHubModal) {
        UserPrivacyHubDialog(
            onDismiss = { showPrivacyHubModal = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        // Top Header Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DEVELOPER HUB",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.2.sp
                        ),
                        color = CyanBright
                    )
                    Text(
                        text = "Official Creator Profile & Direct Assistance",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                    )
                }

                Surface(
                    onClick = { showWelcomeModal = true },
                    shape = RoundedCornerShape(10.dp),
                    color = NeonPurple.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Welcome UI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurple
                        )
                    }
                }
            }
        }

        // Hero Profile Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("developer_hero_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                border = BorderStroke(
                    1.4.dp,
                    Brush.verticalGradient(
                        listOf(
                            CyanBright.copy(alpha = 0.9f),
                            NeonPurple.copy(alpha = 0.6f),
                            Color(0xFF0F172A)
                        )
                    )
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Profile Header
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
                                    .size(54.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                CyanBright.copy(alpha = 0.35f),
                                                NeonPurple.copy(alpha = 0.2f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                                    .border(1.5.dp, CyanBright, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = "Developer",
                                    tint = CyanBright,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = devName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Maintainer",
                                        tint = CyanBright,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = "Lead Developer & System Architect",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = CyanAccent,
                                        fontSize = 11.5.sp
                                    )
                                )
                                Text(
                                    text = "OmniStream VIP Core Engine Maintainer",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        // Verified Status Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF064E3B).copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.7f))
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
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ONLINE",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = EmeraldSuccess
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Notice Chip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF064E3B).copy(alpha = 0.35f))
                            .border(0.8.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Official 24/7 Developer Support • Tap any button below for instant 1-click access",
                            fontSize = 10.sp,
                            color = Color(0xFF34D399)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1-Click Action Buttons with All Details Completely Hidden
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        // 1. WhatsApp Official
                        DeveloperActionButtonRow(
                            icon = Icons.Default.Chat,
                            iconBgColor = Color(0xFF064E3B),
                            iconColor = Color(0xFF34D399),
                            channelTitle = "WhatsApp Support",
                            channelSubtitle = "Direct 1-click encrypted chat with developer",
                            buttonLabel = "Chat on WhatsApp",
                            buttonBg = Color(0xFF064E3B).copy(alpha = 0.8f),
                            buttonBorder = Color(0xFF10B981),
                            onOpen = { openWhatsApp(devWhatsAppNumber) }
                        )

                        // 2. Telegram Support
                        DeveloperActionButtonRow(
                            icon = Icons.Default.Send,
                            iconBgColor = Color(0xFF0C4A6E),
                            iconColor = Color(0xFF38BDF8),
                            channelTitle = "Telegram Support",
                            channelSubtitle = "Official updates, channel & community access",
                            buttonLabel = "Join Telegram",
                            buttonBg = Color(0xFF0C4A6E).copy(alpha = 0.8f),
                            buttonBorder = Color(0xFF0284C7),
                            onOpen = { openUrl(devTelegramUrl) }
                        )

                        // 3. Facebook Profile
                        DeveloperActionButtonRow(
                            icon = Icons.Default.Public,
                            iconBgColor = Color(0xFF1E3A8A),
                            iconColor = Color(0xFF60A5FA),
                            channelTitle = "Facebook Profile",
                            channelSubtitle = "Visit the developer's official Facebook page",
                            buttonLabel = "Visit Profile",
                            buttonBg = Color(0xFF1E3A8A).copy(alpha = 0.8f),
                            buttonBorder = Color(0xFF3B82F6),
                            onOpen = { openUrl(devFacebookUrl) }
                        )

                        // 4. Support Email (Gmail)
                        DeveloperActionButtonRow(
                            icon = Icons.Default.Email,
                            iconBgColor = Color(0xFF7F1D1D),
                            iconColor = Color(0xFFF87171),
                            channelTitle = "Support Email (Gmail)",
                            channelSubtitle = "Send inquiry directly to official developer inbox",
                            buttonLabel = "Send Email",
                            buttonBg = Color(0xFF7F1D1D).copy(alpha = 0.8f),
                            buttonBorder = Color(0xFFEF4444),
                            onOpen = { openEmail(devEmail) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3 Wide Quick Connect Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // WhatsApp
                        Surface(
                            onClick = { openWhatsApp(devWhatsAppNumber) },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("WhatsApp", fontSize = 11.5.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            }
                        }

                        // Telegram
                        Surface(
                            onClick = { openUrl(devTelegramUrl) },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Telegram", fontSize = 11.5.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            }
                        }

                        // Facebook
                        Surface(
                            onClick = { openUrl(devFacebookUrl) },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF3B82F6),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Public, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Facebook", fontSize = 11.5.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Privacy & Data Safety Hub Card Trigger
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPrivacyHubModal = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF042F2E).copy(alpha = 0.5f)),
                border = BorderStroke(1.2.dp, EmeraldSuccess.copy(alpha = 0.6f))
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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldSuccess.copy(alpha = 0.2f))
                                .border(1.dp, EmeraldSuccess, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(22.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "User Privacy & Security Hub",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6EE7B7)
                                )
                            )
                            Text(
                                text = "100% Offline Storage • Zero Logging • Clear Cache",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                        }
                    }

                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Open Support Hub Dialog Trigger Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSupportHubModal = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, CyanBright.copy(alpha = 0.5f))
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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyanBright.copy(alpha = 0.15f))
                                .border(1.dp, CyanBright, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HeadsetMic, contentDescription = null, tint = CyanBright, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Developer Support Hub Modal",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Tap to open complete support channels popup",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                        }
                    }

                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = CyanBright, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Developer Mission & Technical Architecture
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DEVELOPER MISSION & PRIVACY GUARANTEE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.8.sp
                            ),
                            color = CyanAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "OmniStream is engineered by MD RASEL with modern Jetpack Compose and high-performance streaming architecture. The app is 100% privacy-friendly, saving downloads directly to your device storage without any external logging.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 17.sp),
                        color = TextSecondary
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

/**
 * 1-Click Action Button Row that hides all sensitive contact strings/numbers
 */
@Composable
private fun DeveloperActionButtonRow(
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    channelTitle: String,
    channelSubtitle: String,
    buttonLabel: String,
    buttonBg: Color,
    buttonBorder: Color,
    onOpen: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Icon + Title & Subtitle (No private numbers/emails shown)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBgColor)
                        .border(0.8.dp, iconColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = channelTitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = channelSubtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = TextSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: 1-Click Action Button
            Surface(
                onClick = onOpen,
                shape = RoundedCornerShape(8.dp),
                color = buttonBg,
                border = BorderStroke(1.dp, buttonBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buttonLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
