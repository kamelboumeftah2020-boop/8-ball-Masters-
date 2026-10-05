package com.fluently.english.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.Course
import com.fluently.english.data.progress.ProgressRepository
import com.fluently.english.ui.components.AnswerRecord
import com.fluently.english.ui.components.AppTopBar
import com.fluently.english.ui.components.MistakesReview
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.QuizRunner
import com.fluently.english.ui.components.ScoreHeader
import com.fluently.english.ui.components.VSpace
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
    Scaffold(
        topBar = { AppTopBar("امتحان نهاية المستوى", onBack = onClose, close = true) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { Column(Modifier.padding(20.dp)) { PrimaryButton("ابدأ الامتحان", onStart) } },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Text("🎓", fontSize = 56.sp)
            VSpace(8.dp)
            Text("امتحان المستوى ${level.code} — ${level.titleAr}", style = MaterialTheme.typography.headlineSmall)
            VSpace(8.dp)
            Text(
                "امتحان شامل على غرار امتحانات كامبريدج (${level.cambridge})، يغطي القواعد والمفردات والقراءة والاستماع لكل وحدات المستوى.",
                style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VSpace(20.dp)
            InfoRow("📝", "عدد الأسئلة", "$count سؤالاً")
            InfoRow("✅", "درجة النجاح", "${Course.EXAM_PASS_PERCENT}%")
            InfoRow("🔕", "طريقة الامتحان", "لا تظهر الإجابات الصحيحة إلا في النهاية")
            InfoRow("🏅", "عند النجاح", "تحصل على شهادة المستوى وينفتح المستوى التالي")
        }
    }
}

@Composable
private fun InfoRow(emoji: String, title: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 22.sp)
            Column(Modifier.padding(start = 14.dp)) {
                Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun ExamResult(level: CefrLevel, records: List<AnswerRecord>, xp: Int, onRetry: () -> Unit, onDone: () -> Unit) {
    val correct = records.count { it.correct }
    val percent = ProgressRepository.percent(correct, records.size)
    val passed = percent >= Course.EXAM_PASS_PERCENT
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            VSpace(16.dp)
            ScoreHeader(
                percent, passed,
                title = if (passed) "مبروك! اجتزت المستوى ${level.code} 🎉" else "لم تجتز الامتحان هذه المرة",
                subtitle = "$correct من ${records.size} • +$xp نقطة خبرة" +
                    if (!passed) "\nراجع أخطاءك والدروس ثم أعد المحاولة. تحتاج ${Course.EXAM_PASS_PERCENT}%." else "",
            )
            if (passed) {
                VSpace(24.dp)
                Certificate(level, percent)
            }
            Column(Modifier.fillMaxWidth()) { MistakesReview(records) }
            VSpace(24.dp)
            PrimaryButton(if (passed) "متابعة" else "أعد الامتحان", if (passed) onDone else onRetry)
            VSpace(8.dp)
            OutlinedButton(onClick = if (passed) onRetry else onDone, modifier = Modifier.fillMaxWidth()) {
                Text(if (passed) "أعد الامتحان لتحسين نتيجتك" else "العودة إلى الدروس")
            }
            VSpace(16.dp)
        }
    }
}

@Composable
fun Certificate(level: CefrLevel, percent: Int) {
    val c = level.color()
    Card(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, c),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(c.copy(alpha = 0.14f), Color.Transparent))).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("🏅", fontSize = 44.sp)
            Text("شهادة إتمام المستوى", style = MaterialTheme.typography.titleMedium)
            Box(Modifier.clip(RoundedCornerShape(12.dp)).background(c).padding(horizontal = 18.dp, vertical = 6.dp)) {
                Text(level.code, color = Color.White, fontWeight = FontWeight.Black, fontSize = 28.sp)
            }
            Text(level.titleAr, style = MaterialTheme.typography.titleLarge)
            Text(
                "يعادل تقريباً: ${level.cambridge} • IELTS ${level.ielts}",
                style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("النتيجة: $percent%", style = MaterialTheme.typography.labelLarge, color = c)
        }
    }
}
