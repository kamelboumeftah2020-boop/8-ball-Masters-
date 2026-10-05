package com.fluently.english.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.GradedReader
import com.fluently.english.data.content.Readers
import com.fluently.english.data.progress.Progress
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.ui.components.AppCard
import com.fluently.english.ui.components.AutoText
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.InteractiveText
import com.fluently.english.ui.components.LevelBadge
import com.fluently.english.ui.components.LinearMeter
import com.fluently.english.ui.components.Pill
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.QuizRunner
import com.fluently.english.ui.components.ScreenHeader
import com.fluently.english.ui.components.SecondaryButton
import com.fluently.english.ui.components.SectionHeader
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.components.isolateLatin
import com.fluently.english.ui.components.sentencesOf
import com.fluently.english.data.content.activities
import com.fluently.english.ui.components.ltr
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Emerald
import com.fluently.english.ui.theme.Gold
import com.fluently.english.ui.theme.color

private fun Progress.chaptersRead(r: GradedReader) = (readerChapters[r.id] ?: 0).coerceAtMost(r.chapters.size)

// ======================================================================
// Library
// ======================================================================

@Composable
fun ReaderListScreen(progress: Progress, onBack: () -> Unit, onOpen: (String) -> Unit) {
    var filter by rememberSaveable { mutableStateOf<CefrLevel?>(null) }
    val finished = Readers.count { progress.chaptersRead(it) == it.chapters.size }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        ScreenHeader("مكتبة القصص", onBack = onBack, subtitle = "قصص متدرّجة لكل مستوى")
        Column(Modifier.padding(horizontal = 20.dp)) {
            VSpace(8.dp)
            val extra = AppTheme.extra
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(extra.hero).padding(20.dp)) {
                Text("القراءة الحرة", style = MaterialTheme.typography.labelLarge, color = extra.onHeroMuted)
                Text(
                    "${ltr("%,d".format(progress.wordsRead))} كلمة قرأتها",
                    style = MaterialTheme.typography.headlineSmall, color = extra.onHero,
                )
                VSpace(6.dp)
                Text(
                    "اقرأ قصصاً بمستواك أو أسهل قليلاً: تفهم 95% من الكلمات وتلتقط الباقي من السياق — هكذا تكبر حصيلتك دون حفظ. اضغط على الكلمة الملوّنة لمعناها، وعلى أي جملة لتسمعها.",
                    style = MaterialTheme.typography.bodySmall, color = extra.onHeroMuted,
                )
                VSpace(10.dp)
                Pill("أنهيت $finished من ${Readers.size} قصة", Gold, solid = true)
            }
            VSpace(14.dp)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip("الكل", filter == null) { filter = null }
                CefrLevel.entries.forEach { level -> FilterChip(level.code, filter == level) { filter = level } }
            }
            VSpace(8.dp)
            val shown = Readers.filter { filter == null || it.level == filter }
            if (shown.isEmpty()) {
                VSpace(24.dp)
                Text("لا توجد قصص بعد.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            shown.groupBy { it.level }.forEach { (level, readers) ->
                SectionHeader("${level.code} · ${level.titleAr}")
                readers.forEach { r ->
                    BookRow(r, progress.chaptersRead(r)) { onOpen(r.id) }
                    VSpace(10.dp)
                }
            }
            VSpace(24.dp)
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun BookCover(r: GradedReader, width: Int, height: Int) {
    val c = r.level.color()
    Box(
        Modifier
            .size(width.dp, height.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(listOf(c, c.copy(alpha = 0.65f)))),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.AutoMirrored.Rounded.MenuBook, null, tint = Color.White, modifier = Modifier.size((width / 3).dp))
            VSpace(4.dp)
            Text(r.level.code, style = MaterialTheme.typography.labelLarge, color = Color.White)
        }
    }
}

@Composable
private fun BookRow(r: GradedReader, read: Int, onClick: () -> Unit) {
    AppCard(onClick = onClick, padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BookCover(r, 56, 76)
            HSpace(14.dp)
            Column(Modifier.weight(1f)) {
                Text(isolateLatin(r.titleAr), style = MaterialTheme.typography.titleSmall)
                Text(ltr(r.title), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                VSpace(6.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pill(r.genreAr, r.level.color())
                    Text(
                        "${ltr("${r.wordCount}")} كلمة",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                VSpace(8.dp)
                LinearMeter(read.toFloat() / r.chapters.size, height = 5.dp)
            }
            if (read == r.chapters.size) {
                HSpace(8.dp)
                Box(Modifier.size(26.dp).clip(CircleShape).background(Emerald), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ======================================================================
// Reading a story
// ======================================================================

private sealed interface ReaderStage {
    data object Cover : ReaderStage
    data class Read(val ch: Int) : ReaderStage
    data class Quiz(val ch: Int, val seconds: Int) : ReaderStage
    data class Done(val ch: Int, val right: Int, val total: Int, val xp: Int, val wpm: Int) : ReaderStage
}

@Composable
fun ReaderScreen(
    id: String,
    progress: Progress,
    onComplete: (chapter: Int, words: Int, right: Int, total: Int) -> Int,
    onClose: () -> Unit,
    onAddWord: (String) -> Unit = {},
) {
    val reader = remember(id) { Readers.firstOrNull { it.id == id } } ?: run { onClose(); return }
    var stage by remember { mutableStateOf<ReaderStage>(ReaderStage.Cover) }
    when (val st = stage) {
        ReaderStage.Cover -> ReaderCover(reader, progress.chaptersRead(reader), onClose) { stage = ReaderStage.Read(it) }
        is ReaderStage.Read -> ChapterView(reader, st.ch, onClose = { stage = ReaderStage.Cover }, onAddWord = onAddWord) {
            stage = ReaderStage.Quiz(st.ch, it)
        }
        is ReaderStage.Quiz -> {
            val chapter = reader.chapters[st.ch]
            val questions = remember(st) { chapter.questions + chapter.activities() }
            QuizRunner(
                questions = questions,
                instantFeedback = true,
                onClose = { stage = ReaderStage.Cover },
                title = "${reader.title} · ${chapter.title}",
                onFinish = { records ->
                    val right = records.count { it.correct }
                    val xp = onComplete(st.ch, chapter.wordCount, right, records.size)
                    val wpm = if (st.seconds < 20) 0 else chapter.wordCount * 60 / st.seconds
                    stage = ReaderStage.Done(st.ch, right, records.size, xp, wpm)
                },
            )
        }
        is ReaderStage.Done -> {
            val last = st.ch == reader.chapters.lastIndex
            PracticeResult(
                percent = if (st.total == 0) 100 else st.right * 100 / st.total,
                title = if (last) "أنهيت القصة! 🎉" else "أنهيت الفصل ${st.ch + 1}",
                subtitle = if (last) "قرأت ${reader.wordCount} كلمة بالإنجليزية في هذه القصة."
                else "فهمت ${st.right} من ${st.total}. الفصل التالي بانتظارك.",
                xp = st.xp,
                onAgain = { stage = ReaderStage.Read(st.ch) },
                onDone = { if (last) onClose() else stage = ReaderStage.Read(st.ch + 1) },
            ) {
                if (st.wpm > 0) {
                    VSpace(10.dp)
                    Pill("سرعة قراءتك: ${ltr("${st.wpm}")} كلمة في الدقيقة", MaterialTheme.colorScheme.primary)
                }
                if (!last) {
                    val next = reader.chapters[st.ch + 1]
                    VSpace(18.dp)
                    AppCard(color = AppTheme.extra.subtle, bordered = false) {
                        Text("في الفصل القادم: ${next.title}", style = MaterialTheme.typography.titleSmall)
                        VSpace(6.dp)
                        androidx.compose.runtime.CompositionLocalProvider(
                            androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr,
                        ) {
                            Text(
                                next.text.substringBefore("\n").split(Regex("(?<=[.!?])\\s+")).first() + " …",
                                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderCover(r: GradedReader, read: Int, onClose: () -> Unit, onRead: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeader(r.title, onBack = onClose)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            VSpace(12.dp)
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { BookCover(r, 120, 160) }
            VSpace(16.dp)
            Text(isolateLatin(r.titleAr), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            VSpace(8.dp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                LevelBadge(r.level, size = 30.dp)
                HSpace(8.dp)
                Pill(r.genreAr, r.level.color())
                HSpace(8.dp)
                Pill("${ltr("${r.wordCount}")} كلمة", MaterialTheme.colorScheme.primary)
            }
            VSpace(14.dp)
            AppCard(color = AppTheme.extra.subtle, bordered = false) {
                Text(isolateLatin(r.summaryAr), style = MaterialTheme.typography.bodyLarge)
            }
            SectionHeader("الفصول")
            r.chapters.forEachIndexed { i, ch ->
                val done = i < read
                val open = i <= read
                AppCard(modifier = Modifier.padding(vertical = 4.dp), padding = 14.dp, onClick = if (open) ({ onRead(i) }) else null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(32.dp).clip(CircleShape)
                                .background(if (done) Emerald else if (open) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else AppTheme.extra.subtle),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (done) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            else Text("${i + 1}", style = MaterialTheme.typography.labelLarge)
                        }
                        HSpace(12.dp)
                        Column(Modifier.weight(1f)) {
                            Text(ch.title, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${ltr("${ch.wordCount}")} كلمة · ${ch.questions.size} أسئلة فهم",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            VSpace(16.dp)
        }
        Box(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            val next = read.coerceAtMost(r.chapters.lastIndex)
            PrimaryButton(
                when {
                    read == 0 -> "ابدأ القراءة"
                    read >= r.chapters.size -> "اقرأها من جديد"
                    else -> "تابع: الفصل ${read + 1}"
                },
                onClick = { onRead(if (read >= r.chapters.size) 0 else next) },
                icon = Icons.AutoMirrored.Rounded.ArrowForward,
            )
        }
    }
}

@Composable
private fun ChapterView(
    r: GradedReader,
    index: Int,
    onClose: () -> Unit,
    onAddWord: (String) -> Unit,
    onFinished: (seconds: Int) -> Unit,
) {
    val chapter = r.chapters[index]
    val speaker = LocalSpeaker.current
    val paragraphs = remember(chapter) { chapter.text.split(Regex("\\n\\s*\\n")).map { it.trim() }.filter { it.isNotEmpty() } }
    // Sentences per paragraph, as InteractiveText splits them, for the read-along highlight.
    val sentences = remember(paragraphs) { paragraphs.map { sentencesOf(it) } }
    val flat = remember(sentences) { sentences.flatMapIndexed { p, list -> list.indices.map { p to it } } }
    var revealed by rememberSaveable(index) { mutableIntStateOf(1) }
    var playingAt by remember(index) { mutableIntStateOf(-1) }
    var fontScale by rememberSaveable { mutableStateOf(1f) }
    val started = remember(index) { System.currentTimeMillis() }
    val scroll = rememberScrollState()
    DisposableEffect(index) { onDispose { speaker.stop() } }
    LaunchedEffect(revealed) { if (revealed > 1) scroll.animateScrollTo(scroll.maxValue) }

    fun readAlong() {
        if (playingAt >= 0) { speaker.stop(); playingAt = -1; return }
        revealed = paragraphs.size
        val texts = flat.map { (p, i) -> sentences[p][i] }
        playingAt = 0
        speaker.speakSequence(texts, onIndex = { playingAt = it }, onDone = { playingAt = -1 })
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeader(
            "الفصل ${index + 1} من ${r.chapters.size}", onBack = onClose,
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    SmallRound(ltr("A−")) { fontScale = (fontScale - 0.1f).coerceAtLeast(0.8f) }
                    SmallRound(ltr("A+")) { fontScale = (fontScale + 0.1f).coerceAtMost(1.5f) }
                    Box(
                        Modifier.size(40.dp).clip(CircleShape)
                            .background(if (playingAt >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .clickable { readAlong() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (playingAt >= 0) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.VolumeUp, "اقرأ معي",
                            tint = if (playingAt >= 0) Color.White else MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            },
        )
        LinearMeter(
            (index + revealed.toFloat() / paragraphs.size) / r.chapters.size,
            height = 4.dp, modifier = Modifier.padding(horizontal = 20.dp),
        )
        Column(Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 20.dp)) {
            VSpace(18.dp)
            AutoText(chapter.title, style = MaterialTheme.typography.headlineSmall)
            VSpace(4.dp)
            Text(
                "اضغط على الكلمة الملوّنة لمعناها وأضفها لبطاقاتك، وعلى أي جملة لتسمعها، أو اضغط 🔊 ليقرأ معك الفصل.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VSpace(14.dp)
            val playing = flat.getOrNull(playingAt)
            paragraphs.take(revealed).forEachIndexed { i, text ->
                Box(Modifier.padding(bottom = 14.dp)) {
                    InteractiveText(
                        text, chapter.glossary,
                        playing = if (playing?.first == i) playing.second else -1,
                        fontScale = fontScale,
                        onAddWord = onAddWord,
                    )
                }
            }
            if (revealed >= paragraphs.size && chapter.glossary.isNotEmpty()) {
                SectionHeader("كلمات الفصل")
                AppCard(padding = 14.dp) {
                    chapter.glossary.forEach { (en, ar) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(ar, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text(en, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            HSpace(6.dp)
                            Icon(
                                Icons.AutoMirrored.Rounded.VolumeUp, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp).clickable { speaker.speak(en) },
                            )
                        }
                    }
                }
            }
            VSpace(16.dp)
        }
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            PrimaryButton(
                if (revealed < paragraphs.size) "تابع القراءة (${revealed}/${paragraphs.size})" else "أنهيت الفصل — إلى الأنشطة",
                onClick = {
                    if (revealed < paragraphs.size) revealed++
                    else {
                        speaker.stop()
                        onFinished(((System.currentTimeMillis() - started) / 1000).toInt())
                    }
                },
                icon = Icons.AutoMirrored.Rounded.ArrowForward,
            )
        }
    }
}

@Composable
private fun SmallRound(label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(36.dp).clip(CircleShape).background(AppTheme.extra.subtle).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

/** Library entry on the practice hub. */
@Composable
fun ReadersEntryCard(progress: Progress, onClick: () -> Unit) {
    AppCard(onClick = onClick, padding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy((-14).dp)) {
                Readers.distinctBy { it.level }.take(3).forEach { BookCover(it, 34, 46) }
            }
            HSpace(16.dp)
            Column(Modifier.weight(1f)) {
                Text("مكتبة القصص", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${Readers.size} قصة متدرّجة من A1 إلى C2 · ${ltr("%,d".format(progress.wordsRead))} كلمة قرأتها",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
