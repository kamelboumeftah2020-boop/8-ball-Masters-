import { useEffect, useMemo, useRef } from "react";
import { allSurahs, quranArtwork, RECITERS } from "../lib/quran";
import type { Episode } from "../lib/types";
import { isNative, MediaPlayback, type AutoSection } from "../native";
import { useLibrary } from "../store/library";
import { usePlayer } from "../store/player";
import { useSubscriptions } from "../store/subscriptions";

/**
 * Publishes the library to Android Auto (via the native media browser service) and
 * starts playback when an item is picked on the car screen.
 */
export function AutoBridge() {
  const { history, progress, downloads, favEpisodes } = useLibrary();
  const { inbox } = useSubscriptions();
  const { play } = usePlayer();

  const sections = useMemo(() => {
    const cont = history.filter((e) => {
      const p = progress[e.id];
      return p && p.position > 5 && p.position < p.duration * 0.97;
    });
    const list: { id: string; title: string; episodes: Episode[]; artwork?: string }[] = [
      { id: "continue", title: "تابع الاستماع", episodes: cont.slice(0, 30) },
      { id: "new", title: "جديد من مفضلتك", episodes: inbox.slice(0, 50) },
      { id: "downloads", title: "التحميلات", episodes: Object.values(downloads).sort((a, b) => b.savedAt - a.savedAt).map((d) => d.episode) },
      { id: "favorites", title: "حلقات مفضّلة", episodes: favEpisodes.slice(0, 50) },
      ...RECITERS.map((r) => ({ id: `quran-${r.id}`, title: `القرآن الكريم · ${r.name}`, episodes: allSurahs(r), artwork: quranArtwork() })),
    ];
    return list.filter((s) => s.episodes.length);
  }, [history, progress, inbox, downloads, favEpisodes]);

  const sectionsRef = useRef(sections);
  sectionsRef.current = sections;

  // Push a compact snapshot (debounced: progress changes often while playing).
  const snapshot = useMemo<AutoSection[]>(
    () =>
      sections.map((s) => ({
        id: s.id,
        title: s.title,
        artwork: s.artwork ?? s.episodes[0]?.artwork,
        items: s.episodes.map((e) => ({ id: e.id, title: e.title, subtitle: e.podcastTitle, artwork: e.artwork })),
      })),
    [sections]
  );
  const key = JSON.stringify(snapshot);
  useEffect(() => {
    if (!isNative) return;
    const t = setTimeout(() => MediaPlayback.setLibrary({ sections: JSON.parse(key) }).catch(() => {}), 1500);
    return () => clearTimeout(t);
  }, [key]);

  useEffect(() => {
    if (!isNative) return;
    const handle = MediaPlayback.addListener("action", (e) => {
      if (e.action !== "playid" || !e.mediaId) return;
      const [sectionId, ...rest] = e.mediaId.split("|");
      const episodeId = rest.join("|");
      const section = sectionsRef.current.find((s) => s.id === sectionId);
      const ep = section?.episodes.find((x) => x.id === episodeId);
      if (ep) play(ep, section!.episodes);
    });
    return () => {
      handle.then((h) => h.remove()).catch(() => {});
    };
  }, [play]);

  return null;
}
