import { useMemo, useState } from "react";
import { useLocation, useParams } from "react-router-dom";
import { Artwork } from "../components/Artwork";
import { BackButton } from "../components/BackButton";
import { EpisodeRow } from "../components/EpisodeRow";
import { IconHeart, IconPlay, IconSearch } from "../components/Icons";
import { Empty, ErrorState, Loading } from "../components/States";
import { podcastWithEpisodes } from "../lib/api";
import type { Podcast } from "../lib/types";
import { isRssId } from "../lib/rss";
import { useQuery } from "../lib/useAsync";
import { useLibrary } from "../store/library";
import { usePlayer } from "../store/player";

const PAGE = 50;

export function PodcastPage() {
  const { id = "" } = useParams();
  const preview = (useLocation().state as { podcast?: Podcast } | null)?.podcast;
  const { country, isFavPodcast, toggleFavPodcast, favPodcasts, download, downloads } = useLibrary();
  const { play } = usePlayer();
  // Apple's newest 200 first; the show's full RSS history only when asked for.
  const [feedFor, setFeedFor] = useState<string | null>(null);
  const withFeed = feedFor === id;
  const { data, loading, error, retry, refreshing } = useQuery(
    () => podcastWithEpisodes(id, country, 200, withFeed),
    [id, country, withFeed]
  );
  const [lastData, setLastData] = useState<typeof data>(undefined);
  if (data && data !== lastData) setLastData(data);
  const shownData = data ?? (withFeed ? lastData : undefined);
  const [filter, setFilter] = useState("");
  const [oldest, setOldest] = useState(false);
  const [showAbout, setShowAbout] = useState(false);
  const [limit, setLimit] = useState(PAGE);

  // Fall back to whatever we already know while loading (from the card or favorites).
  const podcast: Podcast | undefined =
    (shownData?.podcast && { ...preview, ...shownData.podcast, summary: preview?.summary ?? shownData.podcast.summary }) ??
    preview ??
    favPodcasts.find((p) => p.id === id);

  const episodes = useMemo(() => {
    let list = shownData?.episodes ?? [];
    if (filter.trim()) {
      const f = filter.trim().toLowerCase();
      list = list.filter((e) => e.title.toLowerCase().includes(f) || e.description.toLowerCase().includes(f));
    }
    return oldest ? [...list].reverse() : list;
  }, [shownData, filter, oldest]);

  const fav = isFavPodcast(id);

  return (
    <div className="page podcast-page">
      <div className="podcast-hero">
        {podcast?.artwork && <div className="podcast-hero-bg" style={{ backgroundImage: `url("${podcast.artwork}")` }} />}
        <BackButton />
        <Artwork src={podcast?.artwork} alt={podcast?.title ?? ""} className="podcast-art" />
        <h1>{podcast?.title ?? "…"}</h1>
        <p className="podcast-author">{podcast?.author}</p>
        <div className="podcast-tags">
          {podcast?.genre && <span className="badge">{podcast.genre}</span>}
          {shownData && <span className="badge">{shownData.episodes.length} حلقة</span>}
          {shownData?.episodes.some((e) => e.mediaType === "video") && <span className="badge">🎬 فيديو</span>}
        </div>
        <div className="podcast-actions">
          <button
            className="btn primary"
            disabled={!shownData?.episodes.length}
            onClick={() => shownData && play(shownData.episodes[0], shownData.episodes)}
          >
            <IconPlay size={18} /> أحدث حلقة
          </button>
          <button
            className={`btn ${fav ? "fav-on" : "ghost"}`}
            disabled={!podcast}
            onClick={() => podcast && toggleFavPodcast(podcast)}
            aria-pressed={fav}
          >
            <IconHeart size={18} filled={fav} /> {fav ? "في المفضلة" : "أضف للمفضلة"}
          </button>
        </div>
        {podcast?.summary && (
          <p className={`podcast-summary ${showAbout ? "open" : ""}`} onClick={() => setShowAbout((s) => !s)}>
            {podcast.summary}
          </p>
        )}
      </div>

      {loading && !shownData ? (
        <Loading />
      ) : error ? (
        <ErrorState onRetry={retry} error={error} />
      ) : !shownData?.episodes.length ? (
        <Empty icon="🎙️" title="لا توجد حلقات متاحة">قد يكون هذا البودكاست غير متوفر في بلدك المختار.</Empty>
      ) : (
        <>
          <div className="episodes-toolbar">
            <div className="search-box small">
              <IconSearch size={16} />
              <input type="search" placeholder="ابحث في الحلقات…" value={filter} onChange={(e) => setFilter(e.target.value)} />
            </div>
            <button className="chip" onClick={() => setOldest((o) => !o)}>{oldest ? "الأقدم أولاً" : "الأحدث أولاً"}</button>
            <button
              className="chip"
              title="تحميل آخر 5 حلقات"
              onClick={() => shownData.episodes.slice(0, 5).filter((e) => !downloads[e.id]).forEach(download)}
            >
              تحميل آخر 5
            </button>
          </div>
          <div className="episode-list">
            {episodes.slice(0, limit).map((e) => <EpisodeRow key={e.id} episode={e} queue={episodes} />)}
            {!episodes.length && <Empty title="لا توجد حلقات مطابقة" />}
          </div>
          {!withFeed && !isRssId(id) && (shownData?.episodes.length ?? 0) >= 200 && episodes.length <= limit && (
            <div className="load-more">
              <button className="btn ghost" onClick={() => setFeedFor(id)}>
                عرض كل الحلقات الأقدم
              </button>
            </div>
          )}
          {withFeed && (loading || refreshing) && (
            <div className="load-more"><span className="muted">جارٍ جلب كل الحلقات من خلاصة البرنامج…</span></div>
          )}
          {episodes.length > limit && (
            <div className="load-more">
              <button className="btn ghost" onClick={() => setLimit((l) => l + PAGE)}>
                عرض المزيد ({episodes.length - limit} حلقة أخرى)
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
