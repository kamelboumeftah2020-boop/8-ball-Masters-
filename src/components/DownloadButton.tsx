import type { Episode } from "../lib/types";
import { useLibrary } from "../store/library";
import { IconCheck, IconDownload, IconX } from "./Icons";

export function DownloadButton({ episode, withLabel }: { episode: Episode; withLabel?: boolean }) {
  const { downloads, active, download, cancelDownload, removeDownload } = useLibrary();
  const done = !!downloads[episode.id];
  const job = active[episode.id];

  if (done) {
    return (
      <button
        className="icon-btn done"
        title="محمّلة — اضغط للحذف"
        aria-label="حذف التحميل"
        onClick={(e) => {
          e.stopPropagation();
          if (confirm("حذف هذه الحلقة من التحميلات؟")) removeDownload(episode.id);
        }}
      >
        <IconCheck size={18} />
        {withLabel && <span>محمّلة</span>}
      </button>
    );
  }

  if (job && !job.error) {
    const r = 9;
    const c = 2 * Math.PI * r;
    return (
      <button
        className="icon-btn progress"
        title="إلغاء التحميل"
        aria-label={`جارٍ التحميل ${Math.round(job.progress * 100)}%`}
        onClick={(e) => {
          e.stopPropagation();
          cancelDownload(episode.id);
        }}
      >
        <svg width="26" height="26" viewBox="0 0 24 24" className={job.progress ? "" : "indeterminate"}>
          <circle cx="12" cy="12" r={r} className="ring-bg" />
          <circle
            cx="12" cy="12" r={r} className="ring"
            strokeDasharray={c}
            strokeDashoffset={job.progress ? c * (1 - job.progress) : c * 0.75}
          />
        </svg>
        <IconX size={10} className="ring-x" />
      </button>
    );
  }

  return (
    <button
      className={`icon-btn ${job?.error ? "failed" : ""}`}
      title={job?.error ?? "تحميل"}
      aria-label="تحميل الحلقة"
      onClick={(e) => {
        e.stopPropagation();
        download(episode);
      }}
    >
      <IconDownload size={18} />
      {withLabel && <span>{job?.error ? "إعادة المحاولة" : "تحميل"}</span>}
    </button>
  );
}
