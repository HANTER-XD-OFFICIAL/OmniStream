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

    // Multi-color iridescent prismatic border matching Liquid Glass Kit reference with crisp specular white rim
    val PrismaticCausticBorder = Brush.linearGradient(
        listOf(
            Color(0xF5FFFFFF), // Ultra-crisp specular pure white top-left highlight
            Color(0x8038BDF8), // Refined cyan optical refraction
            Color(0x662DD4BF), // Soft turquoise flare
            Color(0x73C084FC), // Lavender / violet prismatic stop
            Color(0x4DEC4899), // Subtle rose magenta
            Color(0x1F0F172A), // Shadow bevel
            Color(0x6638BDF8)  // Subtle bottom-right return
        )
    )

    // Elevated frosted glass surfaces with high-blur frosted white sheen + deep obsidian crystal base
    val CardBackground = Brush.verticalGradient(
        listOf(
            Color(0x3BFFFFFF), // High-intensity frosted white sheen (23% white overlay)
            Color(0x38162844), // Translucent high-refraction frosted glass
            Color(0xEB08101E)  // Deep obsidian foundation for maximum text contrast
        )
    )

    // Aurora iridescent frosted card background with refined subtlety
    val CardBackgroundAurora = Brush.verticalGradient(
        listOf(
            Color(0x40FFFFFF), // Specular white gloss top
            Color(0x331E2E4E), // Translucent indigo
            Color(0x2E281E44), // Translucent amethyst
            Color(0xEB08101E)  // Deep obsidian foundation
        )
    )

    val CardBackgroundElevated = Brush.verticalGradient(
        listOf(
            Color(0x45FFFFFF), // Elevated frosted white highlight
            Color(0x3D1B2E4E), // High-refraction crystal core
            Color(0xF008101E)  // Deep rich backing
        )
    )

    val ModalGlassBackground = Brush.verticalGradient(
        listOf(
            Color(0x4DFFFFFF), // Crisp frosted top specular reflection
            Color(0xF5081120), // Deep obsidian high-opacity frosted glass for dialog legibility
            Color(0xFA040810)  // Solid true-black anchor
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
 * Liquid Glass frosted card background modifier with high-blur diffusion,
 * specular white sheen, and refined low-intensity ambient glow.
 */
fun Modifier.liquidGlass(
    shape: Shape = LiquidGlassDefaultShape,
    borderBrush: Brush = LiquidGlassBrushes.PrismaticCausticBorder,
    backgroundBrush: Brush = LiquidGlassBrushes.CardBackground,
    borderWidth: Dp = 1.4.dp,
    ambientGlowColor: Color? = CyanBright
): Modifier = this
    .drawBehind {
        // Refined, subtle ambient caustic drop glow (significantly reduced intensity for clean separation)
        val glowColor = ambientGlowColor ?: CyanBright
        val glowRadius = size.maxDimension * 0.45f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor.copy(alpha = 0.08f),
                    NeonPurple.copy(alpha = 0.04f),
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
 * Interactive button with vibrant frosted liquid glass pill styling and refined glow
 */
fun Modifier.liquidGlassButton(
    shape: Shape = LiquidGlassPillShape,
    glowColor: Color = CyanBright,
    isSecondary: Boolean = false
): Modifier = this
    .drawBehind {
        // Refined button caustic drop glow
        val glowRadius = size.maxDimension * 0.45f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor.copy(alpha = 0.16f),
                    NeonPurple.copy(alpha = 0.08f),
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
                    Color(0x381E293B),
                    Color(0x550F172A)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.50f), // Top specular gloss line
                    glowColor.copy(alpha = 0.35f),  // Translucent glowing liquid tint
                    Color(0xDD081120)               // Frosted deep obsidian core
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
 * Deep True Black canvas for Liquid Glass UI.
 * Strictly enforces pure #000000 true black dark mode without gradient washes or grey tones,
 * ensuring all frosted glass cards, glowing specular borders, and floating navigation bar pop brightly with high contrast.
 */
@Composable
fun LiquidGlassAtmosphere(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        content()
    }
}

