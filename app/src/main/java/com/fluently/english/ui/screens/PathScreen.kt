package com.fluently.english.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.Course
import com.fluently.english.data.content.Lesson
import com.fluently.english.data.progress.Progress
import com.fluently.english.ui.components.AppCard
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.LevelBadge
import com.fluently.english.ui.components.LinearMeter
import com.fluently.english.ui.components.Pill
import com.fluently.english.ui.components.ScreenHeader
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.components.icon
import com.fluently.english.ui.components.ltr
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Gold
import com.fluently.english.ui.theme.Success
import com.fluently.english.ui.theme.color

/** A vertical rail with a node, drawn beside each timeline entry. */
@Composable
private fun TimelineRow(
    first: Boolean,
    last: Boolean,
    railColorTop: Color,
    railColorBottom: Color,
    railWidth: Dp = 52.dp,
    node: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(Modifier.width(railWidth).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.width(2.dp).height(14.dp).background(if (first) Color.Transparent else railColorTop))
            node()
            Box(Modifier.width(2.dp).weight(1f).background(if (last) Color.Transparent else railColorBottom))
        }
        HSpace(12.dp)
        Box(Modifier.weight(1f).padding(bottom = 14.dp)) { content() }
    }
}

@Composable
fun PathScreen(progress: Progress, onOpenLevel: (CefrLevel) -> Unit) {
    val rail = AppTheme.extra.border
    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            ScreenHeader("مسار التعلم", subtitle = "ستة مستويات وفق الإطار الأوروبي CEFR")
            VSpace(12.dp)
        }
        itemsIndexed(CefrLevel.entries) { i, level ->
            val unlocked = progress.isLevelUnlocked(level)
            val passed = progress.isLevelPassed(level)
            val current = level == progress.currentLevel && !passed
            Box(Modifier.padding(horizontal = 20.dp)) {
                TimelineRow(
                    first = i == 0,
                    last = i == CefrLevel.entries.lastIndex,
                    railColorTop = if (unlocked) level.color().copy(alpha = 0.5f) else rail,
                    railColorBottom = if (passed) level.color().copy(alpha = 0.5f) else rail,
                    node = { LevelBadge(level, size = 52.dp, locked = !unlocked, filled = passed || current) },
                ) {
                    LevelCard(level, progress, current, onClick = { onOpenLevel(level) })
                }
            }
        }
    }
}

@Composable
fun LevelCard(level: CefrLevel, progress: Progress, current: Boolean, onClick: () -> Unit) {
    val unlocked = progress.isLevelUnlocked(level)
    val passed = progress.isLevelPassed(level)
    val course = Course.level(level)
    val done = course.lessons.count { progress.isLessonDone(it.id) }
    AppCard(
        onClick = if (unlocked) onClick else null,
        padding = 16.dp,
        color = if (unlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                level.titleAr, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f),
                color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            when {
                passed -> Pill("مكتمل", Success, icon = Icons.Rounded.Check)
                current -> Pill("الحالي", level.color(), solid = true)
                !unlocked -> Icon(Icons.Rounded.Lock, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
            }
        }
        Text(
            "${level.stageAr} · ${ltr(level.cambridge)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (unlocked) {
            VSpace(12.dp)
            LinearMeter(progress.levelProgress(level), color = level.color())
            VSpace(6.dp)
            Text(
                "$done / ${course.lessons.size} درس",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
    val rail = AppTheme.extra.border
    val c = level.color()
    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 28.dp),
    ) {
        item {
            ScreenHeader("المستوى ${level.code}", onBack = onBack)
            Box(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) { LevelSummary(level, progress) }
        }
        course.units.forEachIndexed { u, unit ->
            item {
                Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 10.dp)) {
                    Text("الوحدة ${u + 1}", style = MaterialTheme.typography.labelMedium, color = c)
                    Text(unit.titleAr, style = MaterialTheme.typography.titleLarge)
                }
            }
            itemsIndexed(unit.lessons) { i, lesson ->
                val done = progress.isLessonDone(lesson.id)
                Box(Modifier.padding(horizontal = 20.dp)) {
                    TimelineRow(
                        first = i == 0,
                        last = i == unit.lessons.lastIndex,
                        railColorTop = rail,
                        railColorBottom = if (done) c.copy(alpha = 0.45f) else rail,
                        railWidth = 44.dp,
                        node = { LessonNode(lesson, progress) },
                    ) {
                        LessonRow(lesson, progress, onClick = { onOpenLesson(lesson.id) })
                    }
                }
            }
        }
        item {
            Box(Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp)) { ExamCard(level, progress, onOpenExam) }
        }
    }
}

@Composable
private fun LevelSummary(level: CefrLevel, progress: Progress) {
    val extra = AppTheme.extra
    Column(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(extra.hero)
            .padding(22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LevelBadge(level, size = 58.dp, filled = true)
            HSpace(14.dp)
            Column {
                Text(level.titleAr, style = MaterialTheme.typography.headlineSmall, color = extra.onHero)
                Text(level.stageAr, style = MaterialTheme.typography.bodySmall, color = extra.onHeroMuted)
            }
        }
        VSpace(16.dp)
        Text(level.canDoAr, style = MaterialTheme.typography.bodyMedium, color = extra.onHero)
        VSpace(16.dp)
        Row {
            Equivalence("Cambridge", level.cambridge, Modifier.weight(1.4f))
            Equivalence("IELTS", level.ielts, Modifier.weight(1f))
            Equivalence("TOEFL", level.toefl, Modifier.weight(0.8f))
        }
        VSpace(16.dp)
        LinearMeter(progress.levelProgress(level), color = level.color(), track = extra.heroTrack)
    }
}

@Composable
private fun Equivalence(label: String, value: String, modifier: Modifier) {
    val extra = AppTheme.extra
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = extra.onHeroMuted)
        Text(ltr(value), style = MaterialTheme.typography.labelLarge, color = extra.onHero, maxLines = 2)
    }
}

@Composable
private fun LessonNode(lesson: Lesson, progress: Progress) {
    val unlocked = progress.isLessonUnlocked(lesson)
    val done = progress.isLessonDone(lesson.id)
    val c = lesson.level.color()
    val (icon, bg, fg) = when {
        done -> Triple(Icons.Rounded.Check, c, Color.White)
        unlocked -> Triple(lesson.type.icon(), MaterialTheme.colorScheme.surface, c)
        else -> Triple(Icons.Rounded.Lock, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.outline)
    }
    NodeCircle(icon, bg, fg, border = if (unlocked && !done) c else null)
}

@Composable
private fun NodeCircle(icon: ImageVector, bg: Color, fg: Color, border: Color?) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bg)
            .then(if (border != null) Modifier.border(2.dp, border, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun LessonRow(lesson: Lesson, progress: Progress, onClick: () -> Unit) {
    val unlocked = progress.isLessonUnlocked(lesson)
    val score = progress.lessonScores[lesson.id]
    val done = progress.isLessonDone(lesson.id)
    AppCard(
        onClick = if (unlocked) onClick else null,
        padding = 14.dp,
        bordered = unlocked,
        color = if (unlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    lesson.titleAr, style = MaterialTheme.typography.titleSmall,
                    color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "${lesson.type.labelAr} · ${ltr(lesson.title)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            if (score != null) {
                HSpace(8.dp)
                Pill("$score%", if (done) Success else MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
private fun ExamCard(level: CefrLevel, progress: Progress, onOpenExam: () -> Unit) {
    val unlocked = progress.isExamUnlocked(level)
    val best = progress.examScores[level]
    val passedExam = (best ?: 0) >= Course.EXAM_PASS_PERCENT
    AppCard(
        onClick = if (unlocked) onOpenExam else null,
        color = if (unlocked) Gold.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant,
        bordered = false,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NodeCircle(
                if (unlocked) Icons.Rounded.WorkspacePremium else Icons.Rounded.Lock,
                if (unlocked) Gold else MaterialTheme.colorScheme.surface,
                if (unlocked) Color.White else MaterialTheme.colorScheme.outline,
                border = null,
            )
            HSpace(14.dp)
            Column(Modifier.weight(1f)) {
                Text("امتحان المستوى ${level.code}", style = MaterialTheme.typography.titleMedium)
                Text(
                    when {
                        passedExam -> "حصلت على الشهادة بنتيجة $best%"
                        unlocked -> "على نمط ${ltr(level.cambridge)} · النجاح 70%"
                        else -> "أكمل جميع دروس المستوى لفتح الامتحان"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
