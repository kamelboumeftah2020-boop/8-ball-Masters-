package com.fluently.english.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fluently.english.data.content.Course
import com.fluently.english.data.progress.Progress
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.ui.components.AppCard
import com.fluently.english.ui.components.AutoText
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.IconTile
import com.fluently.english.ui.components.LinearMeter
import com.fluently.english.ui.components.Ltr
import com.fluently.english.ui.components.Pill
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.ScreenHeader
import com.fluently.english.ui.components.SectionHeader
import com.fluently.english.ui.components.SpeakButton
import com.fluently.english.ui.components.StatItem
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Coral
import com.fluently.english.ui.theme.Danger
import com.fluently.english.ui.theme.Emerald
import com.fluently.english.ui.theme.Gold
import com.fluently.english.ui.theme.Success

@Composable
fun ReviewScreen(progress: Progress, today: Long, onReview: (String, Boolean) -> Unit, onGoToPath: () -> Unit) {
    // Snapshot of the cards due when the session starts.
    var session by remember { mutableIntStateOf(0) }
    val queue = remember(session) { progress.dueCards(today) }
    var index by remember(session) { mutableIntStateOf(0) }
    var known by remember(session) { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeader("المراجعة الذكية", subtitle = "تكرار متباعد بنظام صناديق لايتنر")
        Box(Modifier.weight(1f)) {
            when {
                queue.isNotEmpty() && index < queue.size -> Flashcards(
                    word = queue[index],
                    box = progress.cards[queue[index]]?.box ?: 0,
                    position = index,
                    total = queue.size,
                    onAnswer = { ok ->
                        onReview(queue[index], ok)
                        if (ok) known++
                        index++
                    },
                )
                queue.isNotEmpty() -> SessionDone(known, queue.size, onAgain = { session++ })
                else -> WordBank(progress, today, onGoToPath, onStart = { session++ })
            }
        }
    }
}

@Composable
private fun Flashcards(word: String, box: Int, position: Int, total: Int, onAnswer: (Boolean) -> Unit) {
    val entry = Course.wordsByEn[word]
    var flipped by remember(word) { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (flipped) 180f else 0f, tween(400), label = "flip")
    val speaker = LocalSpeaker.current
    LaunchedEffect(word) { speaker.speak(word) }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { LinearMeter(position.toFloat() / total, height = 8.dp) }
            HSpace(12.dp)
            Text("${position + 1}/$total", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        VSpace(20.dp)
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, AppTheme.extra.border),
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 14 * density
                }
                .clip(RoundedCornerShape(28.dp))
                .clickable { flipped = !flipped },
        ) {
            Box(
                Modifier.fillMaxSize().padding(24.dp).graphicsLayer { rotationY = if (rotation > 90f) 180f else 0f },
            ) {
                Pill("الصندوق ${box + 1} من 6", MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.TopCenter))
                Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Ltr { Text(word, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center) }
                    VSpace(16.dp)
                    SpeakButton(word, size = 52.dp)
                    if (rotation > 90f && entry != null) {
                        VSpace(28.dp)
                        Text(entry.ar, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                        VSpace(14.dp)
                        AutoText(
                            entry.example,
                            style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (rotation <= 90f) {
                    Text(
                        "تذكّر المعنى ثم اضغط على البطاقة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
        VSpace(18.dp)
        if (flipped) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AnswerButton("لم أتذكرها", Icons.Rounded.Close, Danger, Modifier.weight(1f)) { onAnswer(false) }
                AnswerButton("تذكرتها", Icons.Rounded.Check, Success, Modifier.weight(1f)) { onAnswer(true) }
            }
        } else {
            PrimaryButton(
                "اكشف المعنى", onClick = { flipped = true },
                color = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary,
            )
        }
        VSpace(12.dp)
    }
}

@Composable
private fun AnswerButton(text: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.12f), contentColor = color),
        shape = RoundedCornerShape(16.dp),
        elevation = null,
        modifier = modifier.height(56.dp),
    ) {
        Icon(icon, null)
        HSpace(8.dp)
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun SessionDone(known: Int, total: Int, onAgain: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        IconTile(Icons.Rounded.TaskAlt, Success, size = 80.dp)
        VSpace(20.dp)
        Text("أنهيت جلسة المراجعة", style = MaterialTheme.typography.headlineSmall)
        VSpace(8.dp)
        Text(
            "تذكرت $known من $total كلمة. ما نسيته سيعود غداً، وما تذكرته سيظهر بعد فترة أطول.",
            textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        VSpace(28.dp)
        PrimaryButton("حسناً", onAgain)
    }
}

@Composable
private fun WordBank(progress: Progress, today: Long, onGoToPath: () -> Unit, onStart: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val words = progress.cards.entries
        .mapNotNull { (en, card) -> Course.wordsByEn[en]?.let { it to card } }
        .filter { (w, _) -> query.isBlank() || w.en.contains(query, true) || w.ar.contains(query) }
        .sortedBy { it.first.en.lowercase() }
    val due = progress.dueCards(today).size

    LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp)) {
        item {
            AppCard(color = MaterialTheme.colorScheme.primaryContainer, bordered = false) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(Icons.Rounded.Style, MaterialTheme.colorScheme.primary, size = 48.dp, background = MaterialTheme.colorScheme.surface)
                    HSpace(14.dp)
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (due > 0) "$due كلمة جاهزة للمراجعة" else "لا توجد كلمات مستحقة الآن",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            if (progress.cards.isEmpty()) "أكمل دروس المفردات لتُضاف الكلمات هنا تلقائياً."
                            else "سنذكّرك بكل كلمة في الوقت المثالي قبل أن تنساها.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                if (progress.cards.isEmpty() || due > 0) {
                    VSpace(16.dp)
                    if (progress.cards.isEmpty()) PrimaryButton("اذهب إلى الدروس", onGoToPath)
                    else PrimaryButton("ابدأ المراجعة", onStart)
                }
            }
            VSpace(20.dp)
            AppCard {
                Row {
                    StatItem(Icons.Rounded.Style, Emerald, "${progress.wordsLearned}", "كلمة", Modifier.weight(1f))
                    StatItem(Icons.Rounded.Psychology, Gold, "${progress.wordsMastered}", "متقنة", Modifier.weight(1f))
                    StatItem(Icons.Rounded.Replay, Coral, "${progress.reviewsDone}", "مراجعة", Modifier.weight(1f))
                }
            }
            if (progress.cards.isNotEmpty()) {
                SectionHeader("بنك الكلمات")
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    placeholder = { Text("ابحث عن كلمة…") },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    singleLine = true, shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = AppTheme.extra.border,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                VSpace(12.dp)
            }
        }
        itemsIndexed(words, key = { _, it -> it.first.en }) { i, (word, card) ->
            val shape = when {
                words.size == 1 -> RoundedCornerShape(20.dp)
                i == 0 -> RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                i == words.lastIndex -> RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                else -> RoundedCornerShape(0.dp)
            }
            Surface(color = MaterialTheme.colorScheme.surface, shape = shape) {
                Column {
                    if (i > 0) HorizontalDivider(color = AppTheme.extra.border)
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Ltr { Text(word.en, style = MaterialTheme.typography.titleSmall) }
                            Text(word.ar, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Pill(
                            if (card.box >= 3) "متقنة" else "صندوق ${card.box + 1}",
                            if (card.box >= 3) Success else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        HSpace(10.dp)
                        SpeakButton(word.en, size = 34.dp)
                    }
                }
            }
        }
    }
}
