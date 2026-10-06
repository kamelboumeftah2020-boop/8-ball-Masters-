package com.fluently.english.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.OndemandVideo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.fluently.english.ui.components.HSpace
import com.fluently.english.ui.theme.Gold
import com.unity3d.mediation.LevelPlayAdError
import com.unity3d.mediation.LevelPlayAdInfo
import com.unity3d.mediation.LevelPlayAdSize
import com.unity3d.mediation.banner.LevelPlayBannerAdView
import com.unity3d.mediation.banner.LevelPlayBannerAdViewListener

/** Adds bonus XP to the learner's progress (provided by the app; no-op in previews/tests). */
val LocalAddBonusXp = androidx.compose.runtime.staticCompositionLocalOf<(Int) -> Unit> { {} }

/** "Double your XP" offer for result screens; nothing when [xp] is 0 or no ad is ready. */
@Composable
fun DoubleXpOffer(xp: Int, modifier: Modifier = Modifier) {
    if (xp <= 0) return
    val add = LocalAddBonusXp.current
    RewardedOffer("ضاعف نقاطك: +$xp نقطة إضافية", onReward = { add(xp) }, modifier = modifier)
}

/** A 320×50 LevelPlay banner; takes no space until an ad has loaded. */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    if (!Ads.initialized) return
    val context = LocalContext.current
    val activity = context.findActivity() ?: return
    var loaded by remember { mutableStateOf(false) }
    val banner = remember {
        val config = LevelPlayBannerAdView.Config.Builder().setAdSize(LevelPlayAdSize.BANNER).build()
        LevelPlayBannerAdView(activity, Ads.BANNER_ID, config).apply {
            setBannerListener(object : LevelPlayBannerAdViewListener {
                override fun onAdLoaded(adInfo: LevelPlayAdInfo) { loaded = true }
                override fun onAdLoadFailed(error: LevelPlayAdError) {
                    loaded = false
                    Ads.note("بانر: ${error.errorCode} ${error.errorMessage}")
                    postDelayed({ runCatching { loadAd() } }, 60_000)
                }
            })
            loadAd()
        }
    }
    DisposableEffect(banner) { onDispose { runCatching { banner.destroy() } } }
    Box(
        modifier.fillMaxWidth().height(if (loaded) 50.dp else 0.dp),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(factory = { banner }, modifier = Modifier.size(320.dp, 50.dp))
    }
}

/**
 * An optional "watch an ad for a reward" button. Hidden when no ad is ready;
 * [onReward] runs only after the ad was watched to the end.
 */
@Composable
fun RewardedOffer(label: String, onReward: () -> Unit, modifier: Modifier = Modifier) {
    if (!Ads.rewardedReady) return
    val context = LocalContext.current
    var used by remember { mutableStateOf(false) }
    if (used) return
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Gold.copy(alpha = 0.14f))
            .clickable { Ads.showRewarded(context, onReward = { used = true; onReward() }) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.OndemandVideo, null, tint = Gold)
        HSpace(10.dp)
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall)
            Text("شاهد إعلاناً قصيراً (اختياري)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Hidden diagnostics: what LevelPlay reported, plus its test suite (shows the networks set up for each ad unit). */
@Composable
fun AdsDiagnosticsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var suite by remember { mutableStateOf(Ads.testSuiteEnabled(context)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("حالة الإعلانات") },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                Text(if (Ads.initialized) "LevelPlay: مُهيّأ" else "LevelPlay: غير مُهيّأ", style = MaterialTheme.typography.titleSmall)
                if (Ads.log.isEmpty()) Text("لا توجد أحداث بعد", style = MaterialTheme.typography.bodySmall)
                Ads.log.forEach { Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp)) }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Text("أداة اختبار LevelPlay (بعد إعادة فتح التطبيق)", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Switch(checked = suite, onCheckedChange = { suite = it; Ads.setTestSuiteEnabled(context, it) })
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } },
        dismissButton = {
            if (suite && Ads.initialized) TextButton(onClick = { Ads.launchTestSuite(context) }) { Text("فتح أداة الاختبار") }
        },
    )
}
