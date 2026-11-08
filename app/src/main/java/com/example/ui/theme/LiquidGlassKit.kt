package com.example.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Liquid Glass Kit Design Elements for Jetpack Compose:
 * High-blur frosted glass surfaces, specular light highlights, subtle chromatic borders,
 * and animated dynamic refractive light orbs.
 */

val LiquidGlassDefaultShape = RoundedCornerShape(22.dp)
val LiquidGlassInnerShape = RoundedCornerShape(16.dp)
val LiquidGlassPillShape = RoundedCornerShape(32.dp)

object LiquidGlassBrushes {
    val SpecularBorder = Brush.linearGradient(
        listOf(
            Color(0x73FFFFFF),
            Color(0x3838BDF8),
            Color(0x140F172A),
            Color(0x4DA855F7)
        )
    )

    val SpecularBorderSubtle = Brush.linearGradient(
        listOf(
            Color(0x40FFFFFF),
            Color(0x2038BDF8),
            Color(0x100F172A),
            Color(0x28A855F7)
        )
    )

    val CardBackground = Brush.verticalGradient(
        listOf(
            Color(0x4D1B2D4D),
            Color(0x33101E36),
            Color(0x401C3154)
        )
    )

    val CardBackgroundElevated = Brush.verticalGradient(
        listOf(
            Color(0x66223B64),
            Color(0x47142442),
            Color(0x52233E68)
        )
    )

    val InputBackground = Brush.verticalGradient(
        listOf(
            Color(0x380F1B2E),
            Color(0x260A121F)
        )
    )

    val ErrorGlass = Brush.verticalGradient(
        listOf(
            Color(0x594C0519),
            Color(0x332E0510)
        )
    )

    val ErrorBorder = Brush.linearGradient(
        listOf(
            Color(0x80F43F5E),
            Color(0x40FB7185),
            Color(0x1AE11D48)
        )
    )
}

/**
 * Liquid Glass frosted card background modifier with gradient refraction and specular border.
 */
fun Modifier.liquidGlass(
    shape: Shape = LiquidGlassDefaultShape,
    borderBrush: Brush = LiquidGlassBrushes.SpecularBorder,
    backgroundBrush: Brush = LiquidGlassBrushes.CardBackground,
    borderWidth: Dp = 1.2.dp
): Modifier = this
    .clip(shape)
    .background(backgroundBrush)
    .border(width = borderWidth, brush = borderBrush, shape = shape)

/**
 * Interactive button with vibrant glowing liquid glass pill styling
 */
fun Modifier.liquidGlassButton(
    shape: Shape = LiquidGlassPillShape,
    glowColor: Color = CyanBright,
    isSecondary: Boolean = false
): Modifier = this
    .clip(shape)
    .background(
        if (isSecondary) {
            Brush.horizontalGradient(
                listOf(
                    Color(0x401E293B),
                    Color(0x59334155)
                )
            )
        } else {
            Brush.horizontalGradient(
                listOf(
                    glowColor.copy(alpha = 0.90f),
                    NeonPurple.copy(alpha = 0.85f)
                )
            )
        }
    )
    .border(
        width = 1.2.dp,
        brush = Brush.horizontalGradient(
            listOf(
                Color.White.copy(alpha = 0.7f),
                glowColor.copy(alpha = 0.5f),
                Color.White.copy(alpha = 0.3f)
            )
        ),
        shape = shape
    )

/**
 * Liquid Glass Pill Chip for samples, tags, and quick filters
 */
fun Modifier.liquidGlassChip(
    shape: Shape = LiquidGlassPillShape,
    accentColor: Color = CyanBright,
    isSelected: Boolean = false
): Modifier = this
    .clip(shape)
    .background(
        if (isSelected) {
            Brush.horizontalGradient(
                listOf(
                    accentColor.copy(alpha = 0.28f),
                    NeonPurple.copy(alpha = 0.22f)
                )
            )
        } else {
            Brush.horizontalGradient(
                listOf(
                    Color(0x2B1A2840),
                    Color(0x1F111E30)
                )
            )
        }
    )
    .border(
        width = 1.dp,
        brush = if (isSelected) {
            Brush.horizontalGradient(
                listOf(
                    Color.White.copy(alpha = 0.6f),
                    accentColor.copy(alpha = 0.8f)
                )
            )
        } else {
            Brush.horizontalGradient(
                listOf(
                    Color.White.copy(alpha = 0.25f),
                    accentColor.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.1f)
                )
            )
        },
        shape = shape
    )

/**
 * Fullscreen atmospheric Liquid Glass Ambient Canvas that renders
 * layered floating chromatic gradient orbs behind all screens to provide
 * natural, rich refraction for frosted glass cards.
 */
@Composable
fun LiquidGlassAtmosphere(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glass_orbs")
    val animOffset1 by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1"
    )
    val animOffset2 by infiniteTransition.animateFloat(
        initialValue = 40f,
        targetValue = -70f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .drawBehind {
                val canvasW = size.width
                val canvasH = size.height

                // Orb 1: Cyan/Aquamarine Liquid Glow (Top Right)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CyanBright.copy(alpha = 0.32f),
                            CyanAccent.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        center = Offset(canvasW * 0.82f + animOffset1, canvasH * 0.15f + animOffset2),
                        radius = canvasW * 0.65f
                    ),
                    radius = canvasW * 0.65f,
                    center = Offset(canvasW * 0.82f + animOffset1, canvasH * 0.15f + animOffset2)
                )

                // Orb 2: Neon Purple / Violet Glow (Bottom Left)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonPurple.copy(alpha = 0.28f),
                            CobaltBlue.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = Offset(canvasW * 0.15f + animOffset2, canvasH * 0.72f + animOffset1),
                        radius = canvasW * 0.70f
                    ),
                    radius = canvasW * 0.70f,
                    center = Offset(canvasW * 0.15f + animOffset2, canvasH * 0.72f + animOffset1)
                )

                // Orb 3: Radiant Sapphire Glow (Center Accent)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CobaltBlue.copy(alpha = 0.22f),
                            CyanAccent.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(canvasW * 0.5f - animOffset1, canvasH * 0.42f),
                        radius = canvasW * 0.55f
                    ),
                    radius = canvasW * 0.55f,
                    center = Offset(canvasW * 0.5f - animOffset1, canvasH * 0.42f)
                )
            }
    ) {
        content()
    }
}
