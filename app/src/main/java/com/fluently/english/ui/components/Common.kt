package com.fluently.english.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.ui.theme.color

private val latinRun = Regex("[A-Za-z][A-Za-z0-9 '’/()\\-.,?!]*[A-Za-z0-9)'’?!.]|[A-Za-z]")

/**
 * Wraps English runs inside Arabic text in Unicode direction isolates so their
 * punctuation stays attached (otherwise "I am a student." renders as ".I am a student").
 */
fun isolateLatin(text: String): String =
    if (!isArabic(text)) text else latinRun.replace(text) { "\u2066${it.value}\u2069" }

/** Isolates a left-to-right string for embedding in Arabic text. */
fun ltr(text: String): String = "\u2066$text\u2069"

fun isArabic(text: String): Boolean = text.firstOrNull { it.isLetter() }?.let { it in '؀'..'ۿ' } ?: false

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

@Composable
fun SpeakButton(text: String, modifier: Modifier = Modifier, slow: Boolean = false, size: Dp = 40.dp) {
    val speaker = LocalSpeaker.current
    FilledTonalIconButton(
        onClick = { speaker.speak(text, if (slow) 0.6f else 1f) },
        modifier = modifier.size(size),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "استمع", modifier = Modifier.size(size * 0.5f))
    }
}

@Composable
fun LevelBadge(level: CefrLevel, size: Dp = 44.dp, locked: Boolean = false) {
    val c = if (locked) MaterialTheme.colorScheme.outline else level.color()
    Box(
        modifier = Modifier.size(size).clip(RoundedCornerShape(size * 0.3f)).background(c.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(level.code, color = c, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.36f).sp)
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
            drawArc(color, -90f, 360f * progress.coerceIn(0f, 1f), false, topLeft, arcSize, style = Stroke(w, cap = StrokeCap.Round))
        }
        content()
    }
}

@Composable
fun LinearMeter(progress: Float, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary, height: Dp = 8.dp) {
    Box(
        modifier.fillMaxWidth().height(height).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(height).clip(CircleShape).background(color))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String, onBack: (() -> Unit)? = null, close: Boolean = false, actions: @Composable () -> Unit = {}) {
    TopAppBar(
        title = { Text(isolateLatin(title), style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(if (close) Icons.Rounded.Close else Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "رجوع")
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, color: Color? = null) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = if (color != null) ButtonDefaults.buttonColors(containerColor = color) else ButtonDefaults.buttonColors(),
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text, style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(top = 20.dp, bottom = 10.dp),
    )
}

@Composable
fun StatItem(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun Pill(text: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier.clip(CircleShape).background(color.copy(alpha = 0.14f)).padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun HSpace(width: Dp) = Spacer(Modifier.width(width))

@Composable
fun VSpace(height: Dp) = Spacer(Modifier.height(height))
