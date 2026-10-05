import { Capacitor, CapacitorHttp } from "@capacitor/core";
import type { Episode, Podcast } from "./types";

// Apple's public podcast directory: free, keyless and CORS-enabled.
const ITUNES = "https://itunes.apple.com";

/** Responses younger than this are reused instead of hitting the network again. */
const FRESH_MS = 5 * 60_000;
const cache = new Map<string, { at: number; p: Promise<unknown> }>();

const TIMEOUT_MS = 20_000;

/** In the Android app requests go through the native HTTP stack, not the WebView. */
async function nativeGet(url: string): Promise<unknown> {
  const res = await CapacitorHttp.get({ url, responseType: "text", connectTimeout: TIMEOUT_MS, readTimeout: TIMEOUT_MS });
  if (res.status < 200 || res.status >= 300) throw new Error(`HTTP ${res.status}`);
  return typeof res.data === "string" ? JSON.parse(res.data) : res.data;
}

async function webGet(url: string): Promise<unknown> {
  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), TIMEOUT_MS);
  try {
    const r = await fetch(url, { signal: ctrl.signal });
    if (!r.ok) throw new Error(`HTTP ${r.status}`);
    return JSON.parse(await r.text());
  } finally {
    clearTimeout(timer);
  }
}

function getJson<T>(url: string, fresh = false): Promise<T> {
  const hit = cache.get(url);
  if (hit && (!fresh || Date.now() - hit.at < 30_000) && Date.now() - hit.at < FRESH_MS) return hit.p as Promise<T>;
  const p = Capacitor.isNativePlatform() ? nativeGet(url) : webGet(url);
  cache.set(url, { at: Date.now(), p });
  p.catch(() => cache.delete(url));
  return p as Promise<T>;
}

/**
 * A request that can be re-run. `key` identifies its last result in the offline cache;
 * `fresh` skips the short in-memory cache (used by automatic refreshes).
 */
export interface Query<T> {
  key: string;
  persist: boolean;
  run: (fresh: boolean) => Promise<T>;
}

/** Swap Apple's thumbnail size suffix (e.g. 100x100bb.jpg) for a larger one. */
export function hiRes(url: string | undefined, size = 600): string {
  if (!url) return "";
  return url.replace(/\/\d+x\d+bb\.(jpg|png|webp)$/, `/${size}x${size}bb.$1`);
}

/* ---------- Top charts ---------- */

interface ChartEntry {
  "im:name": { label: string };
  "im:image": { label: string }[];
  "im:artist"?: { label: string };
  summary?: { label: string };
  id: { attributes: { "im:id": string } };
  category?: { attributes: { label: string } };
  link?: { attributes: { href: string } };
}

export function topPodcasts(country: string, genreId?: string, limit = 50): Query<Podcast[]> {
  const genre = genreId ? `/genre=${genreId}` : "";
  const url = `${ITUNES}/${country}/rss/toppodcasts/limit=${limit}${genre}/json`;
  return { key: url, persist: true, run: (fresh) => fetchTop(url, fresh) };
}

async function fetchTop(url: string, fresh: boolean): Promise<Podcast[]> {
  const data = await getJson<{ feed: { entry?: ChartEntry | ChartEntry[] } }>(url, fresh);
  const raw = data.feed.entry;
  const entries = raw ? (Array.isArray(raw) ? raw : [raw]) : [];
  return entries.map((e) => ({
    id: e.id.attributes["im:id"],
    title: e["im:name"].label,
    author: e["im:artist"]?.label ?? "",
    artwork: hiRes(e["im:image"][e["im:image"].length - 1]?.label),
    genre: e.category?.attributes.label,
    summary: e.summary?.label,
    link: e.link?.attributes.href,
  }));
}

/* ---------- Search & lookup ---------- */

interface ITunesPodcast {
  wrapperType: string;
  kind?: string;
  collectionId: number;
  collectionName: string;
  artistName: string;
  artworkUrl600?: string;
  artworkUrl100?: string;
  primaryGenreName?: string;
  feedUrl?: string;
  trackCount?: number;
  collectionViewUrl?: string;
}

interface ITunesEpisode {
  wrapperType: string;
  trackId: number;
  collectionId: number;
  collectionName: string;
  trackName: string;
  description?: string;
  shortDescription?: string;
  episodeUrl?: string;
  artworkUrl600?: string;
  artworkUrl160?: string;
  releaseDate: string;
  trackTimeMillis?: number;
  episodeFileExtension?: string;
}

const toPodcast = (r: ITunesPodcast): Podcast => ({
  id: String(r.collectionId),
  title: r.collectionName,
  author: r.artistName,
  artwork: hiRes(r.artworkUrl600 ?? r.artworkUrl100),
  genre: r.primaryGenreName,
  feedUrl: r.feedUrl,
  episodeCount: r.trackCount,
  link: r.collectionViewUrl,
});

const toEpisode = (r: ITunesEpisode, fallbackArt = ""): Episode => ({
  id: String(r.trackId),
  podcastId: String(r.collectionId),
  podcastTitle: r.collectionName,
  title: r.trackName,
  description: (r.description ?? r.shortDescription ?? "").trim(),
  audioUrl: r.episodeUrl ?? "",
  artwork: hiRes(r.artworkUrl600 ?? r.artworkUrl160) || fallbackArt,
  releaseDate: r.releaseDate,
  durationMs: r.trackTimeMillis ?? 0,
  fileExtension: r.episodeFileExtension,
});

export function searchPodcasts(term: string, country: string): Query<Podcast[]> {
  const q = new URLSearchParams({ term, media: "podcast", entity: "podcast", limit: "40", country });
  const url = `${ITUNES}/search?${q}`;
  return {
    key: url,
    persist: false,
    run: async (fresh) => {
      if (!term.trim()) return [];
      const data = await getJson<{ results: ITunesPodcast[] }>(url, fresh);
      return data.results.filter((r) => r.collectionId && r.collectionName).map(toPodcast);
    },
  };
}

export function searchEpisodes(term: string, country: string): Query<Episode[]> {
  const q = new URLSearchParams({ term, media: "podcast", entity: "podcastEpisode", limit: "40", country });
  const url = `${ITUNES}/search?${q}`;
  return {
    key: url,
    persist: false,
    run: async (fresh) => {
      if (!term.trim()) return [];
      const data = await getJson<{ results: ITunesEpisode[] }>(url, fresh);
      return data.results.filter((r) => r.episodeUrl).map((r) => toEpisode(r));
    },
  };
}

export type PodcastData = { podcast: Podcast | null; episodes: Episode[] };

export function podcastWithEpisodes(id: string, country: string, limit = 200): Query<PodcastData> {
  const q = new URLSearchParams({ id, entity: "podcastEpisode", limit: String(limit), country });
  const url = `${ITUNES}/lookup?${q}`;
  return { key: url, persist: limit >= 200, run: (fresh) => fetchPodcast(url, fresh) };
}

async function fetchPodcast(url: string, fresh: boolean): Promise<PodcastData> {
  const data = await getJson<{ results: (ITunesPodcast | ITunesEpisode)[] }>(url, fresh);
  const head = data.results.find((r) => r.wrapperType === "track") as ITunesPodcast | undefined;
  const podcast = head ? toPodcast(head) : null;
  const episodes = data.results
    .filter((r): r is ITunesEpisode => r.wrapperType === "podcastEpisode")
    .filter((r) => r.episodeUrl)
    .map((r) => toEpisode(r, podcast?.artwork))
    .sort((a, b) => b.releaseDate.localeCompare(a.releaseDate));
  return { podcast, episodes };
}
