import type { Chapter, Episode, Podcast } from "./types";

/**
 * Minimal podcast RSS reader: full episode history (Apple's API stops at 200),
 * video enclosures and chapters (Podcasting 2.0 JSON or Podlove inline).
 */

/** Stable short id from any string (djb2, base36). */
export function hashId(s: string): string {
  let h = 5381;
  for (let i = 0; i < s.length; i++) h = ((h << 5) + h + s.charCodeAt(i)) | 0;
  return (h >>> 0).toString(36);
}

export const rssPodcastId = (feedUrl: string) => `rss-${hashId(feedUrl.trim())}`;
export const isRssId = (id: string) => id.startsWith("rss-");

const kids = (el: Element) => Array.from(el.children);
/** First direct child whose local name matches (namespace prefixes vary between feeds). */
function child(el: Element, name: string, pred?: (e: Element) => boolean): Element | undefined {
  return kids(el).find((c) => c.localName === name && (!pred || pred(c)));
}
const text = (el?: Element) => el?.textContent?.trim() ?? "";

/** HTML show notes -> readable plain text with line breaks. */
export function htmlToText(html: string): string {
  if (!/[<&]/.test(html)) return html.trim();
  const withBreaks = html.replace(/<br\s*\/?>/gi, "\n").replace(/<\/(p|div|li|h\d)>/gi, "\n");
  const doc = new DOMParser().parseFromString(withBreaks, "text/html");
  return (doc.body.textContent ?? "").replace(/\n{3,}/g, "\n\n").trim();
}

/** "1:02:03", "62:03", "3723" -> milliseconds. */
export function parseDuration(s: string): number {
  if (!s) return 0;
  const parts = s.split(":").map(Number);
  if (parts.some((n) => Number.isNaN(n))) return 0;
  const sec = parts.reduce((acc, n) => acc * 60 + n, 0);
  return sec * 1000;
}

/** Normalise any date to the "YYYY-MM-DDTHH:MM:SSZ" form Apple uses, so string comparison works. */
export function isoDate(s: string): string {
  const d = new Date(s);
  return Number.isNaN(d.getTime()) ? "" : d.toISOString().replace(/\.\d{3}Z$/, "Z");
}

function parseNpt(s: string): number {
  // Podlove "HH:MM:SS.mmm" / "MM:SS"
  return parseDuration(s.split(".")[0]) / 1000 + (s.includes(".") ? Number("0." + s.split(".")[1]) || 0 : 0);
}

const VIDEO_EXT = /\.(mp4|m4v|mov|webm)(\?|$)/i;

export function isVideo(type: string, url: string): boolean {
  return type.startsWith("video/") || (!type.startsWith("audio/") && VIDEO_EXT.test(url));
}

export interface ParsedFeed {
  podcast: Podcast;
  episodes: Episode[];
}

export function parseFeed(xml: string, feedUrl: string, podcastId: string): ParsedFeed {
  const doc = new DOMParser().parseFromString(xml, "application/xml");
  const channel = doc.querySelector("channel");
  if (!channel || doc.querySelector("parsererror")) throw new Error("ليس رابط RSS صالحاً");

  const itunesImage = child(channel, "image", (e) => e.hasAttribute("href"))?.getAttribute("href");
  const rssImage = child(channel, "image", (e) => !e.hasAttribute("href"));
  const channelArt = itunesImage || (rssImage ? text(child(rssImage, "url")) : "");
  const title = text(child(channel, "title"));
  const podcast: Podcast = {
    id: podcastId,
    title,
    author: text(child(channel, "author")) || text(child(channel, "managingEditor")),
    artwork: channelArt,
    summary: htmlToText(text(child(channel, "summary")) || text(child(channel, "description"))),
    genre: child(channel, "category")?.getAttribute("text") ?? undefined,
    feedUrl,
  };

  const episodes: Episode[] = [];
  for (const item of kids(channel).filter((c) => c.localName === "item")) {
    const enc = child(item, "enclosure");
    const url = enc?.getAttribute("url") || "";
    if (!url) continue;
    const type = enc?.getAttribute("type") || "";
    const guid = text(child(item, "guid")) || url;
    const art = child(item, "image", (e) => e.hasAttribute("href"))?.getAttribute("href");

    const podlove = child(item, "chapters", (e) => !e.hasAttribute("url"));
    const chapters: Chapter[] | undefined = podlove
      ? kids(podlove)
          .filter((c) => c.localName === "chapter")
          .map((c) => ({ start: parseNpt(c.getAttribute("start") || "0"), title: c.getAttribute("title") || "", img: c.getAttribute("image") || undefined }))
      : undefined;

    episodes.push({
      id: `${podcastId}:${hashId(guid)}`,
      podcastId,
      podcastTitle: title,
      title: text(child(item, "title")),
      description: htmlToText(text(child(item, "encoded")) || text(child(item, "description")) || text(child(item, "summary"))),
      audioUrl: url,
      artwork: art || channelArt,
      releaseDate: isoDate(text(child(item, "pubDate"))),
      durationMs: parseDuration(text(child(item, "duration"))),
      fileExtension: url.split("?")[0].split(".").pop()?.toLowerCase(),
      mediaType: isVideo(type, url) ? "video" : "audio",
      chaptersUrl: child(item, "chapters", (e) => e.hasAttribute("url"))?.getAttribute("url") ?? undefined,
      chapters: chapters?.length ? chapters : undefined,
    });
  }
  episodes.sort((a, b) => b.releaseDate.localeCompare(a.releaseDate));
  return { podcast, episodes };
}

const urlKey = (u: string) => u.split("?")[0].replace(/^https?:\/\//, "").toLowerCase();
const titleKey = (t: string) => t.replace(/\s+/g, " ").trim().toLowerCase();

/**
 * Combine Apple's episodes (stable ids that favourites/progress already use)
 * with the feed's full history and extras.
 */
export function mergeEpisodes(apple: Episode[], feed: Episode[]): Episode[] {
  const byUrl = new Map(apple.map((e) => [urlKey(e.audioUrl), e]));
  const byTitle = new Map(apple.map((e) => [titleKey(e.title), e]));
  const used = new Set<string>();
  const out: Episode[] = [];
  for (const f of feed) {
    const a = byUrl.get(urlKey(f.audioUrl)) ?? byTitle.get(titleKey(f.title));
    if (a && !used.has(a.id)) {
      used.add(a.id);
      out.push({
        ...a,
        description: f.description.length > a.description.length ? f.description : a.description,
        mediaType: f.mediaType,
        chaptersUrl: f.chaptersUrl,
        chapters: f.chapters,
        durationMs: a.durationMs || f.durationMs,
      });
    } else {
      out.push({ ...f, podcastId: apple[0]?.podcastId ?? f.podcastId, podcastTitle: apple[0]?.podcastTitle ?? f.podcastTitle });
    }
  }
  for (const a of apple) if (!used.has(a.id)) out.push(a);
  return out.sort((x, y) => y.releaseDate.localeCompare(x.releaseDate));
}

/** Podcasting 2.0 JSON chapters. */
export function parseChaptersJson(data: unknown): Chapter[] {
  const list = (data as { chapters?: { startTime?: number; title?: string; img?: string; toc?: boolean }[] })?.chapters ?? [];
  return list
    .filter((c) => c.toc !== false && typeof c.startTime === "number")
    .map((c) => ({ start: c.startTime!, title: c.title ?? "", img: c.img }));
}
