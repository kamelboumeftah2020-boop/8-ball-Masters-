package com.fluently.english.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

/** Adds bonus XP to the learner's progress (provided by the app; no-op in previews/tests). */
val LocalAddBonusXp = androidx.compose.runtime.staticCompositionLocalOf<(Int) -> Unit> { {} }

/** "Double your XP" offer for result screens; nothing when [xp] is 0 or no ad is ready. */
@Composable
fun DoubleXpOffer(xp: Int, modifier: Modifier = Modifier) {
    if (xp <= 0) return
    val add = LocalAddBonusXp.current
    RewardedOffer("ضاعف نقاطك: +$xp نقطة إضافية", onReward = { add(xp) }, modifier = modifier)
}

/** A 320×50 banner; takes no space until an ad has loaded. */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    if (!Ads.initialized) return
    val context = LocalContext.current
    val activity = context.findActivity() ?: return
    var loaded by remember { mutableStateOf(false) }
    val banner = remember {
        BannerView(activity, Ads.BANNER, UnityBannerSize(320, 50)).apply {
            setListener(object : BannerView.IListener {
                override fun onBannerLoaded(bannerAdView: BannerView?) { loaded = true }
                override fun onBannerShown(bannerAdView: BannerView?) {}
                override fun onBannerClick(bannerAdView: BannerView?) {}
                override fun onBannerFailedToLoad(bannerAdView: BannerView?, errorInfo: BannerErrorInfo?) { loaded = false }
                override fun onBannerLeftApplication(bannerView: BannerView?) {}
            })
            load()
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

/** Asked once: personalised ads or general ones (GDPR / privacy consent for Unity). */
@Composable
fun AdConsentDialog(onDone: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = {},
        title = { Text("الإعلانات في طلاقة") },
        text = {
            Text(
                "طلاقة مجاني بالكامل، ونعرض إعلانات قليلة لا تظهر أبداً أثناء الدروس أو الاختبارات.\n\n" +
                    "هل تسمح بإعلانات مخصصة حسب اهتماماتك؟ (تستخدم شركة Unity معرّف الإعلانات في هاتفك). " +
                    "يمكنك تغيير اختيارك في أي وقت من «حسابي».",
            )
        },
        confirmButton = {
            TextButton(onClick = { Ads.setPersonalized(context, true); onDone() }) { Text("نعم، أسمح") }
        },
        dismissButton = {
            TextButton(onClick = { Ads.setPersonalized(context, false); onDone() }) { Text("لا، إعلانات عامة") }
        },
    )
}
