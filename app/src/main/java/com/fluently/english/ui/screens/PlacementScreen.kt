package com.fluently.english.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Spellcheck
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.PlacementEngine
import com.fluently.english.data.content.Skill
import com.fluently.english.ui.components.AppCard
import com.fluently.english.ui.components.GhostButton
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.IconTile
import com.fluently.english.ui.components.LevelBadge
import com.fluently.english.ui.components.LinearMeter
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.QuizRunner
import com.fluently.english.ui.components.ScreenHeader
import com.fluently.english.ui.components.SectionHeader
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.components.ltr
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Coral
import com.fluently.english.ui.theme.Emerald
import com.fluently.english.ui.theme.Gold
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
                title = "اختبار تحديد المستوى · المرحلة ${step + 1}",
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

private fun Skill.icon(): ImageVector = when (this) {
    Skill.USE_OF_ENGLISH -> Icons.Rounded.Spellcheck
    Skill.READING -> Icons.AutoMirrored.Rounded.MenuBook
    Skill.LISTENING -> Icons.Rounded.Headphones
}

private fun Skill.tint(): Color = when (this) {
    Skill.USE_OF_ENGLISH -> Emerald
    Skill.READING -> Gold
    Skill.LISTENING -> Coral
}

@Composable
private fun PlacementIntro(onClose: () -> Unit, onStart: () -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeader("اختبار تحديد المستوى", onBack = onClose, close = true)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            VSpace(12.dp)
            Text("اكتشف مستواك الحقيقي", style = MaterialTheme.typography.headlineMedium)
            VSpace(8.dp)
            Text(
                "اختبار تكيّفي على طريقة Oxford Placement Test وCambridge English Placement Test وEF SET: تتغير صعوبة الأسئلة حسب إجاباتك، والنتيجة على مقياس CEFR من A1 إلى C2.",
                style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SectionHeader("أقسام الاختبار")
            AppCard(padding = 0.dp) {
                Skill.entries.forEachIndexed { i, skill ->
                    if (i > 0) HorizontalDivider(color = AppTheme.extra.border)
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconTile(skill.icon(), skill.tint(), size = 40.dp)
                        HSpace(14.dp)
                        Text(skill.labelAr, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
            SectionHeader("قبل أن تبدأ")
            listOf(
                "يستغرق بين 5 و15 دقيقة حسب مستواك.",
                "شغّل الصوت — بعض الأسئلة استماع ويمكن إعادتها.",
                "لا تخمّن: اضغط «لا أعرف الإجابة» لتكون النتيجة دقيقة.",
                "الإجابات الصحيحة لا تظهر أثناء الاختبار، كالاختبارات الرسمية.",
            ).forEach {
                Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier.padding(top = 9.dp).size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    )
                    HSpace(12.dp)
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
            VSpace(16.dp)
        }
        Column(Modifier.navigationBarsPadding()) {
            HorizontalDivider(color = AppTheme.extra.border)
            Box(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) { PrimaryButton("ابدأ الاختبار", onStart) }
        }
    }
}

@Composable
private fun PlacementResult(engine: PlacementEngine, onDone: () -> Unit, onRetake: () -> Unit) {
    val result = engine.result
    val course = engine.recommendedCourse
    val extra = AppTheme.extra
    val c = (result ?: CefrLevel.A1).color()
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        VSpace(24.dp)
        Column(
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(extra.hero)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("مستواك في الإنجليزية", style = MaterialTheme.typography.labelLarge, color = extra.onHeroMuted)
            VSpace(6.dp)
            Text(
                result?.code ?: "Pre-A1",
                style = MaterialTheme.typography.displayLarge, color = c,
            )
            Text(
                result?.titleAr ?: "مبتدئ تماماً",
                style = MaterialTheme.typography.headlineSmall, color = extra.onHero,
            )
            VSpace(12.dp)
            Text(
                result?.canDoAr ?: "لا بأس! كل محترف كان مبتدئاً يوماً ما. سنبدأ معك من الأساسيات خطوة بخطوة.",
                color = extra.onHeroMuted, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium,
            )
            if (result != null) {
                VSpace(20.dp)
                HorizontalDivider(color = extra.heroTrack)
                VSpace(16.dp)
                Row(Modifier.fillMaxWidth()) {
                    ResultEquivalence("Cambridge", result.cambridge, Modifier.weight(1.4f))
                    ResultEquivalence("IELTS", result.ielts, Modifier.weight(1f))
                    ResultEquivalence("TOEFL iBT", result.toefl, Modifier.weight(1f))
                }
            }
        }
        if (result != null) {
            Text(
                "* معادلات تقريبية وفق جداول Cambridge وETS. الشهادة الرسمية تتطلب التقدم للاختبار الرسمي.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        SectionHeader("أداؤك حسب المهارة")
        AppCard {
            Skill.entries.forEachIndexed { i, skill ->
                val percent = engine.skillPercent(skill) ?: return@forEachIndexed
                if (i > 0) VSpace(16.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(skill.icon(), skill.tint(), size = 34.dp)
                    HSpace(12.dp)
                    Column(Modifier.weight(1f)) {
                        Row {
                            Text(skill.labelAr, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                            Text("$percent%", style = MaterialTheme.typography.labelLarge)
                        }
                        VSpace(6.dp)
                        LinearMeter(percent / 100f, color = skill.tint())
                    }
                }
            }
            VSpace(14.dp)
            Text(
                "أجبت عن ${engine.answered} سؤالاً",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SectionHeader("خطتك")
        AppCard(color = course.color().copy(alpha = 0.08f), bordered = false) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LevelBadge(course, size = 52.dp, filled = true)
                HSpace(14.dp)
                Column {
                    Text("ابدأ من ${course.code} — ${course.titleAr}", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (result != null) "فُتحت لك المستويات حتى ${course.code}، ويمكنك مراجعة ما قبلها في أي وقت."
                        else "ستبدأ بالأساسيات: التحيات وفعل الكينونة والكلمات اليومية.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        VSpace(28.dp)
        PrimaryButton("ابدأ التعلم الآن", onDone)
        GhostButton("إعادة الاختبار", onRetake)
        VSpace(20.dp)
    }
}

@Composable
private fun ResultEquivalence(label: String, value: String, modifier: Modifier) {
    val extra = AppTheme.extra
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = extra.onHeroMuted)
        VSpace(2.dp)
        Text(ltr(value), style = MaterialTheme.typography.labelLarge, color = extra.onHero, textAlign = TextAlign.Center)
    }
}
