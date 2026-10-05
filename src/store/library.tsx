import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { allDownloads, deleteDownload, getDownload, putDownload, type StoredDownload } from "../lib/db";
import { usePersistent } from "../lib/storage";
import { episodeFilePath, isNative, nativeDelete, nativeDownload, nativeFileUrl } from "../native";
import type { Episode, Podcast } from "../lib/types";

export interface DownloadInfo {
  episode: Episode;
  size: number;
  savedAt: number;
}

export type ActiveDownload = { progress: number; error?: undefined } | { progress: number; error: string };

export interface Progress {
  position: number;
  duration: number;
  updatedAt: number;
}

interface LibraryValue {
  country: string;
  setCountry: (c: string) => void;

  favPodcasts: Podcast[];
  isFavPodcast: (id: string) => boolean;
  toggleFavPodcast: (p: Podcast) => void;

  favEpisodes: Episode[];
  isFavEpisode: (id: string) => boolean;
  toggleFavEpisode: (e: Episode) => void;

  downloads: Record<string, DownloadInfo>;
  active: Record<string, ActiveDownload>;
  download: (e: Episode) => void;
  cancelDownload: (id: string) => void;
  removeDownload: (id: string) => Promise<void>;
  offlineUrl: (id: string) => Promise<string | null>;

  progress: Record<string, Progress>;
  history: Episode[];
  recordProgress: (e: Episode, position: number, duration: number) => void;
  clearHistory: () => void;
}

const LibraryContext = createContext<LibraryValue | null>(null);

export function useLibrary() {
  const ctx = useContext(LibraryContext);
  if (!ctx) throw new Error("useLibrary outside LibraryProvider");
  return ctx;
}

const HISTORY_LIMIT = 50;

// Optional CORS proxy (see proxy/cloudflare-worker.js) for hosts that block in-browser downloads.
const DOWNLOAD_PROXY = (import.meta.env.VITE_DOWNLOAD_PROXY as string | undefined)?.replace(/\/$/, "");

async function fetchAudio(url: string, signal: AbortSignal): Promise<Response> {
  try {
    return await fetch(url, { signal });
  } catch (err) {
    // A TypeError here almost always means the server sent no CORS headers.
    if (!DOWNLOAD_PROXY || signal.aborted || !(err instanceof TypeError)) throw err;
    return fetch(`${DOWNLOAD_PROXY}/?url=${encodeURIComponent(url)}`, { signal });
  }
}

export function LibraryProvider({ children }: { children: ReactNode }) {
  const [country, setCountry] = usePersistent("sada.country", "sa");
  const [favPodcasts, setFavPodcasts] = usePersistent<Podcast[]>("sada.favPodcasts", []);
  const [favEpisodes, setFavEpisodes] = usePersistent<Episode[]>("sada.favEpisodes", []);
  const [progress, setProgress] = usePersistent<Record<string, Progress>>("sada.progress", {});
  const [history, setHistory] = usePersistent<Episode[]>("sada.history", []);

  const [downloads, setDownloads] = useState<Record<string, DownloadInfo>>({});
  const [active, setActive] = useState<Record<string, ActiveDownload>>({});
  const [controllers] = useState(() => new Map<string, AbortController>());

  useEffect(() => {
    allDownloads()
      .then((list) =>
        setDownloads(Object.fromEntries(list.map((d) => [d.id, { episode: d.episode, size: d.size, savedAt: d.savedAt }])))
      )
      .catch(() => {});
  }, []);

  const toggleFavPodcast = useCallback(
    (p: Podcast) =>
      setFavPodcasts((list) => (list.some((x) => x.id === p.id) ? list.filter((x) => x.id !== p.id) : [p, ...list])),
    [setFavPodcasts]
  );
  const toggleFavEpisode = useCallback(
    (e: Episode) =>
      setFavEpisodes((list) => (list.some((x) => x.id === e.id) ? list.filter((x) => x.id !== e.id) : [e, ...list])),
    [setFavEpisodes]
  );

  const download = useCallback(
    async (ep: Episode) => {
      if (controllers.has(ep.id)) return;
      const ctrl = new AbortController();
      controllers.set(ep.id, ctrl);
      setActive((a) => ({ ...a, [ep.id]: { progress: 0 } }));
      navigator.storage?.persist?.().catch(() => {});

      const finish = async (record: StoredDownload) => {
        await putDownload(record);
        setDownloads((d) => ({ ...d, [ep.id]: { episode: ep, size: record.size, savedAt: record.savedAt } }));
        setActive(({ [ep.id]: _, ...rest }) => rest);
      };

      try {
        if (isNative) {
          // Native download: no CORS restrictions, written straight to app storage.
          const path = episodeFilePath(ep);
          let lastTick = 0;
          const size = await nativeDownload(ep, path, (progress) => {
            const now = performance.now();
            if (now - lastTick > 200 && !ctrl.signal.aborted) {
              lastTick = now;
              setActive((a) => ({ ...a, [ep.id]: { progress } }));
            }
          });
          // The native transfer can't be interrupted; honour a cancel by discarding the file.
          if (ctrl.signal.aborted) {
            await nativeDelete(path);
            return;
          }
          await finish({ id: ep.id, episode: ep, path, size, savedAt: Date.now() });
          return;
        }

        const res = await fetchAudio(ep.audioUrl, ctrl.signal);
        if (!res.ok || !res.body) throw new Error(`HTTP ${res.status}`);
        const total = Number(res.headers.get("content-length")) || 0;
        const reader = res.body.getReader();
        const chunks: Uint8Array[] = [];
        let received = 0;
        let lastTick = 0;
        for (;;) {
          const { done, value } = await reader.read();
          if (done) break;
          chunks.push(value);
          received += value.length;
          const now = performance.now();
          if (total && now - lastTick > 200) {
            lastTick = now;
            setActive((a) => ({ ...a, [ep.id]: { progress: received / total } }));
          }
        }
        const type = res.headers.get("content-type")?.split(";")[0] || "audio/mpeg";
        const blob = new Blob(chunks as BlobPart[], { type });
        await finish({ id: ep.id, episode: ep, blob, size: blob.size, savedAt: Date.now() });
      } catch (err) {
        if (ctrl.signal.aborted) {
          setActive(({ [ep.id]: _, ...rest }) => rest);
        } else {
          // Most failures here are servers that refuse cross-origin downloads (CORS).
          const message =
            err instanceof TypeError && !isNative
              ? "خادم هذا البودكاست لا يسمح بالتحميل داخل المتصفح"
              : "تعذّر التحميل، حاول مرة أخرى";
          setActive((a) => ({ ...a, [ep.id]: { progress: 0, error: message } }));
        }
      } finally {
        controllers.delete(ep.id);
      }
    },
    [controllers]
  );

  const cancelDownload = useCallback(
    (id: string) => {
      controllers.get(id)?.abort();
      setActive(({ [id]: _, ...rest }) => rest);
    },
    [controllers]
  );

  const removeDownload = useCallback(async (id: string) => {
    const rec = await getDownload(id).catch(() => undefined);
    if (rec?.path) await nativeDelete(rec.path);
    await deleteDownload(id);
    setDownloads(({ [id]: _, ...rest }) => rest);
  }, []);

  const offlineUrl = useCallback(async (id: string) => {
    const rec = await getDownload(id).catch(() => undefined);
    if (rec?.path) return nativeFileUrl(rec.path);
    return rec?.blob ? URL.createObjectURL(rec.blob) : null;
  }, []);

  const recordProgress = useCallback(
    (ep: Episode, position: number, duration: number) => {
      setProgress((p) => ({ ...p, [ep.id]: { position, duration, updatedAt: Date.now() } }));
      setHistory((h) => (h[0]?.id === ep.id ? h : [ep, ...h.filter((x) => x.id !== ep.id)].slice(0, HISTORY_LIMIT)));
    },
    [setProgress, setHistory]
  );

  const clearHistory = useCallback(() => {
    setHistory([]);
    setProgress({});
  }, [setHistory, setProgress]);

  const value = useMemo<LibraryValue>(() => {
    const favP = new Set(favPodcasts.map((p) => p.id));
    const favE = new Set(favEpisodes.map((e) => e.id));
    return {
      country,
      setCountry,
      favPodcasts,
      isFavPodcast: (id) => favP.has(id),
      toggleFavPodcast,
      favEpisodes,
      isFavEpisode: (id) => favE.has(id),
      toggleFavEpisode,
      downloads,
      active,
      download,
      cancelDownload,
      removeDownload,
      offlineUrl,
      progress,
      history,
      recordProgress,
      clearHistory,
    };
  }, [
    country, setCountry, favPodcasts, toggleFavPodcast, favEpisodes, toggleFavEpisode, downloads, active,
    download, cancelDownload, removeDownload, offlineUrl, progress, history, recordProgress, clearHistory,
  ]);

  return <LibraryContext.Provider value={value}>{children}</LibraryContext.Provider>;
}
