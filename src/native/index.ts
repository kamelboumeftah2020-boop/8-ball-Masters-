import { Capacitor, registerPlugin, type PluginListenerHandle } from "@capacitor/core";
import { Directory, Filesystem } from "@capacitor/filesystem";
import { FileTransfer } from "@capacitor/file-transfer";
import type { Episode } from "../lib/types";

/** True inside the Android (Capacitor) app, false in the browser/PWA. */
export const isNative = Capacitor.isNativePlatform();

/* ---------- Downloads to app storage (no CORS limits natively) ---------- */

const DIR = "episodes";

export function episodeFilePath(ep: Episode): string {
  const ext = (ep.fileExtension || ep.audioUrl.split("?")[0].split(".").pop() || "mp3").replace(/[^a-z0-9]/gi, "").slice(0, 4) || "mp3";
  return `${DIR}/${ep.id}.${ext}`;
}

/** Download an episode file natively, reporting progress 0..1. Returns size in bytes. */
export async function nativeDownload(ep: Episode, path: string, onProgress: (p: number) => void): Promise<number> {
  await Filesystem.mkdir({ path: DIR, directory: Directory.Data, recursive: true }).catch(() => {});
  const { uri } = await Filesystem.getUri({ path, directory: Directory.Data });
  const listener = await FileTransfer.addListener("progress", (s) => {
    if (s.url === ep.audioUrl && s.lengthComputable && s.contentLength) onProgress(s.bytes / s.contentLength);
  });
  try {
    await FileTransfer.downloadFile({ url: ep.audioUrl, path: uri, progress: true });
    const stat = await Filesystem.stat({ path, directory: Directory.Data });
    return stat.size;
  } catch (err) {
    await Filesystem.deleteFile({ path, directory: Directory.Data }).catch(() => {});
    throw err;
  } finally {
    listener.remove();
  }
}

export async function nativeFileUrl(path: string): Promise<string | null> {
  try {
    const { uri } = await Filesystem.getUri({ path, directory: Directory.Data });
    return Capacitor.convertFileSrc(uri);
  } catch {
    return null;
  }
}

export const nativeDelete = (path: string) => Filesystem.deleteFile({ path, directory: Directory.Data }).catch(() => {});

/* ---------- Background playback service + media notification ---------- */

export type MediaAction = "play" | "pause" | "nexttrack" | "previoustrack" | "seekforward" | "seekbackward" | "seekto";

interface MediaPlaybackPlugin {
  update(options: {
    title: string;
    artist: string;
    artwork: string;
    playing: boolean;
    position: number;
    duration: number;
    rate: number;
  }): Promise<void>;
  stop(): Promise<void>;
  addListener(event: "action", fn: (e: { action: MediaAction; position?: number }) => void): Promise<PluginListenerHandle>;
}

export const MediaPlayback = registerPlugin<MediaPlaybackPlugin>("MediaPlayback");

/* ---------- Background check for new episodes + notifications ---------- */

export interface WatchedPodcast {
  id: string;
  title: string;
  /** releaseDate (ISO) of the newest episode the app already knows about. */
  latest?: string;
}

interface EpisodeCheckerPlugin {
  sync(options: { country: string; podcasts: WatchedPodcast[] }): Promise<void>;
  requestPermission(): Promise<{ granted: boolean }>;
  consumeRoute(): Promise<{ route?: string | null }>;
  addListener(event: "openRoute", fn: (e: { route: string }) => void): Promise<PluginListenerHandle>;
}

export const EpisodeChecker = registerPlugin<EpisodeCheckerPlugin>("EpisodeChecker");
