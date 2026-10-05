package com.fluently.english.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fluently.english.data.content.Course
import com.fluently.english.data.progress.Progress
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.ui.components.AppTopBar
import com.fluently.english.ui.components.AutoText
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.LinearMeter
import com.fluently.english.ui.components.Ltr
import com.fluently.english.ui.components.Pill
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.SectionTitle
import com.fluently.english.ui.components.SpeakButton
import com.fluently.english.ui.components.StatItem
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.theme.Danger
import com.fluently.english.ui.theme.Success

@Composable
fun ReviewScreen(progress: Progress, today: Long, onReview: (String, Boolean) -> Unit, onGoToPath: () -> Unit) {
    // Snapshot of the cards due when the session starts.
    var session by remember { mutableIntStateOf(0) }
    val queue = remember(session) { progress.dueCards(today) }
    var index by remember(session) { mutableIntStateOf(0) }
    var known by remember(session) { mutableIntStateOf(0) }

    Scaffold(topBar = { AppTopBar("المراجعة الذكية") }, containerColor = MaterialTheme.colorScheme.background) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                queue.isNotEmpty() && index < queue.size -> Flashcards(
                    word = queue[index],
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
private fun Flashcards(word: String, position: Int, total: Int, onAnswer: (Boolean) -> Unit) {
    val entry = Course.wordsByEn[word]
    var flipped by remember(word) { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (flipped) 180f else 0f, tween(350), label = "flip")
    val speaker = LocalSpeaker.current
    LaunchedEffect(word) { speaker.speak(word) }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { LinearMeter(position.toFloat() / total) }
            HSpace(12.dp)
            Text("${position + 1}/$total", style = MaterialTheme.typography.labelLarge)
        }
        VSpace(24.dp)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12 * density
                }
                .clickable { flipped = !flipped },
        ) {
            Box(
                Modifier.fillMaxSize().padding(24.dp).graphicsLayer { rotationY = if (rotation > 90f) 180f else 0f },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Ltr { Text(word, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center) }
                    VSpace(12.dp)
                    SpeakButton(word, size = 52.dp)
                    if (rotation > 90f && entry != null) {
                        VSpace(24.dp)
                        Text(entry.ar, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                        VSpace(16.dp)
                        AutoText(
                            entry.example,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        VSpace(24.dp)
                        Text(
                            "هل تتذكر المعنى؟ فكّر ثم اضغط على البطاقة",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        VSpace(20.dp)
        if (flipped) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onAnswer(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).height(54.dp),
                ) { Text("لم أتذكرها", style = MaterialTheme.typography.titleMedium) }
                Button(
                    onClick = { onAnswer(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Success),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).height(54.dp),
                ) { Text("تذكرتها", style = MaterialTheme.typography.titleMedium) }
            }
        } else {
            PrimaryButton("اكشف المعنى", onClick = { flipped = true })
        }
    }
}

@Composable
private fun SessionDone(known: Int, total: Int, onAgain: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🎉", fontSize = 64.sp)
        VSpace(12.dp)
        Text("أنهيت جلسة المراجعة!", style = MaterialTheme.typography.headlineSmall)
        VSpace(8.dp)
        Text(
            "تذكرت $known من $total كلمة. الكلمات التي نسيتها ستعود إليك غداً، والتي تذكرتها ستظهر بعد فترة أطول.",
            textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VSpace(24.dp)
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

    LazyColumn(contentPadding = PaddingValues(20.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✅", fontSize = 40.sp)
                    VSpace(8.dp)
                    Text("لا توجد كلمات للمراجعة الآن", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (progress.cards.isEmpty()) "أكمل دروس المفردات لتُضاف الكلمات هنا تلقائياً."
                        else "عُد لاحقاً — سنذكّرك بكل كلمة في الوقت المثالي قبل أن تنساها.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (progress.cards.isEmpty()) {
                        VSpace(12.dp)
                        PrimaryButton("اذهب إلى الدروس", onGoToPath)
                    } else if (progress.dueCards(today).isNotEmpty()) {
                        VSpace(12.dp)
                        PrimaryButton("ابدأ المراجعة", onStart)
                    }
                }
            }
            VSpace(16.dp)
            Row {
                StatItem("📚", "${progress.wordsLearned}", "كلمة متعلَّمة", Modifier.weight(1f))
                StatItem("🧠", "${progress.wordsMastered}", "كلمة متقنة", Modifier.weight(1f))
                StatItem("🔁", "${progress.reviewsDone}", "مراجعة", Modifier.weight(1f))
            }
            if (progress.cards.isNotEmpty()) {
                SectionTitle("بنك الكلمات")
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    placeholder = { Text("ابحث عن كلمة…") },
                    singleLine = true, shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                VSpace(8.dp)
            }
        }
        items(words, key = { it.first.en }) { (word, card) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    SpeakButton(word.en, size = 36.dp)
                    HSpace(12.dp)
                    Column(Modifier.weight(1f)) {
                        Ltr { Text(word.en, style = MaterialTheme.typography.titleMedium) }
                        Text(word.ar, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Pill(
                        if (card.box >= 3) "متقنة" else "المستوى ${card.box + 1}",
                        if (card.box >= 3) Success else MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
