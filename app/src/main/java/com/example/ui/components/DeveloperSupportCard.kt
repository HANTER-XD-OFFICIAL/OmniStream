package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun DeveloperSupportCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isDetailsRevealed by remember { mutableStateOf(false) }

    val devName = "MD RASEL"
    val devRole = "Lead Developer & System Architect"
    val devEmail = "alexraselchodhury@gmail.com"
    val devFacebook = "https://www.facebook.com/md.rasel.7.8.2.3.4"
    val devWhatsApp = "+8801882278234"
    val devTelegram = "https://t.me/HANTER_XD_OFFICIAL"

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

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

    fun openEmail(email: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, "OmniStream - Developer Support Request")
                putExtra(Intent.EXTRA_TEXT, "Hi MD RASEL,\n\nI need assistance with OmniStream Video Downloader.\n\n")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            openUrl("mailto:$email")
        }
    }

    fun openWhatsApp(phone: String) {
        try {
            val cleanPhone = phone.replace("+", "").replace(" ", "").replace("-", "")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanPhone?text=${Uri.encode("Hello MD RASEL, I am contacting you regarding OmniStream App Support.")}")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            copyToClipboard("WhatsApp Number", phone)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("developer_support_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
        border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(CyanBright, NeonPurple, EmeraldSuccess)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Developer Profile with Verified Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Avatar with Cyber Gradient Ring
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(CyanBright, NeonPurple, Color(0xFF38BDF8))
                                )
                            )
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(CyberDarkSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = CyanBright,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = devName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                ),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Lead Developer",
                                tint = CyanBright,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = devRole,
                            style = MaterialTheme.typography.bodySmall,
                            color = CyanAccent,
                            fontSize = 11.sp
                        )

                        Text(
                            text = "OmniStream VIP Core Engine Maintainer",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Privacy Shield Toggle
                Surface(
                    onClick = { isDetailsRevealed = !isDetailsRevealed },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDetailsRevealed) NeonPurple.copy(alpha = 0.2f) else CyberDarkSurface,
                    border = BorderStroke(1.dp, if (isDetailsRevealed) NeonPurple else CyberBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDetailsRevealed) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Privacy",
                            tint = if (isDetailsRevealed) NeonPurple else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isDetailsRevealed) "VISIBLE" else "MASKED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDetailsRevealed) NeonPurple else TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Developer Support Commitment Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, CyberBorder.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Official 24/7 Developer Support • Tap any channel below for instant direct contact",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }

            // Contact Channels List (Masked by default with one-tap action & copy)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // 1. WhatsApp Channel
                DeveloperContactItem(
                    icon = Icons.Default.Chat,
                    iconColor = Color(0xFF25D366),
                    label = "WhatsApp Official",
                    displayValue = if (isDetailsRevealed) devWhatsApp else "+880 188 ••• •234",
                    realValue = devWhatsApp,
                    actionButtonText = "Chat on WhatsApp",
                    onActionClick = { openWhatsApp(devWhatsApp) },
                    onCopyClick = { copyToClipboard("WhatsApp", devWhatsApp) }
                )

                // 2. Telegram Channel
                DeveloperContactItem(
                    icon = Icons.Default.Send,
                    iconColor = Color(0xFF229ED9),
                    label = "Telegram Support",
                    displayValue = if (isDetailsRevealed) "t.me/HANTER_XD_OFFICIAL" else "t.me/HANTER_••••••••",
                    realValue = "@HANTER_XD_OFFICIAL",
                    actionButtonText = "Open Telegram",
                    onActionClick = { openUrl(devTelegram) },
                    onCopyClick = { copyToClipboard("Telegram ID", "HANTER_XD_OFFICIAL") }
                )

                // 3. Facebook Channel
                DeveloperContactItem(
                    icon = Icons.Default.Public,
                    iconColor = Color(0xFF1877F2),
                    label = "Facebook Profile",
                    displayValue = if (isDetailsRevealed) "md.rasel.7.8.2.3.4" else "facebook.com/md.rasel••••",
                    realValue = devFacebook,
                    actionButtonText = "View Profile",
                    onActionClick = { openUrl(devFacebook) },
                    onCopyClick = { copyToClipboard("Facebook Profile", devFacebook) }
                )

                // 4. Gmail Official
                DeveloperContactItem(
                    icon = Icons.Default.Email,
                    iconColor = Color(0xFFEA4335),
                    label = "Support Email (Gmail)",
                    displayValue = if (isDetailsRevealed) devEmail else "al•••••••••••@gmail.com",
                    realValue = devEmail,
                    actionButtonText = "Send Email",
                    onActionClick = { openEmail(devEmail) },
                    onCopyClick = { copyToClipboard("Gmail", devEmail) }
                )
            }

            // Quick 1-Click Action Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { openWhatsApp(devWhatsApp) },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.Black
                    )
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { openUrl(devTelegram) },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF229ED9),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Telegram", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { openUrl(devFacebook) },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1877F2),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Facebook", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DeveloperContactItem(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    displayValue: String,
    realValue: String,
    actionButtonText: String,
    onActionClick: () -> Unit,
    onCopyClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CyberDarkSurface,
        border = BorderStroke(1.dp, CyberBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f))
                        .border(1.dp, iconColor.copy(alpha = 0.5f), CircleShape),
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

                Column {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = displayValue,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onCopyClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Surface(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(8.dp),
                    color = iconColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, iconColor.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Open",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = iconColor
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}
