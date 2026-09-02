package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanBright
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

/**
 * Unique Cyber Animated App Startup Loading Screen.
 * Features:
 * - High-tech glowing visual identity with pulsating core
 * - Dynamic Boot Telemetry Steps
 * - Smooth progress bar & speed counter (0% -> 100%)
 * - Privacy Shield & Media Engine verification
 * - Seamless automatic transition into Main Application UI with Fast Skip affordance
 */
@Composable
fun AppLoadingScreen(
    onLoadingComplete: () -> Unit
) {
    var bootStageIndex by remember { mutableIntStateOf(0) }
    val progress = remember { Animatable(0f) }

    val bootStages = listOf(
        "Initializing OmniStream Core Engine...",
        "Calibrating Universal 21+ Media Extractors...",
        "Connecting VIP High-Speed Streaming Pipes...",
        "Activating Privacy Sandbox & Storage Guard...",
        "Engine Synchronized • Ready to Launch!"
    )

    // Infinite glowing rotation & pulsation
    val infiniteTransition = rememberInfiniteTransition(label = "splash_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        // Stage 1
        bootStageIndex = 0
        progress.animateTo(0.25f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        delay(350)

        // Stage 2
        bootStageIndex = 1
        progress.animateTo(0.55f, animationSpec = tween(450, easing = FastOutSlowInEasing))
        delay(350)

        // Stage 3
        bootStageIndex = 2
        progress.animateTo(0.80f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        delay(350)

        // Stage 4
        bootStageIndex = 3
        progress.animateTo(0.95f, animationSpec = tween(350, easing = FastOutSlowInEasing))
        delay(300)

        // Stage 5
        bootStageIndex = 4
        progress.animateTo(1.0f, animationSpec = tween(250, easing = LinearEasing))
        delay(400)

        onLoadingComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .testTag("app_loading_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Subtle Cyber Ambient Radial Glow in background
        Box(
            modifier = Modifier
                .size(340.dp)
                .scale(glowScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            CyanBright.copy(alpha = 0.18f),
                            NeonPurple.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Rotating Cyber Hex Ring + Hero Icon
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(glowScale),
                contentAlignment = Alignment.Center
            ) {
                // Rotating Dashed Border Ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotationAngle)
                        .border(
                            2.dp,
                            Brush.sweepGradient(
                                listOf(
                                    CyanBright,
                                    NeonPurple,
                                    Color(0xFF38BDF8),
                                    CyanBright
                                )
                            ),
                            CircleShape
                        )
                )

                // Inner Glowing Circle
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF020617)
                                )
                            )
                        )
                        .border(1.5.dp, CyanBright.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = "OmniStream Logo",
                        tint = CyanBright,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Brand Name & Subtitle
            Text(
                text = "OMNISTREAM",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 4.sp
                ),
                color = CyanBright
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    tint = NeonPurple,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "NEXT-GEN 8K VIDEO & STUDIO AUDIO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Tech Telemetry Card with Glowing Progress
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CyberDarkSurface,
                border = BorderStroke(1.2.dp, CyberBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Current Status Message
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
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (progress.value >= 1f) EmeraldSuccess else CyanBright)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = bootStages.getOrElse(bootStageIndex) { "Loading..." },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                ),
                                color = if (progress.value >= 1f) EmeraldSuccess else TextPrimary,
                                maxLines = 1
                            )
                        }

                        Text(
                            text = "${(progress.value * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            ),
                            color = CyanAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { progress.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CyanBright,
                        trackColor = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom badges: Privacy Guard & Creator Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "100% Privacy Sandbox",
                                fontSize = 10.sp,
                                color = EmeraldSuccess,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "Lead Dev: MD RASEL",
                            fontSize = 10.sp,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Fast Skip button if user wants to immediately enter
            Surface(
                onClick = onLoadingComplete,
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.8f),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Skip to App",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
