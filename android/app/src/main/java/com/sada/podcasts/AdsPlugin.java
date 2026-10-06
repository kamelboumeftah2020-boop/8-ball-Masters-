package com.sada.podcasts;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.unity3d.mediation.LevelPlay;
import com.unity3d.mediation.LevelPlayAdError;
import com.unity3d.mediation.LevelPlayAdInfo;
import com.unity3d.mediation.LevelPlayAdSize;
import com.unity3d.mediation.LevelPlayConfiguration;
import com.unity3d.mediation.LevelPlayInitError;
import com.unity3d.mediation.LevelPlayInitListener;
import com.unity3d.mediation.LevelPlayInitRequest;
import com.unity3d.mediation.banner.LevelPlayBannerAdView;
import com.unity3d.mediation.banner.LevelPlayBannerAdViewListener;
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAd;
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAdListener;
import com.unity3d.mediation.rewarded.LevelPlayReward;
import com.unity3d.mediation.rewarded.LevelPlayRewardedAd;
import com.unity3d.mediation.rewarded.LevelPlayRewardedAdListener;

/**
 * Unity LevelPlay ads: a bottom banner (native view over the WebView), interstitials and
 * rewarded ads. When/where ads appear is decided by the web app (src/lib/ads.ts); this
 * plugin only loads, shows and reports.
 *
 * Unity Ads itself (game ID and its placements) is configured as a network inside the
 * LevelPlay dashboard, so only the LevelPlay app key and ad unit IDs live in code.
 */
@CapacitorPlugin(name = "Ads")
public class AdsPlugin extends Plugin {

    private static final String APP_KEY = "2887d637d";
    private static final String BANNER_UNIT = "m9owyp0nqh110nkg";
    private static final String INTERSTITIAL_UNIT = "h0cjiggk5eskk541";
    private static final String REWARDED_UNIT = "26j8hnu9cfoe9jj2";

    private final Handler main = new Handler(Looper.getMainLooper());
    private boolean ready;
    private LevelPlayInterstitialAd interstitial;
    private LevelPlayRewardedAd rewarded;
    private LevelPlayBannerAdView banner;
    private FrameLayout bannerHost;
    private boolean bannerWanted;
    private PluginCall interstitialCall;
    private PluginCall rewardedCall;
    private boolean rewardEarned;
    private long interstitialRetryMs = 15_000;
    private long rewardedRetryMs = 15_000;

    @Override
    public void load() {
        // Not a children's app; tell the networks so they don't apply child-directed rules.
        LevelPlay.setMetaData("is_child_directed", "false");
        LevelPlayInitRequest request = new LevelPlayInitRequest.Builder(APP_KEY).build();
        LevelPlay.init(getContext().getApplicationContext(), request, new LevelPlayInitListener() {
            @Override
            public void onInitSuccess(LevelPlayConfiguration configuration) {
                main.post(() -> {
                    ready = true;
                    createFullscreenAds();
                    if (bannerWanted) attachBanner();
                    emit("status", "ready", true);
                });
            }

            @Override
            public void onInitFailed(LevelPlayInitError error) {
                emit("status", "ready", false);
            }
        });
    }

    /* ---------- Interstitial & rewarded ---------- */

    private void createFullscreenAds() {
        interstitial = new LevelPlayInterstitialAd(INTERSTITIAL_UNIT);
        interstitial.setListener(new LevelPlayInterstitialAdListener() {
            @Override public void onAdLoaded(LevelPlayAdInfo info) { interstitialRetryMs = 15_000; }
            @Override public void onAdLoadFailed(LevelPlayAdError error) { retry(() -> interstitial.loadAd(), true); }
            @Override public void onAdDisplayed(LevelPlayAdInfo info) {}
            @Override public void onAdDisplayFailed(LevelPlayAdError error, LevelPlayAdInfo info) {
                finishInterstitial(false);
                interstitial.loadAd();
            }
            @Override public void onAdClosed(LevelPlayAdInfo info) {
                finishInterstitial(true);
                interstitial.loadAd();
            }
        });
        interstitial.loadAd();

        rewarded = new LevelPlayRewardedAd(REWARDED_UNIT);
        rewarded.setListener(new LevelPlayRewardedAdListener() {
            @Override public void onAdLoaded(LevelPlayAdInfo info) {
                rewardedRetryMs = 15_000;
                emit("status", "rewardedReady", true);
            }
            @Override public void onAdLoadFailed(LevelPlayAdError error) { retry(() -> rewarded.loadAd(), false); }
            @Override public void onAdDisplayed(LevelPlayAdInfo info) { emit("status", "rewardedReady", false); }
            @Override public void onAdRewarded(LevelPlayReward reward, LevelPlayAdInfo info) { rewardEarned = true; }
            @Override public void onAdDisplayFailed(LevelPlayAdError error, LevelPlayAdInfo info) {
                finishRewarded();
                rewarded.loadAd();
            }
            @Override public void onAdClosed(LevelPlayAdInfo info) {
                // Some networks report the reward just after closing; give it a moment.
                main.postDelayed(AdsPlugin.this::finishRewarded, 700);
                rewarded.loadAd();
            }
        });
        rewarded.loadAd();
    }

    private void retry(Runnable load, boolean isInterstitial) {
        long delay = isInterstitial ? interstitialRetryMs : rewardedRetryMs;
        if (isInterstitial) interstitialRetryMs = Math.min(interstitialRetryMs * 2, 5 * 60_000);
        else rewardedRetryMs = Math.min(rewardedRetryMs * 2, 5 * 60_000);
        main.postDelayed(load, delay);
    }

    private void finishInterstitial(boolean shown) {
        if (interstitialCall == null) return;
        JSObject res = new JSObject();
        res.put("shown", shown);
        interstitialCall.resolve(res);
        interstitialCall = null;
    }

    private void finishRewarded() {
        if (rewardedCall == null) return;
        JSObject res = new JSObject();
        res.put("rewarded", rewardEarned);
        rewardedCall.resolve(res);
        rewardedCall = null;
        rewardEarned = false;
    }

    @PluginMethod
    public void showInterstitial(PluginCall call) {
        main.post(() -> {
            Activity activity = getActivity();
            if (!ready || interstitial == null || !interstitial.isAdReady() || activity == null || interstitialCall != null) {
                JSObject res = new JSObject();
                res.put("shown", false);
                call.resolve(res);
                return;
            }
            interstitialCall = call;
            interstitial.showAd(activity);
        });
    }

    @PluginMethod
    public void isRewardedReady(PluginCall call) {
        main.post(() -> {
            JSObject res = new JSObject();
            res.put("ready", ready && rewarded != null && rewarded.isAdReady());
            call.resolve(res);
        });
    }

    @PluginMethod
    public void showRewarded(PluginCall call) {
        main.post(() -> {
            Activity activity = getActivity();
            if (!ready || rewarded == null || !rewarded.isAdReady() || activity == null || rewardedCall != null) {
                JSObject res = new JSObject();
                res.put("rewarded", false);
                call.resolve(res);
                return;
            }
            rewardEarned = false;
            rewardedCall = call;
            rewarded.showAd(activity);
        });
    }

    /* ---------- Banner (native view pinned above the system navigation bar) ---------- */

    @PluginMethod
    public void showBanner(PluginCall call) {
        main.post(() -> {
            bannerWanted = true;
            if (ready) attachBanner();
            call.resolve();
        });
    }

    @PluginMethod
    public void hideBanner(PluginCall call) {
        main.post(() -> {
            bannerWanted = false;
            if (bannerHost != null) bannerHost.setVisibility(View.GONE);
            if (banner != null) banner.pauseAutoRefresh();
            reportBannerHeight(0);
            call.resolve();
        });
    }

    private void attachBanner() {
        Activity activity = getActivity();
        if (activity == null) return;
        if (bannerHost == null) {
            ViewGroup content = activity.findViewById(android.R.id.content);
            bannerHost = new FrameLayout(activity);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            content.addView(bannerHost, lp);
            ViewCompat.setOnApplyWindowInsetsListener(bannerHost, (v, insets) -> {
                int nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) v.getLayoutParams();
                if (p.bottomMargin != nav) {
                    p.bottomMargin = nav;
                    v.setLayoutParams(p);
                }
                return insets;
            });
            ViewCompat.requestApplyInsets(bannerHost);

            LevelPlayAdSize size = LevelPlayAdSize.createAdaptiveAdSize(activity);
            LevelPlayBannerAdView.Config config = new LevelPlayBannerAdView.Config.Builder()
                .setAdSize(size != null ? size : LevelPlayAdSize.BANNER)
                .build();
            banner = new LevelPlayBannerAdView(activity, BANNER_UNIT, config);
            banner.setBannerListener(new LevelPlayBannerAdViewListener() {
                @Override public void onAdLoaded(LevelPlayAdInfo info) {
                    main.post(() -> {
                        if (bannerWanted && bannerHost != null) {
                            bannerHost.setVisibility(View.VISIBLE);
                            bannerHost.post(() -> reportBannerHeight(bannerHost.getHeight()));
                        }
                    });
                }
                @Override public void onAdLoadFailed(LevelPlayAdError error) {
                    main.post(() -> {
                        if (bannerHost != null) bannerHost.setVisibility(View.GONE);
                        reportBannerHeight(0);
                    });
                }
            });
            bannerHost.addView(banner, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));
            bannerHost.setVisibility(View.GONE); // shown once an ad has loaded
            banner.loadAd();
            return;
        }
        banner.resumeAutoRefresh();
        if (banner.getAdId() != null && !banner.getAdId().isEmpty()) {
            bannerHost.setVisibility(View.VISIBLE);
            bannerHost.post(() -> reportBannerHeight(bannerHost.getHeight()));
        }
    }

    /** Tell the page how much room (in CSS px) the banner takes, so it can lift its bottom bar. */
    private void reportBannerHeight(int px) {
        float density = getContext().getResources().getDisplayMetrics().density;
        JSObject data = new JSObject();
        data.put("height", Math.round(px / density));
        notifyListeners("banner", data, true);
    }

    private void emit(String event, String key, boolean value) {
        JSObject data = new JSObject();
        data.put(key, value);
        notifyListeners(event, data, true);
    }

    @Override
    protected void handleOnDestroy() {
        if (banner != null) banner.destroy();
        banner = null;
        bannerHost = null;
    }
}
