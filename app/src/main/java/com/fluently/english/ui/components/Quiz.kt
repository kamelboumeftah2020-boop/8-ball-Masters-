package com.fluently.english.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fluently.english.data.content.Answers
import com.fluently.english.data.content.Question
import com.fluently.english.tts.LocalSpeaker
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

    Scaffold(
        topBar = {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, "إغلاق") }
                Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                    if (title != null) {
                        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        VSpace(4.dp)
                    }
                    LinearMeter((index.toFloat()) / questions.size.coerceAtLeast(1))
                }
                Text(
                    "${index + 1}/${questions.size}",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
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
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            header?.invoke()
            if (context != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                ) {
                    AutoText(context, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                }
            }
            val heading = when (question) {
                is Question.Choice -> if (question.audio != null) "استمع ثم أجب" else "اختر الإجابة الصحيحة"
                is Question.Order -> "رتّب الكلمات لتكوين جملة صحيحة"
                is Question.Typing -> "اكتب الإجابة"
            }
            Text(heading, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            VSpace(8.dp)
            AutoText(Answers.prompt(question), style = MaterialTheme.typography.titleLarge)

            if (audio != null) {
                VSpace(16.dp)
                AudioControls(audio)
            }
            VSpace(20.dp)

            when (question) {
                is Question.Choice -> options.forEach { option ->
                    val state = when {
                        checked == null || !instantFeedback -> if (option == choice) OptionState.Selected else OptionState.Idle
                        option == question.answer -> OptionState.Correct
                        option == choice -> OptionState.Wrong
                        else -> OptionState.Idle
                    }
                    OptionCard(option, state, enabled = checked == null) { choice = option }
                }
                is Question.Order -> OrderBuilder(tokens, picked, enabled = checked == null)
                is Question.Typing -> Ltr {
                    OutlinedTextField(
                        value = typed,
                        onValueChange = { typed = it },
                        enabled = checked == null,
                        singleLine = true,
                        placeholder = { Text("Type here…") },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Done,
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            VSpace(24.dp)
        }

        val result = checked
        if (result != null && instantFeedback) {
            FeedbackPanel(result) { onNext(result) }
        } else {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp)) {
                PrimaryButton(
                    text = if (instantFeedback) "تحقق" else "التالي",
                    enabled = ready,
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
                    TextButton(
                        onClick = { onNext(AnswerRecord(question, "", false)) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("لا أعرف الإجابة") }
                }
            }
        }
    }
}

@Composable
fun AudioControls(text: String) {
    val speaker = LocalSpeaker.current
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FilledTonalButton(onClick = { speaker.speak(text) }) {
            Icon(Icons.AutoMirrored.Rounded.VolumeUp, null, Modifier.size(20.dp))
            HSpace(8.dp)
            Text("استمع")
        }
        FilledTonalButton(onClick = { speaker.speak(text, 0.6f) }) {
            Icon(Icons.Rounded.SlowMotionVideo, null, Modifier.size(20.dp))
            HSpace(8.dp)
            Text("ببطء")
        }
    }
}

private enum class OptionState { Idle, Selected, Correct, Wrong }

@Composable
private fun OptionCard(text: String, state: OptionState, enabled: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val (border, container) = when (state) {
        OptionState.Idle -> scheme.outlineVariant to scheme.surface
        OptionState.Selected -> scheme.primary to scheme.primaryContainer.copy(alpha = 0.5f)
        OptionState.Correct -> Success to Success.copy(alpha = 0.12f)
        OptionState.Wrong -> Danger to Danger.copy(alpha = 0.12f)
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = container,
        border = BorderStroke(if (state == OptionState.Idle) 1.dp else 2.dp, border),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .heightIn(min = 56.dp)
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Box(Modifier.padding(horizontal = 18.dp, vertical = 16.dp), contentAlignment = Alignment.CenterStart) {
            AutoText(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OrderBuilder(tokens: List<IndexedValue<String>>, picked: MutableList<Int>, enabled: Boolean) {
    Ltr {
        Column {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
            ) {
                FlowRow(
                    Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    picked.forEach { i ->
                        WordChip(tokens.first { it.index == i }.value, filled = true, enabled = enabled) { picked.remove(i) }
                    }
                }
            }
            VSpace(20.dp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
            filled -> scheme.primaryContainer
            else -> scheme.surface
        },
        border = if (faded) null else BorderStroke(1.dp, if (filled) scheme.primary else scheme.outline),
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
    ) {
        Text(
            text,
            color = if (faded) Color.Transparent else scheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun FeedbackPanel(record: AnswerRecord, onContinue: () -> Unit) {
    val color = if (record.correct) Success else Danger
    AnimatedVisibility(visible = true, enter = slideInVertically { it }) {
        Surface(color = color.copy(alpha = 0.12f), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.navigationBarsPadding().padding(20.dp)) {
                Text(
                    if (record.correct) "أحسنت! إجابة صحيحة 🎉" else "ليست صحيحة",
                    color = color, style = MaterialTheme.typography.titleLarge,
                )
                if (!record.correct) {
                    VSpace(6.dp)
                    Text("الإجابة الصحيحة:", style = MaterialTheme.typography.labelLarge, color = color)
                    AutoText(Answers.correctAnswer(record.question), style = MaterialTheme.typography.titleMedium)
                }
                record.question.explanation?.let {
                    VSpace(6.dp)
                    AutoText(it, style = MaterialTheme.typography.bodyMedium)
                }
                VSpace(14.dp)
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
    SectionTitle("راجع أخطاءك (${wrong.size})")
    wrong.forEach { r ->
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        ) {
            Column(Modifier.padding(16.dp)) {
                AutoText(Answers.prompt(r.question), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                VSpace(6.dp)
                if (r.given.isNotBlank()) {
                    AutoText("✗  ${r.given}", style = MaterialTheme.typography.bodyMedium, color = Danger)
                }
                AutoText("✓  ${Answers.correctAnswer(r.question)}", style = MaterialTheme.typography.bodyMedium, color = Success)
                r.question.explanation?.let {
                    VSpace(4.dp)
                    AutoText(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun ScoreHeader(percent: Int, passed: Boolean, title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        ProgressRing(
            progress = percent / 100f,
            color = if (passed) Success else MaterialTheme.colorScheme.tertiary,
            stroke = 12.dp,
            modifier = Modifier.size(150.dp),
        ) {
            Text("$percent%", style = MaterialTheme.typography.headlineLarge)
        }
        VSpace(20.dp)
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        VSpace(6.dp)
        Text(
            subtitle, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
