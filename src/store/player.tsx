import {
  createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode,
} from "react";
import { load, save } from "../lib/storage";
import type { Episode } from "../lib/types";
import { useLibrary } from "./library";

export type SleepTimer = { kind: "at"; at: number } | { kind: "end" } | null;

interface PlayerValue {
  current: Episode | null;
  queue: Episode[];
  isPlaying: boolean;
  isLoading: boolean;
  isOffline: boolean;
  error: string | null;
  rate: number;
  sleep: SleepTimer;
  expanded: boolean;
  setExpanded: (v: boolean) => void;
  play: (ep: Episode, queue?: Episode[]) => void;
  toggle: () => void;
  seek: (sec: number) => void;
  skip: (delta: number) => void;
  next: () => void;
  prev: () => void;
  setRate: (r: number) => void;
  setSleep: (s: SleepTimer) => void;
  addToQueue: (ep: Episode) => void;
}

interface TimeValue {
  position: number;
  duration: number;
  buffered: number;
}

const PlayerContext = createContext<PlayerValue | null>(null);
// Kept separate so components that don't show the clock don't re-render on every tick.
const TimeContext = createContext<TimeValue>({ position: 0, duration: 0, buffered: 0 });

export function usePlayer() {
  const ctx = useContext(PlayerContext);
  if (!ctx) throw new Error("usePlayer outside PlayerProvider");
  return ctx;
}
export const usePlayerTime = () => useContext(TimeContext);

const SAVE_EVERY_MS = 5000;

export function PlayerProvider({ children }: { children: ReactNode }) {
  const lib = useLibrary();
  const libRef = useRef(lib);
  libRef.current = lib;

  const audio = useMemo(() => {
    const a = new Audio();
    a.preload = "metadata";
    return a;
  }, []);

  const [current, setCurrent] = useState<Episode | null>(() => load<Episode | null>("sada.lastEpisode", null));
  const [queue, setQueue] = useState<Episode[]>([]);
  const [isPlaying, setIsPlaying] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isOffline, setIsOffline] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [rate, setRateState] = useState(() => load("sada.rate", 1));
  const [sleep, setSleep] = useState<SleepTimer>(null);
  const [expanded, setExpanded] = useState(false);
  const [time, setTime] = useState<TimeValue>({ position: 0, duration: 0, buffered: 0 });

  const currentRef = useRef(current);
  currentRef.current = current;
  const queueRef = useRef(queue);
  queueRef.current = queue;
  const sleepRef = useRef(sleep);
  sleepRef.current = sleep;
  const objectUrl = useRef<string | null>(null);
  const lastSave = useRef(0);
  const loadedId = useRef<string | null>(null);

  const persistProgress = useCallback(() => {
    const ep = currentRef.current;
    if (ep && audio.duration > 0) libRef.current.recordProgress(ep, audio.currentTime, audio.duration);
  }, [audio]);

  /** Point the audio element at an episode (downloaded copy preferred) and restore its position. */
  const loadEpisode = useCallback(
    async (ep: Episode, autoplay: boolean) => {
      loadedId.current = ep.id;
      setError(null);
      setIsLoading(true);
      if (objectUrl.current) {
        URL.revokeObjectURL(objectUrl.current);
        objectUrl.current = null;
      }
      const local = libRef.current.downloads[ep.id] ? await libRef.current.offlineUrl(ep.id) : null;
      if (loadedId.current !== ep.id) {
        if (local) URL.revokeObjectURL(local);
        return; // another episode was picked meanwhile
      }
      objectUrl.current = local;
      setIsOffline(!!local);
      audio.src = local ?? ep.audioUrl;
      audio.playbackRate = load("sada.rate", 1);

      const saved = libRef.current.progress[ep.id];
      const resumeAt = saved && saved.position < saved.duration - 15 ? saved.position : 0;
      setTime({ position: resumeAt, duration: ep.durationMs / 1000, buffered: 0 });
      const restore = () => {
        if (resumeAt) audio.currentTime = resumeAt;
        audio.removeEventListener("loadedmetadata", restore);
      };
      audio.addEventListener("loadedmetadata", restore);
      if (autoplay) audio.play().catch(() => setIsLoading(false));
      else audio.load();
    },
    [audio]
  );

  const play = useCallback(
    (ep: Episode, list?: Episode[]) => {
      if (currentRef.current?.id === ep.id && loadedId.current === ep.id) {
        audio.play().catch(() => {});
        return;
      }
      persistProgress();
      setCurrent(ep);
      save("sada.lastEpisode", ep);
      if (list) setQueue(list);
      else if (!queueRef.current.some((e) => e.id === ep.id)) setQueue([ep]);
      if (sleepRef.current?.kind === "end") setSleep(null);
      loadEpisode(ep, true);
    },
    [audio, loadEpisode, persistProgress]
  );

  const toggle = useCallback(() => {
    const ep = currentRef.current;
    if (!ep) return;
    if (loadedId.current !== ep.id) {
      loadEpisode(ep, true);
      return;
    }
    if (audio.paused) audio.play().catch(() => {});
    else audio.pause();
  }, [audio, loadEpisode]);

  const seek = useCallback(
    (sec: number) => {
      const max = audio.duration || Infinity;
      audio.currentTime = Math.max(0, Math.min(sec, max));
      setTime((t) => ({ ...t, position: audio.currentTime }));
    },
    [audio]
  );

  const skip = useCallback((delta: number) => seek(audio.currentTime + delta), [audio, seek]);

  const step = useCallback(
    (dir: 1 | -1) => {
      const q = queueRef.current;
      const i = q.findIndex((e) => e.id === currentRef.current?.id);
      // Queues are newest-first, so "next" walks towards the end of the list.
      const target = q[i + dir];
      if (target) play(target);
    },
    [play]
  );
  const next = useCallback(() => step(1), [step]);
  const prev = useCallback(() => {
    if (audio.currentTime > 5) seek(0);
    else step(-1);
  }, [audio, seek, step]);

  const setRate = useCallback(
    (r: number) => {
      audio.playbackRate = r;
      setRateState(r);
      save("sada.rate", r);
    },
    [audio]
  );

  const addToQueue = useCallback((ep: Episode) => {
    setQueue((q) => (q.some((e) => e.id === ep.id) ? q : [...q, ep]));
  }, []);

  // Audio element events.
  useEffect(() => {
    const onPlay = () => setIsPlaying(true);
    const onPause = () => {
      setIsPlaying(false);
      persistProgress();
    };
    const onWaiting = () => setIsLoading(true);
    const onPlaying = () => setIsLoading(false);
    const onCanPlay = () => setIsLoading(false);
    const onTime = () => {
      const b = audio.buffered.length ? audio.buffered.end(audio.buffered.length - 1) : 0;
      setTime({ position: audio.currentTime, duration: audio.duration || 0, buffered: b });
      const now = Date.now();
      if (now - lastSave.current > SAVE_EVERY_MS) {
        lastSave.current = now;
        persistProgress();
      }
      const s = sleepRef.current;
      if (s?.kind === "at" && now >= s.at) {
        audio.pause();
        setSleep(null);
      }
    };
    const onEnded = () => {
      persistProgress();
      if (sleepRef.current?.kind === "end") {
        setSleep(null);
        return;
      }
      step(1);
    };
    const onError = () => {
      setIsLoading(false);
      setIsPlaying(false);
      if (audio.src) setError(navigator.onLine ? "تعذّر تشغيل هذه الحلقة" : "لا يوجد اتصال — حمّل الحلقات للاستماع دون إنترنت");
    };
    const events: [string, () => void][] = [
      ["play", onPlay], ["pause", onPause], ["waiting", onWaiting], ["playing", onPlaying],
      ["canplay", onCanPlay], ["timeupdate", onTime], ["durationchange", onTime], ["ended", onEnded], ["error", onError],
    ];
    events.forEach(([n, f]) => audio.addEventListener(n, f));
    return () => events.forEach(([n, f]) => audio.removeEventListener(n, f));
  }, [audio, persistProgress, step]);

  // Sleep timer that fires even when paused-buffering (timeupdate may not tick).
  useEffect(() => {
    if (sleep?.kind !== "at") return;
    const t = window.setTimeout(() => {
      audio.pause();
      setSleep(null);
    }, Math.max(0, sleep.at - Date.now()));
    return () => clearTimeout(t);
  }, [audio, sleep]);

  // Lock-screen / headset controls.
  useEffect(() => {
    const ms = navigator.mediaSession;
    if (!ms || !current) return;
    ms.metadata = new MediaMetadata({
      title: current.title,
      artist: current.podcastTitle,
      album: "صدى",
      artwork: [{ src: current.artwork, sizes: "600x600", type: "image/jpeg" }],
    });
    const handlers: [MediaSessionAction, MediaSessionActionHandler][] = [
      ["play", () => audio.play().catch(() => {})],
      ["pause", () => audio.pause()],
      ["seekbackward", (d) => skip(-(d.seekOffset ?? 15))],
      ["seekforward", (d) => skip(d.seekOffset ?? 30)],
      ["seekto", (d) => d.seekTime != null && seek(d.seekTime)],
      ["previoustrack", prev],
      ["nexttrack", next],
    ];
    handlers.forEach(([a, h]) => {
      try {
        ms.setActionHandler(a, h);
      } catch {
        /* unsupported action */
      }
    });
  }, [audio, current, next, prev, seek, skip]);

  useEffect(() => {
    const ms = navigator.mediaSession;
    if (!ms) return;
    ms.playbackState = isPlaying ? "playing" : "paused";
  }, [isPlaying]);

  useEffect(() => {
    const ms = navigator.mediaSession;
    if (!ms?.setPositionState || !time.duration || !Number.isFinite(time.duration)) return;
    try {
      ms.setPositionState({ duration: time.duration, position: Math.min(time.position, time.duration), playbackRate: rate });
    } catch {
      /* ignore */
    }
  }, [time.duration, Math.floor(time.position / 5), rate]); // eslint-disable-line react-hooks/exhaustive-deps

  // Restored "last episode" shows its saved position before anything is loaded.
  useEffect(() => {
    if (current && !loadedId.current) {
      const saved = lib.progress[current.id];
      setTime({ position: saved?.position ?? 0, duration: saved?.duration || current.durationMs / 1000, buffered: 0 });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    window.addEventListener("pagehide", persistProgress);
    return () => window.removeEventListener("pagehide", persistProgress);
  }, [persistProgress]);

  const value = useMemo<PlayerValue>(
    () => ({
      current, queue, isPlaying, isLoading, isOffline, error, rate, sleep, expanded, setExpanded,
      play, toggle, seek, skip, next, prev, setRate, setSleep, addToQueue,
    }),
    [current, queue, isPlaying, isLoading, isOffline, error, rate, sleep, expanded, play, toggle, seek, skip, next, prev, setRate, addToQueue]
  );

  return (
    <PlayerContext.Provider value={value}>
      <TimeContext.Provider value={time}>{children}</TimeContext.Provider>
    </PlayerContext.Provider>
  );
}
