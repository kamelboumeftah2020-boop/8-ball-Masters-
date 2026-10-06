import { Link } from "react-router-dom";
import { Artwork } from "../components/Artwork";
import { EpisodeRow } from "../components/EpisodeRow";
import { IconGear, IconPlay, IconRefresh } from "../components/Icons";
import { PodcastCard, Shelf } from "../components/PodcastCard";
import { ErrorState } from "../components/States";
import { topPodcasts } from "../lib/api";
import { formatDuration } from "../lib/format";
import { COUNTRIES, GENRES, genreById } from "../lib/genres";
import { triggerRefresh } from "../lib/refresh";
import { useQuery } from "../lib/useAsync";
import { useLibrary } from "../store/library";
import { usePlayer } from "../store/player";
import { useSubscriptions } from "../store/subscriptions";

function greeting() {
  const h = new Date().getHours();
  if (h < 5) return "سهرة ممتعة";
  if (h < 12) return "صباح الخير";
  if (h < 18) return "مساء النور";
  return "مساء الخير";
}

const FEATURED_GENRES = ["1324", "1303", "1304", "1314", "1321", "1318", "1488", "1489"];

function GenreShelf({ genreId, country }: { genreId: string; country: string }) {
  const g = genreById(genreId)!;
  const { data, loading } = useQuery(() => topPodcasts(country, genreId, 20), [country, genreId]);
  if (!loading && !data?.length) return null;
  return (
    <Shelf title={`${g.emoji} ${g.name}`} action={<Link to={`/genre/${genreId}`} className="see-all">عرض الكل</Link>}>
      {loading
        ? Array.from({ length: 6 }, (_, i) => <div key={i} className="card skeleton-card"><div className="skeleton square" /><div className="skeleton line" /></div>)
        : data!.map((p) => <PodcastCard key={p.id} podcast={p} />)}
    </Shelf>
  );
}

function NewEpisodes() {
  const { inbox } = useSubscriptions();
  if (!inbox.length) return null;
  const shown = inbox.slice(0, 3);
  return (
    <section className="new-episodes">
      <header className="section-head">
        <h2>🆕 جديد من مفضلتك <span className="count-pill">{inbox.length}</span></h2>
        <Link to="/library?t=new" className="see-all">عرض الكل</Link>
      </header>
      <div className="episode-list">
        {shown.map((e) => <EpisodeRow key={e.id} episode={e} queue={inbox} showArtwork showPodcast />)}
      </div>
    </section>
  );
}

function ContinueListening() {
  const { history, progress } = useLibrary();
  const { play } = usePlayer();
  const items = history
    .filter((e) => {
      const p = progress[e.id];
      return p && p.position > 5 && p.position < p.duration * 0.97;
    })
    .slice(0, 10);
  if (!items.length) return null;
  return (
    <Shelf title="تابع الاستماع">
      {items.map((e) => {
        const p = progress[e.id];
        return (
          <button key={e.id} className="continue-card" onClick={() => play(e)}>
            <Artwork src={e.artwork} alt="" className="continue-art" />
            <span className="continue-text">
              <strong>{e.title}</strong>
              <small>متبقٍ {formatDuration((p.duration - p.position) * 1000)}</small>
              <span className="mini-progress"><span style={{ width: `${(p.position / p.duration) * 100}%` }} /></span>
            </span>
            <span className="continue-play"><IconPlay size={16} /></span>
          </button>
        );
      })}
    </Shelf>
  );
}

export function Home() {
  const { country } = useLibrary();
  const top = useQuery(() => topPodcasts(country, undefined, 50), [country]);
  const { checking } = useSubscriptions();
  const refreshing = top.refreshing || checking;
  const hero = top.data?.[0];

  return (
    <div className="page">
      <header className="page-head">
        <div>
          <p className="muted">{greeting()} 👋</p>
          <h1 className="brand">صدى</h1>
        </div>
        <div className="head-actions">
        <button
          className={`icon-btn ${refreshing ? "spinning" : ""}`}
          aria-label="تحديث"
          title="تحديث"
          onClick={triggerRefresh}
        >
          <IconRefresh size={20} />
        </button>
        <Link to="/settings" className="icon-btn" aria-label="الإعدادات" title="الإعدادات">
          <IconGear size={20} />
        </Link>
        </div>
      </header>

      {hero && (
        <Link to={`/podcast/${hero.id}`} state={{ podcast: hero }} className="hero">
          <div className="hero-bg" style={{ backgroundImage: `url("${hero.artwork}")` }} />
          <Artwork src={hero.artwork} alt={hero.title} className="hero-art" />
          <div className="hero-text">
            <span className="badge">🔥 الأول في {COUNTRIES.find((c) => c.code === country)?.name}</span>
            <h2>{hero.title}</h2>
            <p>{hero.author}</p>
            {hero.summary && <p className="hero-summary">{hero.summary}</p>}
          </div>
        </Link>
      )}

      <Link to="/quran" className="quran-card">
        <img src="quran-cover.png" alt="" />
        <span>
          <strong>القرآن الكريم</strong>
          <small>المصحف كاملاً بصوت القارئ عبدالله الخلف</small>
        </span>
        <span className="quran-card-go">استمع</span>
      </Link>

      <div className="chips scroll">
        {GENRES.map((g) => (
          <Link key={g.id} to={`/genre/${g.id}`} className="chip">
            {g.emoji} {g.name}
          </Link>
        ))}
      </div>

      <NewEpisodes />

      <ContinueListening />

      {top.error ? (
        <ErrorState onRetry={top.retry} error={top.error} />
      ) : (
        <Shelf title="🏆 الأكثر استماعاً" action={<Link to="/top" className="see-all">عرض الكل</Link>}>
          {top.loading
            ? Array.from({ length: 6 }, (_, i) => <div key={i} className="card skeleton-card"><div className="skeleton square" /><div className="skeleton line" /></div>)
            : top.data!.slice(1, 20).map((p, i) => <PodcastCard key={p.id} podcast={p} rank={i + 2} />)}
        </Shelf>
      )}

      {FEATURED_GENRES.map((id) => (
        <GenreShelf key={id} genreId={id} country={country} />
      ))}
    </div>
  );
}
