package com.fluently.english.ui.screens

import androidx.compose.foundation.background
import com.fluently.english.ui.theme.Success
import com.fluently.english.ui.components.SpeakButton
import com.fluently.english.ui.components.Ltr
import androidx.compose.runtime.remember
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.Course
import com.fluently.english.data.content.DailyTips
import com.fluently.english.data.progress.Progress
import com.fluently.english.data.progress.localEpochDay
import com.fluently.english.ui.components.AppCard
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.IconTile
import com.fluently.english.ui.components.LinearMeter
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.ProgressRing
import com.fluently.english.ui.components.SectionHeader
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.components.icon
import com.fluently.english.ui.components.isolateLatin
import com.fluently.english.ui.components.ltr
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Coral
import com.fluently.english.ui.theme.Emerald
import com.fluently.english.ui.theme.Gold
import com.fluently.english.ui.theme.color
import java.util.Calendar

@Composable
fun HomeScreen(
    progress: Progress,
    dueCount: Int,
    onOpenLesson: (String) -> Unit,
    onOpenExam: (CefrLevel) -> Unit,
    onOpenLevel: (CefrLevel) -> Unit,
    onReview: () -> Unit,
    onPlacement: () -> Unit,
    onDaily: () -> Unit = {},
    onAddWord: (String) -> Unit = {},
    onOpenRoute: (String) -> Unit = {},
    onChooseGoal: () -> Unit = {},
    onReport: () -> Unit = {},
    onLeaderboard: () -> Unit = {},
    guest: Boolean = false,
    onCreateAccount: () -> Unit = {},
) {
    val today = localEpochDay()
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        TopRow(progress)
        if (guest) {
            VSpace(14.dp)
            AppCard(onClick = onCreateAccount, color = com.fluently.english.ui.theme.Gold.copy(alpha = 0.12f), bordered = false, padding = 14.dp) {
                Text("أنت تستخدم التطبيق كضيف", style = MaterialTheme.typography.titleSmall)
                Text(
                    "تقدّمك غير محفوظ في حساب وقد يضيع. اضغط هنا لإنشاء حساب مجاني وسيُنقل إليه كل ما تعلمته.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        VSpace(20.dp)
        HeroCard(progress, onOpenLesson, onOpenExam, onOpenLevel)

        GoalPlanCard(progress, onOpenRoute, onChooseGoal)
        VSpace(12.dp)
        LeaderboardCard(progress, today, onLeaderboard)

        SectionHeader("هذا الأسبوع", action = "التقرير", onAction = onReport)
        WeekCard(progress, today)

        SectionHeader("اليوم")
        DailyChallengeCard(progress.challengeDoneToday(today), onDaily)
        VSpace(12.dp)
        WordOfTheDay(progress, today, onAddWord)

        SectionHeader("اختصارات")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickCard(
                Icons.Rounded.Style, Coral,
                title = "المراجعة",
                body = if (dueCount > 0) "$dueCount كلمة مستحقة" else "لا شيء مستحق الآن",
                modifier = Modifier.weight(1f),
                onClick = onReview,
            )
            if (!progress.placementTaken) {
                QuickCard(
                    Icons.Rounded.Explore, Emerald,
                    title = "حدد مستواك",
                    body = "اختبار تكيّفي",
                    modifier = Modifier.weight(1f),
                    onClick = onPlacement,
                )
            } else {
                QuickCard(
                    Icons.Rounded.WorkspacePremium, Gold,
                    title = "الشهادات",
                    body = "${CefrLevel.entries.count { progress.isLevelPassed(it) }} من 6 مستويات",
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenLevel(progress.currentLevel) },
                )
            }
        }

        SectionHeader("نصيحة اليوم")
        AppCard(color = MaterialTheme.colorScheme.tertiaryContainer, bordered = false) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Rounded.TipsAndUpdates, null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                HSpace(12.dp)
                Text(
                    DailyTips[(today % DailyTips.size).toInt()],
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        VSpace(28.dp)
    }
}

@Composable
private fun TopRow(progress: Progress) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = if (hour in 4..11) "صباح الخير" else "مساء الخير"
    Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(greeting, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(progress.name.ifBlank { "مرحباً بك" }, style = MaterialTheme.typography.headlineSmall)
        }
        CounterChip(Icons.Rounded.LocalFireDepartment, Coral, "${progress.streak}")
        HSpace(8.dp)
        CounterChip(Icons.Rounded.Bolt, Gold, "${progress.xp}")
    }
}

@Composable
private fun CounterChip(icon: ImageVector, tint: Color, value: String) {
    Row(
        Modifier
            .clip(CircleShape)
            .border(1.dp, AppTheme.extra.border, CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        HSpace(4.dp)
        Text(value, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun HeroCard(
    progress: Progress,
    onOpenLesson: (String) -> Unit,
    onOpenExam: (CefrLevel) -> Unit,
    onOpenLevel: (CefrLevel) -> Unit,
) {
    val extra = AppTheme.extra
    val level = progress.currentLevel
    val next = progress.nextLesson()
    val allDone = CefrLevel.entries.all { progress.isLevelPassed(it) }
    val lessons = Course.level(level).lessons
    val done = lessons.count { progress.isLessonDone(it.id) }
    val levelProgress = progress.levelProgress(level)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(extra.hero)
            .clickable { onOpenLevel(level) }
            .padding(22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("مستواك الحالي", style = MaterialTheme.typography.labelLarge, color = extra.onHeroMuted)
                VSpace(4.dp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(level.code, style = MaterialTheme.typography.displayMedium, color = extra.onHero)
                    HSpace(10.dp)
                    Text(
                        level.titleAr, style = MaterialTheme.typography.titleLarge, color = level.color(),
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                Text(
                    "$done من ${lessons.size} درس · ${ltr(level.cambridge)}",
                    style = MaterialTheme.typography.bodySmall, color = extra.onHeroMuted,
                )
            }
            ProgressRing(
                progress = levelProgress,
                color = level.color(),
                track = extra.heroTrack,
                stroke = 7.dp,
                modifier = Modifier.size(78.dp),
            ) {
                Text("${(levelProgress * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, color = extra.onHero)
            }
        }

        VSpace(20.dp)
        HorizontalDivider(color = extra.heroTrack)
        VSpace(18.dp)

        when {
            allDone -> {
                NextRow(Icons.Rounded.EmojiEvents, Gold, "أتممت جميع المستويات", "أنت الآن في مستوى الإتقان C2")
                VSpace(16.dp)
                PrimaryButton("راجع المسار", { onOpenLevel(CefrLevel.C2) })
            }
            next != null -> {
                NextRow(next.type.icon(), level.color(), next.titleAr, "${next.type.labelAr} · ${ltr(next.title)}")
                VSpace(16.dp)
                PrimaryButton("تابع التعلم", { onOpenLesson(next.id) }, icon = Icons.AutoMirrored.Rounded.ArrowForward)
            }
            else -> {
                NextRow(Icons.Rounded.WorkspacePremium, Gold, "امتحان المستوى ${level.code}", "أنهيت الدروس — اجتز الامتحان بنسبة 70%")
                VSpace(16.dp)
                PrimaryButton("ابدأ الامتحان", { onOpenExam(level) })
            }
        }
    }
}

@Composable
private fun NextRow(icon: ImageVector, tint: Color, title: String, subtitle: String) {
    val extra = AppTheme.extra
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconTile(icon, tint, size = 46.dp, background = extra.heroTrack)
        HSpace(14.dp)
        Column(Modifier.weight(1f)) {
            Text("الخطوة التالية", style = MaterialTheme.typography.labelSmall, color = extra.onHeroMuted)
            Text(isolateLatin(title), style = MaterialTheme.typography.titleMedium, color = extra.onHero, maxLines = 1)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = extra.onHeroMuted, maxLines = 1)
        }
    }
}

private val DayInitials = listOf("ح", "ن", "ث", "ر", "خ", "ج", "س") // Sunday … Saturday

@Composable
private fun WeekCard(progress: Progress, today: Long) {
    val goal = progress.dailyGoal
    val todayXp = if (progress.lastActiveDay == today) progress.todayXp else 0
    AppCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            (6 downTo 0).map { today - it }.forEach { day ->
                val active = day in progress.activeDays
                val isToday = day == today
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        DayInitials[((day + 4) % 7).toInt()],
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isToday) FontWeight.Bold else null,
                    )
                    VSpace(8.dp)
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .then(
                                if (isToday && !active) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                else Modifier,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (active) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        VSpace(18.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("هدف اليوم", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            Text(
                "$todayXp / $goal نقطة",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        VSpace(8.dp)
        LinearMeter(todayXp.toFloat() / goal, height = 8.dp, color = if (todayXp >= goal) Gold else MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun QuickCard(icon: ImageVector, tint: Color, title: String, body: String, modifier: Modifier, onClick: () -> Unit) {
    AppCard(modifier = modifier, onClick = onClick, padding = 16.dp) {
        IconTile(icon, tint, size = 40.dp)
        VSpace(14.dp)
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DailyChallengeCard(done: Boolean, onClick: () -> Unit) {
    AppCard(onClick = onClick, color = if (done) Success.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(if (done) Icons.Rounded.TaskAlt else Icons.Rounded.Bolt, if (done) Success else Gold, size = 46.dp)
            HSpace(14.dp)
            Column(Modifier.weight(1f)) {
                Text(if (done) "أنجزت تحدي اليوم" else "تحدي اليوم", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (done) "عد غداً لتحدٍّ جديد — أو تدرّب مرة أخرى" else "5 أسئلة سريعة مما تعلمته · مكافأة 30 نقطة",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun WordOfTheDay(progress: Progress, today: Long, onAdd: (String) -> Unit) {
    val words = remember(progress.unlockedLevel) {
        Course.levels.filter { progress.isLevelUnlocked(it.level) }.flatMap { it.words }
    }
    if (words.isEmpty()) return
    val word = words[(today % words.size).toInt()]
    val added = word.en in progress.cards
    val extra = AppTheme.extra
    Column(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(extra.hero)
            .padding(20.dp),
    ) {
        Text("كلمة اليوم", style = MaterialTheme.typography.labelLarge, color = extra.onHeroMuted)
        VSpace(6.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Ltr { Text(word.en, style = MaterialTheme.typography.headlineMedium, color = extra.onHero) }
                Text(word.ar, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            SpeakButton(word.en, size = 46.dp)
        }
        VSpace(10.dp)
        Ltr { Text(word.example, style = MaterialTheme.typography.bodyMedium, color = extra.onHeroMuted, modifier = Modifier.fillMaxWidth()) }
        VSpace(14.dp)
        Row(
            Modifier
                .clip(CircleShape)
                .background(if (added) extra.heroTrack else MaterialTheme.colorScheme.primary)
                .clickable(enabled = !added) { onAdd(word.en) }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(if (added) Icons.Rounded.Check else Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
            HSpace(6.dp)
            Text(if (added) "في بطاقات المراجعة" else "أضف إلى المراجعة", style = MaterialTheme.typography.labelLarge, color = Color.White)
        }
    }
}
