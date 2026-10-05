package com.fluently.english.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Spellcheck
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.CompositionLocalProvider
import com.fluently.english.data.content.MockExam
import com.fluently.english.data.content.MockExams
import com.fluently.english.data.content.MockKind
import com.fluently.english.data.content.MockScoring
import com.fluently.english.data.content.MockSection
import com.fluently.english.data.content.SectionType
import com.fluently.english.data.content.SpeakingCriteria
import com.fluently.english.data.content.SpeakingPart
import com.fluently.english.data.content.WritingCriteria
import com.fluently.english.data.content.WritingCheck
import com.fluently.english.data.progress.Progress
import com.fluently.english.data.progress.SkillKey
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.ui.components.AnswerRecord
import com.fluently.english.ui.components.AppCard
import com.fluently.english.ui.components.AutoText
import com.fluently.english.ui.components.GhostButton
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.IconTile
import com.fluently.english.ui.components.LevelBadge
import com.fluently.english.ui.components.LinearMeter
import com.fluently.english.ui.components.MistakesReview
import com.fluently.english.ui.components.Pill
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.QuizRunner
import com.fluently.english.ui.components.ScreenHeader
import com.fluently.english.ui.components.SecondaryButton
import com.fluently.english.ui.components.SectionHeader
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.components.isolateLatin
import com.fluently.english.ui.components.ltr
import com.fluently.english.ui.components.rememberSpeechInput
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Coral
import com.fluently.english.ui.theme.Danger
import com.fluently.english.ui.theme.Emerald
import com.fluently.english.ui.theme.Gold
import com.fluently.english.ui.theme.Success
import kotlinx.coroutines.delay

// ======================================================================
// List
// ======================================================================

/** How a stored best result is shown: IELTS band or Cambridge scale score. */
fun mockResultLabel(kind: MockKind, stored: Int): String =
    if (kind == MockKind.IELTS) "Band ${stored / 10.0}" else "Scale $stored"

@Composable
fun MockListScreen(progress: Progress, onBack: () -> Unit, onOpen: (String) -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        ScreenHeader("اختبارات المحاكاة", onBack = onBack, subtitle = "بنفس أقسام وأنماط الاختبارات الدولية")
        Column(Modifier.padding(horizontal = 20.dp)) {
            VSpace(8.dp)
            AppCard(color = AppTheme.extra.subtle, bordered = false) {
                Text(
                    "عِش تجربة الاختبار الحقيقي: أقسام مؤقتة، أسئلة بنفس الأنماط الرسمية، وتقدير لدرجتك بمقياس الاختبار نفسه. " +
                        "الاستماع والقراءة تُصحح تلقائياً، والكتابة والمحادثة تقيّمها بنفسك وفق معايير الممتحنين مع نموذج إجابة.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            VSpace(16.dp)
            MockExams.forEach { exam ->
                MockCard(exam, progress.mockBest[exam.id]) { onOpen(exam.id) }
                VSpace(12.dp)
            }
            VSpace(20.dp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MockCard(exam: MockExam, best: Int?, onClick: () -> Unit) {
    val tint = kindColor(exam.kind)
    AppCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(Icons.Rounded.WorkspacePremium, tint, size = 48.dp)
            HSpace(14.dp)
            Column(Modifier.weight(1f)) {
                Text(isolateLatin(exam.titleAr), style = MaterialTheme.typography.titleMedium)
                Text(
                    "${exam.sections.size} أقسام · ${ltr("${exam.sections.sumOf { it.minutes }}")} دقيقة تقريباً",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LevelBadge(exam.level, size = 38.dp)
        }
        VSpace(12.dp)
        Text(isolateLatin(exam.descriptionAr), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        VSpace(12.dp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            exam.sections.forEach { s -> Pill(s.type.labelAr, tint) }
        }
        if (best != null) {
            VSpace(10.dp)
            Pill("أفضل نتيجة: ${ltr(mockResultLabel(exam.kind, best))}", Gold, solid = true)
        }
    }
}

private fun kindColor(kind: MockKind): Color = when (kind) {
    MockKind.IELTS -> Coral
    MockKind.CAMBRIDGE_B1 -> Emerald
    MockKind.CAMBRIDGE_B2 -> Gold
}

private fun SectionType.icon(): ImageVector = when (this) {
    SectionType.LISTENING -> Icons.Rounded.Headphones
    SectionType.READING -> Icons.AutoMirrored.Rounded.MenuBook
    SectionType.USE_OF_ENGLISH -> Icons.Rounded.Spellcheck
    SectionType.WRITING -> Icons.Rounded.EditNote
    SectionType.SPEAKING -> Icons.Rounded.RecordVoiceOver
}

// ======================================================================
// Exam runner
// ======================================================================

private sealed interface MockStage {
    data object Intro : MockStage
    data class SectionIntro(val s: Int) : MockStage
    data class PartIntro(val s: Int, val p: Int) : MockStage
    data class Quiz(val s: Int, val p: Int) : MockStage
    data class Writing(val s: Int) : MockStage
    data class Speaking(val s: Int) : MockStage
    data object Result : MockStage
}

/** Final per-section outcome. [percent] drives the Cambridge scale; [band] is IELTS. */
internal data class SectionScore(val section: MockSection, val percent: Int, val band: Double, val correct: Int, val total: Int)

@Composable
fun MockExamScreen(
    id: String,
    onComplete: (score: Int, correct: Int, skills: List<Triple<SkillKey, Int, Int>>) -> Int,
    onClose: () -> Unit,
) {
    val exam = remember(id) { MockExams.first { it.id == id } }
    var attempt by remember { mutableIntStateOf(0) }
    key(attempt) {
        MockRun(exam, onComplete, onClose, onAgain = { attempt++ })
    }
}

@Composable
private fun MockRun(exam: MockExam, onComplete: (Int, Int, List<Triple<SkillKey, Int, Int>>) -> Int, onClose: () -> Unit, onAgain: () -> Unit) {
    var stage by remember { mutableStateOf<MockStage>(MockStage.Intro) }
    val records = remember { mutableStateMapOf<Int, List<AnswerRecord>>() }
    val ratings = remember { mutableStateMapOf<Int, List<Int>>() }

    // One clock per section, like the real test; it keeps running across parts.
    val sectionIndex = when (val st = stage) {
        is MockStage.SectionIntro -> st.s
        is MockStage.PartIntro -> st.s
        is MockStage.Quiz -> st.s
        is MockStage.Writing -> st.s
        is MockStage.Speaking -> st.s
        else -> -1
    }
    var remaining by remember { mutableIntStateOf(0) }
    LaunchedEffect(sectionIndex) {
        if (sectionIndex < 0) return@LaunchedEffect
        remaining = exam.sections[sectionIndex].minutes * 60
        while (true) {
            delay(1000)
            remaining--
        }
    }

    fun afterSection(s: Int) {
        stage = if (s + 1 < exam.sections.size) MockStage.SectionIntro(s + 1) else MockStage.Result
    }

    fun startSection(s: Int) {
        val section = exam.sections[s]
        stage = when {
            section.autoScored -> MockStage.PartIntro(s, 0)
            section.writing != null -> MockStage.Writing(s)
            else -> MockStage.Speaking(s)
        }
    }

    when (val st = stage) {
        MockStage.Intro -> ExamIntro(exam, onClose) { stage = MockStage.SectionIntro(0) }
        is MockStage.SectionIntro -> SectionIntro(exam, st.s, onClose) { startSection(st.s) }
        is MockStage.PartIntro -> PartIntro(exam, st.s, st.p, remaining, onClose) { stage = MockStage.Quiz(st.s, st.p) }
        is MockStage.Quiz -> {
            val section = exam.sections[st.s]
            val part = section.parts[st.p]
            var passageOpen by remember(st) { mutableStateOf(true) }
            QuizRunner(
                questions = part.questions,
                instantFeedback = false,
                onClose = onClose,
                title = "${clock(remaining)} · ${section.title} · ${part.title}",
                header = part.text?.let { text ->
                    {
                        if (section.type == SectionType.LISTENING) {
                            ReplayCard(text)
                        } else {
                            PassageCard(text, passageOpen) { passageOpen = !passageOpen }
                        }
                        VSpace(16.dp)
                    }
                },
                onFinish = { result ->
                    records[st.s] = records[st.s].orEmpty() + result
                    if (st.p + 1 < section.parts.size) stage = MockStage.PartIntro(st.s, st.p + 1) else afterSection(st.s)
                },
            )
        }
        is MockStage.Writing -> WritingStage(exam, st.s, remaining, onClose) { r -> ratings[st.s] = r; afterSection(st.s) }
        is MockStage.Speaking -> SpeakingStage(exam, st.s, remaining, onClose) { r -> ratings[st.s] = r; afterSection(st.s) }
        MockStage.Result -> {
            val scores = remember {
                exam.sections.mapIndexed { i, section ->
                    if (section.autoScored) {
                        val rec = records[i].orEmpty()
                        val correct = rec.count { it.correct }
                        val total = section.questionCount
                        val percent = if (total == 0) 0 else correct * 100 / total
                        val band = MockScoring.ieltsBand(correct, total, listening = section.type == SectionType.LISTENING)
                        SectionScore(section, percent, band, correct, total)
                    } else {
                        val r = ratings[i].orEmpty()
                        val percent = if (r.isEmpty()) 0 else (r.average() * 100 / 3).toInt()
                        SectionScore(section, percent, MockScoring.selfBand(r), 0, 0)
                    }
                }
            }
            val correct = scores.sumOf { it.correct }
            val stored = remember {
                if (exam.kind == MockKind.IELTS) {
                    (MockScoring.overallIelts(scores.map { it.band }) * 10).toInt()
                } else {
                    MockScoring.cambridgeScale(exam.kind, scores.map { it.percent }.average().toInt())
                }
            }
            val xp = remember {
                val skills = exam.sections.mapIndexed { i, section ->
                    val key = when (section.type) {
                        SectionType.LISTENING -> SkillKey.LISTENING
                        SectionType.READING -> SkillKey.READING
                        SectionType.USE_OF_ENGLISH -> SkillKey.GRAMMAR
                        SectionType.WRITING -> SkillKey.WRITING
                        SectionType.SPEAKING -> SkillKey.SPEAKING
                    }
                    if (section.autoScored) Triple(key, scores[i].correct, scores[i].total)
                    else Triple(key, ratings[i].orEmpty().sum(), ratings[i].orEmpty().size * 3)
                }
                onComplete(stored, correct, skills)
            }
            MockResult(exam, scores, stored, xp, records.values.flatten(), onAgain = onAgain, onDone = onClose)
        }
    }
}

private fun clock(seconds: Int): String {
    val s = seconds.coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}

@Composable
private fun TimerPill(remaining: Int) {
    val over = remaining <= 0
    Pill(
        if (over) "انتهى الوقت" else ltr(clock(remaining)),
        if (over || remaining < 60) Danger else MaterialTheme.colorScheme.primary,
        icon = Icons.Rounded.Timer,
    )
}

// ---------- Intro screens ----------

@Composable
private fun ExamIntro(exam: MockExam, onClose: () -> Unit, onStart: () -> Unit) {
    StageScaffold(exam.title, onClose, button = "ابدأ الاختبار", onButton = onStart) {
        val tint = kindColor(exam.kind)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            IconTile(Icons.Rounded.WorkspacePremium, tint, size = 72.dp)
        }
        VSpace(16.dp)
        Text(isolateLatin(exam.titleAr), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        VSpace(8.dp)
        Text(
            isolateLatin(exam.descriptionAr), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth(),
        )
        SectionHeader("أقسام الاختبار")
        exam.sections.forEachIndexed { i, s ->
            AppCard(modifier = Modifier.padding(vertical = 4.dp), padding = 14.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(s.type.icon(), tint, size = 40.dp)
                    HSpace(12.dp)
                    Column(Modifier.weight(1f)) {
                        Text("${i + 1}. ${s.type.labelAr}", style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (s.autoScored) "${s.questionCount} سؤالاً · تصحيح تلقائي" else "تقييم ذاتي مع نموذج إجابة",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Pill("${ltr("${s.minutes}")} د", tint, icon = Icons.Rounded.Timer)
                }
            }
        }
        VSpace(12.dp)
        AppCard(color = AppTheme.extra.subtle, bordered = false, padding = 14.dp) {
            Text("عن الاختبار الحقيقي", style = MaterialTheme.typography.titleSmall)
            VSpace(4.dp)
            Text(isolateLatin(exam.realFormatAr), style = MaterialTheme.typography.bodySmall)
        }
        VSpace(10.dp)
        Text(
            "نصيحة: اجلس في مكان هادئ، ضع سماعات، ولا تراجع شيئاً خلال الاختبار — هكذا تحصل على تقدير واقعي.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionIntro(exam: MockExam, s: Int, onClose: () -> Unit, onStart: () -> Unit) {
    val section = exam.sections[s]
    StageScaffold("القسم ${s + 1} من ${exam.sections.size}", onClose, button = "ابدأ القسم", onButton = onStart) {
        VSpace(24.dp)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            IconTile(section.type.icon(), kindColor(exam.kind), size = 80.dp)
        }
        VSpace(18.dp)
        Text(section.type.labelAr, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(
            isolateLatin(section.title), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth(),
        )
        VSpace(14.dp)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Pill("الوقت: ${ltr("${section.minutes}")} دقيقة", MaterialTheme.colorScheme.primary, icon = Icons.Rounded.Timer)
        }
        VSpace(20.dp)
        AppCard(color = AppTheme.extra.subtle, bordered = false) {
            Text(isolateLatin(section.instructionsAr), style = MaterialTheme.typography.bodyLarge)
        }
        if (section.parts.size > 1) {
            SectionHeader("الأجزاء")
            section.parts.forEach { p ->
                Text("• ${isolateLatin(p.title)} — ${ltr("${p.questions.size}")} أسئلة", style = MaterialTheme.typography.bodyMedium)
                VSpace(4.dp)
            }
        }
    }
}

@Composable
private fun PartIntro(exam: MockExam, s: Int, p: Int, remaining: Int, onClose: () -> Unit, onStart: () -> Unit) {
    val section = exam.sections[s]
    val part = section.parts[p]
    val listening = section.type == SectionType.LISTENING
    val speaker = LocalSpeaker.current
    var played by remember { mutableStateOf(false) }
    StageScaffold(
        section.title, onClose,
        button = if (listening && part.text != null && !played) "شغّل التسجيل وابدأ" else "إلى الأسئلة",
        onButton = {
            if (listening && part.text != null && !played) speaker.speak(part.text)
            onStart()
        },
        trailing = { TimerPill(remaining) },
    ) {
        Pill("الجزء ${p + 1} من ${section.parts.size}", kindColor(exam.kind))
        VSpace(10.dp)
        AutoText(part.title, style = MaterialTheme.typography.headlineSmall)
        VSpace(12.dp)
        AppCard(color = AppTheme.extra.subtle, bordered = false) {
            Text(isolateLatin(part.instructionsAr), style = MaterialTheme.typography.bodyLarge)
        }
        VSpace(14.dp)
        if (listening && part.text != null) {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(Icons.Rounded.Headphones, MaterialTheme.colorScheme.primary)
                    HSpace(12.dp)
                    Text(
                        "اقرأ الأسئلة أولاً (كما يُسمح في الاختبار الحقيقي)، ثم شغّل التسجيل. سيبدأ التسجيل مع أول سؤال، ويمكنك إعادته مرة واحدة فقط.",
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f),
                    )
                }
                VSpace(12.dp)
                part.questions.forEachIndexed { i, q ->
                    AutoText("${i + 1}. ${com.fluently.english.data.content.Answers.prompt(q)}", style = MaterialTheme.typography.bodySmall)
                    VSpace(4.dp)
                }
            }
        } else if (part.text != null) {
            Text("النص سيظهر أعلى كل سؤال، ويمكنك طيّه وفتحه.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Listening header inside the quiz: one extra replay, like the second hearing in Cambridge. */
@Composable
private fun ReplayCard(script: String) {
    val speaker = LocalSpeaker.current
    var replays by remember(script) { mutableIntStateOf(1) }
    AppCard(color = AppTheme.extra.subtle, bordered = false, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Headphones, null, tint = MaterialTheme.colorScheme.primary)
            HSpace(10.dp)
            Text(
                if (replays > 0) "التسجيل يعمل… يمكنك إعادته مرة واحدة" else "انتهت مرات الإعادة — أجب مما سمعت",
                style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f),
            )
            if (replays > 0) {
                Box(
                    Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary)
                        .clickable { replays--; speaker.speak(script) }.padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text("أعد", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun PassageCard(text: String, open: Boolean, onToggle: () -> Unit) {
    AppCard(modifier = Modifier.animateContentSize(), padding = 16.dp) {
        Row(Modifier.fillMaxWidth().clickable(onClick = onToggle), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Rounded.MenuBook, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            HSpace(8.dp)
            Text("النص", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(if (open) "إخفاء" else "إظهار", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        if (open) {
            VSpace(10.dp)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

// ---------- Writing ----------

@Composable
private fun WritingStage(exam: MockExam, s: Int, remaining: Int, onClose: () -> Unit, onDone: (List<Int>) -> Unit) {
    val section = exam.sections[s]
    val task = section.writing ?: return
    var text by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    val words = remember(text) { text.split(Regex("\\s+")).count { it.any(Char::isLetterOrDigit) } }

    if (!submitted) {
        StageScaffold(
            section.title, onClose,
            button = "سلّم الكتابة", onButton = { submitted = true },
            trailing = { TimerPill(remaining) },
        ) {
            AppCard {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(task.prompt, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth())
                }
                VSpace(10.dp)
                Text(isolateLatin(task.promptAr), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            VSpace(14.dp)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    minLines = 10,
                    placeholder = { Text("Start writing here…") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            VSpace(10.dp)
            val enough = words >= task.minWords
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${ltr("$words / ${task.minWords}")} كلمة",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (enough) Success else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (!enough && words > 0) {
                    Text("أقل من الحد الأدنى يخفض الدرجة", style = MaterialTheme.typography.labelSmall, color = Danger)
                }
            }
            VSpace(6.dp)
            LinearMeter((words.toFloat() / task.minWords).coerceAtMost(1f), height = 6.dp)
        }
    } else {
        val report = remember(text) { WritingCheck.check(text, task.minWords, formal = true) }
        SelfAssessment(
            title = "قيّم كتابتك",
            intro = "صحّحنا نصك آلياً واقترحنا تقييماً مبدئياً. قارن نصك بنموذج الإجابة وعدّل التقييم بصدق — هذه معايير الممتحنين.",
            criteria = WritingCriteria,
            onClose = onClose,
            onDone = onDone,
            initial = if (text.isBlank()) null else report.suggested,
        ) {
            if (text.isNotBlank()) {
                SectionHeader("التصحيح الآلي")
                WritingFeedback(text, report)
            }
            ModelAnswer("نموذج إجابة بمستوى عالٍ", task.modelAnswer)
            SectionHeader("نصائح الممتحن")
            task.tipsAr.forEach {
                Text("• ${isolateLatin(it)}", style = MaterialTheme.typography.bodyMedium)
                VSpace(6.dp)
            }
        }
    }
}

@Composable
private fun ModelAnswer(title: String, answer: String) {
    var open by remember { mutableStateOf(false) }
    SectionHeader(title, action = if (open) "إخفاء" else "إظهار", onAction = { open = !open })
    AppCard(modifier = Modifier.animateContentSize(), padding = 14.dp, onClick = { open = !open }) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(
                answer, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth(),
                maxLines = if (open) Int.MAX_VALUE else 3,
            )
        }
    }
}

// ---------- Speaking ----------

@Composable
private fun SpeakingStage(exam: MockExam, s: Int, remaining: Int, onClose: () -> Unit, onDone: (List<Int>) -> Unit) {
    val section = exam.sections[s]
    var partIndex by remember { mutableIntStateOf(0) }
    if (partIndex < section.speaking.size) {
        key(partIndex) {
            SpeakingPartView(
                section, partIndex, remaining, onClose,
                onNext = { partIndex++ },
            )
        }
    } else {
        SelfAssessment(
            title = "قيّم محادثتك",
            intro = "فكّر في إجاباتك واستمع لنماذج الإجابات، ثم قيّم نفسك في كل معيار من معايير ممتحني المحادثة.",
            criteria = SpeakingCriteria,
            onClose = onClose,
            onDone = onDone,
        ) {
            section.speaking.forEach { part ->
                SampleAnswer(part)
            }
        }
    }
}

@Composable
private fun SampleAnswer(part: SpeakingPart) {
    SectionHeader(isolateLatin(part.title))
    AppCard(padding = 14.dp) {
        Row(verticalAlignment = Alignment.Top) {
            Box(Modifier.weight(1f)) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(part.sample, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth())
                }
            }
            HSpace(8.dp)
            com.fluently.english.ui.components.SpeakButton(part.sample)
        }
    }
}

@Composable
private fun SpeakingPartView(section: MockSection, index: Int, remaining: Int, onClose: () -> Unit, onNext: () -> Unit) {
    val part = section.speaking[index]
    val speaker = LocalSpeaker.current
    var q by remember { mutableIntStateOf(0) }
    val isCue = part.cueCard.isNotEmpty()
    val last = index == section.speaking.size - 1

    // Cue-card timing: 0 = not started, 1 = preparing, 2 = talking, 3 = done.
    var phase by remember { mutableIntStateOf(0) }
    var left by remember { mutableIntStateOf(0) }
    LaunchedEffect(phase) {
        when (phase) {
            1 -> { left = part.prepSeconds; while (left > 0) { delay(1000); left-- }; phase = 2 }
            2 -> { left = part.talkSeconds; while (left > 0) { delay(1000); left-- }; phase = 3 }
        }
    }
    val heard = remember { mutableStateListOf<String>() }
    val speech = rememberSpeechInput { results -> results.firstOrNull()?.let { heard += it } }

    LaunchedEffect(q) { if (!isCue) speaker.speak(part.questions[q]) }

    val doneHere = if (isCue) phase == 3 else q == part.questions.size - 1
    StageScaffold(
        section.title, onClose,
        button = when {
            !doneHere && !isCue -> "السؤال التالي"
            !doneHere -> "تخطَّ إلى النهاية"
            last -> "إنهاء المحادثة"
            else -> "الجزء التالي"
        },
        onButton = {
            when {
                !doneHere && !isCue -> q++
                !doneHere -> phase = 3
                else -> onNext()
            }
        },
        trailing = { TimerPill(remaining) },
    ) {
        Pill("الجزء ${index + 1} من ${section.speaking.size}", MaterialTheme.colorScheme.primary)
        VSpace(10.dp)
        AutoText(part.title, style = MaterialTheme.typography.headlineSmall)
        VSpace(8.dp)
        Text(isolateLatin(part.instructionsAr), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        VSpace(16.dp)

        if (isCue) {
            AppCard {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Column {
                        Text(part.questions.first(), style = MaterialTheme.typography.titleMedium)
                        VSpace(8.dp)
                        Text("You should say:", style = MaterialTheme.typography.bodyMedium)
                        part.cueCard.forEach { Text("•  $it", style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }
            VSpace(16.dp)
            val (label, total) = when (phase) {
                1 -> "وقت التحضير — دوّن كلمات مفتاحية" to part.prepSeconds
                2 -> "تحدث الآن بصوت عالٍ!" to part.talkSeconds
                3 -> "انتهى الوقت — أحسنت" to 1
                else -> "عندما تكون جاهزاً ابدأ دقيقة التحضير" to 1
            }
            AppCard(color = AppTheme.extra.subtle, bordered = false) {
                Text(label, style = MaterialTheme.typography.titleSmall)
                if (phase in 1..2) {
                    VSpace(8.dp)
                    Text(ltr(clock(left)), style = MaterialTheme.typography.displaySmall, color = if (phase == 2) Coral else MaterialTheme.colorScheme.primary)
                    VSpace(8.dp)
                    LinearMeter(left.toFloat() / total, height = 8.dp)
                }
            }
            VSpace(12.dp)
            if (phase == 0) SecondaryButton("ابدأ التحضير (${ltr("${part.prepSeconds}")} ث)", onClick = { phase = 1 })
            if (phase == 1) GhostButton("أنا جاهز — ابدأ الحديث", onClick = { phase = 2 })
        } else {
            Text("سؤال ${ltr("${q + 1}/${part.questions.size}")}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            VSpace(8.dp)
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .clickable { speaker.speak(part.questions[q]) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    HSpace(12.dp)
                    Box(Modifier.weight(1f)) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(part.questions[q], style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
            VSpace(10.dp)
            Text(
                "أجب بصوت عالٍ في 2–3 جمل: أجب ← علّل ← أعطِ مثالاً.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (speech.available) {
            VSpace(16.dp)
            AppCard(padding = 14.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(44.dp).clip(CircleShape).background(Coral).clickable { speech.start() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Mic, null, tint = Color.White)
                    }
                    HSpace(12.dp)
                    Text(
                        "سجّل إجابتك لترى ما فهمه الجهاز وكم كلمة قلت",
                        style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f),
                    )
                }
                heard.lastOrNull()?.let { said ->
                    val n = said.split(" ").count { it.isNotBlank() }
                    VSpace(10.dp)
                    AutoText("«$said»", style = MaterialTheme.typography.bodyMedium)
                    VSpace(4.dp)
                    Text(
                        "${ltr("$n")} كلمة — " + if (n >= 20) "إجابة موسعة ممتازة" else "حاول أن توسّع إجابتك أكثر (20 كلمة فأكثر)",
                        style = MaterialTheme.typography.labelMedium, color = if (n >= 20) Success else Gold,
                    )
                }
            }
        }
    }
}

// ---------- Self-assessment ----------

private val RatingLabels = listOf("ضعيف", "مقبول", "جيد", "ممتاز")

@Composable
private fun SelfAssessment(
    title: String,
    intro: String,
    criteria: List<Pair<String, String>>,
    onClose: () -> Unit,
    onDone: (List<Int>) -> Unit,
    initial: List<Int>? = null,
    content: @Composable () -> Unit,
) {
    val picks = remember { mutableStateListOf<Int?>().apply { repeat(criteria.size) { add(initial?.getOrNull(it)) } } }
    StageScaffold(
        title, onClose,
        button = "متابعة", enabled = picks.all { it != null },
        onButton = { onDone(picks.map { it ?: 0 }) },
    ) {
        Text(intro, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
        SectionHeader("تقييمك")
        criteria.forEachIndexed { i, (name, desc) ->
            AppCard(modifier = Modifier.padding(vertical = 5.dp), padding = 14.dp) {
                Text(isolateLatin(name), style = MaterialTheme.typography.titleSmall)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                VSpace(10.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RatingLabels.forEachIndexed { r, label ->
                        val selected = picks[i] == r
                        Box(
                            Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                .background(if (selected) MaterialTheme.colorScheme.primary else AppTheme.extra.subtle)
                                .clickable { picks[i] = r }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                label, style = MaterialTheme.typography.labelLarge,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------- Result ----------

@Composable
internal fun MockResult(
    exam: MockExam,
    scores: List<SectionScore>,
    stored: Int,
    xp: Int,
    records: List<AnswerRecord>,
    onAgain: () -> Unit,
    onDone: () -> Unit,
) {
    val extra = AppTheme.extra
    val ielts = exam.kind == MockKind.IELTS
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        VSpace(40.dp)
        Column(
            Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(extra.hero).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(isolateLatin(exam.titleAr), style = MaterialTheme.typography.labelLarge, color = extra.onHeroMuted, textAlign = TextAlign.Center)
            VSpace(12.dp)
            if (ielts) {
                val band = stored / 10.0
                Text("Overall Band", style = MaterialTheme.typography.titleSmall, color = extra.onHeroMuted)
                Text(ltr("$band"), style = MaterialTheme.typography.displayLarge, color = extra.onHero)
                VSpace(6.dp)
                Text(ieltsVerdict(band), style = MaterialTheme.typography.bodyMedium, color = extra.onHeroMuted, textAlign = TextAlign.Center)
            } else {
                val (grade, cefr) = MockScoring.cambridgeGrade(exam.kind, stored)
                Text("Cambridge English Scale", style = MaterialTheme.typography.titleSmall, color = extra.onHeroMuted)
                Text(ltr("$stored"), style = MaterialTheme.typography.displayLarge, color = extra.onHero)
                VSpace(6.dp)
                Text(ltr(grade), style = MaterialTheme.typography.titleMedium, color = extra.onHero)
                Text("المستوى: ${ltr(cefr)}", style = MaterialTheme.typography.bodyMedium, color = extra.onHeroMuted)
            }
            VSpace(14.dp)
            Pill("${ltr("+$xp")} نقطة خبرة", Gold, solid = true)
        }

        SectionHeader("نتائج الأقسام")
        scores.forEach { sc ->
            AppCard(modifier = Modifier.padding(vertical = 4.dp), padding = 14.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(sc.section.type.icon(), kindColor(exam.kind), size = 40.dp)
                    HSpace(12.dp)
                    Column(Modifier.weight(1f)) {
                        Text(sc.section.type.labelAr, style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (sc.section.autoScored) "${ltr("${sc.correct}/${sc.total}")} إجابة صحيحة" else "تقييم ذاتي",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        ltr(if (ielts) "${sc.band}" else "${MockScoring.cambridgeScale(exam.kind, sc.percent)}"),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                VSpace(8.dp)
                LinearMeter(sc.percent / 100f, height = 6.dp)
            }
        }
        VSpace(12.dp)
        AppCard(color = AppTheme.extra.subtle, bordered = false, padding = 14.dp) {
            Text(
                if (ielts) {
                    "تُحسب درجة الاستماع والقراءة بتحويل إجاباتك إلى مقياس من 40 سؤالاً ثم إلى Band وفق جداول IELTS المنشورة. " +
                        "الدرجة الكلية متوسط المهارات الأربع مقرّباً لأقرب نصف درجة. هذا تقدير تدريبي وليس نتيجة رسمية."
                } else {
                    "كل مهارة تساوي نفس الوزن كما في امتحانات كامبريدج، وتُحوَّل النسبة إلى Cambridge English Scale. " +
                        "هذا تقدير تدريبي وليس نتيجة رسمية."
                }.let(::isolateLatin),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        exam.sections.filter { it.type == SectionType.LISTENING }.flatMap { it.parts }.filter { it.text != null }.forEach { part ->
            Transcript(part.title, part.text!!)
        }

        MistakesReview(records)
        VSpace(28.dp)
        PrimaryButton("انتهيت", onDone)
        VSpace(10.dp)
        SecondaryButton("أعد الاختبار", onAgain)
        VSpace(28.dp)
    }
}

@Composable
private fun Transcript(title: String, script: String) {
    var open by remember { mutableStateOf(false) }
    SectionHeader("نص التسجيل · ${isolateLatin(title)}", action = if (open) "إخفاء" else "إظهار", onAction = { open = !open })
    if (open) {
        AppCard(padding = 14.dp) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(script, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private fun ieltsVerdict(band: Double): String = when {
    band >= 8.0 -> "مستخدم متمكن جداً · Very good user · يعادل C2"
    band >= 7.0 -> "مستخدم جيد · Good user · يعادل C1"
    band >= 5.5 -> "مستخدم متوسط إلى جيد · Competent · يعادل B2"
    band >= 4.0 -> "مستخدم محدود · Modest · يعادل B1"
    else -> "مستخدم مبتدئ — ابدأ بدروس المستويات الأولى"
}.let(::isolateLatin)

// ---------- Scaffold ----------

@Composable
private fun StageScaffold(
    title: String,
    onClose: () -> Unit,
    button: String,
    onButton: () -> Unit,
    enabled: Boolean = true,
    trailing: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding()) {
        ScreenHeader(title, onBack = onClose, close = true, trailing = trailing)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp)) {
            content()
            VSpace(16.dp)
        }
        Box(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            PrimaryButton(button, onButton, enabled = enabled, icon = Icons.AutoMirrored.Rounded.ArrowForward)
        }
    }
}
