package com.fluently.english.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.theme.Indigo
import com.fluently.english.ui.theme.IndigoDeep

@Composable
fun WelcomeScreen(onPlacement: (String) -> Unit, onStartFromZero: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(Brush.linearGradient(listOf(Indigo, IndigoDeep)))
                .padding(horizontal = 24.dp, vertical = 40.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("🎓", fontSize = 64.sp)
                VSpace(12.dp)
                Text("طلاقة", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                Text("Fluently", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.8f))
                VSpace(14.dp)
                Text(
                    "تعلم الإنجليزية من الصفر حتى الاحتراف، وفق المستويات العالمية المعتمدة في كامبريدج وأكسفورد.",
                    color = Color.White.copy(alpha = 0.92f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
                VSpace(20.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CefrLevel.entries.forEach { level ->
                        Box(
                            Modifier.clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.16f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text(level.code, color = Color.White, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        Column(Modifier.padding(24.dp)) {
            Feature("🧭", "اختبار تحديد مستوى", "اختبار تكيّفي على نمط Oxford وCambridge يحدد مستواك بدقة.")
            Feature("🗺️", "مسار متكامل من A1 إلى C2", "72 درساً في القواعد والمفردات والقراءة والاستماع.")
            Feature("🧠", "مراجعة ذكية", "بطاقات بالتكرار المتباعد لتثبيت الكلمات في ذاكرتك.")
            Feature("🎓", "امتحانات وشهادات", "امتحان في نهاية كل مستوى ليفتح لك المستوى التالي.")

            VSpace(16.dp)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("ما اسمك؟ (اختياري)") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            VSpace(20.dp)
            PrimaryButton("حدد مستواك الآن (10–15 دقيقة)", onClick = { onPlacement(name) })
            VSpace(10.dp)
            OutlinedButton(
                onClick = { onStartFromZero(name) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("أنا مبتدئ، أريد البدء من الصفر", style = MaterialTheme.typography.titleMedium)
            }
            VSpace(24.dp)
        }
    }
}

@Composable
private fun Feature(emoji: String, title: String, body: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { Text(emoji, fontSize = 22.sp) }
        HSpace(14.dp)
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
