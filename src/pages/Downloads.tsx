import { useEffect, useMemo, useState } from "react";
import { EpisodeRow } from "../components/EpisodeRow";
import { IconDownloadsNav } from "../components/Icons";
import { Empty } from "../components/States";
import { formatBytes } from "../lib/format";
import { useLibrary } from "../store/library";

export function Downloads() {
  const { downloads, active, removeDownload } = useLibrary();
  const [quota, setQuota] = useState<{ usage: number; quota: number } | null>(null);

  const list = useMemo(
    () => Object.values(downloads).sort((a, b) => b.savedAt - a.savedAt),
    [downloads]
  );
  const episodes = useMemo(() => list.map((d) => d.episode), [list]);
  const total = list.reduce((s, d) => s + d.size, 0);
  const inProgress = Object.entries(active).filter(([, a]) => !a.error).length;

  useEffect(() => {
    navigator.storage?.estimate?.().then((e) => setQuota({ usage: e.usage ?? 0, quota: e.quota ?? 0 })).catch(() => {});
  }, [downloads]);

  return (
    <div className="page">
      <header className="page-head"><h1>التحميلات</h1></header>

      <div className="storage-card">
        <div>
          <strong>{list.length}</strong>
          <span>حلقة محمّلة</span>
        </div>
        <div>
          <strong>{formatBytes(total)}</strong>
          <span>المساحة المستخدمة</span>
        </div>
        {quota?.quota ? (
          <div>
            <strong>{formatBytes(Math.max(0, quota.quota - quota.usage))}</strong>
            <span>المساحة المتاحة</span>
          </div>
        ) : null}
        {inProgress > 0 && <p className="muted full">جارٍ تحميل {inProgress} حلقة…</p>}
      </div>

      {list.length ? (
        <>
          <div className="list-head">
            <span className="muted">تعمل هذه الحلقات بدون إنترنت</span>
            <button
              className="btn ghost small"
              onClick={() => confirm("حذف كل التحميلات؟") && list.forEach((d) => removeDownload(d.episode.id))}
            >
              حذف الكل
            </button>
          </div>
          <div className="episode-list">
            {episodes.map((e) => <EpisodeRow key={e.id} episode={e} queue={episodes} showArtwork showPodcast />)}
          </div>
        </>
      ) : (
        <Empty icon={<IconDownloadsNav size={40} />} title="لا توجد تحميلات">
          حمّل الحلقات بزر التحميل لتستمع إليها في أي مكان، حتى بدون إنترنت.
        </Empty>
      )}
    </div>
  );
}
