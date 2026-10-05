package com.fluently.english.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Spellcheck
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.fluently.english.data.content.IssueKind
import com.fluently.english.data.content.WritingCheck
import com.fluently.english.data.content.WritingPrompt
import com.fluently.english.data.content.WritingPrompts
import com.fluently.english.data.content.WritingReport
import com.fluently.english.data.progress.Progress
import com.fluently.english.ui.components.AppCard
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.IconTile
import com.fluently.english.ui.components.LevelBadge
import com.fluently.english.ui.components.LinearMeter
import com.fluently.english.ui.components.Pill
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.ScreenHeader
import com.fluently.english.ui.components.SecondaryButton
import com.fluently.english.ui.components.SectionHeader
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.components.isolateLatin
import com.fluently.english.ui.components.ltr
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Coral
import com.fluently.english.ui.theme.Danger
import com.fluently.english.ui.theme.Gold
import com.fluently.english.ui.theme.Success
import com.fluently.english.ui.theme.color

private val CriteriaNames = listOf("تحقيق المهمة", "الترابط", "المفردات", "القواعد")

// ======================================================================
// Feedback panel (also used after mock exam writing)
// ======================================================================

@Composable
fun WritingFeedback(text: String, report: WritingReport) {
    // Scores at a glance.
    AppCard {
        Row {
            Stat("${report.words}", "كلمة", Modifier.weight(1f))
            Stat("${report.paragraphs}", "فقرة", Modifier.weight(1f))
            Stat("${report.linkers.size}", "أداة ربط", Modifier.weight(1f))
            Stat("${report.errorCount}", "خطأ", Modifier.weight(1f), if (report.errorCount == 0) Success else Danger)
        }
        VSpace(14.dp)
        report.suggested.forEachIndexed { i, v ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(CriteriaNames[i], style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                Text(listOf("ضعيف", "مقبول", "جيد", "ممتاز")[v], style = MaterialTheme.typography.labelMedium, color = ratingColor(v))
            }
            VSpace(4.dp)
            LinearMeter((v + 1) / 4f, height = 6.dp, color = ratingColor(v))
            VSpace(10.dp)
        }
        Text(
            "تقدير آلي مبدئي: يقيس الطول والتنظيم والتنوع والأخطاء الشائعة، ولا يقيّم الأفكار كما يفعل الممتحن.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (report.strengths.isNotEmpty()) {
        SectionHeader("نقاط قوتك")
        AppCard(color = Success.copy(alpha = 0.08f), bordered = false, padding = 14.dp) {
            report.strengths.forEach {
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 3.dp)) {
                    Icon(Icons.Rounded.ThumbUp, null, tint = Success, modifier = Modifier.size(18.dp))
                    HSpace(8.dp)
                    Text(isolateLatin(it), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    if (report.issues.isNotEmpty()) {
        SectionHeader("ما يمكن تحسينه · ${report.issues.size}")
        // The learner's text with problem spots marked.
        val marks = report.issues.mapNotNull { it.excerpt }.filter { it.length >= 2 }.distinct()
        if (marks.isNotEmpty() && text.isNotBlank()) {
            AppCard(padding = 14.dp) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(highlight(text, marks), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth())
                }
            }
            VSpace(8.dp)
        }
        report.issues.groupBy { it.kind }.forEach { (kind, list) ->
            AppCard(modifier = Modifier.padding(vertical = 4.dp), padding = 14.dp) {
                Text(kind.labelAr, style = MaterialTheme.typography.titleSmall, color = kindColor(kind))
                list.forEach { issue ->
                    VSpace(8.dp)
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Rounded.ErrorOutline, null, tint = kindColor(kind), modifier = Modifier.size(18.dp))
                        HSpace(8.dp)
                        Column(Modifier.weight(1f)) {
                            Text(isolateLatin(issue.messageAr), style = MaterialTheme.typography.bodyMedium)
                            if (issue.excerpt != null || issue.fix != null) {
                                VSpace(4.dp)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    issue.excerpt?.let { Pill("✗ ${ltr(it.trim())}", Danger) }
                                    issue.fix?.let { Pill("✓ ${ltr(it)}", Success) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun ratingColor(v: Int) = when (v) {
    3 -> Success
    2 -> Gold
    else -> Coral
}

private fun kindColor(kind: IssueKind) = when (kind) {
    IssueKind.GRAMMAR, IssueKind.MECHANICS -> Danger
    IssueKind.LENGTH, IssueKind.STRUCTURE -> Coral
    else -> Gold
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun highlight(text: String, marks: List<String>) = buildAnnotatedString {
    val regex = Regex(marks.sortedByDescending { it.length }.joinToString("|") { Regex.escape(it.trim()) }, RegexOption.IGNORE_CASE)
    var last = 0
    regex.findAll(text).forEach { m ->
        append(text.substring(last, m.range.first))
        withStyle(SpanStyle(background = Danger.copy(alpha = 0.14f), color = Danger, textDecoration = TextDecoration.Underline)) { append(m.value) }
        last = m.range.last + 1
    }
    append(text.substring(last))
}

// ======================================================================
// Writing lab
// ======================================================================

@Composable
fun WritingLabScreen(progress: Progress, onBack: () -> Unit, onOpen: (String) -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        ScreenHeader("مختبر الكتابة", onBack = onBack, subtitle = "اكتب واحصل على تصحيح فوري مجاني")
        Column(Modifier.padding(horizontal = 20.dp)) {
            VSpace(8.dp)
            AppCard(color = AppTheme.extra.subtle, bordered = false) {
                Text(
                    "اختر موضوعاً بمستواك واكتب. يفحص التطبيق نصك فوراً — حتى بدون إنترنت: الطول والفقرات وأدوات الربط والكلمات المكررة والأخطاء الشائعة (he go، a apple، discuss about…) — ويعلّم الأخطاء داخل نصك.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            WritingPrompts.groupBy { it.level }.forEach { (level, prompts) ->
                SectionHeader("${level.code} · ${level.titleAr}")
                prompts.forEach { p ->
                    val done = p.id in progress.writingDone
                    AppCard(onClick = { onOpen(p.id) }, padding = 14.dp, modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconTile(Icons.Rounded.EditNote, level.color(), size = 42.dp)
                            HSpace(12.dp)
                            Column(Modifier.weight(1f)) {
                                Text(p.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${p.typeAr} · ${ltr("${p.minWords}+")} كلمة",
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (done) Icon(Icons.Rounded.CheckCircle, null, tint = Success)
                            else Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            VSpace(24.dp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WritingTaskScreen(id: String, onChecked: (words: Int, rating: Int) -> Int, onBack: () -> Unit) {
    val prompt: WritingPrompt = remember(id) { WritingPrompts.firstOrNull { it.id == id } } ?: run { onBack(); return }
    var text by rememberSaveable(id) { mutableStateOf("") }
    var report by remember { mutableStateOf<WritingReport?>(null) }
    var xp by remember { mutableStateOf<Int?>(null) }
    val words = remember(text) { WritingCheck.words(text).size }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding()) {
        ScreenHeader(prompt.title, onBack = onBack, subtitle = prompt.typeAr)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            VSpace(8.dp)
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LevelBadge(prompt.level, size = 32.dp)
                    HSpace(10.dp)
                    Text(isolateLatin(prompt.promptAr), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
                VSpace(10.dp)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(prompt.prompt, style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth())
                }
            }
            SectionHeader("ما يجب أن تتضمنه")
            prompt.pointsAr.forEach {
                Text("• ${isolateLatin(it)}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp))
            }
            SectionHeader("عبارات مفيدة — اضغط لإضافتها")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                prompt.phrases.forEach { phrase ->
                    Box(
                        Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                            .clickable {
                                val core = phrase.replace("…", "").trim()
                                text = (if (text.isBlank() || text.endsWith(" ") || text.endsWith("\n")) text else "$text ") + core + " "
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(ltr(phrase), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            VSpace(16.dp)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it; report = null },
                    minLines = 8,
                    placeholder = { Text("Start writing here… (leave an empty line between paragraphs)") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            VSpace(8.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${ltr("$words / ${prompt.minWords}")} كلمة",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (words >= prompt.minWords) Success else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Box(Modifier.size(10.dp).clip(CircleShape).background(if (words >= prompt.minWords) Success else AppTheme.extra.border))
            }
            VSpace(6.dp)
            LinearMeter((words.toFloat() / prompt.minWords).coerceAtMost(1f), height = 6.dp)

            report?.let { r ->
                VSpace(16.dp)
                xp?.let { Pill("${ltr("+$it")} نقطة خبرة", Gold, solid = true) }
                WritingFeedback(text, r)
            }
            VSpace(20.dp)
        }
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            if (report == null) {
                PrimaryButton(
                    "صحّح نصي", enabled = words >= 5, icon = Icons.Rounded.Spellcheck,
                    onClick = {
                        val r = WritingCheck.check(text, prompt.minWords, prompt.formal)
                        report = r
                        xp = onChecked(r.words, r.suggested.sum())
                    },
                )
            } else {
                SecondaryButton("عدّل نصك ثم صحّح مرة أخرى", onClick = { report = null })
            }
        }
    }
}
