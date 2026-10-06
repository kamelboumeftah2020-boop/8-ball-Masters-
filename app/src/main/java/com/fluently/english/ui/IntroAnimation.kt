package com.fluently.english.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Emerald = Color(0xFF0E8A6A)
private val Gold = Color(0xFFF2B33D)

/**
 * The opening animation, continuing from the system splash: the speech bubble
 * pops in, the three level bars rise one after another, the spark twinkles,
 * the name appears, then everything fades into the app. Tap to skip.
 */
@Composable
fun IntroAnimation(onFinished: () -> Unit) {
    val bubble = remember { Animatable(0.82f) }
    val bars = remember { List(3) { Animatable(0f) } }
    val spark = remember { Animatable(0f) }
    val title = remember { Animatable(0f) }
    val exit = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch { bubble.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow)) }
        delay(180)
        bars.forEachIndexed { i, bar ->
            launch {
                delay(i * 140L)
                bar.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow))
            }
        }
        delay(520)
        launch { spark.animateTo(1f, tween(420, easing = FastOutSlowInEasing)) }
        val t = async { title.animateTo(1f, tween(480, easing = FastOutSlowInEasing)) }
        t.await()
        delay(450)
        exit.animateTo(0f, tween(320))
        onFinished()
    }

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = exit.value }
            .background(Brush.linearGradient(listOf(Color(0xFF16A57F), Color(0xFF0D7A5E), Color(0xFF08503E))))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onFinished() },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(Modifier.size(200.dp)) {
                val u = size.minDimension / 108f
                scale(bubble.value, pivot = center) {
                    drawBubble(u)
                    bars.forEachIndexed { i, bar -> drawBar(u, i, bar.value) }
                }
                drawSpark(u, spark.value)
            }
            Spacer(Modifier.height(4.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = title.value
                    translationY = (1f - title.value) * 24.dp.toPx()
                },
            ) {
                Text("طلاقة", color = Color.White, style = MaterialTheme.typography.displaySmall)
                Text("Fluently", color = Color.White.copy(alpha = 0.75f), fontSize = 16.sp, letterSpacing = 3.sp)
            }
        }
    }
}

/** Same outline as ic_launcher_foreground, in the 108-unit icon space. */
private fun bubblePath(u: Float, dy: Float = 0f) = Path().apply {
    fun p(x: Float, y: Float) = Offset(x * u, (y + dy) * u)
    moveTo(p(40f, 30f).x, p(40f, 30f).y)
    lineTo(p(68f, 30f).x, p(68f, 30f).y)
    quadraticTo(p(80f, 30f).x, p(80f, 30f).y, p(80f, 42f).x, p(80f, 42f).y)
    lineTo(p(80f, 60f).x, p(80f, 60f).y)
    quadraticTo(p(80f, 72f).x, p(80f, 72f).y, p(68f, 72f).x, p(68f, 72f).y)
    lineTo(p(52f, 72f).x, p(52f, 72f).y)
    lineTo(p(39f, 82f).x, p(39f, 82f).y)
    lineTo(p(41f, 72f).x, p(41f, 72f).y)
    lineTo(p(40f, 72f).x, p(40f, 72f).y)
    quadraticTo(p(28f, 72f).x, p(28f, 72f).y, p(28f, 60f).x, p(28f, 60f).y)
    lineTo(p(28f, 42f).x, p(28f, 42f).y)
    quadraticTo(p(28f, 30f).x, p(28f, 30f).y, p(40f, 30f).x, p(40f, 30f).y)
    close()
}

private fun DrawScope.drawBubble(u: Float) {
    drawPath(bubblePath(u, dy = 2f), Color.Black.copy(alpha = 0.15f))
    drawPath(bubblePath(u), Color.White)
}

/** Bar [i] (0..2) grown to [progress] of its height from the shared baseline. */
private fun DrawScope.drawBar(u: Float, i: Int, progress: Float) {
    if (progress <= 0f) return
    val left = floatArrayOf(39f, 50.5f, 62f)[i]
    val top = floatArrayOf(57f, 50f, 42f)[i]
    val bottom = 64f
    val h = (bottom - top) * progress
    drawRoundRect(
        color = if (i == 2) Gold else Emerald,
        topLeft = Offset(left * u, (bottom - h) * u),
        size = Size(7f * u, h * u),
        cornerRadius = CornerRadius(2f * u, 2f * u),
    )
}

/** The four-point spark: scales in with a quarter turn. */
private fun DrawScope.drawSpark(u: Float, progress: Float) {
    if (progress <= 0f) return
    val c = Offset(78f * u, 31f * u)
    val r = 8f * u
    val k = 1.2f * u
    val star = Path().apply {
        moveTo(0f, -r)
        quadraticTo(k, -k, r, 0f)
        quadraticTo(k, k, 0f, r)
        quadraticTo(-k, k, -r, 0f)
        quadraticTo(-k, -k, 0f, -r)
        close()
    }
    translate(c.x, c.y) {
        rotate((1f - progress) * 90f, pivot = Offset.Zero) {
            scale(progress, pivot = Offset.Zero) { drawPath(star, Gold) }
        }
    }
}
