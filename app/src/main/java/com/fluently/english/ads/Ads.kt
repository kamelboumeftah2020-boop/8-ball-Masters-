package com.fluently.english.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.unity3d.ads.metadata.MetaData
import com.unity3d.mediation.LevelPlay
import com.unity3d.mediation.LevelPlayAdError
import com.unity3d.mediation.LevelPlayAdInfo
import com.unity3d.mediation.LevelPlayConfiguration
import com.unity3d.mediation.LevelPlayInitError
import com.unity3d.mediation.LevelPlayInitListener
import com.unity3d.mediation.LevelPlayInitRequest
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAd
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAdListener
import com.unity3d.mediation.rewarded.LevelPlayReward
import com.unity3d.mediation.rewarded.LevelPlayRewardedAd
import com.unity3d.mediation.rewarded.LevelPlayRewardedAdListener

/**
 * Ads through Unity LevelPlay (mediation; Unity Ads plus any networks enabled in
 * the LevelPlay dashboard), used gently: an optional rewarded ad (double XP,
 * restore a streak), at most one interstitial every few finished activities
 * (never during a lesson, test or speaking), a banner and a native card on list
 * screens only.
 */
object Ads {
    const val APP_KEY = "2887a0485"
    const val BANNER_ID = "67exfd6q45taf7bv"
    const val INTERSTITIAL_ID = "xq7jit8fgvn1zfev"
    const val REWARDED_ID = "972gjvqimt42e37i"
    /** Native ad unit. LevelPlay 9.6's native API still loads by placement (the default one). */
    const val NATIVE_ID = "8xja9skp813jdqco"

    /** Show an interstitial at most after every N finished activities… */
    private const val EVERY = 3
    /** …and never twice within this time. */
    private const val MIN_GAP_MS = 3 * 60_000L

    var initialized by mutableStateOf(false)
        private set
    /** A rewarded ad is loaded and can be offered. */
    var rewardedReady by mutableStateOf(false)
        private set

    private var interstitial: LevelPlayInterstitialAd? = null
    private var rewarded: LevelPlayRewardedAd? = null
    private var pendingReward: (() -> Unit)? = null
    private var finishedSinceAd = 0
    private var lastAdAt = 0L

    private const val PREFS = "ads"
    private const val KEY_CONSENT = "personalized" // "yes" / "no"; absent = not asked yet

    fun consentAsked(context: Context) = prefs(context).contains(KEY_CONSENT)
    fun personalized(context: Context) = prefs(context).getString(KEY_CONSENT, "no") == "yes"

    /** Stores the learner's choice and passes it to LevelPlay and Unity (GDPR / CCPA). */
    fun setPersonalized(context: Context, yes: Boolean) {
        prefs(context).edit().putString(KEY_CONSENT, if (yes) "yes" else "no").apply()
        applyConsent(context.applicationContext)
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun applyConsent(context: Context) {
        val yes = personalized(context)
        runCatching { LevelPlay.setConsent(yes) }
        runCatching { LevelPlay.setMetaData("do_not_sell", if (yes) "false" else "true") }
        runCatching { LevelPlay.setMetaData("is_child_directed", "false") }
        runCatching { MetaData(context).apply { set("gdpr.consent", yes); commit() } }
        runCatching { MetaData(context).apply { set("privacy.consent", yes); commit() } }
    }

    fun init(context: Context) {
        if (initialized) return
        val app = context.applicationContext
        applyConsent(app) // consent must be set before initialising
        LevelPlay.init(app, LevelPlayInitRequest.Builder(APP_KEY).build(), object : LevelPlayInitListener {
            override fun onInitSuccess(configuration: LevelPlayConfiguration) {
                initialized = true
                createInterstitial()
                createRewarded()
            }
            override fun onInitFailed(error: LevelPlayInitError) {}
        })
    }

    private fun createInterstitial() {
        interstitial = LevelPlayInterstitialAd(INTERSTITIAL_ID).apply {
            setListener(object : LevelPlayInterstitialAdListener {
                override fun onAdLoaded(adInfo: LevelPlayAdInfo) {}
                override fun onAdLoadFailed(error: LevelPlayAdError) {}
                override fun onAdDisplayed(adInfo: LevelPlayAdInfo) {}
                override fun onAdDisplayFailed(error: LevelPlayAdError, adInfo: LevelPlayAdInfo) { loadAd() }
                override fun onAdClosed(adInfo: LevelPlayAdInfo) { loadAd() }
            })
            loadAd()
        }
    }

    private fun createRewarded() {
        rewarded = LevelPlayRewardedAd(REWARDED_ID).apply {
            setListener(object : LevelPlayRewardedAdListener {
                override fun onAdLoaded(adInfo: LevelPlayAdInfo) { rewardedReady = true }
                override fun onAdLoadFailed(error: LevelPlayAdError) { rewardedReady = false }
                override fun onAdDisplayed(adInfo: LevelPlayAdInfo) {}
                override fun onAdRewarded(reward: LevelPlayReward, adInfo: LevelPlayAdInfo) {
                    pendingReward?.invoke()
                    pendingReward = null
                }
                override fun onAdDisplayFailed(error: LevelPlayAdError, adInfo: LevelPlayAdInfo) {
                    pendingReward = null
                    rewardedReady = false
                    loadAd()
                }
                override fun onAdClosed(adInfo: LevelPlayAdInfo) {
                    pendingReward = null
                    lastAdAt = System.currentTimeMillis() // no interstitial right after
                    rewardedReady = false
                    loadAd()
                }
            })
            loadAd()
        }
    }

    /**
     * Call when the learner leaves a finished lesson, story, conversation or game.
     * Shows an interstitial only every few times and never too often.
     */
    fun activityFinished(context: Context) {
        finishedSinceAd++
        val ad = interstitial ?: return
        val now = System.currentTimeMillis()
        if (finishedSinceAd < EVERY || now - lastAdAt < MIN_GAP_MS || !ad.isAdReady()) return
        val activity = context.findActivity() ?: return
        finishedSinceAd = 0
        lastAdAt = now
        ad.showAd(activity)
    }

    /** Plays a rewarded ad; [onReward] runs only when LevelPlay grants the reward. */
    fun showRewarded(context: Context, onReward: () -> Unit, onFailed: () -> Unit = {}) {
        val ad = rewarded
        val activity = context.findActivity()
        if (ad == null || activity == null || !ad.isAdReady()) { onFailed(); return }
        pendingReward = onReward
        rewardedReady = false
        ad.showAd(activity)
    }
}

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
