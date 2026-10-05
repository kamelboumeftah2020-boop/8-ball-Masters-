package com.fluently.english.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fluently.english.data.content.Course
import com.fluently.english.data.content.Lesson
import com.fluently.english.data.content.LessonType
import com.fluently.english.data.progress.ProgressRepository
import com.fluently.english.ui.components.AppTopBar
import com.fluently.english.ui.components.AudioControls
import com.fluently.english.ui.components.AnswerRecord
import com.fluently.english.ui.components.AutoText
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.isolateLatin
import com.fluently.english.ui.components.Ltr
import com.fluently.english.ui.components.MistakesReview
import com.fluently.english.ui.components.Pill
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.QuizRunner
import com.fluently.english.ui.components.ScoreHeader
import com.fluently.english.ui.components.SectionTitle
import com.fluently.english.ui.components.SpeakButton
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.theme.color

private enum class Stage { LEARN, PRACTICE, RESULT }

@Composable
fun LessonScreen(lessonId: String, onComplete: (String, Int, Int) -> Int, onClose: () -> Unit) {
    val lesson = remember(lessonId) { Course.lesson(lessonId) } ?: return
    var stage by remember { mutableStateOf(Stage.LEARN) }
    var attempt by remember { mutableIntStateOf(0) }
    var records by remember { mutableStateOf<List<AnswerRecord>>(emptyList()) }
    var xp by remember { mutableIntStateOf(0) }

    when (stage) {
        Stage.LEARN -> LearnStage(lesson, onClose, onStart = { stage = Stage.PRACTICE })
        Stage.PRACTICE -> key(attempt) {
            QuizRunner(
                questions = lesson.questions,
                instantFeedback = true,
                title = lesson.titleAr,
                onClose = onClose,
                header = lessonHeader(lesson),
                onFinish = {
                    records = it
                    xp = onComplete(lesson.id, it.count { r -> r.correct }, it.size)
                    stage = Stage.RESULT
                },
            )
        }
        Stage.RESULT -> ResultStage(
            lesson, records, xp,
            onRetry = { attempt++; stage = Stage.PRACTICE },
            onReviewLesson = { stage = Stage.LEARN },
            onDone = onClose,
        )
    }
}

/** Keeps the passage available while answering reading / listening questions. */
private fun lessonHeader(lesson: Lesson): (@Composable () -> Unit)? = when (lesson.type) {
    LessonType.READING -> {
        {
            var open by remember { mutableStateOf(false) }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            ) {
                Column(Modifier.padding(12.dp)) {
                    TextButton(onClick = { open = !open }) { Text(if (open) "إخفاء النص ▲" else "عرض النص ▼") }
                    if (open) AutoText(lesson.passage.orEmpty(), Modifier.padding(horizontal = 8.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
    LessonType.LISTENING -> {
        {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("🎧 المقطع الصوتي", style = MaterialTheme.typography.labelLarge)
                    VSpace(8.dp)
                    AudioControls(lesson.passage.orEmpty())
                }
            }
        }
    }
    else -> null
}

@Composable
private fun LearnStage(lesson: Lesson, onClose: () -> Unit, onStart: () -> Unit) {
    val c = lesson.level.color()
    Scaffold(
        topBar = { AppTopBar(lesson.titleAr, onBack = onClose, close = true) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(Modifier.padding(20.dp)) {
                PrimaryButton("ابدأ التمارين (${lesson.questions.size})", onStart)
            }
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pill(lesson.level.code, c)
                HSpace(8.dp)
                Pill("${lesson.type.emoji()} ${lesson.type.labelAr}", MaterialTheme.colorScheme.primary)
            }
            VSpace(10.dp)
            Ltr { Text(lesson.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.fillMaxWidth()) }

            if (lesson.notes.isNotEmpty()) {
                SectionTitle(if (lesson.type == LessonType.GRAMMAR) "الشرح" else "كيف تدرس هذا الدرس")
                lesson.notes.forEachIndexed { i, note ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    ) {
                        Row(Modifier.padding(16.dp)) {
                            Text("${i + 1}", color = c, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            HSpace(12.dp)
                            Text(isolateLatin(note), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            if (lesson.examples.isNotEmpty()) {
                SectionTitle("أمثلة")
                lesson.examples.forEach { ex ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = c.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                AutoText(ex.en, style = MaterialTheme.typography.titleMedium)
                                VSpace(2.dp)
                                AutoText(ex.ar, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            HSpace(8.dp)
                            SpeakButton(ex.en.substringAfter("→ ").trim())
                        }
                    }
                }
            }

            if (lesson.type == LessonType.READING && lesson.passage != null) {
                SectionTitle("النص")
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        AudioControls(lesson.passage)
                        VSpace(12.dp)
                        AutoText(lesson.passage, style = MaterialTheme.typography.bodyLarge.copy(lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.15f))
                    }
                }
            }

            if (lesson.type == LessonType.LISTENING && lesson.passage != null) {
                SectionTitle("المقطع الصوتي")
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("🎧 استمع جيداً — سيظهر النص بعد التمارين", style = MaterialTheme.typography.bodyMedium)
                        VSpace(12.dp)
                        AudioControls(lesson.passage)
                    }
                }
            }

            if (lesson.words.isNotEmpty()) {
                SectionTitle("الكلمات (${lesson.words.size})")
                lesson.words.forEach { word -> WordCard(word.en, word.ar, word.example) }
            }
            VSpace(16.dp)
        }
    }
}

@Composable
fun WordCard(en: String, ar: String, example: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            SpeakButton(en)
            HSpace(12.dp)
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Ltr { Text(en, style = MaterialTheme.typography.titleMedium) }
                    HSpace(10.dp)
                    Text(ar, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
                AutoText(example, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ResultStage(
    lesson: Lesson,
    records: List<AnswerRecord>,
    xp: Int,
    onRetry: () -> Unit,
    onReviewLesson: () -> Unit,
    onDone: () -> Unit,
) {
    val correct = records.count { it.correct }
    val percent = ProgressRepository.percent(correct, records.size)
    val passed = percent >= Course.LESSON_PASS_PERCENT
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            VSpace(20.dp)
            ScoreHeader(
                percent = percent,
                passed = passed,
                title = when {
                    percent == 100 -> "ممتاز! علامة كاملة 🌟"
                    passed -> "أحسنت! أتممت الدرس 🎉"
                    else -> "اقتربت! حاول مرة أخرى 💪"
                },
                subtitle = "$correct من ${records.size} إجابات صحيحة • +$xp نقطة خبرة" +
                    if (!passed) "\nتحتاج ${Course.LESSON_PASS_PERCENT}% لفتح الدرس التالي" else "",
            )
            if (passed && lesson.words.isNotEmpty()) {
                VSpace(12.dp)
                Pill("🧠 أُضيفت ${lesson.words.size} كلمات إلى المراجعة", MaterialTheme.colorScheme.secondary)
            }

            if (lesson.type == LessonType.LISTENING && lesson.passage != null) {
                SectionTitle("نص المقطع — تدرّب بالتظليل (Shadowing)", Modifier.fillMaxWidth())
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "شغّل المقطع وردد الكلام مع المتحدث في نفس الوقت، جملة بجملة.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        VSpace(8.dp)
                        AudioControls(lesson.passage)
                        VSpace(10.dp)
                        AutoText(lesson.passage, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Column(Modifier.fillMaxWidth()) { MistakesReview(records) }

            VSpace(24.dp)
            if (passed) {
                PrimaryButton("متابعة", onDone)
                VSpace(8.dp)
                OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text("أعد التمارين لتحسين نتيجتك") }
            } else {
                PrimaryButton("حاول مرة أخرى", onRetry)
                VSpace(8.dp)
                OutlinedButton(onClick = onReviewLesson, modifier = Modifier.fillMaxWidth()) { Text("راجع الشرح أولاً") }
                TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("الخروج") }
            }
            VSpace(16.dp)
        }
    }
}
