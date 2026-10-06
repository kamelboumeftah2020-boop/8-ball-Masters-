package com.fluently.english.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.fluently.english.data.content.Answers
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.Reply
import com.fluently.english.data.content.ReplyQuality
import com.fluently.english.data.content.Scenario
import com.fluently.english.data.content.Scenarios
import com.fluently.english.data.progress.Progress
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.ui.components.ltr
import com.fluently.english.ui.components.AppCard
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.LevelBadge
import com.fluently.english.ui.components.Ltr
import com.fluently.english.ui.components.ScreenHeader
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.components.isolateLatin
import com.fluently.english.ui.components.rememberSpeechInput
import com.fluently.english.ui.components.SpeechStatus
import com.fluently.english.ui.components.micPulse
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Coral
import com.fluently.english.ui.theme.Danger
import com.fluently.english.ui.theme.Gold
import com.fluently.english.ui.theme.Success
import com.fluently.english.ui.theme.color
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ConversationListScreen(progress: Progress, onBack: () -> Unit, onOpen: (String) -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            ScreenHeader("محادثات تفاعلية", onBack = onBack, subtitle = "تحدث بثقة في مواقف الحياة الحقيقية")
            AppCard(color = MaterialTheme.colorScheme.primaryContainer, bordered = false, modifier = Modifier.padding(20.dp)) {
                Text(
                    "اقرأ ما تقوله الشخصية واستمع إليه، ثم اختر الرد الأنسب أو قله بصوتك. بعد كل رد ستعرف لماذا هو جيد أو غير مناسب.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        items(Scenarios, key = { it.id }) { s ->
            AppCard(onClick = { onOpen(s.id) }, padding = 16.dp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LevelBadge(s.level, size = 46.dp)
                    HSpace(14.dp)
                    Column(Modifier.weight(1f)) {
                        Text(s.titleAr, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "مع ${s.partnerRoleAr} · ${s.turns.size} ردود",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    val stars = progress.conversationStars[s.id]
                    if (stars != null) Stars(stars)
                }
            }
        }
    }
}

private sealed interface ChatMessage {
    data class Partner(val en: String, val ar: String) : ChatMessage
    data class User(val en: String, val quality: ReplyQuality) : ChatMessage
    data class Coach(val text: String, val quality: ReplyQuality, val better: String?) : ChatMessage
}

@Composable
fun ConversationScreen(id: String, onComplete: (String, Int, Int) -> Int, onClose: () -> Unit) {
    val scenario = remember(id) { Scenarios.first { it.id == id } }
    var round by remember { mutableIntStateOf(0) }
    androidx.compose.runtime.key(round) {
        ConversationRun(scenario, onComplete, onClose, onAgain = { round++ })
    }
}

@Composable
private fun ConversationRun(
    scenario: Scenario,
    onComplete: (String, Int, Int) -> Int,
    onClose: () -> Unit,
    onAgain: () -> Unit,
) {
    val speaker = LocalSpeaker.current
    val scope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var turn by remember { mutableIntStateOf(0) }
    var points by remember { mutableIntStateOf(0) }
    var firstTry by remember { mutableStateOf(true) }
    val disabled = remember { mutableStateListOf<String>() }
    var busy by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var xp by remember { mutableIntStateOf(0) }
    var notHeard by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val current = scenario.turns.getOrNull(turn)
    val options = remember(turn) { current?.replies?.shuffled() ?: emptyList() }

    LaunchedEffect(turn) {
        if (current != null) {
            delay(if (turn == 0) 300 else 700)
            messages += ChatMessage.Partner(current.line, current.lineAr)
            speaker.speak(current.line)
            busy = false
        } else {
            delay(700)
            messages += ChatMessage.Partner(scenario.closing, scenario.closingAr)
            speaker.speak(scenario.closing)
            val ratio = points.toFloat() / scenario.maxPoints
            val stars = when {
                ratio >= 0.9f -> 3
                ratio >= 0.6f -> 2
                else -> 1
            }
            xp = onComplete(scenario.id, stars, points)
            delay(900)
            finished = true
        }
    }
    LaunchedEffect(messages.size, finished) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size + if (finished) 1 else 0)
    }

    fun choose(reply: Reply) {
        if (busy || current == null) return
        notHeard = false
        if (reply.quality == ReplyQuality.WRONG) {
            firstTry = false
            disabled += reply.en
            messages += ChatMessage.Coach(reply.feedback, reply.quality, null)
            return
        }
        busy = true
        if (firstTry) points += reply.quality.points
        messages += ChatMessage.User(reply.en, reply.quality)
        val better = current.replies.firstOrNull { it.quality == ReplyQuality.BEST }?.en
        messages += ChatMessage.Coach(reply.feedback, reply.quality, if (reply.quality == ReplyQuality.OK) better else null)
        speaker.speak(reply.en)
        scope.launch {
            delay(900)
            firstTry = true
            disabled.clear()
            turn++
        }
    }

    val speech = rememberSpeechInput { results ->
        val candidates = options.filter { it.en !in disabled }
        val match = results.flatMap { heard -> candidates.map { it to Answers.speechScore(it.en, heard) } }
            .maxByOrNull { it.second }
        if (match != null && match.second >= 0.5f) choose(match.first) else notHeard = true
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeader(scenario.titleAr, onBack = onClose, close = true, subtitle = "${scenario.partner} · ${scenario.partnerRoleAr}")
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                AppCard(color = AppTheme.extra.subtle, bordered = false, padding = 14.dp) {
                    Text("الموقف", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(scenario.situationAr, style = MaterialTheme.typography.bodyMedium)
                }
            }
            itemsIndexed(messages) { _, m ->
                when (m) {
                    is ChatMessage.Partner -> PartnerBubble(scenario, m)
                    is ChatMessage.User -> UserBubble(m)
                    is ChatMessage.Coach -> CoachNote(m)
                }
            }
            if (finished) {
                item { ConversationSummary(scenario, points, xp, onAgain, onClose) }
            }
        }

        if (!finished && current != null) {
            Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
                HorizontalDivider(color = AppTheme.extra.border)
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ردك:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                        Row(
                            Modifier.micPulse(speech).clip(CircleShape)
                                .background(if (speech.listening) Coral else Coral.copy(alpha = 0.12f))
                                .clickable(enabled = !busy) { notHeard = false; speech.start() }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Mic, null, tint = if (speech.listening) Color.White else Coral, modifier = Modifier.size(18.dp))
                            HSpace(4.dp)
                            Text(
                                if (speech.listening) "أستمع…" else "قلها بصوتك",
                                style = MaterialTheme.typography.labelMedium, color = if (speech.listening) Color.White else Coral,
                            )
                        }
                    }
                    SpeechStatus(speech)
                    if (notHeard) {
                        Text("لم أتعرف على ردك، حاول مرة أخرى أو اختر من القائمة.", style = MaterialTheme.typography.bodySmall, color = Danger)
                    }
                    VSpace(8.dp)
                    options.forEach { reply ->
                        val off = reply.en in disabled
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (off) Danger.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (off) Danger.copy(alpha = 0.3f) else AppTheme.extra.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = !off && !busy) { choose(reply) },
                        ) {
                            Ltr {
                                Text(
                                    reply.en,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (off) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PartnerBubble(scenario: Scenario, m: ChatMessage.Partner) {
    val speaker = LocalSpeaker.current
    var showAr by remember { mutableStateOf(false) }
    Ltr {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Box(
                Modifier.size(34.dp).clip(CircleShape).background(scenario.level.color()),
                contentAlignment = Alignment.Center,
            ) { Text(scenario.partner.first().toString(), color = Color.White, style = MaterialTheme.typography.titleSmall) }
            HSpace(8.dp)
            Column(
                Modifier
                    .widthIn(max = 300.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(14.dp),
            ) {
                Text(m.en, style = MaterialTheme.typography.bodyLarge)
                if (showAr) {
                    VSpace(4.dp)
                    Text(m.ar, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                VSpace(6.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Icon(
                        Icons.AutoMirrored.Rounded.VolumeUp, "استمع", tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp).clickable { speaker.speak(m.en) },
                    )
                    Icon(
                        Icons.Rounded.Translate, "ترجمة", tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp).clickable { showAr = !showAr },
                    )
                }
            }
        }
    }
}

@Composable
private fun UserBubble(m: ChatMessage.User) {
    Ltr {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box(
                Modifier
                    .widthIn(max = 300.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(14.dp),
            ) {
                Text(m.en, style = MaterialTheme.typography.bodyLarge, color = Color.White)
            }
        }
    }
}

@Composable
private fun CoachNote(m: ChatMessage.Coach) {
    val c = when (m.quality) {
        ReplyQuality.BEST -> Success
        ReplyQuality.OK -> Gold
        ReplyQuality.WRONG -> Danger
    }
    AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically { it / 2 }) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(c.copy(alpha = 0.10f))
                .padding(12.dp),
        ) {
            Text(
                when (m.quality) {
                    ReplyQuality.BEST -> "رد ممتاز"
                    ReplyQuality.OK -> "مقبول"
                    ReplyQuality.WRONG -> "غير مناسب — جرّب رداً آخر"
                },
                style = MaterialTheme.typography.labelLarge, color = c,
            )
            Text(isolateLatin(m.text), style = MaterialTheme.typography.bodySmall)
            if (m.better != null) {
                VSpace(4.dp)
                Text("الأفضل: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Ltr { Text(m.better, style = MaterialTheme.typography.bodyMedium, color = Success) }
            }
        }
    }
}

@Composable
private fun ConversationSummary(scenario: Scenario, points: Int, xp: Int, onAgain: () -> Unit, onDone: () -> Unit) {
    val ratio = points.toFloat() / scenario.maxPoints
    val stars = when {
        ratio >= 0.9f -> 3
        ratio >= 0.6f -> 2
        else -> 1
    }
    AppCard(modifier = Modifier.padding(top = 12.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("انتهت المحادثة", style = MaterialTheme.typography.titleLarge)
            VSpace(10.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { i ->
                    Text(if (i < stars) "★" else "☆", style = MaterialTheme.typography.displaySmall, color = Gold)
                }
            }
            VSpace(6.dp)
            Text(
                when (stars) {
                    3 -> "رائع! تحدثت كالمحترفين"
                    2 -> "جيد جداً! أعد المحادثة لتجرب الردود الأفضل"
                    else -> "بداية جيدة — أعد المحاولة بعد قراءة الملاحظات"
                },
                style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VSpace(4.dp)
            Text("${ltr("+$xp")} نقطة خبرة", style = MaterialTheme.typography.labelLarge, color = Gold)
            VSpace(16.dp)
            com.fluently.english.ui.components.PrimaryButton("انتهيت", onDone)
            VSpace(8.dp)
            com.fluently.english.ui.components.SecondaryButton("أعد المحادثة", onAgain)
        }
    }
}

