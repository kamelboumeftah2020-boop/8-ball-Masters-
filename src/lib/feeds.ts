import { load, save } from "./storage";

/** Podcasts added by RSS link: id -> feed URL (persisted). */
const KEY = "sada.feeds";
let feeds: Record<string, string> = load(KEY, {});

export const feedRegistry = {
  get: (id: string): string | undefined => feeds[id],
  set(id: string, url: string) {
    if (feeds[id] === url) return;
    feeds = { ...feeds, [id]: url };
    save(KEY, feeds);
  },
  all: () => feeds,
};
