import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from "react";
import { useLocation } from "react-router-dom";
import { load, save } from "../lib/storage";
import { Ads, isNative } from "../native";
import { usePlayer } from "./player";

/**
 * Ad placement rules (the native plugin only loads/shows):
 *  - Banner at the bottom, hidden on the Quran page, settings and in the full player.
 *  - Interstitials only between screens, never while something is playing or around the
 *    Quran, at most once every 4 minutes and not in the first 2 minutes of a session.
 *  - Rewarded: watch one ad for an hour without ads.
 */

const INTERSTITIAL_GAP_MS = 4 * 60_000;
const SESSION_GRACE_MS = 2 * 60_000;
const AD_FREE_MS = 60 * 60_000;
const NO_BANNER = [/^\/quran/, /^\/settings/, /^\/privacy/];
const INTERSTITIAL_ROUTES = [/^\/podcast\//, /^\/genre\//, /^\/top$/];

interface AdsValue {
  available: boolean;
  adFreeUntil: number;
  rewardedReady: boolean;
  watchForAdFree: () => Promise<boolean>;
}

const Ctx = createContext<AdsValue>({ available: false, adFreeUntil: 0, rewardedReady: false, watchForAdFree: async () => false });
export const useAds = () => useContext(Ctx);

const sessionStart = Date.now();

export function AdsProvider({ children }: { children: ReactNode }) {
  const { pathname } = useLocation();
  const { isPlaying, expanded, toggle } = usePlayer();
  const [ready, setReady] = useState(false);
  const [rewardedReady, setRewardedReady] = useState(false);
  const [adFreeUntil, setAdFreeUntil] = useState(() => load("sada.adFreeUntil", 0));
  const [now, setNow] = useState(Date.now());
  const lastInterstitial = useRef(0);
  const showingFullscreen = useRef(false);

  const adFree = adFreeUntil > now;

  // Re-evaluate when the ad-free hour runs out.
  useEffect(() => {
    if (!adFree) return;
    const t = setTimeout(() => setNow(Date.now()), adFreeUntil - Date.now() + 500);
    return () => clearTimeout(t);
  }, [adFree, adFreeUntil]);

  useEffect(() => {
    if (!isNative) return;
    const subs = [
      Ads.addListener("status", (e) => {
        if (e.ready !== undefined) setReady(e.ready);
        if (e.rewardedReady !== undefined) setRewardedReady(e.rewardedReady);
      }),
      Ads.addListener("banner", (e) => {
        document.documentElement.style.setProperty("--ad-h", `${e.height}px`);
        document.documentElement.classList.toggle("has-banner", e.height > 0);
      }),
    ];
    Ads.isRewardedReady().then((r) => r.ready && (setReady(true), setRewardedReady(true))).catch(() => {});
    return () => subs.forEach((s) => s.then((h) => h.remove()).catch(() => {}));
  }, []);

  // Banner visibility follows the screen.
  const bannerVisible = isNative && !adFree && !expanded && !NO_BANNER.some((r) => r.test(pathname));
  useEffect(() => {
    if (!isNative) return;
    (bannerVisible ? Ads.showBanner() : Ads.hideBanner()).catch(() => {});
  }, [bannerVisible]);

  // Interstitial between screens.
  const prevPath = useRef(pathname);
  useEffect(() => {
    const from = prevPath.current;
    prevPath.current = pathname;
    if (!isNative || !ready || adFree || isPlaying || from === pathname) return;
    if (!INTERSTITIAL_ROUTES.some((r) => r.test(pathname)) || from.startsWith("/quran")) return;
    const t = Date.now();
    if (t - sessionStart < SESSION_GRACE_MS || t - lastInterstitial.current < INTERSTITIAL_GAP_MS || showingFullscreen.current) return;
    showingFullscreen.current = true;
    Ads.showInterstitial()
      .then((r) => {
        if (r.shown) lastInterstitial.current = Date.now();
      })
      .catch(() => {})
      .finally(() => (showingFullscreen.current = false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pathname]);

  const isPlayingRef = useRef(isPlaying);
  isPlayingRef.current = isPlaying;

  const watchForAdFree = useCallback(async () => {
    if (!isNative || showingFullscreen.current) return false;
    const wasPlaying = isPlayingRef.current;
    if (wasPlaying) toggle(); // the ad has sound: pause the episode meanwhile
    showingFullscreen.current = true;
    try {
      const { rewarded } = await Ads.showRewarded();
      if (rewarded) {
        const until = Math.max(Date.now(), load("sada.adFreeUntil", 0)) + AD_FREE_MS;
        save("sada.adFreeUntil", until);
        setAdFreeUntil(until);
        setNow(Date.now());
      }
      return rewarded;
    } catch {
      return false;
    } finally {
      showingFullscreen.current = false;
      if (wasPlaying) toggle();
    }
  }, [toggle]);

  const value = useMemo(
    () => ({ available: isNative && ready, adFreeUntil: adFree ? adFreeUntil : 0, rewardedReady, watchForAdFree }),
    [ready, adFree, adFreeUntil, rewardedReady, watchForAdFree]
  );
  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}
