package com.fluently.english.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fluently.english.R
import com.fluently.english.data.content.CefrLevel

/*
 * "Ink & Emerald": a calm, editorial palette — warm paper background, deep ink
 * for hero surfaces, one emerald accent, and two warm highlights (coral for the
 * streak, gold for XP). Everything else stays neutral so content leads.
 */

val Emerald = Color(0xFF0E8A6A)
val EmeraldSoft = Color(0xFFDDF1EA)
val Ink = Color(0xFF15171C)
val InkSoft = Color(0xFF262A32)
val Coral = Color(0xFFF2643D)
val Gold = Color(0xFFE9A92B)
val Success = Color(0xFF16A06B)
val Danger = Color(0xFFE0483E)

@Immutable
data class ExtraColors(
    val hero: Color,
    val onHero: Color,
    val onHeroMuted: Color,
    val heroTrack: Color,
    val border: Color,
    val subtle: Color,
)

private val LightExtra = ExtraColors(
    hero = Ink,
    onHero = Color.White,
    onHeroMuted = Color(0xFFA9AEB8),
    heroTrack = Color(0xFF30343D),
    border = Color(0xFFE7E4DE),
    subtle = Color(0xFFF1EFEA),
)

private val DarkExtra = ExtraColors(
    hero = Color(0xFF1C2027),
    onHero = Color.White,
    onHeroMuted = Color(0xFF9097A3),
    heroTrack = Color(0xFF2D323B),
    border = Color(0xFF272B33),
    subtle = Color(0xFF1B1E24),
)

val LocalExtraColors = staticCompositionLocalOf { LightExtra }

object AppTheme {
    val extra: ExtraColors @Composable get() = LocalExtraColors.current
}

private val Light = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    primaryContainer = EmeraldSoft,
    onPrimaryContainer = Color(0xFF053A2C),
    secondary = Ink,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDEBE6),
    onSecondaryContainer = Ink,
    tertiary = Gold,
    tertiaryContainer = Color(0xFFFBF0D9),
    onTertiaryContainer = Color(0xFF5C3F05),
    background = Color(0xFFF7F5F1),
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF1EFEA),
    onSurfaceVariant = Color(0xFF6E7380),
    outline = Color(0xFFC9C6BF),
    outlineVariant = Color(0xFFE7E4DE),
    error = Danger,
)

private val Dark = darkColorScheme(
    primary = Color(0xFF3CC79E),
    onPrimary = Color(0xFF00281D),
    primaryContainer = Color(0xFF0F3B30),
    onPrimaryContainer = Color(0xFFBDEFDD),
    secondary = Color(0xFFE8E9EC),
    onSecondary = Ink,
    secondaryContainer = Color(0xFF23272E),
    onSecondaryContainer = Color(0xFFE8E9EC),
    tertiary = Color(0xFFF2BE55),
    tertiaryContainer = Color(0xFF3A2C0D),
    onTertiaryContainer = Color(0xFFF8E3B5),
    background = Color(0xFF0F1115),
    onBackground = Color(0xFFE8E9EC),
    surface = Color(0xFF171A1F),
    onSurface = Color(0xFFE8E9EC),
    surfaceVariant = Color(0xFF1F232A),
    onSurfaceVariant = Color(0xFF959BA6),
    outline = Color(0xFF3A3F48),
    outlineVariant = Color(0xFF272B33),
    error = Color(0xFFFF6B61),
)

val Plex = FontFamily(
    Font(R.font.plex_arabic_regular, FontWeight.Normal),
    Font(R.font.plex_arabic_medium, FontWeight.Medium),
    Font(R.font.plex_arabic_semibold, FontWeight.SemiBold),
    Font(R.font.plex_arabic_bold, FontWeight.Bold),
)

private fun style(size: Int, line: Int, weight: FontWeight, tracking: Double = 0.0) = TextStyle(
    fontFamily = Plex, fontSize = size.sp, lineHeight = line.sp, fontWeight = weight, letterSpacing = tracking.sp,
)

private val AppTypography = Typography(
    displayLarge = style(56, 64, FontWeight.Bold, -1.0),
    displayMedium = style(44, 52, FontWeight.Bold, -0.5),
    displaySmall = style(36, 44, FontWeight.Bold),
    headlineLarge = style(30, 40, FontWeight.Bold),
    headlineMedium = style(26, 36, FontWeight.Bold),
    headlineSmall = style(22, 32, FontWeight.Bold),
    titleLarge = style(19, 28, FontWeight.SemiBold),
    titleMedium = style(16, 24, FontWeight.SemiBold),
    titleSmall = style(14, 20, FontWeight.SemiBold),
    bodyLarge = style(16, 27, FontWeight.Normal),
    bodyMedium = style(14, 23, FontWeight.Normal),
    bodySmall = style(12, 19, FontWeight.Normal),
    labelLarge = style(14, 20, FontWeight.Medium),
    labelMedium = style(12, 16, FontWeight.Medium),
    labelSmall = style(11, 16, FontWeight.Medium),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** One harmonised colour per CEFR level. */
fun CefrLevel.color(): Color = when (this) {
    CefrLevel.A1 -> Color(0xFF16A06B)
    CefrLevel.A2 -> Color(0xFF0E94A0)
    CefrLevel.B1 -> Color(0xFF3B7BE0)
    CefrLevel.B2 -> Color(0xFF6B5BD6)
    CefrLevel.C1 -> Color(0xFFB04FA0)
    CefrLevel.C2 -> Color(0xFFE0533D)
}

@Composable
fun FluentlyTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalExtraColors provides if (dark) DarkExtra else LightExtra) {
        MaterialTheme(
            colorScheme = if (dark) Dark else Light,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
