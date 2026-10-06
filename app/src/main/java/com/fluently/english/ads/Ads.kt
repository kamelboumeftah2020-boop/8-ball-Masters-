package com.fluently.english.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.fluently.english.BuildConfig
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import com.unity3d.ads.metadata.MetaData

/**
 * Unity Ads, used gently: an optional rewarded ad (double XP, restore a streak),
 * at most one interstitial every few finished activities (never during a lesson,
 * test or speaking), and a banner on the list screens only.
 */
object Ads {
    const val GAME_ID = "6200501"
    const val INTERSTITIAL = "Interstitial_Android"
    const val REWARDED = "Rewarded_Android"
    const val BANNER = "Banner_Android"

    /** Show an interstitial at most after every N finished activities… */
    private const val EVERY = 3
    /** …and never twice within this time. */
    private const val MIN_GAP_MS = 3 * 60_000L

    var initialized by mutableStateOf(false)
        private set
    /** A rewarded ad is loaded and can be offered. */
    var rewardedReady by mutableStateOf(false)
        private set
    private var interstitialReady = false
    private var finishedSinceAd = 0
    private var lastAdAt = 0L

    private const val PREFS = "ads"
    private const val KEY_CONSENT = "personalized" // "yes" / "no"; absent = not asked yet

    fun consentAsked(context: Context) = prefs(context).contains(KEY_CONSENT)
    fun personalized(context: Context) = prefs(context).getString(KEY_CONSENT, "no") == "yes"

    /** Stores the learner's choice and passes it to Unity (GDPR / CCPA consent flags). */
    fun setPersonalized(context: Context, yes: Boolean) {
        prefs(context).edit().putString(KEY_CONSENT, if (yes) "yes" else "no").apply()
        applyConsent(context.applicationContext)
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun applyConsent(context: Context) {
        val yes = personalized(context)
        runCatching { MetaData(context).apply { set("gdpr.consent", yes); commit() } }
        runCatching { MetaData(context).apply { set("privacy.consent", yes); commit() } }
    }

    fun init(context: Context) {
        if (initialized || !UnityAds.isSupported) return
        val app = context.applicationContext
        applyConsent(app)
        // Debug builds get Unity's test ads; release builds get real ones.
        UnityAds.initialize(app, GAME_ID, BuildConfig.DEBUG, object : IUnityAdsInitializationListener {
            override fun onInitializationComplete() {
                initialized = true
                loadInterstitial()
                loadRewarded()
            }
            override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {}
        })
    }

    private fun loadInterstitial() = UnityAds.load(INTERSTITIAL, object : IUnityAdsLoadListener {
        override fun onUnityAdsAdLoaded(placementId: String?) { interstitialReady = true }
        override fun onUnityAdsFailedToLoad(placementId: String?, error: UnityAds.UnityAdsLoadError?, message: String?) {
            interstitialReady = false
        }
    })

    private fun loadRewarded() = UnityAds.load(REWARDED, object : IUnityAdsLoadListener {
        override fun onUnityAdsAdLoaded(placementId: String?) { rewardedReady = true }
        override fun onUnityAdsFailedToLoad(placementId: String?, error: UnityAds.UnityAdsLoadError?, message: String?) {
            rewardedReady = false
        }
    })

    /**
     * Call when the learner leaves a finished lesson, story, conversation or game.
     * Shows an interstitial only every few times and never too often.
     */
    fun activityFinished(context: Context) {
        finishedSinceAd++
        val now = System.currentTimeMillis()
        if (finishedSinceAd < EVERY || now - lastAdAt < MIN_GAP_MS || !interstitialReady) return
        val activity = context.findActivity() ?: return
        interstitialReady = false
        finishedSinceAd = 0
        lastAdAt = now
        UnityAds.show(activity, INTERSTITIAL, UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String?, error: UnityAds.UnityAdsShowError?, message: String?) = loadInterstitial()
            override fun onUnityAdsShowStart(placementId: String?) {}
            override fun onUnityAdsShowClick(placementId: String?) {}
            override fun onUnityAdsShowComplete(placementId: String?, state: UnityAds.UnityAdsShowCompletionState?) = loadInterstitial()
        })
    }

    /** Plays a rewarded ad; [onReward] runs only if it was watched to the end. */
    fun showRewarded(context: Context, onReward: () -> Unit, onFailed: () -> Unit = {}) {
        val activity = context.findActivity()
        if (activity == null || !rewardedReady) { onFailed(); return }
        rewardedReady = false
        UnityAds.show(activity, REWARDED, UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String?, error: UnityAds.UnityAdsShowError?, message: String?) {
                loadRewarded()
                onFailed()
            }
            override fun onUnityAdsShowStart(placementId: String?) {}
            override fun onUnityAdsShowClick(placementId: String?) {}
            override fun onUnityAdsShowComplete(placementId: String?, state: UnityAds.UnityAdsShowCompletionState?) {
                if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) onReward()
                lastAdAt = System.currentTimeMillis() // no interstitial right after
                loadRewarded()
            }
        })
    }
}

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
