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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.fluently.english.data.content.Question
import com.fluently.english.data.content.Word
import com.fluently.english.data.progress.Progress
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.ui.components.CircleIconButton
import com.fluently.english.ui.components.GhostButton
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.LinearMeter
import com.fluently.english.ui.components.Ltr
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.QuizRunner
import com.fluently.english.ui.components.SpeakButton
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.components.ltr
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Coral
import com.fluently.english.ui.theme.Danger
import com.fluently.english.ui.theme.Gold
import com.fluently.english.ui.theme.Success
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
private fun GameTopBar(onClose: () -> Unit, progress: Float, label: String) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        CircleIconButton(Icons.Rounded.Close, onClose)
        HSpace(14.dp)
        Box(Modifier.weight(1f)) { LinearMeter(progress, height = 8.dp) }
        HSpace(12.dp)
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

// ---------------- Scramble ----------------

@Composable
fun ScrambleGame(progress: Progress, onComplete: (Int) -> Int, onClose: () -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    key(round) {
        val words = remember {
            gameWords(progress).filter { it.en.length in 3..10 && it.en.all { c -> c.isLetter() } }.shuffled().take(8)
        }
        var index by remember { mutableIntStateOf(0) }
        var correct by remember { mutableIntStateOf(0) }
        var xp by remember { mutableStateOf<Int?>(null) }
        val done = xp
        if (done != null) {
            PracticeResult(
                percent = correct * 100 / words.size,
                title = "أنهيت اللعبة!",
                subtitle = "رتّبت $correct من ${words.size} كلمات بدون مساعدة",
                xp = done, onAgain = { round++ }, onDone = onClose,
            )
        } else {
            Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                GameTopBar(onClose, index.toFloat() / words.size, "${index + 1}/${words.size}")
                key(index) {
                    ScrambleRound(words[index]) { solvedAlone ->
                        if (solvedAlone) correct++
                        if (index + 1 >= words.size) xp = onComplete(correct) else index++
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScrambleRound(word: Word, onNext: (Boolean) -> Unit) {
    val speaker = LocalSpeaker.current
    val target = word.en.lowercase()
    val letters = remember(word) {
        var s = target.toList().shuffled()
        while (s.joinToString("") == target && target.length > 1) s = s.shuffled()
        s
    }
    val picked = remember(word) { mutableStateListOf<Int>() }
    var state by remember(word) { mutableIntStateOf(0) } // 0 playing, 1 right, 2 wrong, 3 revealed
    val answer = picked.joinToString("") { letters[it].toString() }

    LaunchedEffect(answer) {
        if (answer.length == target.length && state == 0) {
            if (answer == target) {
                state = 1
                speaker.speak(word.en)
                delay(1000)
                onNext(true)
            } else {
                state = 2
                delay(700)
                picked.clear()
                state = 0
            }
        }
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        VSpace(24.dp)
        Text("رتّب الحروف لتكوين الكلمة", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        VSpace(12.dp)
        Text(word.ar, style = MaterialTheme.typography.headlineMedium)
        VSpace(8.dp)
        SpeakButton(word.en)
        VSpace(32.dp)
        val slotColor by animateColorAsState(
            when (state) {
                1 -> Success
                2 -> Danger
                else -> MaterialTheme.colorScheme.primary
            }, label = "slots",
        )
        Ltr {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                target.indices.forEach { i ->
                    val ch = answer.getOrNull(i)
                    Box(
                        Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (ch != null) slotColor.copy(alpha = 0.12f) else AppTheme.extra.subtle)
                            .border(1.5.dp, if (ch != null) slotColor else AppTheme.extra.border, RoundedCornerShape(10.dp))
                            .clickable(enabled = ch != null && state == 0) { picked.removeAt(i) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(ch?.toString() ?: "", style = MaterialTheme.typography.titleLarge, color = slotColor)
                    }
                }
            }
            VSpace(36.dp)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                letters.forEachIndexed { i, c ->
                    val used = i in picked
                    Box(
                        Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (used) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
                            .border(1.dp, if (used) Color.Transparent else AppTheme.extra.border, RoundedCornerShape(14.dp))
                            .clickable(enabled = !used && state == 0) { picked.add(i) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (used) "" else c.toString(), style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                }
            }
        }
        VSpace(28.dp)
        if (state == 3) {
            Text("الكلمة: ${ltr(word.en)}", style = MaterialTheme.typography.titleLarge, color = Coral)
            VSpace(12.dp)
            PrimaryButton("التالي", { onNext(false) })
        } else if (state == 0) {
            GhostButton("أظهر الإجابة", {
                state = 3
                speaker.speak(word.en)
            })
        }
    }
}

// ---------------- Speed round ----------------

private const val SPEED_SECONDS = 60

/** 60 seconds: is this the right meaning? Tap ✓ or ✗ as fast as you can. */
@Composable
fun SpeedGame(progress: Progress, onComplete: (Int, Int) -> Int, onClose: () -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    key(round) {
        val pool = remember { gameWords(progress).shuffled() }
        var started by remember { mutableStateOf(false) }
        var timeLeft by remember { mutableIntStateOf(SPEED_SECONDS) }
        var score by remember { mutableIntStateOf(0) }
        var answered by remember { mutableIntStateOf(0) }
        var flash by remember { mutableStateOf<Boolean?>(null) }
        var xp by remember { mutableStateOf<Int?>(null) }
        val best = remember { progress.speedBest }
        val item = remember(answered) {
            val w = pool[answered % pool.size]
            val showCorrect = (0..1).random() == 0
            val shown = if (showCorrect) w.ar else pool.filter { it != w }.random().ar
            Triple(w, shown, shown == w.ar)
        }

        LaunchedEffect(started) {
            if (!started) return@LaunchedEffect
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            xp = onComplete(score, score)
        }
        LaunchedEffect(flash) {
            if (flash != null) {
                delay(250)
                flash = null
            }
        }

        val done = xp
        when {
            done != null -> PracticeResult(
                percent = if (answered == 0) 0 else score * 100 / answered,
                title = if (score > best) "رقم قياسي جديد! 🏆" else "انتهى الوقت!",
                subtitle = "أجبت $score إجابة صحيحة من $answered" + if (best > 0) " · أفضل نتيجة سابقة: $best" else "",
                xp = done, onAgain = { round++ }, onDone = onClose,
            )
            !started -> Column(
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Rounded.Timer, null, tint = Coral, modifier = Modifier.size(72.dp))
                VSpace(16.dp)
                Text("تحدي السرعة", style = MaterialTheme.typography.headlineMedium)
                VSpace(8.dp)
                Text(
                    "ستظهر كلمة إنجليزية مع معنى عربي. هل المعنى صحيح؟ اضغط ✓ أو ✗ بأسرع ما يمكن خلال 60 ثانية.",
                    style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (best > 0) {
                    VSpace(10.dp)
                    Text("أفضل نتيجة لك: $best", style = MaterialTheme.typography.titleMedium, color = Gold)
                }
                VSpace(28.dp)
                PrimaryButton("ابدأ!", { started = true })
                GhostButton("رجوع", onClose)
            }
            else -> Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                GameTopBar(onClose, timeLeft / SPEED_SECONDS.toFloat(), "$timeLeft ث")
                Column(
                    Modifier.weight(1f).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("النتيجة: $score", style = MaterialTheme.typography.titleMedium, color = Gold)
                    VSpace(24.dp)
                    val cardColor by animateColorAsState(
                        when (flash) {
                            true -> Success.copy(alpha = 0.18f)
                            false -> Danger.copy(alpha = 0.18f)
                            null -> MaterialTheme.colorScheme.surface
                        }, label = "flash",
                    )
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp)
                            .clip(MaterialTheme.shapes.large)
                            .background(cardColor)
                            .border(1.dp, AppTheme.extra.border, MaterialTheme.shapes.large)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Ltr { Text(item.first.en, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center) }
                        VSpace(16.dp)
                        Text("=", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        VSpace(16.dp)
                        Text(item.second, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    VSpace(36.dp)
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        listOf(false, true).forEach { yes ->
                            val c = if (yes) Success else Danger
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(84.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(c)
                                    .clickable {
                                        val right = yes == item.third
                                        if (right) score++
                                        flash = right
                                        answered++
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(if (yes) Icons.Rounded.Check else Icons.Rounded.Close, null, tint = Color.White, modifier = Modifier.size(40.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- Dictation ----------------

@Composable
fun DictationGame(progress: Progress, onComplete: (Int) -> Int, onClose: () -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    key(round) {
        val questions = remember {
            gameWords(progress).filter { it.en.length >= 3 }.shuffled().take(8).map {
                Question.Typing("اكتب الكلمة التي تسمعها (المعنى: ${it.ar})", listOf(it.en), audio = it.en)
            }
        }
        var xp by remember { mutableStateOf<Int?>(null) }
        var correct by remember { mutableIntStateOf(0) }
        val done = xp
        if (done != null) {
            PracticeResult(
                percent = correct * 100 / questions.size,
                title = if (correct == questions.size) "إملاء مثالي!" else "أحسنت!",
                subtitle = "كتبت $correct من ${questions.size} كلمات بشكل صحيح",
                xp = done, onAgain = { round++ }, onDone = onClose,
            )
        } else {
            QuizRunner(
                questions = questions,
                instantFeedback = true,
                title = "الإملاء",
                onClose = onClose,
                onFinish = { records ->
                    correct = records.count { it.correct }
                    xp = onComplete(correct)
                },
            )
        }
    }
}
