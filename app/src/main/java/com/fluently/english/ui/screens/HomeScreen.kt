package com.fluently.english.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.Course
import com.fluently.english.data.content.DailyTips
import com.fluently.english.data.progress.Progress
import com.fluently.english.data.progress.localEpochDay
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.isolateLatin
import com.fluently.english.ui.components.ltr
import com.fluently.english.ui.components.LevelBadge
import com.fluently.english.ui.components.LinearMeter
import com.fluently.english.ui.components.Pill
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.ProgressRing
import com.fluently.english.ui.components.SectionTitle
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.theme.Amber
import com.fluently.english.ui.theme.Indigo
import com.fluently.english.ui.theme.IndigoDeep
import com.fluently.english.ui.theme.color

@Composable
fun HomeScreen(
    progress: Progress,
    dueCount: Int,
    onOpenLesson: (String) -> Unit,
    onOpenExam: (CefrLevel) -> Unit,
    onOpenLevel: (CefrLevel) -> Unit,
    onReview: () -> Unit,
    onPlacement: () -> Unit,
) {
    val level = progress.currentLevel
    val next = progress.nextLesson()
    val allDone = CefrLevel.entries.all { progress.isLevelPassed(it) }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()),
    ) {
        Header(progress)

        Column(Modifier.padding(horizontal = 20.dp)) {
            VSpace(20.dp)
            // Continue learning
            when {
                allDone -> HighlightCard(
                    emoji = "🏆", title = "مبروك! أتممت جميع المستويات",
                    body = "وصلت إلى مستوى C2 — مستوى الإتقان. استمر في المراجعة للحفاظ على مستواك.",
                    button = "راجع المسار", onClick = { onOpenLevel(CefrLevel.C2) },
                )
                next != null -> HighlightCard(
                    emoji = next.type.emoji(),
                    title = next.titleAr,
                    body = "${next.level.code} • ${next.type.labelAr} • ${ltr(next.title)}",
                    button = "تابع التعلم",
                    onClick = { onOpenLesson(next.id) },
                )
                else -> HighlightCard(
                    emoji = "🎓", title = "امتحان المستوى ${level.code}",
                    body = "أنهيت جميع دروس المستوى! اجتز الامتحان (70%) لتحصل على الشهادة وتنتقل للمستوى التالي.",
                    button = "ابدأ الامتحان", onClick = { onOpenExam(level) },
                )
            }

            if (!progress.placementTaken) {
                VSpace(14.dp)
                ActionRow("🧭", "لم تحدد مستواك بعد", "خذ اختبار تحديد المستوى لتبدأ من المكان المناسب", onPlacement)
            }
            if (dueCount > 0) {
                VSpace(14.dp)
                ActionRow("🧠", "$dueCount كلمة تنتظر المراجعة", "المراجعة في الوقت المناسب تثبت الكلمات في ذاكرتك", onReview)
            }

            SectionTitle("مستواك الحالي")
            LevelCard(level, progress, onClick = { onOpenLevel(level) })

            SectionTitle("نصيحة اليوم")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("💡", fontSize = 24.sp)
                    HSpace(12.dp)
                    Text(
                        DailyTips[(localEpochDay() % DailyTips.size).toInt()],
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            VSpace(24.dp)
        }
    }
}

@Composable
private fun Header(progress: Progress) {
    val goalProgress = progress.todayXp.toFloat() / progress.dailyGoal
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Brush.linearGradient(listOf(Indigo, IndigoDeep)))
            .padding(24.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (progress.name.isNotBlank()) "أهلاً، ${progress.name} 👋" else "أهلاً بك 👋",
                        style = MaterialTheme.typography.headlineSmall, color = Color.White,
                    )
                    VSpace(4.dp)
                    Text(
                        "لنواصل رحلتك نحو الطلاقة",
                        style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f),
                    )
                }
                ProgressRing(
                    progress = goalProgress,
                    color = Amber,
                    track = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(64.dp),
                ) {
                    Text(
                        if (goalProgress >= 1f) "✓" else "${(goalProgress * 100).toInt()}%",
                        color = Color.White, style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            VSpace(20.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeaderStat("🔥", "${progress.streak}", "يوم متتالي", Modifier.weight(1f))
                HeaderStat("⚡", "${progress.xp}", "نقطة خبرة", Modifier.weight(1f))
                HeaderStat("🎯", "${progress.todayXp}/${progress.dailyGoal}", "هدف اليوم", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeaderStat(emoji: String, value: String, label: String, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.14f)).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("$emoji $value", color = Color.White, style = MaterialTheme.typography.titleMedium)
        Text(label, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun HighlightCard(emoji: String, title: String, body: String, button: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) { Text(emoji, fontSize = 26.sp) }
                HSpace(14.dp)
                Column(Modifier.weight(1f)) {
                    Text("الخطوة التالية", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(isolateLatin(title), style = MaterialTheme.typography.titleLarge)
                }
            }
            VSpace(8.dp)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            VSpace(16.dp)
            PrimaryButton(button, onClick)
        }
    }
}

@Composable
private fun ActionRow(emoji: String, title: String, body: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth().clip(CardDefaults.shape).clickable(onClick = onClick),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 26.sp)
            HSpace(12.dp)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
fun LevelCard(level: CefrLevel, progress: Progress, onClick: () -> Unit) {
    val unlocked = progress.isLevelUnlocked(level)
    val passed = progress.isLevelPassed(level)
    val course = Course.level(level)
    val done = course.lessons.count { progress.isLessonDone(it.id) }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().clip(CardDefaults.shape).clickable(enabled = unlocked, onClick = onClick),
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            LevelBadge(level, size = 56.dp, locked = !unlocked)
            HSpace(14.dp)
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(level.titleAr, style = MaterialTheme.typography.titleMedium)
                    HSpace(8.dp)
                    when {
                        passed -> Pill("✓ مكتمل", level.color())
                        !unlocked -> Pill("🔒 مقفل", MaterialTheme.colorScheme.outline)
                    }
                }
                Text(
                    "${level.stageAr} • ${level.cambridge}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                VSpace(10.dp)
                LinearMeter(progress.levelProgress(level), color = if (unlocked) level.color() else MaterialTheme.colorScheme.outline)
                VSpace(4.dp)
                Text(
                    "$done / ${course.lessons.size} درس",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
