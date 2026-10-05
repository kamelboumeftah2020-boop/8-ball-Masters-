package com.fluently.english.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fluently.english.data.content.CefrLevel

val Indigo = Color(0xFF4F46E5)
val IndigoDeep = Color(0xFF3730A3)
val Teal = Color(0xFF0EA5A4)
val Amber = Color(0xFFF59E0B)
val Success = Color(0xFF16A34A)
val Danger = Color(0xFFDC2626)

private val Light = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF134E4A),
    tertiary = Amber,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF78350F),
    background = Color(0xFFF7F7FB),
    onBackground = Color(0xFF111827),
    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF1F2F7),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB),
    error = Danger,
)

private val Dark = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = Color(0xFFFBBF24),
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = Color(0xFF0B0F19),
    onBackground = Color(0xFFE5E7EB),
    surface = Color(0xFF151A26),
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFF1F2533),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF374151),
    outlineVariant = Color(0xFF283041),
    error = Color(0xFFF87171),
)

private val AppTypography = Typography().run {
    copy(
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.Bold),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 26.sp),
        bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 22.sp),
    )
}

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
)

/** One colour per CEFR level, used for badges, cards and progress. */
fun CefrLevel.color(): Color = when (this) {
    CefrLevel.A1 -> Color(0xFF22C55E)
    CefrLevel.A2 -> Color(0xFF14B8A6)
    CefrLevel.B1 -> Color(0xFF3B82F6)
    CefrLevel.B2 -> Color(0xFF6366F1)
    CefrLevel.C1 -> Color(0xFFA855F7)
    CefrLevel.C2 -> Color(0xFFE11D48)
}

@Composable
fun FluentlyTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (dark) Dark else Light
    MaterialTheme(colorScheme = colors, typography = AppTypography, shapes = AppShapes, content = content)
}
