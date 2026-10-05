import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { formatClock } from "../lib/format";
import { useLibrary } from "../store/library";
import { usePlayer, type SleepTimer } from "../store/player";
import { Artwork } from "./Artwork";
import { DownloadButton } from "./DownloadButton";
import {
  IconBack, IconChevronDown, IconForward, IconHeart, IconMoon, IconNext, IconPause, IconPlay, IconPrev,
  IconQueue, IconShare, IconWifiOff,
} from "./Icons";
import { SeekBar } from "./SeekBar";
import { Spinner } from "./States";

const RATES = [0.75, 1, 1.25, 1.5, 1.75, 2];
const SLEEP_OPTIONS: { label: string; value: SleepTimer | "min"; min?: number }[] = [
  { label: "5 دقائق", value: "min", min: 5 },
  { label: "15 دقيقة", value: "min", min: 15 },
  { label: "30 دقيقة", value: "min", min: 30 },
  { label: "45 دقيقة", value: "min", min: 45 },
  { label: "ساعة", value: "min", min: 60 },
  { label: "نهاية الحلقة", value: { kind: "end" } },
];

type Panel = "none" | "sleep" | "queue";

export function FullPlayer() {
  const p = usePlayer();
  const { isFavEpisode, toggleFavEpisode } = useLibrary();
  const navigate = useNavigate();
  const [panel, setPanel] = useState<Panel>("none");
  const [, force] = useState(0);

  // Keep the sleep countdown fresh.
  useEffect(() => {
    if (p.sleep?.kind !== "at" || !p.expanded) return;
    const t = setInterval(() => force((n) => n + 1), 1000);
    return () => clearInterval(t);
  }, [p.sleep, p.expanded]);

  useEffect(() => {
    if (!p.expanded) return;
    const onKey = (e: KeyboardEvent) => e.key === "Escape" && p.setExpanded(false);
    document.addEventListener("keydown", onKey);
    document.body.classList.add("no-scroll");
    return () => {
      document.removeEventListener("keydown", onKey);
      document.body.classList.remove("no-scroll");
    };
  }, [p.expanded, p.setExpanded]);

  if (!p.current) return null;
  const ep = p.current;
  const fav = isFavEpisode(ep.id);

  const cycleRate = () => p.setRate(RATES[(RATES.indexOf(p.rate) + 1) % RATES.length] ?? 1);
  const sleepLabel =
    p.sleep?.kind === "at" ? formatClock((p.sleep.at - Date.now()) / 1000) : p.sleep?.kind === "end" ? "نهاية الحلقة" : null;

  const share = async () => {
    const text = `${ep.title} — ${ep.podcastTitle}`;
    const url = `https://podcasts.apple.com/podcast/id${ep.podcastId}?i=${ep.id}`;
    if (navigator.share) navigator.share({ title: ep.title, text, url }).catch(() => {});
    else {
      await navigator.clipboard?.writeText(`${text}\n${url}`);
      alert("تم نسخ الرابط");
    }
  };

  const upNext = (() => {
    const i = p.queue.findIndex((e) => e.id === ep.id);
    return p.queue.slice(i + 1);
  })();

  return (
    <div className={`full-player ${p.expanded ? "open" : ""}`} aria-hidden={!p.expanded} role="dialog" aria-label="المشغّل">
      <div className="fp-bg" style={{ backgroundImage: `url("${ep.artwork}")` }} />
      <div className="fp-inner">
        <header className="fp-head">
          <button className="icon-btn" aria-label="إغلاق" onClick={() => p.setExpanded(false)}>
            <IconChevronDown size={28} />
          </button>
          <div className="fp-head-title">
            {p.isOffline ? <span className="badge"><IconWifiOff size={12} /> من التحميلات</span> : "يتم التشغيل الآن"}
          </div>
          <button className="icon-btn" aria-label="مشاركة" onClick={share}>
            <IconShare size={22} />
          </button>
        </header>

        <div className={`fp-art-wrap ${p.isPlaying ? "playing" : ""}`}>
          <Artwork src={ep.artwork} alt={ep.title} className="fp-art" />
        </div>

        <div className="fp-info">
          <div className="fp-text">
            <h2 className="fp-title">{ep.title}</h2>
            <button
              className="fp-podcast link"
              onClick={() => {
                p.setExpanded(false);
                navigate(`/podcast/${ep.podcastId}`);
              }}
            >
              {ep.podcastTitle}
            </button>
          </div>
          <button className={`icon-btn ${fav ? "fav" : ""}`} aria-label="المفضلة" aria-pressed={fav} onClick={() => toggleFavEpisode(ep)}>
            <IconHeart size={26} filled={fav} />
          </button>
        </div>

        {p.error && <div className="fp-error">{p.error}</div>}

        <SeekBar />

        <div className="fp-controls">
          <button className="icon-btn" aria-label="الحلقة السابقة" onClick={p.prev}><IconPrev size={26} className="flip" /></button>
          <button className="icon-btn" aria-label="رجوع 15 ثانية" onClick={() => p.skip(-15)}><IconBack size={34} label="15" /></button>
          <button className="fp-play" aria-label={p.isPlaying ? "إيقاف مؤقت" : "تشغيل"} onClick={p.toggle}>
            {p.isLoading && p.isPlaying ? <Spinner /> : p.isPlaying ? <IconPause size={34} /> : <IconPlay size={34} />}
          </button>
          <button className="icon-btn" aria-label="تقديم 30 ثانية" onClick={() => p.skip(30)}><IconForward size={34} label="30" /></button>
          <button className="icon-btn" aria-label="الحلقة التالية" onClick={p.next} disabled={!upNext.length}><IconNext size={26} className="flip" /></button>
        </div>

        <div className="fp-tools">
          <button className="tool" onClick={cycleRate} aria-label="سرعة التشغيل">
            <strong dir="ltr">{p.rate}×</strong>
            <span>السرعة</span>
          </button>
          <button className={`tool ${p.sleep ? "on" : ""}`} onClick={() => setPanel(panel === "sleep" ? "none" : "sleep")}>
            <IconMoon size={20} />
            <span>{sleepLabel ?? "مؤقت النوم"}</span>
          </button>
          <div className="tool">
            <DownloadButton episode={ep} />
            <span>تحميل</span>
          </div>
          <button className={`tool ${panel === "queue" ? "on" : ""}`} onClick={() => setPanel(panel === "queue" ? "none" : "queue")}>
            <IconQueue size={20} />
            <span>التالي ({upNext.length})</span>
          </button>
        </div>

        {panel === "sleep" && (
          <div className="fp-panel">
            <h3>إيقاف التشغيل بعد</h3>
            <div className="chips">
              {SLEEP_OPTIONS.map((o) => (
                <button
                  key={o.label}
                  className="chip"
                  onClick={() => {
                    p.setSleep(o.value === "min" ? { kind: "at", at: Date.now() + (o.min ?? 0) * 60_000 } : o.value);
                    setPanel("none");
                  }}
                >
                  {o.label}
                </button>
              ))}
              {p.sleep && (
                <button className="chip danger" onClick={() => { p.setSleep(null); setPanel("none"); }}>إلغاء المؤقت</button>
              )}
            </div>
          </div>
        )}

        {panel === "queue" && (
          <div className="fp-panel">
            <h3>التالي في قائمة التشغيل</h3>
            {upNext.length ? (
              <ul className="queue-list">
                {upNext.slice(0, 30).map((e) => (
                  <li key={e.id}>
                    <button onClick={() => p.play(e)}>
                      <Artwork src={e.artwork} alt="" className="queue-art" />
                      <span>
                        <strong>{e.title}</strong>
                        <small>{e.podcastTitle}</small>
                      </span>
                    </button>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="muted">لا توجد حلقات تالية. أضف حلقات من زر قائمة التشغيل.</p>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
