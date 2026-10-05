package com.fluently.english.ui.components

import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.fluently.english.data.content.Answers
import com.fluently.english.data.content.Example
import com.fluently.english.data.content.Mistake
import com.fluently.english.data.content.Question
import com.fluently.english.data.content.Table
import com.fluently.english.data.content.plain
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Coral
import com.fluently.english.ui.theme.Danger
import com.fluently.english.ui.theme.Emerald
import com.fluently.english.ui.theme.Gold
import com.fluently.english.ui.theme.Success
import kotlinx.coroutines.delay

// ---------- Highlighted text ----------

/** Renders "[...]" segments of [text] in bold accent colour. */
@Composable
fun highlighted(text: String, color: Color = MaterialTheme.colorScheme.primary): AnnotatedString = buildAnnotatedString {
    var inside = false
    val buffer = StringBuilder()
    fun flush() {
        if (buffer.isEmpty()) return
        if (inside) {
            withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold, background = color.copy(alpha = 0.10f))) {
                append(buffer.toString())
            }
        } else {
            append(buffer.toString())
        }
        buffer.clear()
    }
    text.forEach { c ->
        when (c) {
            '[' -> { flush(); inside = true }
            ']' -> { flush(); inside = false }
            else -> buffer.append(c)
        }
    }
    flush()
}

// ---------- Formula ----------

private val FormulaColors = listOf(Emerald, Coral, Gold, Color(0xFF3B7BE0), Color(0xFF6B5BD6))

/** "A + B + C" rendered as coloured building blocks, read left to right like English. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormulaView(formula: String) {
    val parts = formula.split(" + ")
    Ltr {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(AppTheme.extra.subtle)
                .padding(14.dp),
        ) {
            parts.forEachIndexed { i, part ->
                val c = FormulaColors[i % FormulaColors.size]
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(c.copy(alpha = 0.14f))
                        .border(1.dp, c.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(isolateLatin(part), style = MaterialTheme.typography.titleSmall, color = c)
                }
                if (i < parts.lastIndex) {
                    Text(
                        "+", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                }
            }
        }
    }
}

// ---------- Table ----------

@Composable
fun TableView(table: Table) {
    AppCard(padding = 0.dp) {
        Row(Modifier.background(AppTheme.extra.subtle).padding(horizontal = 14.dp, vertical = 10.dp)) {
            table.headers.forEach {
                Text(
                    isolateLatin(it), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f),
                )
            }
        }
        table.rows.forEachIndexed { i, row ->
            if (i > 0) HorizontalDivider(color = AppTheme.extra.border)
            Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                row.forEachIndexed { col, cell ->
                    Box(Modifier.weight(1f)) {
                        AutoText(
                            cell,
                            style = if (col == 0) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                            color = if (col == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

// ---------- Example with hidden translation ----------

/** English example with highlighted key part; the translation is revealed on tap. */
@Composable
fun ExampleCard(example: Example, modifier: Modifier = Modifier) {
    var revealed by remember(example) { mutableStateOf(false) }
    val speakable = example.en.substringAfter("→ ").plain()
    AppCard(modifier = modifier.padding(vertical = 5.dp), onClick = { revealed = !revealed }, padding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Ltr {
                    Text(
                        highlighted(example.en), style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                VSpace(6.dp)
                if (revealed) {
                    AutoText(example.ar, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.TouchApp, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                        HSpace(6.dp)
                        Text("اضغط لترى الترجمة", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
            HSpace(10.dp)
            SpeakButton(speakable)
        }
    }
}

// ---------- Inline quick check ----------

/** A single multiple-choice question with instant feedback, used inside explanations. */
@Composable
fun CheckCard(question: Question.Choice, onAnswered: (Boolean) -> Unit) {
    val options = remember(question) { question.options.shuffled() }
    var chosen by remember(question) { mutableStateOf<String?>(null) }
    val onAnsweredState by rememberUpdatedState(onAnswered)
    AppCard(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f), bordered = false) {
        Pill("سؤال سريع", MaterialTheme.colorScheme.primary, icon = Icons.Rounded.TouchApp)
        VSpace(10.dp)
        AutoText(question.prompt, style = MaterialTheme.typography.titleLarge)
        VSpace(12.dp)
        Ltr {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                options.forEach { option ->
                    val state = when {
                        chosen == null -> 0
                        option == question.answer -> 1
                        option == chosen -> 2
                        else -> 3
                    }
                    val bg by animateColorAsState(
                        when (state) {
                            1 -> Success
                            2 -> Danger
                            else -> MaterialTheme.colorScheme.surface
                        }, label = "check",
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(bg)
                            .border(1.dp, if (state == 0 || state == 3) AppTheme.extra.border else Color.Transparent, RoundedCornerShape(14.dp))
                            .clickable(enabled = chosen == null) {
                                chosen = option
                                onAnsweredState(option == question.answer)
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            option, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleSmall,
                            color = if (state == 1 || state == 2) Color.White else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
        AnimatedVisibility(chosen != null, enter = expandVertically() + fadeIn()) {
            Column {
                VSpace(12.dp)
                val ok = chosen == question.answer
                Text(
                    if (ok) "صحيح! 👏" else "الإجابة الصحيحة: ${ltr(question.answer)}",
                    style = MaterialTheme.typography.titleSmall, color = if (ok) Success else Danger,
                )
                question.explanation?.let {
                    VSpace(4.dp)
                    AutoText(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ---------- Mistakes ----------

@Composable
fun MistakeCard(mistake: Mistake) {
    AppCard(modifier = Modifier.padding(vertical = 6.dp), padding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MarkCircle(false)
            HSpace(10.dp)
            Ltr {
                Text(
                    mistake.wrong, style = MaterialTheme.typography.titleMedium, color = Danger,
                    textDecoration = TextDecoration.LineThrough, modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        VSpace(8.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            MarkCircle(true)
            HSpace(10.dp)
            Ltr { Text(mistake.right, style = MaterialTheme.typography.titleMedium, color = Success, modifier = Modifier.fillMaxWidth()) }
        }
        VSpace(10.dp)
        Text(isolateLatin(mistake.why), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MarkCircle(ok: Boolean) {
    val c = if (ok) Success else Danger
    Box(Modifier.size(26.dp).clip(CircleShape).background(c.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
        Icon(if (ok) Icons.Rounded.Check else Icons.Rounded.Close, null, tint = c, modifier = Modifier.size(16.dp))
    }
}

// ---------- Matching game ----------

/**
 * Tap an English word, then its Arabic meaning. Correct pairs lock in green;
 * wrong pairs flash red. [onComplete] receives the number of mistakes.
 */
@Composable
fun MatchBoard(pairs: List<Pair<String, String>>, enabled: Boolean = true, onComplete: (Int) -> Unit) {
    val left = remember(pairs) { pairs.map { it.first }.shuffled() }
    val right = remember(pairs) { pairs.map { it.second }.shuffled() }
    var pickedLeft by remember(pairs) { mutableStateOf<String?>(null) }
    var pickedRight by remember(pairs) { mutableStateOf<String?>(null) }
    val matched = remember(pairs) { mutableStateListOf<String>() }
    var wrong by remember(pairs) { mutableStateOf<Pair<String, String>?>(null) }
    var mistakes by remember(pairs) { mutableIntStateOf(0) }
    val speaker = LocalSpeaker.current
    val onCompleteState by rememberUpdatedState(onComplete)

    fun tryMatch() {
        val l = pickedLeft ?: return
        val r = pickedRight ?: return
        if (pairs.any { it.first == l && it.second == r }) {
            matched += l
            if (matched.size == pairs.size) onCompleteState(mistakes)
        } else {
            mistakes++
            wrong = l to r
        }
        pickedLeft = null
        pickedRight = null
    }

    LaunchedEffect(wrong) {
        if (wrong != null) {
            delay(650)
            wrong = null
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // Arabic column first: in the right-to-left layout it sits on the right.
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            right.forEach { ar ->
                val done = pairs.any { it.second == ar && it.first in matched }
                MatchTile(
                    text = ar,
                    state = when {
                        done -> TileState.Done
                        wrong?.second == ar -> TileState.Wrong
                        pickedRight == ar -> TileState.Picked
                        else -> TileState.Idle
                    },
                    enabled = enabled && !done,
                ) {
                    pickedRight = ar
                    tryMatch()
                }
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            left.forEach { en ->
                val done = en in matched
                MatchTile(
                    text = en,
                    state = when {
                        done -> TileState.Done
                        wrong?.first == en -> TileState.Wrong
                        pickedLeft == en -> TileState.Picked
                        else -> TileState.Idle
                    },
                    enabled = enabled && !done,
                ) {
                    speaker.speak(en)
                    pickedLeft = en
                    tryMatch()
                }
            }
        }
    }
}

private enum class TileState { Idle, Picked, Wrong, Done }

@Composable
private fun MatchTile(text: String, state: TileState, enabled: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val (bg, stroke, fg) = when (state) {
        TileState.Idle -> Triple(scheme.surface, AppTheme.extra.border, scheme.onSurface)
        TileState.Picked -> Triple(scheme.primaryContainer, scheme.primary, scheme.primary)
        TileState.Wrong -> Triple(Danger.copy(alpha = 0.10f), Danger, Danger)
        TileState.Done -> Triple(Success.copy(alpha = 0.10f), Success.copy(alpha = 0.4f), Success)
    }
    val bgAnim by animateColorAsState(bg, label = "tile")
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgAnim,
        border = BorderStroke(1.5.dp, stroke),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Box(Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
            Text(isolateLatin(text), style = MaterialTheme.typography.titleSmall, color = fg, textAlign = TextAlign.Center)
        }
    }
}

// ---------- Speaking practice ----------

class SpeechInput(val available: Boolean, val start: () -> Unit)

/** Launches the system speech recogniser (English) and returns the candidates heard. */
@Composable
fun rememberSpeechInput(onResult: (List<String>) -> Unit): SpeechInput {
    val context = LocalContext.current
    val onResultState by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        onResultState(res.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).orEmpty())
    }
    val available = remember {
        runCatching { SpeechRecognizer.isRecognitionAvailable(context) }.getOrDefault(false)
    }
    return remember(available) {
        SpeechInput(available) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                .putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now…")
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            runCatching { launcher.launch(intent) }.onFailure { onResultState(emptyList()) }
        }
    }
}

/**
 * Listen → speak → get a score. Used for speaking questions and for practising
 * new words. [onScored] receives the best match score (0..1) and what was heard.
 */
@Composable
fun SpeakPractice(target: String, compact: Boolean = false, onScored: (Float, String) -> Unit = { _, _ -> }) {
    var heard by remember(target) { mutableStateOf<String?>(null) }
    var score by remember(target) { mutableStateOf(0f) }
    val onScoredState by rememberUpdatedState(onScored)
    val input = rememberSpeechInput { results ->
        if (results.isEmpty()) {
            heard = ""
            score = 0f
        } else {
            val best = results.maxBy { Answers.speechScore(target, it) }
            heard = best
            score = Answers.speechScore(target, best)
        }
        onScoredState(score, heard.orEmpty())
    }
    val animated by animateFloatAsState(score, tween(600), label = "speech")

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        if (!input.available) {
            Text(
                "التعرف على الصوت غير متاح على هذا الجهاز. استمع وكرر بصوت عالٍ بنفسك.",
                style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }
        Box(
            Modifier
                .size(if (compact) 56.dp else 76.dp)
                .clip(CircleShape)
                .background(Coral)
                .clickable { input.start() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Mic, "تحدث", tint = Color.White, modifier = Modifier.size(if (compact) 28.dp else 36.dp))
        }
        VSpace(8.dp)
        Text(
            if (heard == null) "اضغط على الميكروفون وانطق" else "حاول مرة أخرى إن أردت",
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val said = heard
        if (said != null) {
            VSpace(12.dp)
            val pass = score >= Answers.SPEECH_PASS
            val c = if (pass) Success else Coral
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(c.copy(alpha = 0.10f))
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    when {
                        said.isBlank() -> "لم أسمع شيئاً، حاول مرة أخرى"
                        pass -> "نطق رائع! ${(score * 100).toInt()}%"
                        else -> "قريب! ${(score * 100).toInt()}% — استمع وحاول مجدداً"
                    },
                    style = MaterialTheme.typography.titleSmall, color = c,
                )
                if (said.isNotBlank()) {
                    VSpace(4.dp)
                    Ltr { Text("“$said”", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                }
                VSpace(8.dp)
                LinearMeter(animated, color = c)
            }
        }
    }
}

// ---------- Flip card ----------

/** A card that flips on tap to reveal its back side. */
@Composable
fun FlipCard(
    modifier: Modifier = Modifier,
    flipped: Boolean,
    onFlip: () -> Unit,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
) {
    val rotation by animateFloatAsState(if (flipped) 180f else 0f, tween(450), label = "flip")
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = if (rotation > 90f) AppTheme.extra.hero else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, AppTheme.extra.border),
        shadowElevation = 2.dp,
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14 * density
            }
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onFlip),
    ) {
        Box(
            Modifier.fillMaxSize().padding(24.dp).graphicsLayer { rotationY = if (rotation > 90f) 180f else 0f },
            contentAlignment = Alignment.Center,
        ) {
            if (rotation > 90f) back() else front()
        }
    }
}

/** Splits a passage into sentences for sentence-by-sentence playback. */
fun sentencesOf(text: String): List<String> =
    text.split(Regex("(?<=[.!?])\\s+")).map { it.trim() }.filter { it.isNotEmpty() }

// ---------- Interactive text ----------

private fun words(text: String) = text.lowercase().split(Regex("[^a-z']+")).filter { it.isNotBlank() }

/** Finds the glossary entry closest to [segment] (handles "[ran out of]" vs "run out of"). */
private fun lookup(segment: String, glossary: Map<String, String>): Pair<String, String>? {
    glossary.entries.firstOrNull { it.key.equals(segment, true) }?.let { return it.key to it.value }
    glossary.entries.firstOrNull { segment.startsWith(it.key, true) || it.key.startsWith(segment, true) }
        ?.let { return it.key to it.value }
    val seg = words(segment)
    return glossary.entries
        .map { e -> e to words(e.key).count { it in seg }.toFloat() / words(e.key).size.coerceAtLeast(1) }
        .filter { it.second >= 0.5f }
        .maxByOrNull { it.second }
        ?.let { it.first.key to it.first.value }
}

/**
 * Reading text you can touch: tap a highlighted word to see its meaning, or any
 * other part of a sentence to hear that sentence. Words are highlighted either
 * where the text marks them with [brackets] or wherever a [glossary] term occurs.
 */
@Composable
fun InteractiveText(
    text: String,
    glossary: Map<String, String>,
    modifier: Modifier = Modifier,
    /** Sentence being read aloud (read-along highlight), or -1. */
    playing: Int = -1,
    fontScale: Float = 1f,
    /** When set, the word popup offers "add to my cards". */
    onAddWord: ((String) -> Unit)? = null,
) {
    val speaker = LocalSpeaker.current
    val accent = MaterialTheme.colorScheme.primary
    val bracketMode = '[' in text
    val sentences = remember(text) { sentencesOf(text) }
    var activeSentence by remember(text) { mutableStateOf(-1) }
    var selected by remember(text) { mutableStateOf<Pair<String, String?>?>(null) }
    val termRegex = remember(glossary) {
        glossary.keys.sortedByDescending { it.length }
            .joinToString("|") { "\\b" + Regex.escape(it) + "\\b" }
            .takeIf { it.isNotEmpty() }
            ?.let { Regex(it, RegexOption.IGNORE_CASE) }
    }

    val annotated = buildAnnotatedString {
        sentences.forEachIndexed { si, sentence ->
            // Split the sentence into (text, isTerm) pieces.
            val pieces = mutableListOf<Pair<String, Boolean>>()
            if (bracketMode) {
                var inside = false
                val buf = StringBuilder()
                sentence.forEach { c ->
                    when (c) {
                        '[' -> { if (buf.isNotEmpty()) pieces += buf.toString() to false; buf.clear(); inside = true }
                        ']' -> { if (buf.isNotEmpty()) pieces += buf.toString() to true; buf.clear(); inside = false }
                        else -> buf.append(c)
                    }
                }
                if (buf.isNotEmpty()) pieces += buf.toString() to inside
            } else {
                var last = 0
                termRegex?.findAll(sentence)?.forEach { m ->
                    if (m.range.first > last) pieces += sentence.substring(last, m.range.first) to false
                    pieces += m.value to true
                    last = m.range.last + 1
                }
                if (last < sentence.length) pieces += sentence.substring(last) to false
            }
            val plainSentence = sentence.replace("[", "").replace("]", "")
            pieces.forEachIndexed { pi, (piece, isTerm) ->
                if (isTerm) {
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "t$si-$pi",
                            styles = TextLinkStyles(
                                style = SpanStyle(color = accent, fontWeight = FontWeight.Bold, background = accent.copy(alpha = 0.10f)),
                            ),
                        ) {
                            val found = lookup(piece, glossary)
                            selected = piece to found?.second
                            speaker.speak(found?.first ?: piece)
                        },
                    ) { append(piece) }
                } else {
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "s$si-$pi",
                            styles = TextLinkStyles(
                                style = when (si) {
                                    playing -> SpanStyle(background = accent.copy(alpha = 0.22f))
                                    activeSentence -> SpanStyle(background = accent.copy(alpha = 0.08f))
                                    else -> SpanStyle()
                                },
                            ),
                        ) {
                            activeSentence = si
                            speaker.speak(plainSentence)
                        },
                    ) { append(piece) }
                }
            }
            if (si < sentences.lastIndex) append(" ")
        }
    }

    Column(modifier) {
        Ltr {
            Text(
                annotated,
                style = MaterialTheme.typography.bodyLarge.let {
                    it.copy(fontSize = it.fontSize * fontScale, lineHeight = it.lineHeight * 1.25f * fontScale)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        val sel = selected
        AnimatedVisibility(sel != null, enter = expandVertically() + fadeIn()) {
            if (sel != null) {
                Row(
                    Modifier
                        .padding(top = 14.dp)
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Ltr { Text(sel.first, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.fillMaxWidth()) }
                        Text(
                            sel.second ?: "اضغط على الصوت لسماعها",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    if (onAddWord != null) {
                        var added by remember(sel) { mutableStateOf(false) }
                        Box(
                            Modifier.padding(end = 8.dp).clip(RoundedCornerShape(12.dp))
                                .background(if (added) Success else MaterialTheme.colorScheme.primary)
                                .clickable(enabled = !added) {
                                    onAddWord(lookup(sel.first, glossary)?.first ?: sel.first.lowercase())
                                    added = true
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        ) {
                            Text(if (added) "أُضيفت ✓" else "+ بطاقاتي", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        }
                    }
                    SpeakButton(sel.first)
                }
            }
        }
    }
}
