package com.fluently.english.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Close
import com.fluently.english.ui.components.ltr
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fluently.english.data.content.SoundLesson
import com.fluently.english.data.content.SoundLessons
import com.fluently.english.data.progress.Progress
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.ui.components.AppCard
import com.fluently.english.ui.components.CircleIconButton
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.IconTile
import com.fluently.english.ui.components.LinearMeter
import com.fluently.english.ui.components.Ltr
import com.fluently.english.ui.components.Pill
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.ScreenHeader
import com.fluently.english.ui.components.SectionHeader
import com.fluently.english.ui.components.SpeakPractice
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.components.isolateLatin
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Coral
import com.fluently.english.ui.theme.Danger
import com.fluently.english.ui.theme.Success
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SoundListScreen(progress: Progress, onBack: () -> Unit, onOpen: (String) -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            ScreenHeader("مختبر النطق", onBack = onBack, subtitle = "درّب أذنك ثم لسانك")
            AppCard(color = Coral.copy(alpha = 0.10f), bordered = false, modifier = Modifier.padding(20.dp)) {
                Text(
                    "هذه الأصوات غير موجودة في العربية أو تختلف عنها، لذلك يخلط بينها أغلب المتعلمين العرب. ستسمع كلمتين متشابهتين وتحدد أيهما نُطقت — هكذا تتعلم أذنك الفرق أولاً.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        items(SoundLessons, key = { it.id }) { s ->
            AppCard(onClick = { onOpen(s.id) }, padding = 16.dp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(Icons.Rounded.Hearing, Coral, size = 46.dp)
                    HSpace(14.dp)
                    Column(Modifier.weight(1f)) {
                        Text(s.titleAr, style = MaterialTheme.typography.titleSmall)
                        Ltr {
                            Text(
                                s.pairs.take(2).joinToString("   ") { "${it.first} / ${it.second}" },
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    progress.soundScores[s.id]?.let { Pill("$it%", if (it >= 75) Success else Coral) }
                }
            }
        }
    }
}

@Composable
fun SoundScreen(id: String, onComplete: (String, Int, Int) -> Int, onClose: () -> Unit) {
    val lesson = remember(id) { SoundLessons.first { it.id == id } }
    var stage by remember { mutableIntStateOf(0) } // 0 learn, 1 drill, 2 result
    var round by remember { mutableIntStateOf(0) }
    var result by remember { mutableStateOf(0 to 0) }
    var xp by remember { mutableIntStateOf(0) }

    when (stage) {
        0 -> SoundLearn(lesson, onClose) { stage = 1 }
        1 -> key(round) {
            SoundDrill(lesson, onClose) { correct, total ->
                result = correct to total
                xp = onComplete(lesson.id, correct * 100 / total, correct)
                stage = 2
            }
        }
        else -> PracticeResult(
            percent = result.first * 100 / result.second,
            title = if (result.first * 100 / result.second >= 75) "أذنك ممتازة!" else "تحتاج تدريباً أكثر",
            subtitle = "ميّزت ${result.first} من ${result.second} كلمات بشكل صحيح",
            xp = xp,
            onAgain = { round++; stage = 1 },
            onDone = onClose,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SoundLearn(lesson: SoundLesson, onClose: () -> Unit, onStart: () -> Unit) {
    val speaker = LocalSpeaker.current
    var practiceWord by remember { mutableStateOf(lesson.pairs.first().first) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeader(lesson.titleAr, onBack = onClose, close = true)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            VSpace(8.dp)
            Ltr { Text(lesson.title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.fillMaxWidth()) }
            VSpace(12.dp)
            Text(isolateLatin(lesson.howAr), style = MaterialTheme.typography.bodyLarge)
            VSpace(14.dp)
            AppCard(color = MaterialTheme.colorScheme.tertiaryContainer, bordered = false) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Lightbulb, null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                    HSpace(8.dp)
                    Text("جرّب هذا", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
                VSpace(6.dp)
                Text(isolateLatin(lesson.tipAr), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }

            SectionHeader("استمع وقارن")
            AppCard(padding = 0.dp) {
                lesson.pairs.forEachIndexed { i, (a, b) ->
                    if (i > 0) HorizontalDivider(color = AppTheme.extra.border)
                    Ltr {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            WordPlay(a, Modifier.weight(1f)) { speaker.speak(a, 0.8f) }
                            Text("·", color = MaterialTheme.colorScheme.outline)
                            WordPlay(b, Modifier.weight(1f)) { speaker.speak(b, 0.8f) }
                        }
                    }
                }
            }

            SectionHeader("انطق بنفسك")
            Text("اختر كلمة ثم اضغط على الميكروفون:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            VSpace(10.dp)
            Ltr {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    lesson.pairs.take(4).flatMap { listOf(it.first, it.second) }.forEach { w ->
                        val selected = w == practiceWord
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (selected) Color.Transparent else AppTheme.extra.border, CircleShape)
                                .clickable { practiceWord = w; speaker.speak(w) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text(w, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }
            VSpace(14.dp)
            AppCard { key(practiceWord) { SpeakPractice(practiceWord, compact = true) } }
            VSpace(20.dp)
        }
        Column(Modifier.navigationBarsPadding()) {
            HorizontalDivider(color = AppTheme.extra.border)
            Box(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                PrimaryButton("ابدأ اختبار الأذن", onStart)
            }
        }
    }
}

@Composable
private fun WordPlay(word: String, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.AutoMirrored.Rounded.VolumeUp, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        HSpace(8.dp)
        Text(word, style = MaterialTheme.typography.titleMedium)
    }
}

/** Hear one word of a minimal pair and pick which it was. */
@Composable
private fun SoundDrill(lesson: SoundLesson, onClose: () -> Unit, onFinish: (Int, Int) -> Unit) {
    val speaker = LocalSpeaker.current
    val scope = rememberCoroutineScope()
    val rounds = remember { lesson.pairs.shuffled().map { pair -> pair to listOf(pair.first, pair.second).random() } }
    var index by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    var picked by remember { mutableStateOf<String?>(null) }
    val (pair, target) = rounds[index]
    val options = remember(index) { listOf(pair.first, pair.second).shuffled() }

    LaunchedEffect(index) {
        delay(350)
        speaker.speak(target, 0.85f)
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            CircleIconButton(Icons.Rounded.Close, onClose)
            HSpace(14.dp)
            Box(Modifier.weight(1f)) { LinearMeter(index.toFloat() / rounds.size, height = 8.dp) }
            HSpace(12.dp)
            Text("${index + 1}/${rounds.size}", style = MaterialTheme.typography.labelLarge)
        }
        Column(
            Modifier.weight(1f).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("أي كلمة سمعت؟", style = MaterialTheme.typography.headlineSmall)
            VSpace(20.dp)
            Box(
                Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                    .clickable { speaker.speak(target, 0.85f) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Rounded.VolumeUp, "استمع", tint = Color.White, modifier = Modifier.size(44.dp))
            }
            VSpace(8.dp)
            Text("اضغط لإعادة الاستماع", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            VSpace(36.dp)
            Ltr {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    options.forEach { option ->
                        val state = when {
                            picked == null -> 0
                            option == target -> 1
                            option == picked -> 2
                            else -> 0
                        }
                        val bg by animateColorAsState(
                            when (state) {
                                1 -> Success
                                2 -> Danger
                                else -> MaterialTheme.colorScheme.surface
                            }, label = "pick",
                        )
                        Box(
                            Modifier
                                .weight(1f)
                                .height(96.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(bg)
                                .border(1.dp, AppTheme.extra.border, RoundedCornerShape(22.dp))
                                .clickable(enabled = picked == null) {
                                    picked = option
                                    if (option == target) correct++
                                    scope.launch {
                                        // Let the learner hear both words after answering.
                                        speaker.speak(target, 0.85f)
                                        delay(1300)
                                        if (index + 1 >= rounds.size) {
                                            onFinish(correct, rounds.size)
                                        } else {
                                            picked = null
                                            index++
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                option, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center,
                                color = if (state != 0) Color.White else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
            VSpace(20.dp)
            if (picked != null) {
                Text(
                    if (picked == target) "صحيح!" else "الكلمة كانت: ${ltr(target)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (picked == target) Success else Danger,
                )
            }
        }
    }
}

