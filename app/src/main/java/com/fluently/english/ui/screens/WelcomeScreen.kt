package com.fluently.english.ui.screens

import com.fluently.english.data.progress.LearningGoal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.IconTile
import com.fluently.english.ui.components.Ltr
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.SecondaryButton
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Coral
import com.fluently.english.ui.theme.Emerald
import com.fluently.english.ui.theme.Gold
import com.fluently.english.ui.theme.color

@Composable
fun WelcomeScreen(
    name: String,
    goal: LearningGoal?,
    onGoal: (LearningGoal) -> Unit,
    onPlacement: () -> Unit,
    onStartFromZero: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        VSpace(20.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandMark(36.dp)
            HSpace(10.dp)
            Text("طلاقة", style = MaterialTheme.typography.titleLarge)
            HSpace(6.dp)
            Text("Fluently", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        VSpace(36.dp)
        if (name.isNotBlank()) {
            Text("أهلاً ${name.trim()} 👋", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            VSpace(4.dp)
        }
        Text("تعلّم الإنجليزية", style = MaterialTheme.typography.displaySmall)
        Text("من الصفر حتى الاحتراف", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        VSpace(12.dp)
        Text(
            "منهج كامل وفق المستويات العالمية المعتمدة في كامبريدج وأكسفورد، بدروس قصيرة ومراجعة ذكية وامتحانات.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        VSpace(28.dp)
        LevelStaircase()

        VSpace(28.dp)
        Feature(Icons.Rounded.Explore, Emerald, "اختبار تحديد المستوى", "تكيّفي على نمط Oxford وCambridge")
        Feature(Icons.Rounded.Psychology, Coral, "مراجعة ذكية بالتكرار المتباعد", "تثبّت الكلمات في ذاكرتك طويلاً")
        Feature(Icons.Rounded.WorkspacePremium, Gold, "امتحان وشهادة لكل مستوى", "من A1 حتى C2")

        VSpace(24.dp)
        Text("لماذا تتعلم الإنجليزية؟", style = MaterialTheme.typography.titleMedium)
        Text(
            "نبني لك خطة تناسب هدفك (يمكنك تغييره لاحقاً)",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VSpace(10.dp)
        GoalPicker(goal, onGoal)
        VSpace(14.dp)
        PrimaryButton("حدد مستواك الآن", onClick = onPlacement, icon = Icons.AutoMirrored.Rounded.ArrowForward)
        VSpace(10.dp)
        SecondaryButton("أنا مبتدئ، أبدأ من الصفر", onClick = onStartFromZero)
        VSpace(28.dp)
    }
}

/** Six rising bars — the A1→C2 journey — used as the welcome illustration. */
@Composable
private fun LevelStaircase() {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(AppTheme.extra.hero)
            .padding(20.dp),
    ) {
        Column {
            Text("رحلتك", style = MaterialTheme.typography.labelLarge, color = AppTheme.extra.onHeroMuted)
            Text("6 مستويات · 90 درساً", style = MaterialTheme.typography.titleLarge, color = AppTheme.extra.onHero)
            VSpace(18.dp)
            Ltr {
                Row(
                    Modifier.fillMaxWidth().height(130.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    CefrLevel.entries.forEachIndexed { i, level ->
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height((28 + i * 16).dp)
                                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                    .background(level.color()),
                            )
                            VSpace(6.dp)
                            Text(level.code, style = MaterialTheme.typography.labelMedium, color = AppTheme.extra.onHero)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Feature(icon: ImageVector, tint: Color, title: String, body: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(icon, tint, size = 44.dp)
        HSpace(14.dp)
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** The app's logo: three rising bars on an emerald tile. */
@Composable
fun BrandMark(size: Dp) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(size * 0.3f)).background(Emerald).padding(size * 0.22f),
    ) {
        Ltr {
            Row(
                Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(size * 0.08f),
                verticalAlignment = Alignment.Bottom,
            ) {
                listOf(0.4f, 0.7f, 1f).forEachIndexed { i, h ->
                    Box(
                        Modifier
                            .width(size * 0.14f)
                            .fillMaxHeight(h)
                            .clip(RoundedCornerShape(size * 0.05f))
                            .background(if (i == 2) Gold else Color.White),
                    )
                }
            }
        }
    }
}
