import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { EpisodeRow } from "../components/EpisodeRow";
import { IconSearch, IconX } from "../components/Icons";
import { PodcastCard } from "../components/PodcastCard";
import { Empty, ErrorState, Loading, SkeletonGrid } from "../components/States";
import { searchEpisodes, searchPodcasts } from "../lib/api";
import { GENRES } from "../lib/genres";
import { useQuery } from "../lib/useAsync";
import { useLibrary } from "../store/library";

type Tab = "podcasts" | "episodes";

function Results({ q, tab }: { q: string; tab: Tab }) {
  const { country } = useLibrary();
  const pods = useQuery(() => searchPodcasts(tab === "podcasts" ? q : "", country), [q, tab, country]);
  const eps = useQuery(() => searchEpisodes(tab === "episodes" ? q : "", country), [q, tab, country]);

  if (tab === "podcasts") {
    if (pods.loading) return <SkeletonGrid />;
    if (pods.error) return <ErrorState onRetry={pods.retry} error={pods.error} />;
    if (!pods.data?.length) return <Empty icon="🔎" title="لا توجد نتائج">جرّب كلمات أخرى أو ابحث بالإنجليزية.</Empty>;
    return <div className="grid">{pods.data.map((p) => <PodcastCard key={p.id} podcast={p} />)}</div>;
  }
  if (eps.loading) return <Loading />;
  if (eps.error) return <ErrorState onRetry={eps.retry} error={eps.error} />;
  if (!eps.data?.length) return <Empty icon="🔎" title="لا توجد حلقات مطابقة" />;
  return (
    <div className="episode-list">
      {eps.data.map((e) => <EpisodeRow key={e.id} episode={e} queue={eps.data} showArtwork showPodcast />)}
    </div>
  );
}

export function Explore() {
  const [params, setParams] = useSearchParams();
  const q = params.get("q") ?? "";
  const tab = (params.get("t") as Tab) || "podcasts";
  const [input, setInput] = useState(q);

  // Debounce typing into the URL.
  useEffect(() => {
    const t = setTimeout(() => {
      const next = new URLSearchParams(params);
      if (input.trim()) next.set("q", input.trim());
      else next.delete("q");
      if (next.toString() !== params.toString()) setParams(next, { replace: true });
    }, 400);
    return () => clearTimeout(t);
  }, [input]); // eslint-disable-line react-hooks/exhaustive-deps

  const setTab = (t: Tab) => {
    const next = new URLSearchParams(params);
    next.set("t", t);
    setParams(next, { replace: true });
  };

  return (
    <div className="page">
      <header className="page-head"><h1>استكشاف</h1></header>
      <form className="search-box" role="search" onSubmit={(e) => e.preventDefault()}>
        <IconSearch size={20} />
        <input
          type="search"
          placeholder="ابحث عن بودكاست، مقدّم، أو موضوع…"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          autoFocus={!q}
          enterKeyHint="search"
        />
        {input && (
          <button type="button" className="icon-btn" aria-label="مسح" onClick={() => setInput("")}>
            <IconX size={18} />
          </button>
        )}
      </form>

      {q ? (
        <>
          <div className="tabs" role="tablist">
            <button role="tab" aria-selected={tab === "podcasts"} className={tab === "podcasts" ? "active" : ""} onClick={() => setTab("podcasts")}>البرامج</button>
            <button role="tab" aria-selected={tab === "episodes"} className={tab === "episodes" ? "active" : ""} onClick={() => setTab("episodes")}>الحلقات</button>
          </div>
          <Results q={q} tab={tab} />
        </>
      ) : (
        <>
          <h2 className="section-title">تصفّح حسب التصنيف</h2>
          <div className="genre-grid">
            {GENRES.map((g) => (
              <Link
                key={g.id}
                to={`/genre/${g.id}`}
                className="genre-tile"
                style={{ "--c1": g.colors[0], "--c2": g.colors[1] } as React.CSSProperties}
              >
                <span>{g.name}</span>
                <span className="genre-emoji">{g.emoji}</span>
              </Link>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
