import {
  createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode,
} from "react";
import { fetchChapters } from "../lib/api";
import { AudioFx, type FxSettings } from "../lib/audioFx";
import { load, save } from "../lib/storage";
import type { Chapter, Episode } from "../lib/types";
import { isNative, MediaPlayback, type MediaAction } from "../native";
import { useLibrary } from "./library";

export type SleepTimer = { kind: "at"; at: number } | { kind: "end" } | null;

interface PlayerValue {
  current: Episode | null;
  queue: Episode[];
  isPlaying: boolean;
  isLoading: boolean;
  isOffline: boolean;
  isVideo: boolean;
  /** The <video> element, for the full player to mount while a video episode plays. */
  videoEl: HTMLVideoElement;
  error: string | null;
  rate: number;
  sleep: SleepTimer;
  expanded: boolean;
  chapters: Chapter[];
  fx: FxSettings;
  /** Whether voice boost / silence trimming is applied to what's playing right now. */
  fxActive: boolean;
  timeSaved: number;
  setExpanded: (v: boolean) => void;
  play: (ep: Episode, queue?: Episode[]) => void;
  toggle: () => void;
  seek: (sec: number) => void;
  skip: (delta: number) => void;
  next: () => void;
  prev: () => void;
  setRate: (r: number) => void;
  setSleep: (s: SleepTimer) => void;
  setFx: (fx: FxSettings) => void;
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
/** Hosts known to send CORS headers, so Web Audio effects can read their streams. */
const CORS_HOSTS = /(^|\.)mp3quran\.net$/;

function canProcess(src: string): boolean {
  if (src.startsWith("blob:")) return true;
  try {
    const u = new URL(src, window.location.href);
    return u.origin === window.location.origin || CORS_HOSTS.test(u.hostname);
  } catch {
    return false;
  }
}

export function PlayerProvider({ children }: { children: ReactNode }) {
  const lib = useLibrary();
  const libRef = useRef(lib);
  libRef.current = lib;

  // Three elements: plain audio (any host), video, and an audio element wired to Web Audio
  // effects (only for sources it is allowed to read — see AudioFx).
  const els = useMemo(() => {
    const audio = new Audio();
    audio.preload = "metadata";
    const video = document.createElement("video");
    video.preload = "metadata";
    video.playsInline = true;
    video.className = "fp-video";
    const fxAudio = new Audio();
    fxAudio.preload = "metadata";
    fxAudio.crossOrigin = "anonymous";
    return { audio, video, fxAudio };
  }, []);
  const media = useRef<HTMLMediaElement>(els.audio);
  const el = () => media.current;
  const fxEngine = useRef<AudioFx | null>(null);

  const [current, setCurrent] = useState<Episode | null>(() => load<Episode | null>("sada.lastEpisode", null));
  const [queue, setQueue] = useState<Episode[]>([]);
  const [isPlaying, setIsPlaying] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isOffline, setIsOffline] = useState(false);
  const [isVideo, setIsVideo] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [rate, setRateState] = useState(() => load("sada.rate", 1));
  const [sleep, setSleep] = useState<SleepTimer>(null);
  const [expanded, setExpanded] = useState(false);
  const [time, setTime] = useState<TimeValue>({ position: 0, duration: 0, buffered: 0 });
  const [chapters, setChapters] = useState<Chapter[]>([]);
  const [fx, setFxState] = useState<FxSettings>(() => load("sada.fx", { boost: false, trimSilence: false }));
  const [fxActive, setFxActive] = useState(false);
  const [timeSaved, setTimeSaved] = useState(() => load("sada.timeSaved", 0));

  const currentRef = useRef(current);
  currentRef.current = current;
  const queueRef = useRef(queue);
  queueRef.current = queue;
  const sleepRef = useRef(sleep);
  sleepRef.current = sleep;
  const fxRef = useRef(fx);
  fxRef.current = fx;
  const isPlayingRef = useRef(isPlaying);
  isPlayingRef.current = isPlaying;
  const objectUrl = useRef<string | null>(null);
  const lastSave = useRef(0);
  const loadedId = useRef<string | null>(null);
  const savedBase = useRef(load("sada.timeSaved", 0));

  const persistProgress = useCallback(() => {
    const ep = currentRef.current;
    const m = media.current;
    if (ep && m.duration > 0 && Number.isFinite(m.duration)) libRef.current.recordProgress(ep, m.currentTime, m.duration);
  }, []);

  const switchElement = useCallback(
    (next: HTMLMediaElement) => {
      const prev = media.current;
      if (prev === next) return;
      prev.pause();
      prev.removeAttribute("src");
      prev.load();
      media.current = next;
    },
    []
  );

  const fxWanted = () => fxRef.current.boost || fxRef.current.trimSilence;

  /** Point the right element at an episode (downloaded copy preferred) and restore its position. */
  const loadEpisode = useCallback(
    async (ep: Episode, autoplay: boolean, startAt?: number) => {
      loadedId.current = ep.id;
      setError(null);
      setIsLoading(true);
      if (objectUrl.current) {
        if (objectUrl.current.startsWith("blob:")) URL.revokeObjectURL(objectUrl.current);
        objectUrl.current = null;
      }
      const local = libRef.current.downloads[ep.id] ? await libRef.current.offlineUrl(ep.id) : null;
      if (loadedId.current !== ep.id) {
        if (local?.startsWith("blob:")) URL.revokeObjectURL(local);
        return; // another episode was picked meanwhile
      }
      objectUrl.current = local;
      setIsOffline(!!local);
      const src = local ?? ep.audioUrl;

      const video = ep.mediaType === "video";
      const withFx = !video && fxWanted() && canProcess(src);
      const target = video ? els.video : withFx ? els.fxAudio : els.audio;
      switchElement(target);
      setIsVideo(video);
      setFxActive(withFx);
      if (withFx) {
        fxEngine.current ??= new AudioFx(els.fxAudio);
        fxEngine.current.onSaved = (s) => {
          const total = Math.round(savedBase.current + s);
          setTimeSaved((t) => (t === total ? t : total));
        };
        fxEngine.current.apply(fxRef.current);
      }

      target.src = src;
      const r = load("sada.rate", 1);
      if (withFx) fxEngine.current!.setBaseRate(r);
      else target.playbackRate = r;

      const saved = libRef.current.progress[ep.id];
      const resumeAt = startAt ?? (saved && saved.position < saved.duration - 15 ? saved.position : 0);
      setTime({ position: resumeAt, duration: ep.durationMs / 1000, buffered: 0 });
      const restore = () => {
        if (resumeAt) target.currentTime = resumeAt;
        target.removeEventListener("loadedmetadata", restore);
      };
      target.addEventListener("loadedmetadata", restore);
      if (autoplay) {
        if (withFx) fxEngine.current!.resume();
        target.play().catch(() => setIsLoading(false));
      } else target.load();
    },
    [els, switchElement]
  );

  const play = useCallback(
    (ep: Episode, list?: Episode[]) => {
      if (currentRef.current?.id === ep.id && loadedId.current === ep.id) {
        el().play().catch(() => {});
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
    [loadEpisode, persistProgress]
  );

  const toggle = useCallback(() => {
    const ep = currentRef.current;
    if (!ep) return;
    if (loadedId.current !== ep.id) {
      loadEpisode(ep, true);
      return;
    }
    const m = el();
    if (m.paused) {
      if (m === els.fxAudio) fxEngine.current?.resume();
      m.play().catch(() => {});
    } else m.pause();
  }, [els, loadEpisode]);

  const seek = useCallback((sec: number) => {
    const m = el();
    const max = m.duration || Infinity;
    m.currentTime = Math.max(0, Math.min(sec, max));
    setTime((t) => ({ ...t, position: m.currentTime }));
  }, []);

  const skip = useCallback((delta: number) => seek(el().currentTime + delta), [seek]);

  const step = useCallback(
    (dir: 1 | -1) => {
      const q = queueRef.current;
      const i = q.findIndex((e) => e.id === currentRef.current?.id);
      // Episode lists are newest-first and the Quran is in mushaf order: "next" walks down the list.
      const target = q[i + dir];
      if (target) play(target);
    },
    [play]
  );
  const next = useCallback(() => step(1), [step]);
  const prev = useCallback(() => {
    if (el().currentTime > 5) seek(0);
    else step(-1);
  }, [seek, step]);

  const setRate = useCallback(
    (r: number) => {
      if (media.current === els.fxAudio && fxEngine.current) fxEngine.current.setBaseRate(r);
      else media.current.playbackRate = r;
      setRateState(r);
      save("sada.rate", r);
    },
    [els]
  );

  const setFx = useCallback(
    (next: FxSettings) => {
      setFxState(next);
      fxRef.current = next;
      save("sada.fx", next);
      const ep = currentRef.current;
      if (!ep || loadedId.current !== ep.id) return;
      const m = media.current;
      const wantFx = (next.boost || next.trimSilence) && m !== els.video && canProcess(m.currentSrc || m.src);
      if (wantFx === (m === els.fxAudio)) {
        if (wantFx) fxEngine.current?.apply(next);
        return;
      }
      // Switch elements at the current position.
      persistProgress();
      loadEpisode(ep, !m.paused, m.currentTime);
    },
    [els, loadEpisode, persistProgress]
  );

  const addToQueue = useCallback((ep: Episode) => {
    setQueue((q) => (q.some((e) => e.id === ep.id) ? q : [...q, ep]));
  }, []);

  // Media element events (only the active element counts).
  useEffect(() => {
    const own = (f: () => void) => (e: Event) => {
      if (e.target === media.current) f();
    };
    const onTime = () => {
      const m = media.current;
      const b = m.buffered.length ? m.buffered.end(m.buffered.length - 1) : 0;
      setTime({ position: m.currentTime, duration: Number.isFinite(m.duration) ? m.duration : 0, buffered: b });
      const now = Date.now();
      if (now - lastSave.current > SAVE_EVERY_MS) {
        lastSave.current = now;
        persistProgress();
        if (fxEngine.current) {
          const total = savedBase.current + fxEngine.current.saved;
          save("sada.timeSaved", Math.round(total));
        }
      }
      const s = sleepRef.current;
      if (s?.kind === "at" && now >= s.at) {
        m.pause();
        setSleep(null);
      }
    };
    const handlers: [string, () => void][] = [
      ["play", () => setIsPlaying(true)],
      ["pause", () => { setIsPlaying(false); persistProgress(); }],
      ["waiting", () => setIsLoading(true)],
      ["playing", () => setIsLoading(false)],
      ["canplay", () => setIsLoading(false)],
      ["timeupdate", onTime],
      ["durationchange", onTime],
      ["ended", () => {
        persistProgress();
        if (sleepRef.current?.kind === "end") {
          setSleep(null);
          return;
        }
        step(1);
      }],
      ["error", () => {
        const m = media.current;
        if (!m.getAttribute("src")) return;
        setIsLoading(false);
        setIsPlaying(false);
        setError(navigator.onLine ? "تعذّر تشغيل هذه الحلقة" : "لا يوجد اتصال — حمّل الحلقات للاستماع دون إنترنت");
      }],
    ];
    const bound = handlers.map(([n, f]) => [n, own(f)] as const);
    const all = [els.audio, els.video, els.fxAudio];
    all.forEach((m) => bound.forEach(([n, f]) => m.addEventListener(n, f)));
    return () => all.forEach((m) => bound.forEach(([n, f]) => m.removeEventListener(n, f)));
  }, [els, persistProgress, step]);

  // Chapters for the current episode.
  useEffect(() => {
    setChapters(current?.chapters ?? []);
    if (current?.chapters?.length || !current?.chaptersUrl) return;
    let alive = true;
    fetchChapters(current.chaptersUrl).then((c) => alive && setChapters(c), () => {});
    return () => {
      alive = false;
    };
  }, [current]);

  // Sleep timer that fires even when paused-buffering (timeupdate may not tick).
  useEffect(() => {
    if (sleep?.kind !== "at") return;
    const t = window.setTimeout(() => {
      media.current.pause();
      setSleep(null);
    }, Math.max(0, sleep.at - Date.now()));
    return () => clearTimeout(t);
  }, [sleep]);

  // Lock-screen / headset controls (browser). The Android app uses a native service instead, below.
  useEffect(() => {
    const ms = navigator.mediaSession;
    if (isNative || !ms || !current) return;
    ms.metadata = new MediaMetadata({
      title: current.title,
      artist: current.podcastTitle,
      album: "صدى",
      artwork: [{ src: current.artwork, sizes: "600x600", type: "image/jpeg" }],
    });
    const handlers: [MediaSessionAction, MediaSessionActionHandler][] = [
      ["play", () => toggle()],
      ["pause", () => media.current.pause()],
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
  }, [current, next, prev, seek, skip, toggle]);

  useEffect(() => {
    const ms = navigator.mediaSession;
    if (isNative || !ms) return;
    ms.playbackState = isPlaying ? "playing" : "paused";
  }, [isPlaying]);

  useEffect(() => {
    const ms = navigator.mediaSession;
    if (isNative || !ms?.setPositionState || !time.duration || !Number.isFinite(time.duration)) return;
    try {
      ms.setPositionState({ duration: time.duration, position: Math.min(time.position, time.duration), playbackRate: rate });
    } catch {
      /* ignore */
    }
  }, [time.duration, Math.floor(time.position / 5), rate]); // eslint-disable-line react-hooks/exhaustive-deps

  // Android: keep the foreground playback service + notification in sync.
  const posBucket = Math.floor(time.position / 10);
  useEffect(() => {
    if (!isNative || !current) return;
    const m = media.current;
    MediaPlayback.update({
      title: current.title,
      artist: current.podcastTitle,
      artwork: current.artwork,
      playing: isPlaying,
      position: m.currentTime || time.position,
      duration: (Number.isFinite(m.duration) && m.duration) || time.duration || current.durationMs / 1000,
      rate,
    }).catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [current, isPlaying, rate, posBucket, time.duration]);

  const actionsRef = useRef<Partial<Record<MediaAction, (pos?: number) => void>>>({});
  actionsRef.current = {
    play: () => toggle(),
    pause: () => media.current.pause(),
    nexttrack: next,
    previoustrack: prev,
    seekforward: () => skip(30),
    seekbackward: () => skip(-15),
    seekto: (pos) => pos != null && seek(pos),
  };
  useEffect(() => {
    if (!isNative) return;
    const handle = MediaPlayback.addListener("action", (e) => {
      if (e.action === "play" && !media.current.paused) return;
      actionsRef.current[e.action]?.(e.position);
    });
    handle.catch((err) => console.warn("MediaPlayback listener failed", err));
    return () => {
      handle.then((h) => h.remove()).catch(() => {});
    };
  }, []);

  // Restored "last episode" shows its saved position before anything is loaded.
  useEffect(() => {
    if (current && !loadedId.current) {
      const saved = lib.progress[current.id];
      setTime({ position: saved?.position ?? 0, duration: saved?.duration || current.durationMs / 1000, buffered: 0 });
      setIsVideo(current.mediaType === "video");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    window.addEventListener("pagehide", persistProgress);
    return () => window.removeEventListener("pagehide", persistProgress);
  }, [persistProgress]);

  const value = useMemo<PlayerValue>(
    () => ({
      current, queue, isPlaying, isLoading, isOffline, isVideo, videoEl: els.video, error, rate, sleep, expanded,
      chapters, fx, fxActive, timeSaved,
      setExpanded, play, toggle, seek, skip, next, prev, setRate, setSleep, setFx, addToQueue,
    }),
    [current, queue, isPlaying, isLoading, isOffline, isVideo, els, error, rate, sleep, expanded, chapters, fx, fxActive,
      timeSaved, play, toggle, seek, skip, next, prev, setRate, setFx, addToQueue]
  );

  return (
    <PlayerContext.Provider value={value}>
      <TimeContext.Provider value={time}>{children}</TimeContext.Provider>
    </PlayerContext.Provider>
  );
}
