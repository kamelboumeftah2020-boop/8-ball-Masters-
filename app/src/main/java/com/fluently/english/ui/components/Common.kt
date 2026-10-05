package com.fluently.english.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Spellcheck
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.LessonType
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.color

// ---------- Bidirectional text ----------

private val latinRun = Regex("[A-Za-z][A-Za-z0-9 '’/()\\-.,?!…]*[A-Za-z0-9)'’?!.…]|[A-Za-z]")

/**
 * Wraps English runs inside Arabic text in Unicode direction isolates so their
 * punctuation stays attached (otherwise "I am a student." renders as ".I am a student").
 */
fun isolateLatin(text: String): String =
    if (!isArabic(text)) text else latinRun.replace(text) { ltr(it.value) }

/**
 * Embeds a left-to-right string in Arabic text. Uses LRE…PDF plus a trailing LRM
 * (supported by every Android version) so trailing punctuation stays with the
 * English words even when the line wraps.
 */
fun ltr(text: String): String = "\u202A$text\u200E\u202C"

/** True if the text contains Arabic, i.e. it is an Arabic sentence (possibly with English inside). */
fun isArabic(text: String): Boolean = text.any { it in '\u0600'..'\u06FF' }

/** Lays out [content] left-to-right (for English text inside the Arabic UI). */
@Composable
fun Ltr(content: @Composable () -> Unit) =
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr, content = content)

/** Text whose direction follows its language: Arabic right-to-left, English left-to-right. */
@Composable
fun AutoText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
) {
    val direction = if (isArabic(text)) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        Text(
            isolateLatin(text), modifier = modifier.fillMaxWidth(), style = style, color = color,
            fontWeight = fontWeight, textAlign = TextAlign.Start,
        )
    }
}

// ---------- Surfaces ----------

/** Flat card with a hairline border — the basic building block of every screen. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.surface,
    bordered: Boolean = true,
    shape: Shape = MaterialTheme.shapes.medium,
    padding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        shape = shape,
        color = color,
        border = if (bordered) BorderStroke(1.dp, AppTheme.extra.border) else null,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Column(Modifier.padding(padding), content = content)
    }
}

/** Rounded square holding an icon on a tinted background. */
@Composable
fun IconTile(icon: ImageVector, tint: Color, size: Dp = 44.dp, background: Color = tint.copy(alpha = 0.12f)) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(size * 0.32f)).background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

// ---------- Header ----------

/** Minimal screen header: optional back/close button, title and trailing slot. */
@Composable
fun ScreenHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    close: Boolean = false,
    subtitle: String? = null,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            CircleIconButton(if (close) Icons.Rounded.Close else Icons.AutoMirrored.Rounded.ArrowBack, onBack)
            HSpace(10.dp)
        } else {
            HSpace(8.dp)
        }
        Column(Modifier.weight(1f)) {
            Text(isolateLatin(title), style = MaterialTheme.typography.titleLarge, maxLines = 1)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        trailing()
    }
}

@Composable
fun CircleIconButton(icon: ImageVector, onClick: () -> Unit, tint: Color = MaterialTheme.colorScheme.onSurface) {
    Box(
        Modifier
            .size(42.dp)
            .clip(CircleShape)
            .border(1.dp, AppTheme.extra.border, CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // Auto-mirrored icons point right ("back") in the right-to-left layout.
        Icon(icon, contentDescription = "رجوع", tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier.fillMaxWidth().padding(top = 28.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            Text(
                action,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(CircleShape).clickable(onClick = onAction).padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

// ---------- Buttons ----------

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        elevation = null,
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
        if (icon != null) {
            HSpace(8.dp)
            Icon(icon, null, Modifier.size(20.dp))
        }
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.extra.border),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun GhostButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Box(
        modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = color)
    }
}

@Composable
fun SpeakButton(text: String, modifier: Modifier = Modifier, slow: Boolean = false, size: Dp = 40.dp) {
    val speaker = LocalSpeaker.current
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable { speaker.speak(text, if (slow) 0.6f else 1f) },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "استمع",
            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(size * 0.48f),
        )
    }
}

// ---------- Data display ----------

@Composable
fun LevelBadge(level: CefrLevel, size: Dp = 44.dp, locked: Boolean = false, filled: Boolean = false) {
    val c = if (locked) MaterialTheme.colorScheme.outline else level.color()
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(if (filled) c else c.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            level.code, color = if (filled) Color.White else c,
            fontWeight = FontWeight.Bold, fontSize = (size.value * 0.34f).sp,
        )
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    track: Color = MaterialTheme.colorScheme.surfaceVariant,
    stroke: Dp = 8.dp,
    content: @Composable () -> Unit = {},
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val w = stroke.toPx()
            val arcSize = Size(size.width - w, size.height - w)
            val topLeft = Offset(w / 2, w / 2)
            drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(w))
            if (progress > 0f) {
                drawArc(color, -90f, 360f * progress.coerceIn(0f, 1f), false, topLeft, arcSize, style = Stroke(w, cap = StrokeCap.Round))
            }
        }
        content()
    }
}

@Composable
fun LinearMeter(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    track: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: Dp = 6.dp,
) {
    Box(modifier.fillMaxWidth().height(height).clip(CircleShape).background(track)) {
        if (progress > 0f) {
            Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(height).clip(CircleShape).background(color))
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, tint: Color, value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        IconTile(icon, tint, size = 38.dp)
        VSpace(8.dp)
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun Pill(text: String, color: Color, modifier: Modifier = Modifier, icon: ImageVector? = null, solid: Boolean = false) {
    Row(
        modifier
            .clip(CircleShape)
            .background(if (solid) color else color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val fg = if (solid) Color.White else color
        if (icon != null) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(14.dp))
            HSpace(4.dp)
        }
        Text(isolateLatin(text), color = fg, style = MaterialTheme.typography.labelMedium)
    }
}

fun LessonType.icon(): ImageVector = when (this) {
    LessonType.GRAMMAR -> Icons.Rounded.Spellcheck
    LessonType.VOCABULARY -> Icons.Rounded.Translate
    LessonType.READING -> Icons.AutoMirrored.Rounded.MenuBook
    LessonType.LISTENING -> Icons.Rounded.Headphones
}

@Composable
fun HSpace(width: Dp) = Spacer(Modifier.width(width))

@Composable
fun VSpace(height: Dp) = Spacer(Modifier.height(height))
