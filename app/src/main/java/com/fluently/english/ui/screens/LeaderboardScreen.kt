package com.fluently.english.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fluently.english.account.AuthException
import com.fluently.english.account.LeaderEntry
import com.fluently.english.data.progress.Progress
import com.fluently.english.data.progress.weekStart
import com.fluently.english.ui.components.AppCard
import com.fluently.english.ui.components.CircleIconButton
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.IconTile
import com.fluently.english.ui.components.Pill
import com.fluently.english.ui.components.ScreenHeader
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.components.isolateLatin
import com.fluently.english.ui.components.ltr
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Coral
import com.fluently.english.ui.theme.Gold

private val Silver = Color(0xFF9AA3AE)
private val Bronze = Color(0xFFC07A45)

@Composable
fun LeaderboardScreen(
    progress: Progress,
    uid: String,
    cloud: Boolean,
    today: Long,
    load: suspend () -> List<LeaderEntry>,
    onToggleShow: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    var rows by remember { mutableStateOf<List<LeaderEntry>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableIntStateOf(0) }
    LaunchedEffect(reload, progress.showOnLeaderboard) {
        if (!cloud) return@LaunchedEffect
        error = null
        try {
            rows = load()
        } catch (e: AuthException) {
            error = e.message
        }
    }
    val myXp = progress.weekXp(today)
    val daysLeft = (weekStart(today) + 6 - today).toInt()
    val list = rows.orEmpty()
    val myRank = list.indexOfFirst { it.uid == uid }.takeIf { it >= 0 }?.plus(1)

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        ScreenHeader(
            "ترتيب المتعلمين", onBack = onBack, subtitle = "دوري هذا الأسبوع",
            trailing = { if (cloud) CircleIconButton(Icons.Rounded.Refresh, { reload++ }) },
        )
        Column(Modifier.padding(horizontal = 20.dp)) {
            VSpace(8.dp)
            val extra = AppTheme.extra
            Column(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(extra.hero).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Rounded.EmojiEvents, null, tint = Gold, modifier = Modifier.size(44.dp))
                VSpace(6.dp)
                Text(
                    when {
                        !cloud -> "ترتيبك"
                        myRank != null -> "أنت في المركز #$myRank"
                        !progress.showOnLeaderboard -> "اسمك مخفي من الترتيب"
                        myXp == 0 -> "اجمع نقاطاً لتدخل الترتيب"
                        else -> "خارج أفضل 50 — واصل!"
                    },
                    style = MaterialTheme.typography.headlineSmall, color = extra.onHero, textAlign = TextAlign.Center,
                )
                VSpace(4.dp)
                Text(
                    "${ltr("$myXp")} نقطة هذا الأسبوع · ${if (daysLeft == 0) "ينتهي الأسبوع اليوم" else "ينتهي بعد $daysLeft ${if (daysLeft <= 10) "أيام" else "يوماً"}"}",
                    style = MaterialTheme.typography.bodyMedium, color = extra.onHeroMuted,
                )
            }

            if (!cloud) {
                VSpace(16.dp)
                AppCard(color = AppTheme.extra.subtle, bordered = false) {
                    Text("الترتيب يحتاج حساباً على الإنترنت. سجّل الدخول بحساب سحابي لتنافس المتعلمين الآخرين.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                VSpace(16.dp)
                when {
                    error != null -> AppCard(color = AppTheme.extra.subtle, bordered = false) {
                        Text(error.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                    }
                    rows == null -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    list.isEmpty() -> AppCard(color = AppTheme.extra.subtle, bordered = false) {
                        Text("لا أحد في الترتيب بعد هذا الأسبوع — كن الأول! أكمل درساً لتظهر هنا.", style = MaterialTheme.typography.bodyMedium)
                    }
                    else -> {
                        if (list.size >= 3) Podium(list.take(3), uid)
                        VSpace(12.dp)
                        AppCard(padding = 4.dp) {
                            list.forEachIndexed { i, e -> if (list.size < 3 || i >= 3) LeaderRow(i + 1, e, e.uid == uid) }
                            if (list.size == 3) {
                                Text(
                                    "ادعُ أصدقاءك ليتنافسوا معك!", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(14.dp),
                                )
                            }
                        }
                        if (myRank == null && progress.showOnLeaderboard && myXp > 0) {
                            VSpace(10.dp)
                            AppCard(padding = 4.dp) { LeaderRow(null, LeaderEntry(uid, progress.name, myXp, progress.currentLevel.code, progress.streak), true) }
                        }
                    }
                }
                VSpace(16.dp)
                AppCard(padding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("أظهر اسمي في الترتيب", style = MaterialTheme.typography.titleSmall)
                            Text("يظهر اسمك ونقاط الأسبوع فقط", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = progress.showOnLeaderboard, onCheckedChange = onToggleShow)
                    }
                }
            }
            VSpace(12.dp)
            Text(
                "كيف تصعد؟ كل درس واختبار وقصة ومحادثة يمنحك نقاطاً. الترتيب يبدأ من جديد كل يوم اثنين.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VSpace(28.dp)
        }
    }
}

@Composable
private fun Podium(top: List<LeaderEntry>, uid: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        // Visual order: 2nd, 1st, 3rd.
        listOf(1, 0, 2).forEach { i ->
            val e = top[i]
            val color = listOf(Gold, Silver, Bronze)[i]
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Avatar(e.name, color, if (i == 0) 64 else 52, e.uid == uid)
                VSpace(6.dp)
                Text(isolateLatin(e.name.ifBlank { "متعلم" }), style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Text("${ltr("${e.xp}")} نقطة", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                VSpace(6.dp)
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 6.dp)
                        .height(listOf(70, 52, 40)[i].dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(color.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("${i + 1}", style = MaterialTheme.typography.headlineSmall, color = color)
                }
            }
        }
    }
}

@Composable
private fun Avatar(name: String, color: Color, size: Int, me: Boolean) {
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(color)
            .then(if (me) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(name.trim().firstOrNull()?.uppercase() ?: "؟", style = MaterialTheme.typography.titleLarge, color = Color.White)
    }
}

@Composable
private fun LeaderRow(rank: Int?, e: LeaderEntry, me: Boolean) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(if (me) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(rank?.let { "$it" } ?: "—", style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
        HSpace(8.dp)
        Avatar(e.name, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), 38, false)
        HSpace(12.dp)
        Column(Modifier.weight(1f)) {
            Text(isolateLatin(e.name.ifBlank { "متعلم" }) + if (me) " (أنت)" else "", style = MaterialTheme.typography.titleSmall, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (e.level.isNotBlank()) Text(e.level, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (e.streak > 0) {
                    HSpace(8.dp)
                    Icon(Icons.Rounded.LocalFireDepartment, null, tint = Coral, modifier = Modifier.size(14.dp))
                    Text("${e.streak}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Pill("${ltr("${e.xp}")} نقطة", Gold)
    }
}

/** Compact entry card for the home screen. */
@Composable
fun LeaderboardCard(progress: Progress, today: Long, onClick: () -> Unit) {
    AppCard(onClick = onClick, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(Icons.Rounded.EmojiEvents, Gold, size = 44.dp)
            HSpace(12.dp)
            Column(Modifier.weight(1f)) {
                Text("ترتيب المتعلمين", style = MaterialTheme.typography.titleSmall)
                Text(
                    "${ltr("${progress.weekXp(today)}")} نقطة هذا الأسبوع — شاهد مركزك",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
