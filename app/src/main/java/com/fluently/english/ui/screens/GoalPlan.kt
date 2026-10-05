package com.fluently.english.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.fluently.english.ui.components.ScreenHeader
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Spellcheck
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fluently.english.data.content.MockExams
import com.fluently.english.data.content.Readers
import com.fluently.english.data.content.Scenarios
import com.fluently.english.data.progress.LearningGoal
import com.fluently.english.data.progress.Progress
import com.fluently.english.ui.components.AppCard
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.IconTile
import com.fluently.english.ui.components.SectionHeader
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.components.isolateLatin
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Coral
import com.fluently.english.ui.theme.Emerald
import com.fluently.english.ui.theme.Gold

fun LearningGoal.icon(): ImageVector = when (this) {
    LearningGoal.TRAVEL -> Icons.Rounded.Flight
    LearningGoal.WORK -> Icons.Rounded.WorkOutline
    LearningGoal.STUDY -> Icons.Rounded.School
    LearningGoal.EXAM -> Icons.Rounded.WorkspacePremium
    LearningGoal.DAILY -> Icons.Rounded.Chat
}

/** One choice per row: icon, title and what the plan focuses on. */
@Composable
fun GoalPicker(selected: LearningGoal?, onSelect: (LearningGoal) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LearningGoal.entries.forEach { goal ->
            val on = goal == selected
            val primary = MaterialTheme.colorScheme.primary
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (on) primary.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface)
                    .border(1.dp, if (on) primary else AppTheme.extra.border, RoundedCornerShape(16.dp))
                    .clickable { onSelect(goal) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconTile(goal.icon(), primary, size = 40.dp)
                HSpace(12.dp)
                Column(Modifier.weight(1f)) {
                    Text(goal.titleAr, style = MaterialTheme.typography.titleSmall)
                    Text(isolateLatin(goal.descAr), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (on) Icon(Icons.Rounded.CheckCircle, null, tint = primary)
            }
        }
    }
}

/** A recommended activity; [route] is a navigation route in the app. */
data class PlanItem(val icon: ImageVector, val tint: Color, val title: String, val subtitle: String, val route: String, val done: Boolean)

/** The personal plan: activities matching the goal and level, unfinished ones first. */
fun goalPlan(goal: LearningGoal, p: Progress): List<PlanItem> {
    val level = p.currentLevel
    fun conv(id: String): PlanItem? = Scenarios.firstOrNull { it.id == id }?.let {
        PlanItem(Icons.Rounded.Forum, Coral, "محادثة: ${it.titleAr}", "تدرّب على موقف حقيقي · ${it.level.code}", "conversation/${it.id}", it.id in p.conversationStars)
    }
    fun mock(id: String): PlanItem? = MockExams.firstOrNull { it.id == id }?.let {
        PlanItem(Icons.Rounded.WorkspacePremium, Gold, it.titleAr, "اختبار محاكاة كامل بالتوقيت", "mock/${it.id}", it.id in p.mockBest)
    }
    val reader = (Readers.firstOrNull { it.level == level && (p.readerChapters[it.id] ?: 0) < it.chapters.size }
        ?: Readers.firstOrNull { (p.readerChapters[it.id] ?: 0) < it.chapters.size })?.let {
        PlanItem(Icons.AutoMirrored.Rounded.MenuBook, Emerald, "قصة: ${it.titleAr}", "قراءة ممتعة بمستوى ${it.level.code}", "reader/${it.id}", false)
    }
    val items: List<PlanItem?> = when (goal) {
        LearningGoal.TRAVEL -> listOf(
            conv("conv-a1-cafe"), conv("conv-a2-hotel"), conv("conv-a2-shop"), conv("conv-b1-doctor"),
            PlanItem(Icons.Rounded.Hearing, Coral, "مختبر النطق", "لتفهمك الناس من أول مرة", "sounds", p.soundScores.size >= 3),
            reader,
        )
        LearningGoal.WORK -> listOf(
            conv("conv-b2-interview"), conv("conv-c1-meeting"), conv("conv-b2-complaint"), conv("conv-c2-diplomacy"),
            mock("mock-fce-1"), reader,
        )
        LearningGoal.STUDY -> listOf(
            reader, mock("mock-ielts-1"),
            PlanItem(Icons.Rounded.Spellcheck, Emerald, "مرجع القواعد", "راجع القواعد بأمثلة تفاعلية", "grammar", false),
            conv("conv-c2-debate"), mock("mock-ielts-2"),
        )
        LearningGoal.EXAM -> listOf(
            if (!p.placementTaken) PlanItem(Icons.Rounded.Explore, Emerald, "اختبار تحديد المستوى", "اعرف من أين تبدأ", "placement", false) else null,
            mock("mock-ielts-1"), mock("mock-pet-1"), mock("mock-fce-1"), mock("mock-ielts-2"), mock("mock-pet-2"), mock("mock-fce-2"),
            PlanItem(Icons.Rounded.EditNote, Coral, "دفتر أخطائي", "أصلح أخطاءك قبل الاختبار", "mistakes", p.mistakes.isEmpty()),
        )
        LearningGoal.DAILY -> listOf(
            conv("conv-a1-meet"), conv("conv-b1-plans"), reader,
            PlanItem(Icons.Rounded.Hearing, Coral, "مختبر النطق", "نطق واضح في الحياة اليومية", "sounds", p.soundScores.size >= 3),
            conv("conv-a1-cafe"),
        )
    }
    return items.filterNotNull().sortedBy { it.done }
}

@Composable
fun GoalPlanCard(progress: Progress, onOpen: (String) -> Unit, onChooseGoal: () -> Unit) {
    val goal = progress.learningGoal
    if (goal == null) {
        SectionHeader("خطتك الشخصية")
        AppCard(onClick = onChooseGoal, padding = 16.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(Icons.Rounded.Explore, MaterialTheme.colorScheme.primary)
                HSpace(12.dp)
                Column(Modifier.weight(1f)) {
                    Text("اختر هدفك من التعلم", style = MaterialTheme.typography.titleSmall)
                    Text("سفر، عمل، دراسة أو اختبار — ونقترح لك ما يناسبك", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }
    SectionHeader("خطتك: ${goal.titleAr}", action = "تغيير", onAction = onChooseGoal)
    AppCard(padding = 6.dp) {
        goalPlan(goal, progress).take(3).forEachIndexed { i, item ->
            if (i > 0) androidx.compose.material3.HorizontalDivider(color = AppTheme.extra.border, modifier = Modifier.padding(horizontal = 12.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onOpen(item.route) }.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconTile(item.icon, item.tint, size = 38.dp)
                HSpace(12.dp)
                Column(Modifier.weight(1f)) {
                    Text(isolateLatin(item.title), style = MaterialTheme.typography.titleSmall, maxLines = 1)
                    Text(isolateLatin(item.subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (item.done) Icon(Icons.Rounded.CheckCircle, null, tint = Emerald, modifier = Modifier.size(20.dp))
                else Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    VSpace(4.dp)
}

@Composable
fun GoalScreen(selected: LearningGoal?, onSelect: (LearningGoal) -> Unit, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenHeader("هدفك من التعلم", onBack = onBack, subtitle = "نقترح لك الدروس والمحادثات والقصص المناسبة")
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            GoalPicker(selected, onSelect)
        }
    }
}
