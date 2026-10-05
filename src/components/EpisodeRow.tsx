import { memo, useState } from "react";
import { Link } from "react-router-dom";
import { formatDate, formatDuration } from "../lib/format";
import type { Episode } from "../lib/types";
import { useLibrary } from "../store/library";
import { usePlayer } from "../store/player";
import { Artwork } from "./Artwork";
import { DownloadButton } from "./DownloadButton";
import { IconHeart, IconPause, IconPlay, IconQueue, IconWifiOff } from "./Icons";

interface Props {
  episode: Episode;
  /** Episodes to queue after this one when played. */
  queue?: Episode[];
  showArtwork?: boolean;
  showPodcast?: boolean;
}

export const EpisodeRow = memo(function EpisodeRow({ episode, queue, showArtwork, showPodcast }: Props) {
  const { current, isPlaying, play, toggle, addToQueue } = usePlayer();
  const { isFavEpisode, toggleFavEpisode, progress, active } = useLibrary();
  const [open, setOpen] = useState(false);

  const isCurrent = current?.id === episode.id;
  const playingNow = isCurrent && isPlaying;
  const prog = progress[episode.id];
  const pct = prog?.duration ? Math.min(1, prog.position / prog.duration) : 0;
  const finished = pct > 0.97;
  const remaining = prog && !finished && prog.position > 5 ? (prog.duration - prog.position) * 1000 : 0;
  const fav = isFavEpisode(episode.id);
  const error = active[episode.id]?.error;

  return (
    <article className={`episode ${isCurrent ? "current" : ""}`}>
      {showArtwork && <Artwork src={episode.artwork} alt="" className="episode-art" />}
      <div className="episode-body">
        <div className="episode-meta">
          {formatDate(episode.releaseDate)}
          {showPodcast && (
            <>
              {" · "}
              <Link to={`/podcast/${episode.podcastId}`} className="link">{episode.podcastTitle}</Link>
            </>
          )}
        </div>
        <button className="episode-title" onClick={() => setOpen((o) => !o)} aria-expanded={open}>
          {episode.title}
        </button>
        {episode.description && (
          <p className={`episode-desc ${open ? "open" : ""}`} onClick={() => setOpen((o) => !o)}>
            {episode.description}
          </p>
        )}
        {error && (
          <p className="episode-error">
            <IconWifiOff size={14} /> {error} —{" "}
            <a href={episode.audioUrl} target="_blank" rel="noreferrer" className="link">فتح الملف</a>
          </p>
        )}
        <div className="episode-actions">
          <button
            className={`play-pill ${playingNow ? "active" : ""}`}
            onClick={() => (isCurrent ? toggle() : play(episode, queue))}
            aria-label={playingNow ? "إيقاف مؤقت" : "تشغيل"}
          >
            {playingNow ? <IconPause size={14} /> : <IconPlay size={14} />}
            {finished ? (
              <span>تمّ الاستماع</span>
            ) : remaining ? (
              <>
                <span className="mini-progress"><span style={{ width: `${pct * 100}%` }} /></span>
                <span>متبقٍ {formatDuration(remaining)}</span>
              </>
            ) : (
              <span>{formatDuration(episode.durationMs) || "تشغيل"}</span>
            )}
          </button>
          <div className="spacer" />
          <button className="icon-btn" aria-label="إضافة لقائمة التشغيل" title="إضافة لقائمة التشغيل" onClick={() => addToQueue(episode)}>
            <IconQueue size={18} />
          </button>
          <button
            className={`icon-btn ${fav ? "fav" : ""}`}
            aria-label={fav ? "إزالة من المفضلة" : "إضافة للمفضلة"}
            aria-pressed={fav}
            onClick={() => toggleFavEpisode(episode)}
          >
            <IconHeart size={18} filled={fav} />
          </button>
          <DownloadButton episode={episode} />
        </div>
      </div>
    </article>
  );
});
