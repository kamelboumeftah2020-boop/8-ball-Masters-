package com.fluently.english.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FactCheck
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.Course
import com.fluently.english.data.progress.ProgressRepository
import com.fluently.english.ui.components.AnswerRecord
import com.fluently.english.ui.components.AppCard
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.IconTile
import com.fluently.english.ui.components.LevelBadge
import com.fluently.english.ui.components.MistakesReview
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.QuizRunner
import com.fluently.english.ui.components.ScoreHeader
import com.fluently.english.ui.components.ScreenHeader
import com.fluently.english.ui.components.SecondaryButton
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.components.ltr
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Coral
import com.fluently.english.ui.theme.Emerald
import com.fluently.english.ui.theme.Gold
import com.fluently.english.ui.theme.color

@Composable
fun ExamScreen(level: CefrLevel, onComplete: (Int, Int) -> Int, onClose: () -> Unit) {
    var attempt by remember { mutableIntStateOf(0) }
    var started by remember { mutableStateOf(false) }
    var records by remember { mutableStateOf<List<AnswerRecord>?>(null) }
    var xp by remember { mutableIntStateOf(0) }
    val questions = remember(attempt) { Course.examQuestions(level, seed = System.nanoTime()) }

    val result = records
    when {
        !started -> ExamIntro(level, questions.size, onClose, onStart = { started = true })
        result == null -> key(attempt) {
            QuizRunner(
                questions = questions,
                instantFeedback = false,
                title = "امتحان المستوى ${level.code}",
                onClose = onClose,
                onFinish = {
                    records = it
                    xp = onComplete(it.count { r -> r.correct }, it.size)
                },
            )
        }
        else -> ExamResult(level, result, xp, onRetry = {
            attempt++
            records = null
        }, onDone = onClose)
    }
}

@Composable
private fun ExamIntro(level: CefrLevel, count: Int, onClose: () -> Unit, onStart: () -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeader("امتحان نهاية المستوى", onBack = onClose, close = true)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            VSpace(12.dp)
            LevelBadge(level, size = 64.dp, filled = true)
            VSpace(16.dp)
            Text("امتحان ${level.code} — ${level.titleAr}", style = MaterialTheme.typography.headlineMedium)
            VSpace(8.dp)
            Text(
                "امتحان شامل على نمط ${ltr(level.cambridge)}، يغطي القواعد والمفردات والقراءة والاستماع في كل وحدات المستوى.",
                style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VSpace(24.dp)
            AppCard(padding = 0.dp) {
                InfoRow(Icons.Rounded.Quiz, Emerald, "عدد الأسئلة", "$count سؤالاً")
                HorizontalDivider(color = AppTheme.extra.border)
                InfoRow(Icons.AutoMirrored.Rounded.FactCheck, Gold, "درجة النجاح", "${Course.EXAM_PASS_PERCENT}%")
                HorizontalDivider(color = AppTheme.extra.border)
                InfoRow(Icons.Rounded.VisibilityOff, Coral, "طريقة الامتحان", "الإجابات تظهر في النهاية فقط")
                HorizontalDivider(color = AppTheme.extra.border)
                InfoRow(Icons.Rounded.WorkspacePremium, level.color(), "عند النجاح", "شهادة المستوى وفتح المستوى التالي")
            }
        }
        Column(Modifier.navigationBarsPadding()) {
            HorizontalDivider(color = AppTheme.extra.border)
            Box(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) { PrimaryButton("ابدأ الامتحان", onStart) }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, tint: Color, title: String, value: String) {
    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(icon, tint, size = 40.dp)
        HSpace(14.dp)
        Column {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun ExamResult(level: CefrLevel, records: List<AnswerRecord>, xp: Int, onRetry: () -> Unit, onDone: () -> Unit) {
    val correct = records.count { it.correct }
    val percent = ProgressRepository.percent(correct, records.size)
    val passed = percent >= Course.EXAM_PASS_PERCENT
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
            percent, passed,
            title = if (passed) "مبروك! اجتزت المستوى ${level.code}" else "لم تجتز الامتحان هذه المرة",
            subtitle = "$correct من ${records.size} · +$xp نقطة" +
                if (!passed) "\nراجع أخطاءك والدروس ثم أعد المحاولة" else "",
        )
        if (passed) {
            VSpace(28.dp)
            Certificate(level, percent)
        }
        Column(Modifier.fillMaxWidth()) { MistakesReview(records) }
        VSpace(28.dp)
        PrimaryButton(if (passed) "متابعة" else "أعد الامتحان", if (passed) onDone else onRetry)
        VSpace(10.dp)
        SecondaryButton(if (passed) "أعد الامتحان لتحسين نتيجتك" else "العودة إلى الدروس", if (passed) onRetry else onDone)
        VSpace(24.dp)
    }
}

/** Certificate card shown after passing a level exam. */
@Composable
fun Certificate(level: CefrLevel, percent: Int) {
    val extra = AppTheme.extra
    val c = level.color()
    Surface(
        shape = MaterialTheme.shapes.large,
        color = extra.hero,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(Modifier.padding(10.dp)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .border(BorderStroke(1.dp, Gold.copy(alpha = 0.6f)), MaterialTheme.shapes.medium)
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                IconTile(Icons.Rounded.WorkspacePremium, Gold, size = 52.dp, background = extra.heroTrack)
                Text("شهادة إتمام المستوى", style = MaterialTheme.typography.labelLarge, color = extra.onHeroMuted)
                Text(level.code, style = MaterialTheme.typography.displayMedium, color = c)
                Text(level.titleAr, style = MaterialTheme.typography.titleLarge, color = extra.onHero)
                Text(
                    "يعادل تقريباً ${ltr(level.cambridge)} · IELTS ${ltr(level.ielts)}",
                    style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = extra.onHeroMuted,
                )
                Text("النتيجة $percent%", style = MaterialTheme.typography.labelLarge, color = Gold)
            }
        }
    }
}
