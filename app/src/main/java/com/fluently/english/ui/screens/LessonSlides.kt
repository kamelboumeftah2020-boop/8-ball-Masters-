package com.fluently.english.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.fluently.english.data.content.Concept
import com.fluently.english.data.content.Guide
import com.fluently.english.data.content.Lesson
import com.fluently.english.data.content.LessonType
import com.fluently.english.data.content.Word
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.ui.components.AppCard
import com.fluently.english.ui.components.AudioControls
import com.fluently.english.ui.components.AutoText
import com.fluently.english.ui.components.CheckCard
import com.fluently.english.ui.components.CircleIconButton
import com.fluently.english.ui.components.ExampleCard
import com.fluently.english.ui.components.FlipCard
import com.fluently.english.ui.components.FormulaView
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.IconTile
import com.fluently.english.ui.components.Ltr
import com.fluently.english.ui.components.MatchBoard
import com.fluently.english.ui.components.MistakeCard
import com.fluently.english.ui.components.Pill
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.SpeakButton
import com.fluently.english.ui.components.SpeakPractice
import com.fluently.english.ui.components.TableView
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.components.icon
import com.fluently.english.ui.components.isolateLatin
import com.fluently.english.ui.components.sentencesOf
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Coral
import com.fluently.english.ui.theme.Danger
import com.fluently.english.ui.theme.Gold
import com.fluently.english.ui.theme.Success
import com.fluently.english.ui.theme.color

/** One screen of the step-by-step explanation shown before practice. */
private sealed interface Slide {
    /** True if the learner must interact before moving on. */
    val gated: Boolean get() = false

    data class Intro(val lesson: Lesson, val guide: Guide?) : Slide
    data class ConceptSlide(val concept: Concept, val index: Int, val total: Int) : Slide {
        override val gated get() = concept.check != null
    }
    data class Mistakes(val guide: Guide) : Slide
    data class Summary(val lesson: Lesson, val tip: String?) : Slide
    data class WordCard(val word: Word, val index: Int, val total: Int) : Slide
    data class WarmUpMatch(val words: List<Word>) : Slide {
        override val gated get() = true
    }
    data class Passage(val text: String) : Slide
    data class Listen(val script: String) : Slide
}

private fun slidesFor(lesson: Lesson): List<Slide> {
    val guide = lesson.guide
    return when (lesson.type) {
        LessonType.GRAMMAR -> buildList {
            add(Slide.Intro(lesson, guide))
            if (guide != null) {
                guide.concepts.forEachIndexed { i, c -> add(Slide.ConceptSlide(c, i, guide.concepts.size)) }
                if (guide.mistakes.isNotEmpty()) add(Slide.Mistakes(guide))
            }
            add(Slide.Summary(lesson, guide?.tip))
        }
        LessonType.VOCABULARY -> buildList {
            add(Slide.Intro(lesson, null))
            lesson.words.forEachIndexed { i, w -> add(Slide.WordCard(w, i, lesson.words.size)) }
            if (lesson.words.size >= 4) add(Slide.WarmUpMatch(lesson.words.takeLast(4)))
        }
        LessonType.READING -> listOf(Slide.Intro(lesson, null), Slide.Passage(lesson.passage.orEmpty()))
        LessonType.LISTENING -> listOf(Slide.Intro(lesson, null), Slide.Listen(lesson.passage.orEmpty()))
    }
}

/** The learning part of a lesson: a short, interactive slide deck. */
@Composable
fun LearnPlayer(lesson: Lesson, onClose: () -> Unit, onStart: () -> Unit) {
    val slides = remember(lesson.id) { slidesFor(lesson) }
    var step by remember { mutableIntStateOf(0) }
    val unlocked = remember { mutableStateMapOf<Int, Boolean>() }
    val slide = slides[step]
    val canContinue = !slide.gated || unlocked[step] == true
    val last = step == slides.lastIndex
    val c = lesson.level.color()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Story-style segmented progress.
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleIconButton(Icons.Rounded.Close, onClose)
            HSpace(12.dp)
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                slides.indices.forEach { i ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(if (i <= step) c else MaterialTheme.colorScheme.surfaceVariant),
                    )
                }
            }
        }

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val forward = targetState > initialState
                // In the right-to-left layout "next" enters from the left.
                (slideInHorizontally { if (forward) -it / 3 else it / 3 } + fadeIn()) togetherWith
                    (slideOutHorizontally { if (forward) it / 3 else -it / 3 } + fadeOut())
            },
            modifier = Modifier.weight(1f),
            label = "slides",
        ) { index ->
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                SlideContent(slides[index], lesson, onUnlock = { unlocked[index] = true })
                VSpace(24.dp)
            }
        }

        Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
            HorizontalDivider(color = AppTheme.extra.border)
            Row(
                Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (step > 0) {
                    CircleIconButton(Icons.AutoMirrored.Rounded.ArrowBack, { step-- })
                    HSpace(12.dp)
                }
                PrimaryButton(
                    text = when {
                        !canContinue -> if (slide is Slide.WarmUpMatch) "أكمل اللعبة للمتابعة" else "أجب عن السؤال للمتابعة"
                        last -> "ابدأ التمارين · ${lesson.questions.size}"
                        else -> "التالي"
                    },
                    enabled = canContinue,
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    onClick = { if (last) onStart() else step++ },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SlideContent(slide: Slide, lesson: Lesson, onUnlock: () -> Unit) {
    when (slide) {
        is Slide.Intro -> IntroSlide(slide.lesson, slide.guide)
        is Slide.ConceptSlide -> ConceptSlide(slide, lesson, onUnlock)
        is Slide.Mistakes -> MistakesSlide(slide.guide)
        is Slide.Summary -> SummarySlide(slide.lesson, slide.tip)
        is Slide.WordCard -> WordSlide(slide)
        is Slide.WarmUpMatch -> WarmUpSlide(slide.words, onUnlock)
        is Slide.Passage -> PassageSlide(slide.text)
        is Slide.Listen -> ListenSlide(slide.script)
    }
}

@Composable
private fun SlideLabel(text: String, color: Color = MaterialTheme.colorScheme.primary) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = color)
    VSpace(6.dp)
}

// ---------- Intro ----------

@Composable
private fun IntroSlide(lesson: Lesson, guide: Guide?) {
    val c = lesson.level.color()
    VSpace(8.dp)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Pill(lesson.level.code, c, solid = true)
        HSpace(8.dp)
        Pill(lesson.type.labelAr, MaterialTheme.colorScheme.primary, icon = lesson.type.icon())
    }
    VSpace(14.dp)
    Text(lesson.titleAr, style = MaterialTheme.typography.headlineMedium)
    Ltr { Text(lesson.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth()) }
    VSpace(20.dp)

    val extra = AppTheme.extra
    Column(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(extra.hero)
            .padding(22.dp),
    ) {
        IconTile(Icons.Rounded.Lightbulb, Gold, size = 44.dp, background = extra.heroTrack)
        VSpace(14.dp)
        Text(
            isolateLatin(guide?.hook ?: introFor(lesson)),
            style = MaterialTheme.typography.bodyLarge,
            color = extra.onHero,
        )
    }

    val goals = guide?.goals ?: goalsFor(lesson)
    VSpace(24.dp)
    Text("في نهاية هذا الدرس ستستطيع:", style = MaterialTheme.typography.titleMedium)
    VSpace(10.dp)
    goals.forEach {
        Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.TaskAlt, null, tint = Success, modifier = Modifier.size(22.dp))
            HSpace(12.dp)
            Text(isolateLatin(it), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun introFor(lesson: Lesson): String = when (lesson.type) {
    LessonType.VOCABULARY -> "ستتعرف على ${lesson.words.size} كلمات جديدة. لكل كلمة بطاقة: استمع إليها، فكّر في معناها، ثم اقلب البطاقة لتتأكد. انطق الكلمة بصوتك لتثبت في ذاكرتك، وفي النهاية لعبة توصيل سريعة."
    LessonType.READING -> "ستقرأ نصاً حقيقياً بمستواك. اقرأه مرة لفهم الفكرة العامة، ثم اضغط على أي جملة لتسمع نطقها. القراءة مع الاستماع تحسّن النطق والفهم معاً."
    LessonType.LISTENING -> "ستستمع إلى مقطع من الحياة اليومية. استمع أولاً دون نص، ثم جملةً جملة. لا تقلق إن لم تفهم كل كلمة؛ المهم أن تلتقط الفكرة والمعلومات الأساسية."
    LessonType.GRAMMAR -> lesson.notes.firstOrNull().orEmpty()
}

private fun goalsFor(lesson: Lesson): List<String> = when (lesson.type) {
    LessonType.VOCABULARY -> listOf("تفهم ${lesson.words.size} كلمات جديدة وتنطقها", "تستخدمها في جمل حقيقية", "تراجعها لاحقاً في البطاقات الذكية")
    LessonType.READING -> listOf("تفهم الفكرة الرئيسية للنص", "تستخرج معلومات محددة", "تتعرف على كلمات من السياق")
    LessonType.LISTENING -> listOf("تفهم الفكرة العامة للمحادثة", "تلتقط الأرقام والتفاصيل", "تتدرب على النطق بالتظليل")
    LessonType.GRAMMAR -> lesson.notes.drop(1).take(3)
}

// ---------- Concept ----------

@Composable
private fun ConceptSlide(slide: Slide.ConceptSlide, lesson: Lesson, onUnlock: () -> Unit) {
    val concept = slide.concept
    LaunchedEffect(concept) { if (concept.check == null) onUnlock() }
    SlideLabel("الفكرة ${slide.index + 1} من ${slide.total}", lesson.level.color())
    Text(isolateLatin(concept.title), style = MaterialTheme.typography.headlineSmall)
    VSpace(12.dp)
    Text(isolateLatin(concept.body), style = MaterialTheme.typography.bodyLarge)

    concept.formula?.let {
        VSpace(18.dp)
        Text("التركيب", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        VSpace(8.dp)
        FormulaView(it)
    }
    concept.table?.let {
        VSpace(18.dp)
        TableView(it)
    }
    if (concept.examples.isNotEmpty()) {
        VSpace(18.dp)
        Text("أمثلة — اضغط على المثال لترى ترجمته", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        VSpace(4.dp)
        concept.examples.forEach { ExampleCard(it) }
    }
    concept.check?.let {
        VSpace(18.dp)
        CheckCard(it) { onUnlock() }
    }
}

// ---------- Mistakes ----------

@Composable
private fun MistakesSlide(guide: Guide) {
    SlideLabel("انتبه!", Danger)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.WarningAmber, null, tint = Danger)
        HSpace(8.dp)
        Text("أخطاء شائعة يقع فيها المتعلمون العرب", style = MaterialTheme.typography.headlineSmall)
    }
    VSpace(8.dp)
    Text(
        "تعرّف عليها الآن حتى لا تقع فيها. الجملة المشطوبة خطأ، والخضراء هي الصحيحة.",
        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    VSpace(10.dp)
    guide.mistakes.forEach { MistakeCard(it) }
}

// ---------- Summary ----------

@Composable
private fun SummarySlide(lesson: Lesson, tip: String?) {
    SlideLabel("الخلاصة")
    Text("ملخص الدرس في دقيقة", style = MaterialTheme.typography.headlineSmall)
    VSpace(14.dp)
    AppCard {
        lesson.notes.forEachIndexed { i, note ->
            if (i > 0) VSpace(12.dp)
            Row {
                Icon(Icons.Rounded.TaskAlt, null, tint = Success, modifier = Modifier.size(20.dp).padding(top = 2.dp))
                HSpace(10.dp)
                Text(isolateLatin(note), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    if (tip != null) {
        VSpace(16.dp)
        AppCard(color = MaterialTheme.colorScheme.tertiaryContainer, bordered = false) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Lightbulb, null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                HSpace(8.dp)
                Text("حيلة للحفظ", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            VSpace(8.dp)
            Text(isolateLatin(tip), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
    }
    val sample = lesson.examples.firstOrNull()
    if (sample != null) {
        VSpace(16.dp)
        AppCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Mic, null, tint = Coral)
                HSpace(8.dp)
                Text("تحدّ صغير: انطق هذه الجملة", style = MaterialTheme.typography.titleSmall)
            }
            VSpace(10.dp)
            val sentence = sample.en.substringAfter("→ ")
            AutoText(sentence, style = MaterialTheme.typography.titleLarge)
            VSpace(10.dp)
            AudioControls(sentence)
            VSpace(14.dp)
            SpeakPractice(sentence, compact = true)
        }
    }
}

// ---------- Vocabulary ----------

@Composable
private fun WordSlide(slide: Slide.WordCard) {
    val word = slide.word
    var flipped by remember(word) { mutableStateOf(false) }
    val speaker = LocalSpeaker.current
    LaunchedEffect(word) { speaker.speak(word.en) }
    val extra = AppTheme.extra

    SlideLabel("الكلمة ${slide.index + 1} من ${slide.total}")
    Text("خمّن المعنى، ثم اقلب البطاقة", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    VSpace(14.dp)
    FlipCard(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.15f),
        flipped = flipped,
        onFlip = { flipped = !flipped },
        front = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Ltr { Text(word.en, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center) }
                VSpace(16.dp)
                SpeakButton(word.en, size = 52.dp)
                VSpace(20.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.TouchApp, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                    HSpace(6.dp)
                    Text("اضغط لقلب البطاقة", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                }
            }
        },
        back = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(word.ar, style = MaterialTheme.typography.headlineMedium, color = extra.onHero, textAlign = TextAlign.Center)
                VSpace(4.dp)
                Ltr { Text(word.en, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
                VSpace(18.dp)
                Ltr {
                    Text(
                        highlightWord(word.example, word.en), style = MaterialTheme.typography.titleMedium,
                        color = extra.onHero, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
                    )
                }
                VSpace(12.dp)
                SpeakButton(word.example)
            }
        },
    )
    VSpace(20.dp)
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Mic, null, tint = Coral)
            HSpace(8.dp)
            Text("انطق الكلمة بصوتك", style = MaterialTheme.typography.titleSmall)
        }
        VSpace(12.dp)
        SpeakPractice(word.en, compact = true)
    }
}

@Composable
private fun highlightWord(sentence: String, word: String) = buildAnnotatedString {
    val i = sentence.indexOf(word, ignoreCase = true)
    if (i < 0) {
        append(sentence)
    } else {
        append(sentence.substring(0, i))
        pushStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold))
        append(sentence.substring(i, i + word.length))
        pop()
        append(sentence.substring(i + word.length))
    }
}

@Composable
private fun WarmUpSlide(words: List<Word>, onUnlock: () -> Unit) {
    var mistakes by remember { mutableStateOf<Int?>(null) }
    SlideLabel("لعبة سريعة")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Link, null, tint = MaterialTheme.colorScheme.primary)
        HSpace(8.dp)
        Text("صِل كل كلمة بمعناها", style = MaterialTheme.typography.headlineSmall)
    }
    VSpace(6.dp)
    Text(
        "اضغط على كلمة إنجليزية ثم على معناها العربي.",
        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    VSpace(18.dp)
    MatchBoard(words.map { it.en to it.ar }) {
        mistakes = it
        onUnlock()
    }
    mistakes?.let {
        VSpace(18.dp)
        AppCard(color = Success.copy(alpha = 0.10f), bordered = false) {
            Text(
                if (it == 0) "ممتاز! بدون أي خطأ 🎉" else "أحسنت! أكملتها بـ $it ${if (it == 1) "خطأ" else "أخطاء"}",
                style = MaterialTheme.typography.titleMedium, color = Success,
            )
        }
    }
}

// ---------- Reading ----------

@Composable
private fun PassageSlide(text: String) {
    val speaker = LocalSpeaker.current
    val sentences = remember(text) { sentencesOf(text) }
    var active by remember { mutableStateOf(-1) }
    val accent = MaterialTheme.colorScheme.primary

    SlideLabel("النص")
    Text("اقرأ واستمع", style = MaterialTheme.typography.headlineSmall)
    VSpace(6.dp)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.TouchApp, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        HSpace(6.dp)
        Text("اضغط على أي جملة لتسمعها", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    VSpace(14.dp)
    AudioControls(text)
    VSpace(14.dp)
    AppCard(padding = 20.dp) {
        val annotated = buildAnnotatedString {
            sentences.forEachIndexed { i, sentence ->
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "s$i",
                        styles = TextLinkStyles(
                            style = if (i == active) SpanStyle(background = accent.copy(alpha = 0.16f), color = accent) else SpanStyle(),
                        ),
                    ) {
                        active = i
                        speaker.speak(sentence)
                    },
                ) { append(sentence) }
                if (i < sentences.lastIndex) append(" ")
            }
        }
        Ltr {
            Text(
                annotated,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ---------- Listening ----------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ListenSlide(script: String) {
    val speaker = LocalSpeaker.current
    val sentences = remember(script) { sentencesOf(script) }
    var played by remember { mutableStateOf(setOf<Int>()) }
    var showText by remember { mutableStateOf(false) }
    val extra = AppTheme.extra

    SlideLabel("الاستماع")
    Text("استمع جيداً", style = MaterialTheme.typography.headlineSmall)
    VSpace(14.dp)
    Column(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(extra.hero).padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconTile(Icons.Rounded.Headphones, MaterialTheme.colorScheme.primary, size = 56.dp, background = extra.heroTrack)
        VSpace(12.dp)
        Text("الخطوة 1: استمع للمقطع كاملاً", style = MaterialTheme.typography.titleMedium, color = extra.onHero)
        VSpace(14.dp)
        AudioControls(script)
    }
    VSpace(22.dp)
    Text("الخطوة 2: استمع جملةً جملة", style = MaterialTheme.typography.titleMedium)
    VSpace(4.dp)
    Text(
        "اضغط على كل رقم، وحاول أن تكرر الجملة بعد سماعها.",
        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    VSpace(12.dp)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        sentences.forEachIndexed { i, s ->
            val done = i in played
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                    .clickable {
                        played = played + i
                        speaker.speak(s)
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (done) Icon(Icons.AutoMirrored.Rounded.VolumeUp, null, tint = Color.White, modifier = Modifier.size(20.dp))
                else Text("${i + 1}", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
    VSpace(22.dp)
    AppCard(onClick = { showText = !showText }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(if (showText) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            HSpace(10.dp)
            Text(
                if (showText) "إخفاء النص" else "صعب؟ اعرض النص كمساعدة",
                style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f),
            )
            Icon(Icons.Rounded.Flag, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
        }
        if (showText) {
            VSpace(12.dp)
            AutoText(script, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
