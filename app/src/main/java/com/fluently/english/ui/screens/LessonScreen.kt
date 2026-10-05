package com.fluently.english.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fluently.english.data.content.Course
import com.fluently.english.data.content.Lesson
import com.fluently.english.data.content.LessonType
import com.fluently.english.data.progress.ProgressRepository
import com.fluently.english.ui.components.AnswerRecord
import com.fluently.english.ui.components.AppCard
import com.fluently.english.ui.components.AudioControls
import com.fluently.english.ui.components.AutoText
import com.fluently.english.ui.components.GhostButton
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.IconTile
import com.fluently.english.ui.components.Ltr
import com.fluently.english.ui.components.MistakesReview
import com.fluently.english.ui.components.Pill
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.QuizRunner
import com.fluently.english.ui.components.ScoreHeader
import com.fluently.english.ui.components.ScreenHeader
import com.fluently.english.ui.components.SecondaryButton
import com.fluently.english.ui.components.SectionHeader
import com.fluently.english.ui.components.SpeakButton
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.components.icon
import com.fluently.english.ui.components.isolateLatin
import com.fluently.english.ui.theme.AppTheme
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
            AppCard(color = AppTheme.extra.subtle, bordered = false, padding = 14.dp, modifier = Modifier.padding(bottom = 18.dp)) {
                Row(
                    Modifier.fillMaxWidth().clip(CircleShape).clickable { open = !open },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (open) "إخفاء النص" else "عرض النص",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(if (open) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null, tint = MaterialTheme.colorScheme.primary)
                }
                if (open) {
                    VSpace(8.dp)
                    AutoText(lesson.passage.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
    LessonType.LISTENING -> {
        { ListeningPlayer(lesson.passage.orEmpty(), Modifier.padding(bottom = 18.dp)) }
    }
    else -> null
}

@Composable
private fun ListeningPlayer(script: String, modifier: Modifier = Modifier, note: String? = null) {
    val extra = AppTheme.extra
    Column(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(extra.hero)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(Icons.Rounded.Headphones, MaterialTheme.colorScheme.primary, size = 40.dp, background = extra.heroTrack)
            HSpace(12.dp)
            Column {
                Text("المقطع الصوتي", style = MaterialTheme.typography.titleSmall, color = extra.onHero)
                Text(note ?: "استمع مرتين على الأقل", style = MaterialTheme.typography.bodySmall, color = extra.onHeroMuted)
            }
        }
        VSpace(14.dp)
        AudioControls(script)
    }
}

@Composable
private fun LearnStage(lesson: Lesson, onClose: () -> Unit, onStart: () -> Unit) {
    val c = lesson.level.color()
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeader(lesson.titleAr, onBack = onClose, close = true)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        ) {
            VSpace(8.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pill(lesson.level.code, c, solid = true)
                HSpace(8.dp)
                Pill(lesson.type.labelAr, MaterialTheme.colorScheme.primary, icon = lesson.type.icon())
            }
            VSpace(14.dp)
            Ltr {
                Text(lesson.title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.fillMaxWidth())
            }

            if (lesson.notes.isNotEmpty()) {
                SectionHeader(if (lesson.type == LessonType.GRAMMAR) "الشرح" else "طريقة الدراسة")
                lesson.notes.forEachIndexed { i, note ->
                    Row(Modifier.padding(vertical = 7.dp)) {
                        Box(
                            Modifier.size(28.dp).clip(CircleShape).background(c.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("${i + 1}", color = c, style = MaterialTheme.typography.labelLarge)
                        }
                        HSpace(12.dp)
                        Text(isolateLatin(note), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    }
                }
            }

            if (lesson.examples.isNotEmpty()) {
                SectionHeader("أمثلة")
                AppCard(padding = 0.dp) {
                    lesson.examples.forEachIndexed { i, ex ->
                        if (i > 0) HorizontalDivider(color = AppTheme.extra.border)
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                AutoText(ex.en, style = MaterialTheme.typography.titleMedium)
                                VSpace(2.dp)
                                AutoText(ex.ar, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            HSpace(10.dp)
                            SpeakButton(ex.en.substringAfter("→ ").trim())
                        }
                    }
                }
            }

            if (lesson.type == LessonType.READING && lesson.passage != null) {
                SectionHeader("النص")
                AppCard(padding = 20.dp) {
                    AudioControls(lesson.passage)
                    VSpace(16.dp)
                    AutoText(
                        lesson.passage,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.15f),
                    )
                }
            }

            if (lesson.type == LessonType.LISTENING && lesson.passage != null) {
                SectionHeader("الاستماع")
                ListeningPlayer(lesson.passage, note = "سيظهر النص بعد التمارين")
            }

            if (lesson.words.isNotEmpty()) {
                SectionHeader("الكلمات · ${lesson.words.size}")
                AppCard(padding = 0.dp) {
                    lesson.words.forEachIndexed { i, word ->
                        if (i > 0) HorizontalDivider(color = AppTheme.extra.border)
                        WordRow(word.en, word.ar, word.example)
                    }
                }
            }
            VSpace(20.dp)
        }
        Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
            HorizontalDivider(color = AppTheme.extra.border)
            Box(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                PrimaryButton(
                    "ابدأ التمارين · ${lesson.questions.size}", onStart,
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                )
            }
        }
    }
}

@Composable
fun WordRow(en: String, ar: String, example: String) {
    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Ltr { Text(en, style = MaterialTheme.typography.titleMedium) }
                HSpace(10.dp)
                Text(ar, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            VSpace(2.dp)
            AutoText(example, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HSpace(10.dp)
        SpeakButton(en)
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
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        VSpace(40.dp)
        ScoreHeader(
            percent = percent,
            passed = passed,
            title = when {
                percent == 100 -> "ممتاز! علامة كاملة"
                passed -> "أحسنت، أتممت الدرس"
                else -> "اقتربت! حاول مرة أخرى"
            },
            subtitle = "$correct من ${records.size} إجابات صحيحة · +$xp نقطة" +
                if (!passed) "\nتحتاج ${Course.LESSON_PASS_PERCENT}% لفتح الدرس التالي" else "",
        )
        if (passed && lesson.words.isNotEmpty()) {
            VSpace(14.dp)
            Pill("أُضيفت ${lesson.words.size} كلمات إلى المراجعة", MaterialTheme.colorScheme.primary, icon = Icons.Rounded.Psychology)
        }

        if (lesson.type == LessonType.LISTENING && lesson.passage != null) {
            SectionHeader("النص — تدرّب بالتظليل (Shadowing)")
            ListeningPlayer(lesson.passage, note = "ردّد الكلام مع المتحدث جملةً بجملة")
            VSpace(10.dp)
            AppCard { AutoText(lesson.passage, style = MaterialTheme.typography.bodyMedium) }
        }

        Column(Modifier.fillMaxWidth()) { MistakesReview(records) }

        VSpace(28.dp)
        if (passed) {
            PrimaryButton("متابعة", onDone)
            VSpace(10.dp)
            SecondaryButton("أعد التمارين", onRetry)
        } else {
            PrimaryButton("حاول مرة أخرى", onRetry)
            VSpace(10.dp)
            SecondaryButton("راجع الشرح أولاً", onReviewLesson)
            GhostButton("الخروج", onDone)
        }
        VSpace(24.dp)
    }
}
