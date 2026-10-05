package com.fluently.english.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Abc
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fluently.english.data.content.Course
import com.fluently.english.data.content.Scenarios
import com.fluently.english.data.content.SoundLessons
import com.fluently.english.data.content.Word
import com.fluently.english.data.progress.Progress
import com.fluently.english.ui.components.ltr
import com.fluently.english.ui.components.AppCard
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.components.IconTile
import com.fluently.english.ui.components.Pill
import com.fluently.english.ui.components.PrimaryButton
import com.fluently.english.ui.components.ScoreHeader
import com.fluently.english.ui.components.ScreenHeader
import com.fluently.english.ui.components.SecondaryButton
import com.fluently.english.ui.components.SectionHeader
import com.fluently.english.ui.components.VSpace
import com.fluently.english.ui.theme.AppTheme
import com.fluently.english.ui.theme.Coral
import com.fluently.english.ui.theme.Danger
import com.fluently.english.ui.theme.Emerald
import com.fluently.english.ui.theme.Gold

/** Words used by games: the learner's review cards, or the words of unlocked levels. */
fun gameWords(progress: Progress): List<Word> {
    val learned = progress.cards.keys.mapNotNull { Course.wordsByEn[it] }
    if (learned.size >= 12) return learned
    return Course.levels.filter { progress.isLevelUnlocked(it.level) }.flatMap { it.words }
}

@Composable
fun PracticeScreen(
    progress: Progress,
    onConversations: () -> Unit,
    onSounds: () -> Unit,
    onScramble: () -> Unit,
    onSpeed: () -> Unit,
    onDictation: () -> Unit,
    onMistakes: () -> Unit,
    onVerbs: () -> Unit,
    onGrammar: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()),
    ) {
        ScreenHeader("تدرّب", subtitle = "طرق مختلفة لتثبيت ما تتعلمه")
        Column(Modifier.padding(horizontal = 20.dp)) {
            VSpace(8.dp)
            HeroFeature(
                Icons.Rounded.Forum, "محادثات تفاعلية",
                "تحدث مع شخصيات في مواقف حقيقية: المقهى، الفندق، الطبيب، مقابلة العمل… واختر ردك أو قله بصوتك.",
                "${progress.conversationStars.size} من ${Scenarios.size} محادثة",
                onConversations,
            )

            SectionHeader("مهارات")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureTile(
                    Icons.Rounded.Hearing, Coral, "مختبر النطق", "${SoundLessons.size} أصوات صعبة على العرب",
                    Modifier.weight(1f), onSounds,
                )
                FeatureTile(
                    Icons.Rounded.EditNote, Danger, "دفتر أخطائي",
                    if (progress.mistakes.isEmpty()) "لا أخطاء محفوظة" else "${progress.mistakes.size} سؤال للمراجعة",
                    Modifier.weight(1f), onMistakes,
                )
            }
            VSpace(12.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureTile(Icons.Rounded.Abc, Gold, "الأفعال الشاذة", "62 فعلاً بالجدول والاختبار", Modifier.weight(1f), onVerbs)
                FeatureTile(Icons.AutoMirrored.Rounded.MenuBook, Emerald, "مرجع القواعد", "كل الشروحات في مكان واحد", Modifier.weight(1f), onGrammar)
            }

            SectionHeader("ألعاب")
            GameRow(Icons.Rounded.Shuffle, Emerald, "رتّب الحروف", "كوّن الكلمة من حروفها المبعثرة", onScramble)
            VSpace(10.dp)
            GameRow(
                Icons.Rounded.Timer, Coral, "تحدي السرعة",
                if (progress.speedBest > 0) "60 ثانية · أفضل نتيجة: ${progress.speedBest}" else "كم كلمة تعرف في 60 ثانية؟",
                onSpeed,
            )
            VSpace(10.dp)
            GameRow(Icons.Rounded.Keyboard, Gold, "الإملاء", "استمع واكتب الكلمة بشكل صحيح", onDictation)
            VSpace(28.dp)
        }
    }
}

@Composable
private fun HeroFeature(icon: ImageVector, title: String, body: String, meta: String, onClick: () -> Unit) {
    val extra = AppTheme.extra
    Column(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(extra.hero)
            .then(Modifier)
            .padding(22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(icon, MaterialTheme.colorScheme.primary, size = 48.dp, background = extra.heroTrack)
            HSpace(14.dp)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = extra.onHero)
                Text(meta, style = MaterialTheme.typography.labelMedium, color = extra.onHeroMuted)
            }
        }
        VSpace(12.dp)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = extra.onHeroMuted)
        VSpace(16.dp)
        PrimaryButton("ابدأ محادثة", onClick)
    }
}

@Composable
private fun FeatureTile(icon: ImageVector, tint: Color, title: String, body: String, modifier: Modifier, onClick: () -> Unit) {
    AppCard(modifier = modifier, onClick = onClick, padding = 16.dp) {
        IconTile(icon, tint, size = 42.dp)
        VSpace(12.dp)
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, minLines = 2)
    }
}

@Composable
private fun GameRow(icon: ImageVector, tint: Color, title: String, body: String, onClick: () -> Unit) {
    AppCard(onClick = onClick, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(icon, tint, size = 44.dp)
            HSpace(14.dp)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Shared end screen for practice activities. */
@Composable
fun PracticeResult(
    percent: Int,
    title: String,
    subtitle: String,
    xp: Int,
    onAgain: () -> Unit,
    onDone: () -> Unit,
    extra: @Composable () -> Unit = {},
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        VSpace(48.dp)
        ScoreHeader(percent, percent >= 60, title, subtitle)
        VSpace(14.dp)
        Pill("${ltr("+$xp")} نقطة خبرة", Gold)
        extra()
        VSpace(32.dp)
        PrimaryButton("انتهيت", onDone)
        VSpace(10.dp)
        SecondaryButton("مرة أخرى", onAgain)
        VSpace(24.dp)
    }
}

/** Five-star style rating used by conversations. */
@Composable
fun Stars(count: Int, max: Int = 3) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(max) { i ->
            Box(
                Modifier.size(14.dp).clip(CircleShape).background(if (i < count) Gold else MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}

