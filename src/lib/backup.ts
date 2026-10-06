import { Directory, Encoding, Filesystem } from "@capacitor/filesystem";
import { Share } from "@capacitor/share";
import { isNative } from "../native";
import { loadFeed, lookupPodcast, searchPodcasts } from "./api";
import { feedRegistry } from "./feeds";
import { rssPodcastId } from "./rss";
import type { Podcast } from "./types";

/* ---------- Files ---------- */

/** Save a text file: share sheet in the app, download in the browser. */
export async function exportFile(name: string, content: string, mime: string) {
  if (isNative) {
    const { uri } = await Filesystem.writeFile({ path: name, data: content, directory: Directory.Cache, encoding: Encoding.UTF8 });
    await Share.share({ title: name, files: [uri], dialogTitle: "حفظ أو مشاركة الملف" });
    return;
  }
  const url = URL.createObjectURL(new Blob([content], { type: mime }));
  const a = Object.assign(document.createElement("a"), { href: url, download: name });
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 5000);
}

const stamp = () => new Date().toISOString().slice(0, 10);

/* ---------- OPML (podcast subscriptions, understood by every podcast app) ---------- */

const esc = (s: string) => s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");

async function feedUrlOf(p: Podcast, country: string): Promise<string | undefined> {
  return p.feedUrl || feedRegistry.get(p.id) || (await lookupPodcast(p.id, country).catch(() => null))?.feedUrl;
}

export async function exportOpml(podcasts: Podcast[], country: string): Promise<number> {
  const rows: string[] = [];
  for (const p of podcasts.filter((p) => !p.id.startsWith("quran-"))) {
    const url = await feedUrlOf(p, country);
    if (url) rows.push(`    <outline type="rss" text="${esc(p.title)}" title="${esc(p.title)}" xmlUrl="${esc(url)}" />`);
  }
  const xml = `<?xml version="1.0" encoding="UTF-8"?>
<opml version="2.0">
  <head><title>صدى — اشتراكاتي</title><dateCreated>${new Date().toUTCString()}</dateCreated></head>
  <body>
${rows.join("\n")}
  </body>
</opml>
`;
  await exportFile(`sada-podcasts-${stamp()}.opml`, xml, "text/x-opml");
  return rows.length;
}

export function parseOpml(xml: string): { title: string; feedUrl: string }[] {
  const doc = new DOMParser().parseFromString(xml, "application/xml");
  if (doc.querySelector("parsererror")) throw new Error("ملف OPML غير صالح");
  return Array.from(doc.querySelectorAll("outline[xmlUrl], outline[xmlurl]"))
    .map((o) => ({
      title: o.getAttribute("text") || o.getAttribute("title") || "",
      feedUrl: (o.getAttribute("xmlUrl") || o.getAttribute("xmlurl") || "").trim(),
    }))
    .filter((o) => /^https?:\/\//i.test(o.feedUrl));
}

const sameFeed = (a?: string, b?: string) =>
  !!a && !!b && a.replace(/^https?:\/\//, "").replace(/\/$/, "").toLowerCase() === b.replace(/^https?:\/\//, "").replace(/\/$/, "").toLowerCase();

/**
 * Turn OPML entries into podcasts: prefer the matching Apple directory entry (works
 * everywhere, has artwork), otherwise follow the feed directly.
 */
export async function resolveOpml(
  entries: { title: string; feedUrl: string }[],
  country: string,
  onProgress: (done: number) => void
): Promise<Podcast[]> {
  const out: Podcast[] = [];
  let done = 0;
  const queue = [...entries];
  const worker = async () => {
    for (let e = queue.shift(); e; e = queue.shift()) {
      let podcast: Podcast | null = null;
      if (e.title) {
        const hits = await searchPodcasts(e.title, country).run(true).catch(() => [] as Podcast[]);
        podcast = hits.find((h) => sameFeed(h.feedUrl, e.feedUrl)) ?? null;
      }
      if (!podcast) {
        const id = rssPodcastId(e.feedUrl);
        feedRegistry.set(id, e.feedUrl);
        podcast = await loadFeed(e.feedUrl)
          .then((f) => f.podcast)
          .catch(() => ({ id, title: e.title || e.feedUrl, author: "", artwork: "", feedUrl: e.feedUrl }));
      }
      out.push(podcast);
      onProgress(++done);
    }
  };
  await Promise.all([worker(), worker(), worker()]);
  return out;
}

/* ---------- Full backup (favourites, progress, history, settings) ---------- */

const SKIP = new Set(["sada.lastEpisode"]);

export async function exportBackup() {
  const data: Record<string, unknown> = {};
  for (let i = 0; i < localStorage.length; i++) {
    const k = localStorage.key(i)!;
    if (k.startsWith("sada.") && !SKIP.has(k)) data[k] = JSON.parse(localStorage.getItem(k) ?? "null");
  }
  const file = { app: "sada", version: 1, createdAt: new Date().toISOString(), data };
  await exportFile(`sada-backup-${stamp()}.json`, JSON.stringify(file, null, 1), "application/json");
}

export function restoreBackup(json: string): number {
  const file = JSON.parse(json) as { app?: string; data?: Record<string, unknown> };
  if (file.app !== "sada" || !file.data) throw new Error("هذا ليس ملف نسخة احتياطية من صدى");
  const keys = Object.keys(file.data).filter((k) => k.startsWith("sada."));
  for (const k of keys) localStorage.setItem(k, JSON.stringify(file.data[k]));
  return keys.length;
}
