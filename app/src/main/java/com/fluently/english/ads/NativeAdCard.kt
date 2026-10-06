package com.fluently.english.ads

import android.graphics.Color as AColor
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.ironsource.mediationsdk.ads.nativead.LevelPlayMediaView
import com.ironsource.mediationsdk.ads.nativead.LevelPlayNativeAd
import com.ironsource.mediationsdk.ads.nativead.LevelPlayNativeAdListener
import com.ironsource.mediationsdk.ads.nativead.NativeAdLayout
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo
import com.ironsource.mediationsdk.logger.IronSourceError

/**
 * A native ad styled like the app's cards (icon, title, text, media and a
 * button), clearly labelled "إعلان". Shows nothing until an ad has loaded.
 */
@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    if (!Ads.initialized) return
    val activity = LocalContext.current.findActivity() ?: return
    var loaded by remember { mutableStateOf<LevelPlayNativeAd?>(null) }
    val ad = remember {
        LevelPlayNativeAd.Builder()
            .withActivity(activity)
            .withListener(object : LevelPlayNativeAdListener {
                override fun onAdLoaded(nativeAd: LevelPlayNativeAd?, adInfo: AdInfo?) { loaded = nativeAd }
                override fun onAdLoadFailed(nativeAd: LevelPlayNativeAd?, error: IronSourceError?) { loaded = null }
                override fun onAdClicked(nativeAd: LevelPlayNativeAd?, adInfo: AdInfo?) {}
                override fun onAdImpression(nativeAd: LevelPlayNativeAd?, adInfo: AdInfo?) {}
            })
            .build()
            .also { it.loadAd() }
    }
    DisposableEffect(ad) { onDispose { runCatching { ad.destroyAd() } } }
    val nativeAd = loaded ?: return
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx -> buildLayout(ctx, nativeAd) },
    )
}

private fun buildLayout(ctx: android.content.Context, ad: LevelPlayNativeAd): NativeAdLayout {
    val d = ctx.resources.displayMetrics.density
    fun px(v: Int) = (v * d).toInt()
    val ink = AColor.parseColor("#15171C")
    val muted = AColor.parseColor("#6B7280")
    val emerald = AColor.parseColor("#0E8A6A")

    val layout = NativeAdLayout(ctx)
    val column = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(px(14), px(12), px(14), px(14))
        background = GradientDrawable().apply {
            cornerRadius = px(18).toFloat()
            setColor(AColor.WHITE)
            setStroke(px(1), AColor.parseColor("#E6E1D8"))
        }
    }
    val header = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    val icon = ImageView(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(px(40), px(40))
        ad.icon?.drawable?.let { setImageDrawable(it) }
    }
    val texts = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = px(10) }
    }
    val title = TextView(ctx).apply { text = ad.title; setTextColor(ink); textSize = 15f; setTypeface(typeface, Typeface.BOLD); maxLines = 1 }
    val advertiser = TextView(ctx).apply { text = ad.advertiser ?: ""; setTextColor(muted); textSize = 12f; maxLines = 1 }
    val badge = TextView(ctx).apply {
        text = "إعلان"
        textSize = 11f
        setTextColor(AColor.parseColor("#9A6B00"))
        setPadding(px(8), px(2), px(8), px(2))
        background = GradientDrawable().apply { cornerRadius = px(10).toFloat(); setColor(AColor.parseColor("#FDF1D6")) }
    }
    texts.addView(title); texts.addView(advertiser)
    header.addView(icon); header.addView(texts); header.addView(badge)
    val body = TextView(ctx).apply {
        text = ad.body ?: ""
        setTextColor(muted); textSize = 13f; maxLines = 2
        setPadding(0, px(8), 0, px(8))
    }
    val media = LevelPlayMediaView(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, px(170))
    }
    val cta = TextView(ctx).apply {
        text = ad.callToAction ?: "المزيد"
        gravity = Gravity.CENTER
        setTextColor(AColor.WHITE); textSize = 14f; setTypeface(typeface, Typeface.BOLD)
        setPadding(0, px(10), 0, px(10))
        background = GradientDrawable().apply { cornerRadius = px(14).toFloat(); setColor(emerald) }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = px(10) }
    }
    column.addView(header); column.addView(body); column.addView(media); column.addView(cta)
    layout.addView(column)
    layout.setIconView(icon)
    layout.setTitleView(title)
    layout.setAdvertiserView(advertiser)
    layout.setBodyView(body)
    layout.setMediaView(media)
    layout.setCallToActionView(cta)
    layout.registerNativeAdViews(ad)
    return layout
}
