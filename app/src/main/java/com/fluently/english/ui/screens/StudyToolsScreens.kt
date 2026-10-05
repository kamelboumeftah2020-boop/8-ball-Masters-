package com.fluently.english.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fluently.english.data.content.Answers
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.Course
import com.fluently.english.data.content.IrregularVerbGroups
import com.fluently.english.data.content.IrregularVerbs
import com.fluently.english.data.content.LessonType
import com.fluently.english.data.content.Question
import com.fluently.english.data.progress.Progress
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.ui.components.AppCard
import com.fluently.english.ui.components.AutoText
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.IconTile
import com.fluently.english.ui.components.LevelBadge
import com.fluently.english.ui.components.Ltr
import com.fluently.english.ui.components.Pill
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.QuizRunner
import com.fluently.english.ui.components.ScreenHeader
import com.fluently.english.ui.components.SectionHeader
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Danger
import com.fluently.english.ui.theme.Success

// ---------------- Mistakes notebook ----------------

private fun questionFor(key: String): Question? {
    val (lessonId, index) = key.split("#").let { it[0] to it.getOrNull(1)?.toIntOrNull() }
    return index?.let { Course.lesson(lessonId)?.questions?.getOrNull(it) }
}

@Composable
fun MistakesScreen(progress: Progress, onResolve: (List<String>) -> Int, onClose: () -> Unit) {
    var reviewing by remember { mutableStateOf(false) }
    var round by remember { mutableIntStateOf(0) }
    var result by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var xp by remember { mutableIntStateOf(0) }
    val items = remember(round, reviewing) {
        progress.mistakes.mapNotNull { k -> questionFor(k)?.let { k to it } }.shuffled().take(15)
    }

    val r = result
    when {
        r != null -> PracticeResult(
            percent = r.first * 100 / r.second.coerceAtLeast(1),
            title = "أحسنت المراجعة!",
            subtitle = "صححت ${r.first} من ${r.second} أخطاء وأُزيلت من دفترك",
            xp = xp,
            onAgain = { result = null; round++; reviewing = progress.mistakes.isNotEmpty() },
            onDone = onClose,
        )
        reviewing && items.isNotEmpty() -> key(round) {
            QuizRunner(
                questions = items.map { it.second },
                instantFeedback = true,
                title = "دفتر أخطائي",
                onClose = { reviewing = false },
                onFinish = { records ->
                    val fixed = records.mapIndexedNotNull { i, rec -> if (rec.correct) items[i].first else null }
                    xp = onResolve(fixed)
                    result = fixed.size to records.size
                    reviewing = false
                },
            )
        }
        else -> Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            ScreenHeader("دفتر أخطائي", onBack = onClose)
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(20.dp)) {
                item {
                    AppCard(color = Danger.copy(alpha = 0.08f), bordered = false) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconTile(Icons.Rounded.EditNote, Danger, size = 46.dp, background = MaterialTheme.colorScheme.surface)
                            HSpace(14.dp)
                            Column {
                                Text(
                                    if (progress.mistakes.isEmpty()) "دفترك نظيف!" else "${progress.mistakes.size} سؤالاً أخطأت فيه",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    "كل سؤال تخطئ فيه في الدروس يُحفظ هنا تلقائياً. أجب عنه صحيحاً ليختفي من الدفتر. التعلم من الأخطاء من أقوى طرق التعلم.",
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    VSpace(16.dp)
                }
                if (progress.mistakes.isEmpty()) {
                    item {
                        Column(Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            IconTile(Icons.Rounded.TaskAlt, Success, size = 72.dp)
                            VSpace(12.dp)
                            Text("لا توجد أخطاء محفوظة", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "أكمل الدروس، وأي سؤال تخطئ فيه سيظهر هنا للمراجعة.",
                                style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    item { SectionHeader("آخر الأخطاء") }
                    items(progress.mistakes.toList().take(30)) { k ->
                        val q = questionFor(k) ?: return@items
                        val lesson = Course.lesson(k.substringBefore("#"))
                        AppCard(padding = 14.dp, modifier = Modifier.padding(vertical = 4.dp)) {
                            if (lesson != null) {
                                Text(
                                    "${lesson.level.code} · ${lesson.titleAr}", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                VSpace(4.dp)
                            }
                            AutoText(Answers.prompt(q), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            if (items.isNotEmpty()) {
                Column(Modifier.navigationBarsPadding()) {
                    HorizontalDivider(color = AppTheme.extra.border)
                    Box(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                        PrimaryButton("راجع ${items.size} أسئلة الآن", { reviewing = true })
                    }
                }
            }
        }
    }
}

// ---------------- Irregular verbs ----------------

@Composable
fun VerbsScreen(onComplete: (Int) -> Int, onClose: () -> Unit) {
    var quiz by remember { mutableStateOf(false) }
    var round by remember { mutableIntStateOf(0) }
    var result by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var xp by remember { mutableIntStateOf(0) }
    val speaker = LocalSpeaker.current

    val r = result
    when {
        r != null -> PracticeResult(
            percent = r.first * 100 / r.second,
            title = if (r.first == r.second) "ممتاز! حفظتها كلها" else "أحسنت!",
            subtitle = "${r.first} من ${r.second} إجابات صحيحة",
            xp = xp,
            onAgain = { result = null; round++; quiz = true },
            onDone = onClose,
        )
        quiz -> key(round) {
            val questions = remember {
                IrregularVerbs.shuffled().take(10).mapIndexed { i, v ->
                    if (i % 2 == 0) {
                        Question.Typing(
                            "اكتب الماضي البسيط من «${v.base}» (${v.ar})",
                            v.past.split(" / "),
                            explanation = "${v.base} → ${v.past} → ${v.participle}",
                        )
                    } else {
                        val others = IrregularVerbs.filter { it != v }.shuffled().take(3).map { it.participle }
                        Question.Choice(
                            "ما التصريف الثالث (past participle) لـ «${v.base}»؟",
                            (listOf(v.participle) + others).distinct(),
                            explanation = "${v.base} → ${v.past} → ${v.participle}",
                        )
                    }
                }
            }
            QuizRunner(
                questions = questions,
                instantFeedback = true,
                title = "اختبار الأفعال الشاذة",
                onClose = { quiz = false },
                onFinish = { records ->
                    val correct = records.count { it.correct }
                    xp = onComplete(correct)
                    result = correct to records.size
                },
            )
        }
        else -> Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            ScreenHeader("الأفعال الشاذة", onBack = onClose, subtitle = "مجمّعة حسب النمط لتسهيل الحفظ")
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp)) {
                item {
                    AppCard(color = MaterialTheme.colorScheme.tertiaryContainer, bordered = false, modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            "بدل حفظ قائمة طويلة عشوائية، احفظ الأفعال في مجموعات لها نفس النمط. اضغط على أي فعل لتسمع أشكاله الثلاثة.",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                }
                IrregularVerbGroups.forEach { (group, verbs) ->
                    item { SectionHeader(group) }
                    item {
                        AppCard(padding = 0.dp) {
                            Ltr {
                                Row(Modifier.background(AppTheme.extra.subtle).padding(horizontal = 14.dp, vertical = 8.dp)) {
                                    listOf("Base", "Past", "Participle").forEach {
                                        Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                    }
                                    Box(Modifier.size(18.dp))
                                }
                            }
                            verbs.forEachIndexed { i, v ->
                                if (i > 0) HorizontalDivider(color = AppTheme.extra.border)
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable { speaker.speak("${v.base}. ${v.past}. ${v.participle}.", 0.85f) }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                ) {
                                    Ltr {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(v.base, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                            Text(v.past, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                                            Text(v.participle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.weight(1f))
                                            Icon(Icons.AutoMirrored.Rounded.VolumeUp, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Text(v.ar, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
            Column(Modifier.navigationBarsPadding()) {
                HorizontalDivider(color = AppTheme.extra.border)
                Box(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    PrimaryButton("اختبر نفسك · 10 أسئلة", { quiz = true })
                }
            }
        }
    }
}

// ---------------- Grammar reference ----------------

@Composable
fun GrammarReferenceScreen(onBack: () -> Unit, onOpen: (String) -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            ScreenHeader("مرجع القواعد", onBack = onBack, subtitle = "ارجع لأي شرح في أي وقت")
        }
        CefrLevel.entries.forEach { level ->
            item {
                Row(Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    LevelBadge(level, size = 32.dp, filled = true)
                    HSpace(10.dp)
                    Text(level.titleAr, style = MaterialTheme.typography.titleMedium)
                }
            }
            items(Course.level(level).lessons.filter { it.type == LessonType.GRAMMAR }) { lesson ->
                AppCard(
                    onClick = { onOpen(lesson.id) }, padding = 14.dp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                ) {
                    Text(lesson.titleAr, style = MaterialTheme.typography.titleSmall)
                    Ltr { Text(lesson.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth()) }
                }
            }
        }
    }
}

@Composable
fun GuideScreen(lessonId: String, onClose: () -> Unit) {
    val lesson = remember(lessonId) { Course.lesson(lessonId) } ?: return
    LearnPlayer(lesson, onClose = onClose, onStart = onClose, finishLabel = "تم")
}

// ---------------- Daily challenge ----------------

/** Five questions drawn from lessons the learner has studied, different every day. */
fun dailyQuestions(progress: Progress, day: Long): List<Question> {
    val studied = Course.levels.flatMap { it.lessons }.filter { progress.isLessonDone(it.id) }
    val source = studied.ifEmpty { Course.level(progress.currentLevel).lessons.take(3) }
    return source.flatMap { it.questions }
        .filter { it is Question.Choice || it is Question.Order || it is Question.Typing }
        .shuffled(kotlin.random.Random(day))
        .take(5)
}

@Composable
fun DailyChallengeScreen(progress: Progress, today: Long, onComplete: (Int) -> Int, onClose: () -> Unit) {
    val questions = remember { dailyQuestions(progress, today) }
    val alreadyDone = remember { progress.challengeDoneToday(today) }
    var result by remember { mutableStateOf<Int?>(null) }
    var xp by remember { mutableIntStateOf(0) }
    var round by remember { mutableIntStateOf(0) }
    val r = result
    if (r != null) {
        PracticeResult(
            percent = r * 100 / questions.size,
            title = if (alreadyDone) "تدريب إضافي رائع!" else "أنجزت تحدي اليوم! 🎯",
            subtitle = "$r من ${questions.size} إجابات صحيحة" + if (!alreadyDone) " · مكافأة يومية 30 نقطة" else "",
            xp = xp, onAgain = { result = null; round++ }, onDone = onClose,
        )
    } else {
        key(round) {
            QuizRunner(
                questions = questions,
                instantFeedback = true,
                title = "تحدي اليوم",
                onClose = onClose,
                onFinish = { records ->
                    val correct = records.count { it.correct }
                    xp = onComplete(correct)
                    result = correct
                },
            )
        }
    }
}

