package com.fluently.english.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ads through Unity LevelPlay (mediation; Unity Ads plus any networks enabled in
 * the LevelPlay dashboard), used gently: an optional rewarded ad (double XP,
 * restore a streak), at most one interstitial every few finished activities
 * (never during a lesson, test or speaking), a banner and a native card on list
 * screens only. Ads are always general (non-personalised); nobody is asked.
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
    /** Latest events (init, loads, failures) for the hidden diagnostics screen. */
    val log = mutableStateListOf<String>()

    private val main = Handler(Looper.getMainLooper())
    private var interstitial: LevelPlayInterstitialAd? = null
    private var rewarded: LevelPlayRewardedAd? = null
    private var pendingReward: (() -> Unit)? = null
    private var finishedSinceAd = 0
    private var lastAdAt = 0L
    private var initStarted = false
    private var initAttempts = 0
    private var interstitialRetries = 0
    private var rewardedRetries = 0

    private const val PREFS = "ads"
    private const val KEY_TEST_SUITE = "test_suite"

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun note(text: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        main.post {
            log.add(0, "$time  $text")
            while (log.size > 40) log.removeAt(log.lastIndex)
        }
    }

    /** General ads only: no personalisation, no sale of data (GDPR / CCPA / Unity). */
    private fun applyPrivacy(context: Context) {
        runCatching { LevelPlay.setConsent(false) }
        runCatching { LevelPlay.setMetaData("do_not_sell", "true") }
        runCatching { LevelPlay.setMetaData("is_child_directed", "false") }
        runCatching { MetaData(context).apply { set("gdpr.consent", false); commit() } }
        runCatching { MetaData(context).apply { set("privacy.consent", false); commit() } }
    }

    fun testSuiteEnabled(context: Context) = prefs(context).getBoolean(KEY_TEST_SUITE, false)

    /** The LevelPlay test suite must be enabled before init, so it takes effect on the next launch. */
    fun setTestSuiteEnabled(context: Context, on: Boolean) {
        prefs(context).edit().putBoolean(KEY_TEST_SUITE, on).apply()
    }

    fun launchTestSuite(context: Context) {
        runCatching { LevelPlay.launchTestSuite(context.findActivity() ?: context) }
            .onFailure { note("تعذر فتح أداة الاختبار: ${it.message}") }
    }

    fun init(context: Context) {
        if (initialized || initStarted) return
        initStarted = true
        val app = context.applicationContext
        applyPrivacy(app) // must be set before initialising
        if (testSuiteEnabled(app)) runCatching { LevelPlay.setMetaData("is_test_suite", "enable") }
        startInit(app)
    }

    private fun startInit(app: Context) {
        initAttempts++
        note("بدء التهيئة (محاولة $initAttempts) — SDK ${runCatching { LevelPlay.getSdkVersion() }.getOrDefault("?")}")
        LevelPlay.init(app, LevelPlayInitRequest.Builder(APP_KEY).build(), object : LevelPlayInitListener {
            override fun onInitSuccess(configuration: LevelPlayConfiguration) {
                main.post {
                    note("التهيئة نجحت")
                    initialized = true
                    createInterstitial()
                    createRewarded()
                }
            }
            override fun onInitFailed(error: LevelPlayInitError) {
                note("فشل التهيئة: ${error.errorCode} ${error.errorMessage}")
                // Usually no internet at launch: try again later.
                val delay = minOf(5, initAttempts) * 30_000L
                main.postDelayed({ startInit(app) }, delay)
            }
        })
    }

    /** Waits longer after each failed load (no fill, no internet): 30 s, 60 s … up to 5 min. */
    private fun retryDelay(attempt: Int) = minOf(10, attempt) * 30_000L

    private fun createInterstitial() {
        interstitial = LevelPlayInterstitialAd(INTERSTITIAL_ID).apply {
            setListener(object : LevelPlayInterstitialAdListener {
                override fun onAdLoaded(adInfo: LevelPlayAdInfo) {
                    interstitialRetries = 0
                    note("إعلان بيني جاهز (${adInfo.adNetwork})")
                }
                override fun onAdLoadFailed(error: LevelPlayAdError) {
                    note("إعلان بيني: ${error.errorCode} ${error.errorMessage}")
                    interstitialRetries++
                    main.postDelayed({ runCatching { loadAd() } }, retryDelay(interstitialRetries))
                }
                override fun onAdDisplayed(adInfo: LevelPlayAdInfo) {}
                override fun onAdDisplayFailed(error: LevelPlayAdError, adInfo: LevelPlayAdInfo) {
                    note("عرض البيني فشل: ${error.errorCode} ${error.errorMessage}")
                    loadAd()
                }
                override fun onAdClosed(adInfo: LevelPlayAdInfo) { loadAd() }
            })
            loadAd()
        }
    }

    private fun createRewarded() {
        rewarded = LevelPlayRewardedAd(REWARDED_ID).apply {
            setListener(object : LevelPlayRewardedAdListener {
                override fun onAdLoaded(adInfo: LevelPlayAdInfo) {
                    rewardedRetries = 0
                    rewardedReady = true
                    note("إعلان بمكافأة جاهز (${adInfo.adNetwork})")
                }
                override fun onAdLoadFailed(error: LevelPlayAdError) {
                    rewardedReady = false
                    note("إعلان بمكافأة: ${error.errorCode} ${error.errorMessage}")
                    rewardedRetries++
                    main.postDelayed({ runCatching { loadAd() } }, retryDelay(rewardedRetries))
                }
                override fun onAdDisplayed(adInfo: LevelPlayAdInfo) {}
                override fun onAdRewarded(reward: LevelPlayReward, adInfo: LevelPlayAdInfo) {
                    pendingReward?.invoke()
                    pendingReward = null
                }
                override fun onAdDisplayFailed(error: LevelPlayAdError, adInfo: LevelPlayAdInfo) {
                    note("عرض المكافأة فشل: ${error.errorCode} ${error.errorMessage}")
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
