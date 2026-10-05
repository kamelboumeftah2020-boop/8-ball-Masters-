package com.fluently.english.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fluently.english.data.progress.Progress
import com.fluently.english.data.progress.SkillKey
import com.fluently.english.ui.components.AppCard
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.LinearMeter
import com.fluently.english.ui.components.ScreenHeader
import com.fluently.english.ui.components.SectionHeader
import com.fluently.english.ui.components.StatItem
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.components.ltr
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Coral
import com.fluently.english.ui.theme.Danger
import com.fluently.english.ui.theme.Emerald
import com.fluently.english.ui.theme.Gold
import com.fluently.english.ui.theme.Success

private val DayNames = listOf("خ", "ج", "س", "ح", "ن", "ث", "ر") // epoch day 0 was a Thursday

/** Practice suggested for a weak skill: label and route. */
private fun SkillKey.remedy(): Pair<String, String> = when (this) {
    SkillKey.GRAMMAR -> "راجع مرجع القواعد ودفتر أخطائك" to "grammar"
    SkillKey.VOCABULARY -> "راجع بطاقاتك والعب تحدي السرعة" to "game/speed"
    SkillKey.READING -> "اقرأ قصة من المكتبة بمستواك" to "readers"
    SkillKey.LISTENING -> "تدرّب على الإملاء والاستماع" to "game/dictation"
    SkillKey.SPEAKING -> "خُض محادثة تفاعلية وتمرّن في مختبر النطق" to "conversations"
    SkillKey.WRITING -> "اكتب مهمة الكتابة في اختبار محاكاة" to "mocks"
}

@Composable
fun ReportScreen(progress: Progress, today: Long, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val week = (today - 6..today).toList()
    val thisWeek = progress.xpBetween(today - 6, today)
    val lastWeek = progress.xpBetween(today - 13, today - 7)
    val activeDays = week.count { (progress.dayXp[it] ?: 0) > 0 || it in progress.activeDays }
    val accuracy = progress.skillAccuracy()
    val weakest = accuracy.entries.filter { (progress.skillTotal[it.key.name] ?: 0) >= 5 }.minByOrNull { it.value }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        ScreenHeader("تقريري الأسبوعي", onBack = onBack, subtitle = "آخر 7 أيام")
        Column(Modifier.padding(horizontal = 20.dp)) {
            VSpace(8.dp)
            AppCard {
                Row {
                    StatItem(Icons.Rounded.Bolt, Gold, ltr("$thisWeek"), "نقاط هذا الأسبوع", Modifier.weight(1f))
                    StatItem(Icons.Rounded.LocalFireDepartment, Coral, "$activeDays/7", "أيام نشطة", Modifier.weight(1f))
                    StatItem(Icons.AutoMirrored.Rounded.MenuBook, Emerald, ltr("${progress.wordsRead}"), "كلمة قرأتها", Modifier.weight(1f))
                }
                VSpace(14.dp)
                val diff = thisWeek - lastWeek
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (diff >= 0) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown, null,
                        tint = if (diff >= 0) Success else Danger,
                    )
                    HSpace(8.dp)
                    Text(
                        when {
                            lastWeek == 0 && thisWeek == 0 -> "ابدأ اليوم — درس واحد يكفي لبدء سلسلتك"
                            lastWeek == 0 -> "بداية رائعة! هذا أول أسبوع لك"
                            diff >= 0 -> "أفضل من الأسبوع الماضي بـ ${ltr("$diff")} نقطة 👏"
                            else -> "أقل من الأسبوع الماضي بـ ${ltr("${-diff}")} نقطة — عُد إلى إيقاعك"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            SectionHeader("نشاطك اليومي")
            AppCard {
                val max = week.maxOf { progress.dayXp[it] ?: 0 }.coerceAtLeast(progress.dailyGoal)
                run {
                    Row(Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
                        week.forEach { day ->
                            val xp = progress.dayXp[day] ?: 0
                            Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                                if (xp > 0) Text("$xp", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight((xp.toFloat() / max).coerceIn(0.03f, 1f) * 0.78f)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                                        .background(
                                            when {
                                                xp >= progress.dailyGoal -> Emerald
                                                xp > 0 -> Emerald.copy(alpha = 0.45f)
                                                else -> AppTheme.extra.subtle
                                            },
                                        ),
                                )
                                VSpace(6.dp)
                                Text(
                                    DayNames[(day % 7).toInt()], style = MaterialTheme.typography.labelMedium,
                                    color = if (day == today) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                VSpace(8.dp)
                Text(
                    "الأعمدة الخضراء الكاملة = أيام حققت فيها هدفك اليومي (${ltr("${progress.dailyGoal}")} نقطة).",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionHeader("مستواك في كل مهارة")
            AppCard {
                if (accuracy.isEmpty()) {
                    Text(
                        "أكمل بعض الدروس والتمارين لتظهر هنا دقتك في كل مهارة.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SkillKey.entries.forEach { skill ->
                    val pct = accuracy[skill] ?: return@forEach
                    val total = progress.skillTotal[skill.name] ?: 0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(skill.labelAr, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        Text("${ltr("$pct%")} صحيحة من ${ltr("$total")}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    VSpace(6.dp)
                    LinearMeter(pct / 100f, height = 8.dp, color = if (pct >= 80) Success else if (pct >= 60) Gold else Coral)
                    VSpace(14.dp)
                }
            }

            if (weakest != null) {
                SectionHeader("ركّز هذا الأسبوع على")
                val (label, route) = weakest.key.remedy()
                AppCard(onClick = { onOpen(route) }, color = MaterialTheme.colorScheme.primaryContainer, bordered = false) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Lightbulb, null, tint = MaterialTheme.colorScheme.primary)
                        HSpace(12.dp)
                        Column(Modifier.weight(1f)) {
                            Text("${weakest.key.labelAr} (${ltr("${weakest.value}%")})", style = MaterialTheme.typography.titleSmall)
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            VSpace(28.dp)
        }
    }
}
