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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.LearningMethods
import com.fluently.english.data.progress.Progress
import com.fluently.english.data.progress.achievements
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.ui.components.AppTopBar
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.LevelBadge
import com.fluently.english.ui.components.SectionTitle
import com.fluently.english.ui.components.StatItem
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.theme.Danger
import com.fluently.english.ui.theme.Indigo
import com.fluently.english.ui.theme.IndigoDeep

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    progress: Progress,
    onNameChange: (String) -> Unit,
    onGoalChange: (Int) -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    onPlacement: () -> Unit,
    onMethods: () -> Unit,
    onReset: () -> Unit,
) {
    var editingName by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            // Header
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.linearGradient(listOf(Indigo, IndigoDeep)))
                    .padding(24.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(64.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(progress.name.firstOrNull()?.uppercase() ?: "👤", color = Color.White, fontSize = 28.sp)
                    }
                    HSpace(16.dp)
                    Column(Modifier.weight(1f)) {
                        Text(
                            progress.name.ifBlank { "متعلم طموح" },
                            style = MaterialTheme.typography.headlineSmall, color = Color.White,
                        )
                        Text(
                            "المستوى الحالي: ${progress.currentLevel.code} — ${progress.currentLevel.titleAr}",
                            style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                    IconButton(onClick = { editingName = true }) { Icon(Icons.Rounded.Edit, "تعديل الاسم", tint = Color.White) }
                }
            }

            Column(Modifier.padding(horizontal = 20.dp)) {
                VSpace(20.dp)
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(vertical = 18.dp)) {
                        Row {
                            StatItem("⚡", "${progress.xp}", "نقطة خبرة", Modifier.weight(1f))
                            StatItem("🔥", "${progress.streak}", "سلسلة حالية", Modifier.weight(1f))
                            StatItem("🏅", "${progress.bestStreak}", "أطول سلسلة", Modifier.weight(1f))
                        }
                        VSpace(16.dp)
                        Row {
                            StatItem("📘", "${progress.lessonsCompleted}", "درس مكتمل", Modifier.weight(1f))
                            StatItem("📚", "${progress.wordsLearned}", "كلمة", Modifier.weight(1f))
                            StatItem("🎓", "${CefrLevel.entries.count { progress.isLevelPassed(it) }}", "مستوى مجتاز", Modifier.weight(1f))
                        }
                    }
                }

                SectionTitle("الشهادات")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    CefrLevel.entries.forEach { level ->
                        val passed = progress.isLevelPassed(level)
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            LevelBadge(level, size = 48.dp, locked = !passed)
                            VSpace(4.dp)
                            Text(
                                if (passed) "✓" else "—", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (progress.placementTaken) {
                    Text(
                        "نتيجة اختبار تحديد المستوى: ${progress.placementLevel?.code ?: "Pre-A1"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                SectionTitle("الإنجازات")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    maxItemsInEachRow = 4,
                ) {
                    progress.achievements().forEach { a ->
                        Column(
                            Modifier.weight(1f).alpha(if (a.unlocked) 1f else 0.35f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiaryContainer),
                                contentAlignment = Alignment.Center,
                            ) { Text(a.icon, fontSize = 24.sp) }
                            VSpace(4.dp)
                            Text(a.title, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                            Text(
                                a.description, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                SectionTitle("الهدف اليومي")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(20 to "خفيف", 50 to "متوسط", 100 to "جاد", 200 to "مكثف").forEach { (goal, label) ->
                        FilterChip(
                            selected = progress.dailyGoal == goal,
                            onClick = { onGoalChange(goal) },
                            label = { Text("$label • $goal") },
                        )
                    }
                }

                SectionTitle("سرعة النطق")
                SpeechRateSetting(progress.speechRate, onSpeechRateChange)

                SectionTitle("المزيد")
                SettingRow("🧭", "إعادة اختبار تحديد المستوى", onPlacement)
                SettingRow("📖", "المنهجية والمصادر العالمية", onMethods)
                SettingRow("🗑️", "إعادة ضبط التقدم", { confirmReset = true }, color = Danger)
                VSpace(24.dp)
            }
        }
    }

    if (editingName) {
        var text by remember { mutableStateOf(progress.name) }
        AlertDialog(
            onDismissRequest = { editingName = false },
            title = { Text("اسمك") },
            text = { OutlinedTextField(text, { text = it }, singleLine = true) },
            confirmButton = { TextButton(onClick = { onNameChange(text); editingName = false }) { Text("حفظ") } },
            dismissButton = { TextButton(onClick = { editingName = false }) { Text("إلغاء") } },
        )
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("إعادة ضبط التقدم؟") },
            text = { Text("سيتم حذف جميع الدروس المكتملة والنقاط والكلمات والشهادات. لا يمكن التراجع عن هذا.") },
            confirmButton = {
                TextButton(onClick = { confirmReset = false; onReset() }) { Text("حذف كل شيء", color = Danger) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("إلغاء") } },
        )
    }
}

@Composable
private fun SpeechRateSetting(rate: Float, onChange: (Float) -> Unit) {
    val speaker = LocalSpeaker.current
    var value by remember(rate) { mutableFloatStateOf(rate) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Row {
                Text("بطيء", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                Text("×${"%.1f".format(value)}", style = MaterialTheme.typography.labelLarge)
                Text("سريع", style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            }
            Slider(
                value = value,
                onValueChange = { value = it },
                valueRange = 0.5f..1.3f,
                steps = 7,
                onValueChangeFinished = {
                    onChange(value)
                    speaker.baseRate = value
                    speaker.speak("This is how I sound now.")
                },
            )
        }
    }
}

@Composable
private fun SettingRow(emoji: String, title: String, onClick: () -> Unit, color: Color = Color.Unspecified) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(CardDefaults.shape).clickable(onClick = onClick),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 20.sp)
            HSpace(14.dp)
            Text(title, style = MaterialTheme.typography.titleMedium, color = color, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun MethodsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { AppTopBar("المنهجية والمصادر", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Text(
                "صُمم «طلاقة» بالاعتماد على أشهر المعايير والطرق العلمية في تعليم اللغة الإنجليزية حول العالم:",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VSpace(8.dp)
            LearningMethods.forEach { (title, body) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        VSpace(4.dp)
                        Text(body, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            SectionTitle("جدول المستويات")
            CefrLevel.entries.forEach { level ->
                Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    LevelBadge(level)
                    HSpace(12.dp)
                    Column(Modifier.weight(1f)) {
                        Text("${level.titleAr} • ${level.cambridge}", style = MaterialTheme.typography.titleSmall)
                        Text(level.canDoAr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "IELTS ${level.ielts}  •  TOEFL ${level.toefl}",
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            VSpace(24.dp)
        }
    }
}
