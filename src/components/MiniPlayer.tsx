import { usePlayer, usePlayerTime } from "../store/player";
import { Artwork } from "./Artwork";
import { IconForward, IconPause, IconPlay } from "./Icons";
import { Spinner } from "./States";

export function MiniPlayer() {
  const { current, isPlaying, isLoading, toggle, skip, setExpanded } = usePlayer();
  const { position, duration } = usePlayerTime();
  if (!current) return null;
  const pct = duration ? (position / duration) * 100 : 0;

  return (
    <div className="mini-player" onClick={() => setExpanded(true)} role="button" aria-label="فتح المشغّل">
      <div className="mini-progress-bar"><span style={{ width: `${pct}%` }} /></div>
      <Artwork src={current.artwork} alt="" className="mini-art" />
      <div className="mini-text">
        <div className="mini-title">{current.title}</div>
        <div className="mini-sub">{current.podcastTitle}</div>
      </div>
      <button
        className="icon-btn big"
        aria-label={isPlaying ? "إيقاف مؤقت" : "تشغيل"}
        onClick={(e) => {
          e.stopPropagation();
          toggle();
        }}
      >
        {isLoading && isPlaying ? <Spinner small /> : isPlaying ? <IconPause size={26} /> : <IconPlay size={26} />}
      </button>
      <button
        className="icon-btn"
        aria-label="تقديم 30 ثانية"
        onClick={(e) => {
          e.stopPropagation();
          skip(30);
        }}
      >
        <IconForward size={26} />
      </button>
    </div>
  );
}
