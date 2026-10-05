import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from "react";
import { podcastWithEpisodes } from "../lib/api";
import { useRefreshTick } from "../lib/refresh";
import { load, usePersistent } from "../lib/storage";
import type { Episode } from "../lib/types";
import { EpisodeChecker, isNative } from "../native";
import { useLibrary } from "./library";
import { usePlayer } from "./player";

/**
 * Follows the user's favourite podcasts: finds episodes published since the last
 * check, keeps them in a "new" inbox, optionally downloads them, and hands the
 * list to the Android background worker so it can notify while the app is closed.
 */

interface SubscriptionsValue {
  inbox: Episode[];
  autoDownload: boolean;
  setAutoDownload: (v: boolean) => void;
  checking: boolean;
  lastCheck: number;
  checkNow: () => void;
  dismiss: (id: string) => void;
  clearInbox: () => void;
}

const Ctx = createContext<SubscriptionsValue | null>(null);

export function useSubscriptions() {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error("useSubscriptions outside SubscriptionsProvider");
  return ctx;
}

const INBOX_LIMIT = 100;
const MIN_GAP_MS = 5 * 60_000;
const CONCURRENCY = 3;

export function SubscriptionsProvider({ children }: { children: ReactNode }) {
  const { favPodcasts, country, download, downloads } = useLibrary();
  const { current } = usePlayer();
  const tick = useRefreshTick();

  const [latest, setLatest] = usePersistent<Record<string, string>>("sada.subs.latest", {});
  const [inbox, setInbox] = usePersistent<Episode[]>("sada.inbox", []);
  const [autoDownload, setAutoDownload] = usePersistent("sada.autoDownload", false);
  const [lastCheck, setLastCheck] = usePersistent("sada.subs.lastCheck", 0);
  const [checking, setChecking] = useState(false);

  const ref = useRef({ favPodcasts, country, latest, autoDownload, download, downloads });
  ref.current = { favPodcasts, country, latest, autoDownload, download, downloads };
  const running = useRef(false);

  const check = useCallback(
    async (force: boolean) => {
      const { favPodcasts: pods, country: cc } = ref.current;
      if (running.current || !pods.length || navigator.onLine === false) return;
      if (!force && Date.now() - load("sada.subs.lastCheck", 0) < MIN_GAP_MS) return;
      running.current = true;
      setChecking(true);
      const found: Episode[] = [];
      const newest: Record<string, string> = {};
      try {
        const queue = [...pods];
        const worker = async () => {
          for (let p = queue.shift(); p; p = queue.shift()) {
            try {
              const { episodes } = await podcastWithEpisodes(p.id, cc, 15).run(true);
              if (!episodes.length) continue;
              const prev = ref.current.latest[p.id];
              newest[p.id] = episodes[0].releaseDate;
              // First check of a podcast only records a baseline.
              if (prev) found.push(...episodes.filter((e) => e.releaseDate > prev));
            } catch {
              /* try again next time */
            }
          }
        };
        await Promise.all(Array.from({ length: CONCURRENCY }, worker));

        setLatest((l) => {
          const next = { ...l };
          for (const [id, d] of Object.entries(newest)) if (!next[id] || d > next[id]) next[id] = d;
          return next;
        });
        if (found.length) {
          found.sort((a, b) => b.releaseDate.localeCompare(a.releaseDate));
          setInbox((list) => {
            const ids = new Set(list.map((e) => e.id));
            return [...found.filter((e) => !ids.has(e.id)), ...list].slice(0, INBOX_LIMIT);
          });
          if (ref.current.autoDownload) {
            found.filter((e) => !ref.current.downloads[e.id]).forEach((e) => ref.current.download(e));
          }
        }
        setLastCheck(Date.now());
      } finally {
        running.current = false;
        setChecking(false);
      }
    },
    [setInbox, setLastCheck, setLatest]
  );

  // Check on start, on every app-wide refresh, and when favourites change.
  const favKey = favPodcasts.map((p) => p.id).join(",");
  useEffect(() => {
    check(false);
  }, [tick, check]);
  useEffect(() => {
    check(true);
  }, [favKey, country]); // eslint-disable-line react-hooks/exhaustive-deps

  // Unfollowed podcasts drop out of the inbox and the baseline.
  useEffect(() => {
    const ids = new Set(favPodcasts.map((p) => p.id));
    setInbox((list) => (list.every((e) => ids.has(e.podcastId)) ? list : list.filter((e) => ids.has(e.podcastId))));
    setLatest((l) => (Object.keys(l).every((id) => ids.has(id)) ? l : Object.fromEntries(Object.entries(l).filter(([id]) => ids.has(id)))));
  }, [favKey]); // eslint-disable-line react-hooks/exhaustive-deps

  // Playing an episode takes it out of "new".
  useEffect(() => {
    if (current) setInbox((list) => (list.some((e) => e.id === current.id) ? list.filter((e) => e.id !== current.id) : list));
  }, [current, setInbox]);

  // Keep the Android background checker in sync.
  useEffect(() => {
    if (!isNative) return;
    EpisodeChecker.sync({
      country,
      podcasts: favPodcasts.map((p) => ({ id: p.id, title: p.title, latest: latest[p.id] })),
    }).catch(() => {});
  }, [favPodcasts, latest, country]);

  // Ask for notification permission the first time the user follows a podcast.
  const asked = useRef(load("sada.notifAsked", false));
  useEffect(() => {
    if (!isNative || !favPodcasts.length || asked.current) return;
    asked.current = true;
    localStorage.setItem("sada.notifAsked", "true");
    EpisodeChecker.requestPermission().catch(() => {});
  }, [favPodcasts.length]);

  const value = useMemo<SubscriptionsValue>(
    () => ({
      inbox,
      autoDownload,
      setAutoDownload,
      checking,
      lastCheck,
      checkNow: () => check(true),
      dismiss: (id) => setInbox((l) => l.filter((e) => e.id !== id)),
      clearInbox: () => setInbox([]),
    }),
    [inbox, autoDownload, setAutoDownload, checking, lastCheck, check, setInbox]
  );

  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}
