import type { Episode, Podcast } from "./types";

// Apple's public podcast directory: free, keyless and CORS-enabled.
const ITUNES = "https://itunes.apple.com";

const cache = new Map<string, Promise<unknown>>();

async function getJson<T>(url: string): Promise<T> {
  const hit = cache.get(url);
  if (hit) return hit as Promise<T>;
  const p = fetch(url).then((r) => {
    if (!r.ok) throw new Error(`HTTP ${r.status}`);
    return r.json() as Promise<T>;
  });
  cache.set(url, p);
  p.catch(() => cache.delete(url));
  return p;
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

export async function topPodcasts(country: string, genreId?: string, limit = 50): Promise<Podcast[]> {
  const genre = genreId ? `/genre=${genreId}` : "";
  const data = await getJson<{ feed: { entry?: ChartEntry | ChartEntry[] } }>(
    `${ITUNES}/${country}/rss/toppodcasts/limit=${limit}${genre}/json`
  );
  const raw = data.feed.entry;
  const entries = raw ? (Array.isArray(raw) ? raw : [raw]) : [];
  return entries.map((e) => ({
    id: e.id.attributes["im:id"],
    title: e["im:name"].label,
    author: e["im:artist"]?.label ?? "",
    artwork: hiRes(e["im:image"].at(-1)?.label),
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

export async function searchPodcasts(term: string, country: string): Promise<Podcast[]> {
  const q = new URLSearchParams({ term, media: "podcast", entity: "podcast", limit: "40", country });
  const data = await getJson<{ results: ITunesPodcast[] }>(`${ITUNES}/search?${q}`);
  return data.results.filter((r) => r.collectionId && r.collectionName).map(toPodcast);
}

export async function searchEpisodes(term: string, country: string): Promise<Episode[]> {
  const q = new URLSearchParams({ term, media: "podcast", entity: "podcastEpisode", limit: "40", country });
  const data = await getJson<{ results: ITunesEpisode[] }>(`${ITUNES}/search?${q}`);
  return data.results.filter((r) => r.episodeUrl).map((r) => toEpisode(r));
}

export async function podcastWithEpisodes(
  id: string,
  country: string
): Promise<{ podcast: Podcast | null; episodes: Episode[] }> {
  const q = new URLSearchParams({ id, entity: "podcastEpisode", limit: "200", country });
  const data = await getJson<{ results: (ITunesPodcast | ITunesEpisode)[] }>(`${ITUNES}/lookup?${q}`);
  const head = data.results.find((r) => r.wrapperType === "track") as ITunesPodcast | undefined;
  const podcast = head ? toPodcast(head) : null;
  const episodes = data.results
    .filter((r): r is ITunesEpisode => r.wrapperType === "podcastEpisode")
    .filter((r) => r.episodeUrl)
    .map((r) => toEpisode(r, podcast?.artwork))
    .sort((a, b) => b.releaseDate.localeCompare(a.releaseDate));
  return { podcast, episodes };
}
