package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.border
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyanBright
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.LiquidGlassBrushes
import com.example.ui.theme.LiquidGlassModalShape
import com.example.ui.theme.LiquidGlassInnerShape
import com.example.ui.theme.LiquidGlassPillShape
import com.example.ui.theme.liquidGlass
import com.example.ui.theme.liquidGlassButton

object StoragePermissionHelper {

    fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        return permissions.toTypedArray()
    }

    fun hasStoragePermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val video = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
            val audio = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
            return video || audio
        } else {
            val write = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            val read = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            return write && read
        }
    }
}

@Composable
fun StoragePermissionDialog(
    onDismiss: () -> Unit,
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    var permissionDeniedCount by remember { mutableStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = StoragePermissionHelper.hasStoragePermission(context)
        if (granted) {
            onPermissionGranted()
            onDismiss()
        } else {
            permissionDeniedCount++
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 480.dp)
                .liquidGlass(
                    shape = LiquidGlassModalShape,
                    borderBrush = LiquidGlassBrushes.PrismaticCausticBorder,
                    backgroundBrush = LiquidGlassBrushes.ModalGlassBackground,
                    ambientGlowColor = CyanBright
                )
                .padding(20.dp)
                .testTag("storage_permission_dialog")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x3314223A))
                            .border(1.2.dp, LiquidGlassBrushes.PrismaticCausticBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = CyanBright,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Storage Permission Required",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Direct Media Storage Access",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Text(
                    text = "OmniStream needs storage access to save downloaded videos & audio directly into your phone's internal storage under the 'Download/OmniStream' folder.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                Surface(
                    shape = LiquidGlassInnerShape,
                    color = Color(0x3314223A),
                    border = BorderStroke(1.dp, LiquidGlassBrushes.SpecularBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Direct Internal Storage (/Download/OmniStream)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            "Downloaded media will be visible directly in your Gallery, VLC, MX Player & File Manager.",
                            color = TextMuted,
                            fontSize = 11.5.sp
                        )
                    }
                }

                if (permissionDeniedCount > 0) {
                    Surface(
                        shape = LiquidGlassInnerShape,
                        color = Color(0x33EF4444),
                        border = BorderStroke(1.dp, Color(0x80EF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Permission is necessary to download files to device storage. Please allow in settings if blocked.",
                                color = Color(0xFFFCA5A5),
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = LiquidGlassPillShape,
                        border = BorderStroke(1.dp, LiquidGlassBrushes.SpecularBorderSubtle),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("Cancel", color = TextSecondary, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            if (permissionDeniedCount >= 2) {
                                try {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            } else {
                                permissionLauncher.launch(StoragePermissionHelper.getRequiredPermissions())
                            }
                        },
                        shape = LiquidGlassPillShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .liquidGlassButton(shape = LiquidGlassPillShape, glowColor = CyanBright)
                            .testTag("grant_permission_button")
                    ) {
                        Text(
                            text = if (permissionDeniedCount >= 2) "Open Settings" else "Grant Permission",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
