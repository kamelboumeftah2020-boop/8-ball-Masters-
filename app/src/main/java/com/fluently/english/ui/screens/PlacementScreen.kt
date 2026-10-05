package com.fluently.english.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import com.fluently.english.data.content.PlacementEngine
import com.fluently.english.data.content.Skill
import com.fluently.english.ui.components.AppTopBar
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.LinearMeter
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.QuizRunner
import com.fluently.english.ui.components.SectionTitle
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.theme.color

@Composable
fun PlacementScreen(onApply: (CefrLevel?, CefrLevel) -> Unit, onClose: () -> Unit) {
    var engine by remember { mutableStateOf(PlacementEngine()) }
    var started by remember { mutableStateOf(false) }
    // Bumped after each block so the UI re-reads the engine's state.
    var step by remember { mutableIntStateOf(0) }

    when {
        !started -> PlacementIntro(onClose, onStart = { started = true })
        !engine.finished -> key(step) {
            val block = remember { engine.currentBlock() }
            QuizRunner(
                questions = block.map { it.question },
                instantFeedback = false,
                allowSkip = true,
                title = "اختبار تحديد المستوى • المرحلة ${step + 1}",
                contextFor = { block[it].text },
                onClose = onClose,
                onFinish = { records ->
                    engine.submit(records.map { it.correct })
                    if (engine.finished) onApply(engine.result, engine.recommendedCourse)
                    step++
                },
            )
        }
        else -> PlacementResult(
            engine,
            onDone = onClose,
            onRetake = {
                engine = PlacementEngine()
                step = 0
            },
        )
    }
}

@Composable
private fun PlacementIntro(onClose: () -> Unit, onStart: () -> Unit) {
    Scaffold(
        topBar = { AppTopBar("اختبار تحديد المستوى", onBack = onClose, close = true) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { Column(Modifier.padding(20.dp)) { PrimaryButton("ابدأ الاختبار", onStart) } },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Text("🧭", fontSize = 56.sp)
            VSpace(8.dp)
            Text("اكتشف مستواك الحقيقي", style = MaterialTheme.typography.headlineSmall)
            VSpace(8.dp)
            Text(
                "اختبار تكيّفي مبني على طريقة أشهر اختبارات المعاهد العالمية: Oxford Placement Test وCambridge English Placement Test وEF SET. تتغير صعوبة الأسئلة حسب إجاباتك، والنتيجة على مقياس CEFR من A1 إلى C2.",
                style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SectionTitle("أقسام الاختبار")
            Skill.entries.forEach { skill ->
                Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        when (skill) {
                            Skill.USE_OF_ENGLISH -> "📐"
                            Skill.READING -> "📖"
                            Skill.LISTENING -> "🎧"
                        },
                        fontSize = 22.sp,
                    )
                    HSpace(12.dp)
                    Text(skill.labelAr, style = MaterialTheme.typography.titleMedium)
                }
            }
            SectionTitle("تعليمات مهمة")
            listOf(
                "يستغرق الاختبار بين 5 و 15 دقيقة حسب مستواك.",
                "شغّل الصوت — بعض الأسئلة استماع ويمكنك إعادة المقطع.",
                "لا تخمّن! إذا لم تعرف الإجابة اضغط «لا أعرف الإجابة» لتكون النتيجة دقيقة.",
                "لا تظهر الإجابات الصحيحة أثناء الاختبار، تماماً كالاختبارات الرسمية.",
            ).forEach {
                Text("•  $it", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun PlacementResult(engine: PlacementEngine, onDone: () -> Unit, onRetake: () -> Unit) {
    val result = engine.result
    val course = engine.recommendedCourse
    val shown = result ?: CefrLevel.A1
    val c = shown.color()
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.linearGradient(listOf(c, c.copy(alpha = 0.7f))))
                    .padding(28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("نتيجتك", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.titleMedium)
                    VSpace(8.dp)
                    Text(
                        if (result == null) "Pre-A1" else result.code,
                        color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Black,
                    )
                    Text(
                        if (result == null) "مبتدئ تماماً" else result.titleAr,
                        color = Color.White, style = MaterialTheme.typography.headlineSmall,
                    )
                    VSpace(12.dp)
                    Text(
                        if (result == null) {
                            "لا بأس! كل محترف كان مبتدئاً يوماً ما. سنبدأ معك من الأساسيات خطوة بخطوة."
                        } else {
                            result.canDoAr
                        },
                        color = Color.White, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            Column(Modifier.padding(20.dp)) {
                if (result != null) {
                    SectionTitle("ما يعادل مستواك في الاختبارات الدولية")
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(16.dp)) {
                            EquivalenceRow("Cambridge English", result.cambridge)
                            HorizontalDivider(Modifier.padding(vertical = 10.dp))
                            EquivalenceRow("IELTS", result.ielts)
                            HorizontalDivider(Modifier.padding(vertical = 10.dp))
                            EquivalenceRow("TOEFL iBT", result.toefl)
                        }
                    }
                    Text(
                        "* المعادلات تقريبية وفق جداول المقارنة المنشورة من Cambridge وETS، وللحصول على شهادة رسمية يجب التقدم للاختبار الرسمي.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                SectionTitle("أداؤك حسب المهارة")
                Skill.entries.forEach { skill ->
                    val percent = engine.skillPercent(skill) ?: return@forEach
                    Column(Modifier.padding(vertical = 6.dp)) {
                        Row {
                            Text(skill.labelAr, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text("$percent%", style = MaterialTheme.typography.labelLarge)
                        }
                        VSpace(4.dp)
                        LinearMeter(percent / 100f, color = c)
                    }
                }
                Text(
                    "أجبت عن ${engine.answered} سؤالاً.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )

                SectionTitle("خطتك")
                Card(colors = CardDefaults.cardColors(containerColor = course.color().copy(alpha = 0.12f))) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(course.color()),
                            contentAlignment = Alignment.Center,
                        ) { Text(course.code, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                        HSpace(14.dp)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("ابدأ من مستوى ${course.code} — ${course.titleAr}", style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (result != null) "فُتحت لك كل المستويات حتى ${course.code}، ويمكنك مراجعة المستويات السابقة في أي وقت." else "ستبدأ بأساسيات اللغة: التحيات، فعل الكينونة، والكلمات اليومية.",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
                VSpace(24.dp)
                PrimaryButton("ابدأ التعلم الآن", onDone)
                VSpace(8.dp)
                androidx.compose.material3.TextButton(onClick = onRetake, modifier = Modifier.fillMaxWidth()) {
                    Text("إعادة الاختبار")
                }
            }
        }
    }
}

@Composable
private fun EquivalenceRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
