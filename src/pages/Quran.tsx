import { useMemo, useRef, useState } from "react";
import { Artwork } from "../components/Artwork";
import { BackButton } from "../components/BackButton";
import { DownloadButton } from "../components/DownloadButton";
import { IconDownload, IconHeart, IconPause, IconPlay, IconSearch } from "../components/Icons";
import { allSurahs, isMakki, quranArtwork, RECITERS, SURAH_NAMES } from "../lib/quran";
import type { Episode } from "../lib/types";
import { useLibrary } from "../store/library";
import { usePlayer } from "../store/player";

const FULL_SIZE = "1.4 جيجابايت";

function SurahRow({ ep, n, list }: { ep: Episode; n: number; list: Episode[] }) {
  const { current, isPlaying, play, toggle } = usePlayer();
  const { isFavEpisode, toggleFavEpisode, progress } = useLibrary();
  const isCurrent = current?.id === ep.id;
  const fav = isFavEpisode(ep.id);
  const prog = progress[ep.id];
  const pct = prog?.duration ? Math.min(1, prog.position / prog.duration) : 0;
  return (
    <div className={`surah ${isCurrent ? "current" : ""}`}>
      <span className="surah-num">{n}</span>
      <button className="surah-name" onClick={() => (isCurrent ? toggle() : play(ep, list))}>
        <strong>{ep.title}</strong>
        <small>
          {isMakki(n) ? "مكية" : "مدنية"}
          {pct > 0.02 && pct < 0.97 && <span className="mini-progress"><span style={{ width: `${pct * 100}%` }} /></span>}
          {pct >= 0.97 && " · ✓ تمّ الاستماع"}
        </small>
      </button>
      <button
        className={`icon-btn ${fav ? "fav" : ""}`}
        aria-label={fav ? "إزالة من المفضلة" : "إضافة للمفضلة"}
        aria-pressed={fav}
        onClick={() => toggleFavEpisode(ep)}
      >
        <IconHeart size={18} filled={fav} />
      </button>
      <DownloadButton episode={ep} />
      <button
        className={`surah-play ${isCurrent && isPlaying ? "active" : ""}`}
        aria-label={isCurrent && isPlaying ? "إيقاف مؤقت" : `تشغيل ${ep.title}`}
        onClick={() => (isCurrent ? toggle() : play(ep, list))}
      >
        {isCurrent && isPlaying ? <IconPause size={16} /> : <IconPlay size={16} />}
      </button>
    </div>
  );
}

export function Quran() {
  const reciter = RECITERS[0];
  const surahs = useMemo(() => allSurahs(reciter), [reciter]);
  const { play } = usePlayer();
  const { downloads, active, download, history } = useLibrary();
  const [filter, setFilter] = useState("");
  const [bulk, setBulk] = useState<{ done: number; total: number } | null>(null);
  const cancelBulk = useRef(false);

  const downloaded = surahs.filter((e) => downloads[e.id]).length;
  const lastSurah = history.find((e) => e.podcastId === surahs[0].podcastId);

  const shown = useMemo(() => {
    const f = filter.trim();
    if (!f) return surahs.map((e, i) => ({ e, n: i + 1 }));
    return surahs
      .map((e, i) => ({ e, n: i + 1 }))
      .filter(({ e, n }) => String(n) === f || SURAH_NAMES[n - 1].includes(f) || e.title.includes(f));
  }, [filter, surahs]);

  const downloadAll = async () => {
    const todo = surahs.filter((e) => !downloads[e.id] && !active[e.id]);
    if (!todo.length) return;
    if (!confirm(`سيتم تحميل ${todo.length} سورة (المصحف كاملاً قرابة ${FULL_SIZE}). يُفضّل استخدام الواي فاي. متابعة؟`)) return;
    cancelBulk.current = false;
    setBulk({ done: 0, total: todo.length });
    // Two at a time keeps the phone responsive and the CDN happy.
    const queue = [...todo];
    let done = 0;
    const worker = async () => {
      for (let e = queue.shift(); e && !cancelBulk.current; e = queue.shift()) {
        await download(e);
        setBulk({ done: ++done, total: todo.length });
      }
    };
    await Promise.all([worker(), worker()]);
    setBulk(null);
  };

  return (
    <div className="page quran-page">
      <div className="quran-hero">
        <BackButton />
        <Artwork src={quranArtwork()} alt="القرآن الكريم" className="quran-art" />
        <div className="quran-hero-text">
          <h1>القرآن الكريم</h1>
          <p>بصوت القارئ <strong>{reciter.name}</strong></p>
          <p className="muted">{reciter.rewaya} · 114 سورة</p>
        </div>
        <div className="podcast-actions">
          <button className="btn primary quran-btn" onClick={() => play(lastSurah ?? surahs[0], surahs)}>
            <IconPlay size={18} /> {lastSurah ? `متابعة: ${lastSurah.title}` : "ابدأ من الفاتحة"}
          </button>
          {bulk ? (
            <button className="btn ghost" onClick={() => (cancelBulk.current = true)}>
              جارٍ التحميل {bulk.done}/{bulk.total} · إيقاف
            </button>
          ) : downloaded < surahs.length ? (
            <button className="btn ghost" onClick={downloadAll}>
              <IconDownload size={18} /> تحميل المصحف كاملاً
            </button>
          ) : (
            <span className="badge">✓ المصحف كاملاً محمّل</span>
          )}
        </div>
        {downloaded > 0 && downloaded < surahs.length && !bulk && (
          <p className="muted small-note">محمّل {downloaded} من 114 سورة</p>
        )}
      </div>

      <div className="search-box small quran-search">
        <IconSearch size={16} />
        <input type="search" placeholder="ابحث باسم السورة أو رقمها…" value={filter} onChange={(e) => setFilter(e.target.value)} />
      </div>

      <div className="surah-list">
        {shown.map(({ e, n }) => <SurahRow key={e.id} ep={e} n={n} list={surahs} />)}
      </div>
      <p className="muted small-note center">
        التلاوات من موقع mp3quran.net
      </p>
    </div>
  );
}
