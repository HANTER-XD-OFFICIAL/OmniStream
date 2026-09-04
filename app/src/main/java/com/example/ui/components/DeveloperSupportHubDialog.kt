package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

/**
 * Developer Support Hub Dialog matching the exact high-polish UI design.
 * Developer Name: MD RASEL
 * Contact Channels: WhatsApp, Telegram, Facebook, Gmail, 100% Privacy
 */
@Composable
fun DeveloperSupportHubDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val devName = "MD RASEL"
    val devFacebookUrl = "https://www.facebook.com/md.rasel.7.8.2.3.4"
    val devWhatsAppNumber = "+8801882278234"
    val devTelegramUrl = "https://t.me/HANTER_XD_OFFICIAL"
    val botTelegramUrl = "https://t.me/OmniStream34_bot"
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
                Uri.parse("https://wa.me/$cleanPhone?text=${Uri.encode("Hello MD RASEL, I am contacting you regarding OmniStream App Support.")}")
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
                putExtra(Intent.EXTRA_TEXT, "Hello MD RASEL,\n\nI need assistance with OmniStream Video Downloader.\n\n")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            openUrl("mailto:$email")
        }
    }

    // Pulse animation for the support badge
    val infiniteTransition = rememberInfiniteTransition(label = "support_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF0B132B))
                .border(
                    BorderStroke(
                        1.2.dp,
                        Brush.verticalGradient(
                            listOf(
                                CyanBright.copy(alpha = 0.8f),
                                Color(0xFF1E293B),
                                Color(0xFF0F172A)
                            )
                        )
                    ),
                    RoundedCornerShape(26.dp)
                )
                .padding(20.dp)
                .testTag("developer_support_hub_dialog")
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Bar: Icon, Title & Subtitle, Close Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Glowing Headset Icon
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1D4ED8).copy(alpha = 0.25f))
                                    .border(1.2.dp, CyanBright, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HeadsetMic,
                                    contentDescription = "Support Hub",
                                    tint = CyanBright,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Developer Support Hub",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 17.sp,
                                            color = TextPrimary
                                        )
                                    )
                                }
                                Text(
                                    text = "Official Assistance & Community",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }

                        // Close Icon Button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Lead Developer Name Badge Card
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
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
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DEVELOPER: $devName",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp,
                                        color = CyanBright
                                    )
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Maintainer",
                                tint = CyanBright,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Section Title: OFFICIAL CONTACT CHANNELS
                item {
                    Text(
                        text = "OFFICIAL CONTACT CHANNELS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            color = CyanAccent
                        ),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                // 1. WhatsApp Support
                item {
                    HubContactChannelCard(
                        icon = Icons.Default.Chat,
                        iconBgColor = Color(0xFF064E3B),
                        iconTintColor = Color(0xFF34D399),
                        title = "WhatsApp Support",
                        subtitle = "Direct Encrypted Chat • Tap to connect",
                        buttonText = "Chat →",
                        buttonBgColor = Color(0xFF064E3B).copy(alpha = 0.7f),
                        buttonBorderColor = Color(0xFF10B981),
                        buttonTextColor = Color(0xFF34D399),
                        onClick = { openWhatsApp(devWhatsAppNumber) }
                    )
                }

                // 2. Official Telegram Channel
                item {
                    HubContactChannelCard(
                        icon = Icons.Default.Send,
                        iconBgColor = Color(0xFF0C4A6E),
                        iconTintColor = Color(0xFF38BDF8),
                        title = "Official Telegram Channel",
                        subtitle = "Community & Announcements • Tap to join",
                        buttonText = "Join →",
                        buttonBgColor = Color(0xFF0C4A6E).copy(alpha = 0.7f),
                        buttonBorderColor = Color(0xFF0284C7),
                        buttonTextColor = Color(0xFF38BDF8),
                        onClick = { openUrl(devTelegramUrl) }
                    )
                }

                // 3. Official Telegram Bot (DOWNLOAD ALL IN ONE)
                item {
                    HubContactChannelCard(
                        icon = Icons.Default.SmartToy,
                        iconBgColor = Color(0xFF0C4A6E),
                        iconTintColor = CyanBright,
                        title = "OmniStream Official Bot",
                        subtitle = "@OmniStream34_bot • Automated Telegram Media Bot",
                        buttonText = "Start Bot →",
                        buttonBgColor = Color(0xFF0C4A6E).copy(alpha = 0.7f),
                        buttonBorderColor = CyanBright,
                        buttonTextColor = CyanBright,
                        onClick = { openUrl(botTelegramUrl) }
                    )
                }

                // 4. Direct Support Email
                item {
                    HubContactChannelCard(
                        icon = Icons.Default.Email,
                        iconBgColor = Color(0xFF7F1D1D),
                        iconTintColor = Color(0xFFF87171),
                        title = "Direct Support Email",
                        subtitle = "Official Help Desk • Tap to send email",
                        buttonText = "Email →",
                        buttonBgColor = Color(0xFF7F1D1D).copy(alpha = 0.7f),
                        buttonBorderColor = Color(0xFFEF4444),
                        buttonTextColor = Color(0xFFF87171),
                        onClick = { openEmail(devEmail) }
                    )
                }

                // 5. Official Facebook Profile
                item {
                    HubContactChannelCard(
                        icon = Icons.Default.Public,
                        iconBgColor = Color(0xFF1E3A8A),
                        iconTintColor = Color(0xFF60A5FA),
                        title = "Official Facebook Profile",
                        subtitle = "$devName • Tap to open Facebook",
                        buttonText = "Visit →",
                        buttonBgColor = Color(0xFF1E3A8A).copy(alpha = 0.7f),
                        buttonBorderColor = Color(0xFF3B82F6),
                        buttonTextColor = Color(0xFF60A5FA),
                        onClick = { openUrl(devFacebookUrl) }
                    )
                }

                // 6. 100% User Privacy Guarantee
                item {
                    HubContactChannelCard(
                        icon = Icons.Default.Shield,
                        iconBgColor = Color(0xFF064E3B),
                        iconTintColor = EmeraldSuccess,
                        title = "100% User Privacy Shield",
                        subtitle = "Zero logs, encrypted local media downloader",
                        buttonText = "Guarantee →",
                        buttonBgColor = Color(0xFF064E3B).copy(alpha = 0.7f),
                        buttonBorderColor = EmeraldSuccess,
                        buttonTextColor = EmeraldSuccess,
                        onClick = {
                            Toast.makeText(
                                context,
                                "OmniStream Guarantee: 100% Client-Side Privacy, Zero Server Logging",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }

                // Section Title: QUICK ACTIONS
                item {
                    Text(
                        text = "QUICK ACTIONS (TAP TO OPEN)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            color = TextMuted
                        ),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Quick Action Chips Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { openWhatsApp(devWhatsAppNumber) },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF064E3B).copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 9.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                            }
                        }

                        Surface(
                            onClick = { openUrl(devTelegramUrl) },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0C4A6E).copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 9.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Telegram", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                            }
                        }

                        Surface(
                            onClick = { openUrl(devFacebookUrl) },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E3A8A).copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 9.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Public, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Facebook", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                            }
                        }
                    }
                }

                // Big Bottom Close Button
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, Color(0xFF334155)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF0F172A),
                            contentColor = TextPrimary
                        )
                    ) {
                        Text(
                            text = "Close",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reusable Contact Channel Card matching the uploaded reference UI
 */
@Composable
private fun HubContactChannelCard(
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    title: String,
    subtitle: String,
    buttonText: String,
    buttonBgColor: Color,
    buttonBorderColor: Color,
    buttonTextColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF111C33),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Icon + Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBgColor)
                        .border(0.8.dp, iconTintColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTintColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = TextSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Pill Action Button
            Surface(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp),
                color = buttonBgColor,
                border = BorderStroke(1.dp, buttonBorderColor)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = buttonTextColor,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}
