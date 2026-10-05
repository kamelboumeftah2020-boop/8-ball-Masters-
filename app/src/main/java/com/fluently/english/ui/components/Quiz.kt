package com.fluently.english.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Reorder
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fluently.english.data.content.Answers
import com.fluently.english.data.content.Question
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Danger
import com.fluently.english.ui.theme.Success

/** Result of one answered question, used for scoring and the mistakes review. */
data class AnswerRecord(val question: Question, val given: String, val correct: Boolean)

/**
 * Runs a list of questions one after another. With [instantFeedback] the learner
 * sees the right answer after each item (lessons); without it (exams, placement)
 * answers are only reviewed at the end.
 */
@Composable
fun QuizRunner(
    questions: List<Question>,
    instantFeedback: Boolean,
    onClose: () -> Unit,
    onFinish: (List<AnswerRecord>) -> Unit,
    allowSkip: Boolean = false,
    header: (@Composable () -> Unit)? = null,
    contextFor: (Int) -> String? = { null },
    title: String? = null,
) {
    var index by remember(questions) { mutableIntStateOf(0) }
    val records = remember(questions) { mutableStateListOf<AnswerRecord>() }
    val animated by animateFloatAsState(index.toFloat() / questions.size.coerceAtLeast(1), label = "progress")

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding()) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleIconButton(Icons.Rounded.Close, onClose)
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                if (title != null) {
                    Text(
                        isolateLatin(title), style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
                    )
                    VSpace(6.dp)
                }
                LinearMeter(animated, height = 8.dp)
            }
            Text(
                "${index + 1}/${questions.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val question = questions.getOrNull(index) ?: return@Column
        QuestionView(
            question = question,
            key = index,
            instantFeedback = instantFeedback,
            allowSkip = allowSkip,
            context = contextFor(index),
            header = header,
            onNext = { record ->
                records += record
                if (index + 1 >= questions.size) onFinish(records.toList()) else index++
            },
        )
    }
}

@Composable
private fun QuestionView(
    question: Question,
    key: Int,
    instantFeedback: Boolean,
    allowSkip: Boolean,
    context: String?,
    header: (@Composable () -> Unit)?,
    onNext: (AnswerRecord) -> Unit,
) {
    // All per-question state is keyed on the position so it resets for each item.
    var checked by remember(key) { mutableStateOf<AnswerRecord?>(null) }
    var choice by remember(key) { mutableStateOf<String?>(null) }
    val picked = remember(key) { mutableStateListOf<Int>() }
    var typed by remember(key) { mutableStateOf("") }

    val options = remember(key) { (question as? Question.Choice)?.options?.shuffled() ?: emptyList() }
    val tokens = remember(key) { (question as? Question.Order)?.tokens?.withIndex()?.shuffled() ?: emptyList() }
    val speaker = LocalSpeaker.current

    val audio = when (question) {
        is Question.Choice -> question.audio
        is Question.Typing -> question.audio
        is Question.Order -> null
    }
    LaunchedEffect(key) { audio?.let { speaker.speak(it) } }

    val ready = when (question) {
        is Question.Choice -> choice != null
        is Question.Order -> picked.size == tokens.size
        is Question.Typing -> typed.isNotBlank()
    }

    fun evaluate(): AnswerRecord = when (question) {
        is Question.Choice -> AnswerRecord(question, choice.orEmpty(), Answers.checkChoice(question, choice))
        is Question.Order -> {
            val words = picked.map { i -> tokens.first { it.index == i }.value }
            AnswerRecord(question, words.joinToString(" "), Answers.checkOrder(question, words))
        }
        is Question.Typing -> AnswerRecord(question, typed, Answers.checkTyping(question, typed))
    }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            header?.invoke()
            if (context != null) {
                AppCard(color = AppTheme.extra.subtle, bordered = false, modifier = Modifier.padding(bottom = 18.dp)) {
                    AutoText(context, style = MaterialTheme.typography.bodyLarge)
                }
            }
            val (kindIcon, kindLabel) = when (question) {
                is Question.Choice ->
                    if (question.audio != null) Icons.Rounded.Headphones to "استمع ثم أجب"
                    else Icons.Rounded.TaskAlt to "اختر الإجابة الصحيحة"
                is Question.Order -> Icons.Rounded.Reorder to "رتّب الكلمات"
                is Question.Typing -> Icons.Rounded.Edit to "اكتب الإجابة"
            }
            Pill(kindLabel, MaterialTheme.colorScheme.primary, icon = kindIcon)
            VSpace(14.dp)
            AutoText(Answers.prompt(question), style = MaterialTheme.typography.headlineSmall)

            if (audio != null) {
                VSpace(18.dp)
                AudioControls(audio)
            }
            VSpace(24.dp)

            when (question) {
                is Question.Choice -> options.forEachIndexed { i, option ->
                    val state = when {
                        checked == null || !instantFeedback -> if (option == choice) OptionState.Selected else OptionState.Idle
                        option == question.answer -> OptionState.Correct
                        option == choice -> OptionState.Wrong
                        else -> OptionState.Dimmed
                    }
                    OptionCard(('A' + i).toString(), option, state, enabled = checked == null) { choice = option }
                }
                is Question.Order -> OrderBuilder(tokens, picked, enabled = checked == null)
                is Question.Typing -> Ltr {
                    OutlinedTextField(
                        value = typed,
                        onValueChange = { typed = it },
                        enabled = checked == null,
                        singleLine = true,
                        placeholder = { Text("Type your answer…") },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Done,
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = AppTheme.extra.border,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.titleLarge,
                    )
                }
            }
            VSpace(24.dp)
        }

        val result = checked
        if (result != null && instantFeedback) {
            FeedbackPanel(result) { onNext(result) }
        } else {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp)) {
                PrimaryButton(
                    text = if (instantFeedback) "تحقق" else "التالي",
                    enabled = ready,
                    color = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    onClick = {
                        speaker.stop()
                        val record = evaluate()
                        if (instantFeedback) {
                            checked = record
                            if (record.correct && question is Question.Order) speaker.speak(question.sentence)
                        } else {
                            onNext(record)
                        }
                    },
                )
                if (allowSkip) {
                    GhostButton("لا أعرف الإجابة", onClick = { onNext(AnswerRecord(question, "", false)) })
                }
            }
        }
    }
}

@Composable
fun AudioControls(text: String) {
    val speaker = LocalSpeaker.current
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AudioChip(Icons.AutoMirrored.Rounded.VolumeUp, "استمع", primary = true) { speaker.speak(text) }
        AudioChip(Icons.Rounded.SlowMotionVideo, "ببطء", primary = false) { speaker.speak(text, 0.6f) }
    }
}

@Composable
private fun AudioChip(icon: ImageVector, label: String, primary: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .clip(CircleShape)
            .background(if (primary) scheme.primary else scheme.surface)
            .border(1.dp, if (primary) scheme.primary else AppTheme.extra.border, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val fg = if (primary) Color.White else scheme.onSurface
        Icon(icon, null, tint = fg, modifier = Modifier.size(18.dp))
        HSpace(8.dp)
        Text(label, color = fg, style = MaterialTheme.typography.labelLarge)
    }
}

private enum class OptionState { Idle, Selected, Correct, Wrong, Dimmed }

@Composable
private fun OptionCard(letter: String, text: String, state: OptionState, enabled: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val border = AppTheme.extra.border
    val (stroke, container, accent) = when (state) {
        OptionState.Idle, OptionState.Dimmed -> Triple(border, scheme.surface, scheme.onSurfaceVariant)
        OptionState.Selected -> Triple(scheme.primary, scheme.primaryContainer.copy(alpha = 0.45f), scheme.primary)
        OptionState.Correct -> Triple(Success, Success.copy(alpha = 0.10f), Success)
        OptionState.Wrong -> Triple(Danger, Danger.copy(alpha = 0.08f), Danger)
    }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = container,
        border = BorderStroke(if (state == OptionState.Idle || state == OptionState.Dimmed) 1.dp else 1.5.dp, stroke),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .heightIn(min = 60.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Ltr {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                val filled = state != OptionState.Idle && state != OptionState.Dimmed
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (filled) accent else scheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    when (state) {
                        OptionState.Correct -> Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        OptionState.Wrong -> Icon(Icons.Rounded.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        else -> Text(
                            letter, style = MaterialTheme.typography.labelLarge,
                            color = if (filled) Color.White else scheme.onSurfaceVariant,
                        )
                    }
                }
                HSpace(14.dp)
                Box(Modifier.weight(1f)) {
                    AutoText(
                        text,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (state == OptionState.Dimmed) scheme.onSurfaceVariant else Color.Unspecified,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OrderBuilder(tokens: List<IndexedValue<String>>, picked: MutableList<Int>, enabled: Boolean) {
    Ltr {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppTheme.extra.subtle)
                    .padding(12.dp),
            ) {
                if (picked.isEmpty()) {
                    Text(
                        "Tap the words below…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterStart).padding(6.dp),
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    picked.forEach { i ->
                        WordChip(tokens.first { it.index == i }.value, filled = true, enabled = enabled) { picked.remove(i) }
                    }
                }
            }
            VSpace(22.dp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                tokens.forEach { token ->
                    val used = token.index in picked
                    WordChip(token.value, filled = false, enabled = enabled && !used, faded = used) { picked.add(token.index) }
                }
            }
        }
    }
}

@Composable
private fun WordChip(text: String, filled: Boolean, enabled: Boolean, faded: Boolean = false, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = when {
            faded -> scheme.surfaceVariant
            filled -> scheme.surface
            else -> scheme.surface
        },
        border = if (faded) null else BorderStroke(1.dp, if (filled) scheme.primary else AppTheme.extra.border),
        shadowElevation = if (faded) 0.dp else 1.dp,
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(enabled = enabled, onClick = onClick),
    ) {
        Text(
            text,
            color = if (faded) Color.Transparent else if (filled) scheme.primary else scheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
        )
    }
}

@Composable
private fun FeedbackPanel(record: AnswerRecord, onContinue: () -> Unit) {
    val color = if (record.correct) Success else Danger
    AnimatedVisibility(visible = true, enter = slideInVertically { it } + fadeIn()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            border = BorderStroke(1.dp, AppTheme.extra.border),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.navigationBarsPadding().padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(color),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(if (record.correct) Icons.Rounded.Check else Icons.Rounded.Close, null, tint = Color.White)
                    }
                    HSpace(12.dp)
                    Text(
                        if (record.correct) "إجابة صحيحة، أحسنت!" else "ليست صحيحة",
                        color = color, style = MaterialTheme.typography.titleLarge,
                    )
                }
                if (!record.correct) {
                    VSpace(14.dp)
                    Text("الإجابة الصحيحة", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    VSpace(2.dp)
                    AutoText(Answers.correctAnswer(record.question), style = MaterialTheme.typography.titleMedium)
                }
                record.question.explanation?.let {
                    VSpace(10.dp)
                    AppCard(color = AppTheme.extra.subtle, bordered = false, padding = 12.dp) {
                        AutoText(it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                VSpace(16.dp)
                PrimaryButton("متابعة", onContinue, color = color)
            }
        }
    }
}

/** Lists wrong answers with the correct version, shown on result screens. */
@Composable
fun MistakesReview(records: List<AnswerRecord>) {
    val wrong = records.filter { !it.correct }
    if (wrong.isEmpty()) return
    SectionHeader("راجع أخطاءك · ${wrong.size}")
    wrong.forEach { r ->
        AppCard(modifier = Modifier.padding(vertical = 5.dp), padding = 16.dp) {
            AutoText(Answers.prompt(r.question), style = MaterialTheme.typography.titleSmall)
            VSpace(10.dp)
            if (r.given.isNotBlank()) {
                AnswerLine(Icons.Rounded.Close, Danger, r.given)
                VSpace(6.dp)
            }
            AnswerLine(Icons.Rounded.Check, Success, Answers.correctAnswer(r.question))
            r.question.explanation?.let {
                VSpace(8.dp)
                AutoText(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AnswerLine(icon: ImageVector, color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(22.dp).clip(CircleShape).background(color.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        }
        HSpace(10.dp)
        Box(Modifier.weight(1f)) { AutoText(text, style = MaterialTheme.typography.bodyMedium, color = color) }
    }
}

@Composable
fun ScoreHeader(percent: Int, passed: Boolean, title: String, subtitle: String) {
    val animated by animateFloatAsState(percent / 100f, label = "score")
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        ProgressRing(
            progress = animated,
            color = if (passed) Success else MaterialTheme.colorScheme.tertiary,
            stroke = 10.dp,
            modifier = Modifier.size(156.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$percent%", style = MaterialTheme.typography.displaySmall)
                Text("النتيجة", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        VSpace(24.dp)
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        VSpace(6.dp)
        Text(
            subtitle, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
