package com.fluently.english.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.Course
import com.fluently.english.data.content.Lesson
import com.fluently.english.data.content.LessonType
import com.fluently.english.data.progress.Progress
import com.fluently.english.ui.components.AppTopBar
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.ltr
import com.fluently.english.ui.components.LinearMeter
import com.fluently.english.ui.components.Pill
import com.fluently.english.ui.components.SectionTitle
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.theme.Success
import com.fluently.english.ui.theme.color

@Composable
fun PathScreen(progress: Progress, onOpenLevel: (CefrLevel) -> Unit) {
    Scaffold(topBar = { AppTopBar("مسار التعلم") }, containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        ) {
            item {
                Text(
                    "ست مراحل وفق الإطار الأوروبي المرجعي (CEFR). أكمل الدروس ثم اجتز امتحان المستوى لتفتح المستوى التالي.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            CefrLevel.entries.groupBy { it.stageAr }.forEach { (stage, levels) ->
                item { SectionTitle(stage) }
                items(levels) { level ->
                    LevelCard(level, progress, onClick = { onOpenLevel(level) })
                    VSpace(12.dp)
                }
            }
        }
    }
}

@Composable
fun LevelScreen(
    level: CefrLevel,
    progress: Progress,
    onBack: () -> Unit,
    onOpenLesson: (String) -> Unit,
    onOpenExam: () -> Unit,
) {
    val course = Course.level(level)
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize()) {
            item { LevelHeader(level, progress, onBack) }
            course.units.forEachIndexed { index, unit ->
                item {
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        SectionTitle("الوحدة ${index + 1}: ${unit.titleAr}")
                    }
                }
                items(unit.lessons) { lesson ->
                    LessonRow(lesson, progress, onClick = { onOpenLesson(lesson.id) })
                }
            }
            item {
                Column(Modifier.padding(20.dp)) {
                    ExamCard(level, progress, onOpenExam)
                }
            }
        }
    }
}

@Composable
private fun LevelHeader(level: CefrLevel, progress: Progress, onBack: () -> Unit) {
    val c = level.color()
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Brush.linearGradient(listOf(c, c.copy(alpha = 0.75f))))
            .padding(20.dp),
    ) {
        Column {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack, "رجوع", tint = Color.White,
                modifier = Modifier.clip(CircleShape).clickable(onClick = onBack).padding(6.dp),
            )
            VSpace(12.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(level.code, fontSize = 44.sp, fontWeight = FontWeight.Black, color = Color.White)
                HSpace(14.dp)
                Column {
                    Text(level.titleAr, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    Text(level.stageAr, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
                }
            }
            VSpace(12.dp)
            Text(level.canDoAr, style = MaterialTheme.typography.bodyMedium, color = Color.White)
            VSpace(14.dp)
            Row {
                Pill("Cambridge: ${level.cambridge}", Color.White)
                HSpace(8.dp)
                Pill("IELTS ${level.ielts}", Color.White)
            }
            VSpace(14.dp)
            LinearMeter(progress.levelProgress(level), color = Color.White)
        }
    }
}

@Composable
private fun LessonRow(lesson: Lesson, progress: Progress, onClick: () -> Unit) {
    val unlocked = progress.isLessonUnlocked(lesson)
    val score = progress.lessonScores[lesson.id]
    val done = progress.isLessonDone(lesson.id)
    val c = lesson.level.color()
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = unlocked, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(
                when {
                    done -> Success
                    unlocked -> c.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
            ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                done -> Icon(Icons.Rounded.Check, null, tint = Color.White)
                !unlocked -> Icon(Icons.Rounded.Lock, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                else -> Text(lesson.type.emoji(), fontSize = 20.sp)
            }
        }
        HSpace(14.dp)
        Column(Modifier.weight(1f)) {
            Text(
                lesson.titleAr, style = MaterialTheme.typography.titleMedium,
                color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
            )
            Text(
                "${lesson.type.labelAr} • ${ltr(lesson.title)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (score != null) Pill("$score%", if (done) Success else MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
private fun ExamCard(level: CefrLevel, progress: Progress, onOpenExam: () -> Unit) {
    val unlocked = progress.isExamUnlocked(level)
    val best = progress.examScores[level]
    val passedExam = (best ?: 0) >= Course.EXAM_PASS_PERCENT
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) level.color().copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth().clip(CardDefaults.shape).clickable(enabled = unlocked, onClick = onOpenExam),
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (passedExam) "🏅" else if (unlocked) "🎓" else "🔒", fontSize = 34.sp)
            HSpace(14.dp)
            Column(Modifier.weight(1f)) {
                Text("امتحان المستوى ${level.code}", style = MaterialTheme.typography.titleMedium)
                Text(
                    when {
                        passedExam -> "حصلت على الشهادة بنتيجة $best%"
                        unlocked -> "${level.cambridge} — النجاح 70%"
                        else -> "أكمل جميع دروس المستوى لفتح الامتحان"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

fun LessonType.emoji() = when (this) {
    LessonType.GRAMMAR -> "📐"
    LessonType.VOCABULARY -> "🔤"
    LessonType.READING -> "📖"
    LessonType.LISTENING -> "🎧"
}
