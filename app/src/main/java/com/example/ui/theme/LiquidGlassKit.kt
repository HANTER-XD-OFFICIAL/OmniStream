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

val LiquidGlassDefaultShape = RoundedCornerShape(24.dp)
val LiquidGlassInnerShape = RoundedCornerShape(16.dp)
val LiquidGlassPillShape = RoundedCornerShape(32.dp)
val LiquidGlassModalShape = RoundedCornerShape(28.dp)

object LiquidGlassBrushes {
    // Distinct soft specular border with chromatic refraction highlights
    val SpecularBorder = Brush.linearGradient(
        listOf(
            Color(0x8CFFFFFF), // Crisp specular white edge
            Color(0x5938BDF8), // Cyan glow refraction
            Color(0x1F0F172A), // Shadow bevel
            Color(0x59C084FC)  // Soft purple chromatic edge
        )
    )

    val SpecularBorderSubtle = Brush.linearGradient(
        listOf(
            Color(0x59FFFFFF),
            Color(0x3338BDF8),
            Color(0x140F172A),
            Color(0x38A855F7)
        )
    )

    val GlowingBorderCyan = Brush.linearGradient(
        listOf(
            Color(0x99FFFFFF),
            CyanBright,
            Color(0x260F172A),
            CyanBright.copy(alpha = 0.75f)
        )
    )

    val GlowingBorderPurple = Brush.linearGradient(
        listOf(
            Color(0x99FFFFFF),
            NeonPurple,
            Color(0x260F172A),
            NeonPurple.copy(alpha = 0.75f)
        )
    )

    val GlowingBorderEmerald = Brush.linearGradient(
        listOf(
            Color(0x99FFFFFF),
            EmeraldSuccess,
            Color(0x260F172A),
            EmeraldSuccess.copy(alpha = 0.75f)
        )
    )

    // Multi-color iridescent prismatic border matching Liquid Glass Kit reference
    val PrismaticCausticBorder = Brush.linearGradient(
        listOf(
            Color(0xE6FFFFFF), // Specular crisp white top-left highlight
            Color(0x9938BDF8), // Cyan optical refraction
            Color(0x732DD4BF), // Turquoise caustic flare
            Color(0x8CC084FC), // Lavender / violet prismatic stop
            Color(0x66F472B6), // Soft rose magenta
            Color(0x260F172A), // Shadow bevel
            Color(0x8038BDF8)  // Bottom-right rim return
        )
    )

    // True frosted glass surfaces with low-alpha white overlay + translucent dark sapphire base
    val CardBackground = Brush.verticalGradient(
        listOf(
            Color(0x2EFFFFFF), // Specular low-alpha white frosted overlay (18%)
            Color(0x3D14243D), // Semi-transparent sapphire glass
            Color(0x4D0C1628)  // Deep atmospheric base
        )
    )

    // Aurora iridescent frosted card background matching bottom-right card in reference image
    val CardBackgroundAurora = Brush.verticalGradient(
        listOf(
            Color(0x33FFFFFF), // Specular white gloss top
            Color(0x401E2E4E), // Translucent indigo
            Color(0x33281E44), // Translucent amethyst
            Color(0x400C1628)  // Dark sapphire foundation
        )
    )

    val CardBackgroundElevated = Brush.verticalGradient(
        listOf(
            Color(0x3DFFFFFF), // Elevated low-alpha white highlight
            Color(0x521D3254), // Ambient glow core
            Color(0x5C13223A)  // Deep rich backing
        )
    )

    val ModalGlassBackground = Brush.verticalGradient(
        listOf(
            Color(0x40FFFFFF), // Top specular reflection overlay
            Color(0xF00B1528), // High opacity frosted glass for dialog legibility
            Color(0xF5070E1C)  // Dark foundation
        )
    )

    val InputBackground = Brush.verticalGradient(
        listOf(
            Color(0x3D0F1C30),
            Color(0x290A1322)
        )
    )

    val ErrorGlass = Brush.verticalGradient(
        listOf(
            Color(0x40FFFFFF),
            Color(0x594C0519),
            Color(0x4D2E0510)
        )
    )

    val ErrorBorder = Brush.linearGradient(
        listOf(
            Color(0x99FFFFFF),
            Color(0x80F43F5E),
            Color(0x33E11D48),
            Color(0x66FB7185)
        )
    )
}

/**
 * Liquid Glass frosted card background modifier with gradient refraction,
 * low-alpha white overlay, and distinct specular border matching the Liquid Glass Kit.
 */
fun Modifier.liquidGlass(
    shape: Shape = LiquidGlassDefaultShape,
    borderBrush: Brush = LiquidGlassBrushes.PrismaticCausticBorder,
    backgroundBrush: Brush = LiquidGlassBrushes.CardBackground,
    borderWidth: Dp = 1.4.dp,
    ambientGlowColor: Color? = CyanBright
): Modifier = this
    .drawBehind {
        // Volumetric radial caustic drop glow matching Liquid Glass Kit reference
        val glowColor = ambientGlowColor ?: CyanBright
        val glowRadius = size.maxDimension * 0.70f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor.copy(alpha = 0.35f),
                    NeonPurple.copy(alpha = 0.18f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.5f, size.height * 0.70f),
                radius = glowRadius
            ),
            radius = glowRadius,
            center = Offset(size.width * 0.5f, size.height * 0.70f)
        )
    }
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
    .drawBehind {
        val glowRadius = size.maxDimension * 0.65f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor.copy(alpha = 0.55f),
                    NeonPurple.copy(alpha = 0.28f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.5f, size.height * 0.85f),
                radius = glowRadius
            ),
            radius = glowRadius,
            center = Offset(size.width * 0.5f, size.height * 0.85f)
        )
    }
    .clip(shape)
    .background(
        if (isSecondary) {
            Brush.verticalGradient(
                listOf(
                    Color(0x4DFFFFFF),
                    Color(0x4D1E293B),
                    Color(0x66334155)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.45f), // Top specular gloss line
                    glowColor.copy(alpha = 0.95f),
                    glowColor.copy(alpha = 0.80f)
                )
            )
        }
    )
    .border(
        width = 1.4.dp,
        brush = Brush.horizontalGradient(
            listOf(
                Color.White.copy(alpha = 0.92f),
                glowColor.copy(alpha = 0.75f),
                Color.White.copy(alpha = 0.45f),
                NeonPurple.copy(alpha = 0.65f)
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
                    accentColor.copy(alpha = 0.32f),
                    NeonPurple.copy(alpha = 0.25f)
                )
            )
        } else {
            Brush.horizontalGradient(
                listOf(
                    Color(0x331E2E4A),
                    Color(0x24111D30)
                )
            )
        }
    )
    .border(
        width = 1.1.dp,
        brush = if (isSelected) {
            Brush.horizontalGradient(
                listOf(
                    Color.White.copy(alpha = 0.7f),
                    accentColor.copy(alpha = 0.85f),
                    Color.White.copy(alpha = 0.3f)
                )
            )
        } else {
            Brush.horizontalGradient(
                listOf(
                    Color.White.copy(alpha = 0.35f),
                    accentColor.copy(alpha = 0.45f),
                    Color.White.copy(alpha = 0.15f)
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

                // Orb 4: Rose Peach / Sunset Magenta Caustic Glow (Bottom Right, matching reference kit)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFB7185).copy(alpha = 0.28f),
                            Color(0xFFFDA4AF).copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = Offset(canvasW * 0.85f - animOffset2, canvasH * 0.85f - animOffset1),
                        radius = canvasW * 0.65f
                    ),
                    radius = canvasW * 0.65f,
                    center = Offset(canvasW * 0.85f - animOffset2, canvasH * 0.85f - animOffset1)
                )
            }
    ) {
        content()
    }
}
