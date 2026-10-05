import { useSearchParams } from "react-router-dom";
import { EpisodeRow } from "../components/EpisodeRow";
import { IconBell, IconHeart, IconRefresh } from "../components/Icons";
import { PodcastCard } from "../components/PodcastCard";
import { Empty } from "../components/States";
import { formatDate } from "../lib/format";
import { useLibrary } from "../store/library";
import { useSubscriptions } from "../store/subscriptions";

const TABS = [
  { id: "new", label: "جديد" },
  { id: "shows", label: "البرامج" },
  { id: "episodes", label: "حلقات مفضّلة" },
  { id: "history", label: "سجل الاستماع" },
] as const;
type TabId = (typeof TABS)[number]["id"];

export function Library() {
  const [params, setParams] = useSearchParams();
  const { favPodcasts, favEpisodes, history, clearHistory } = useLibrary();
  const subs = useSubscriptions();
  const tab = (params.get("t") as TabId) || (subs.inbox.length ? "new" : "shows");

  return (
    <div className="page">
      <header className="page-head"><h1>مكتبتي</h1></header>
      <div className="tabs" role="tablist">
        {TABS.map((t) => (
          <button
            key={t.id}
            role="tab"
            aria-selected={tab === t.id}
            className={tab === t.id ? "active" : ""}
            onClick={() => setParams({ t: t.id }, { replace: true })}
          >
            {t.label}
            {t.id === "new" && subs.inbox.length > 0 && <span className="tab-count">{subs.inbox.length}</span>}
          </button>
        ))}
      </div>

      {tab === "new" && (
        <>
          <div className="subs-card">
            <div className="subs-row">
              <span>
                <strong>التحديث التلقائي</strong>
                <small>
                  {favPodcasts.length
                    ? `يتابع ${favPodcasts.length} برنامج${subs.lastCheck ? ` · آخر فحص ${formatDate(new Date(subs.lastCheck).toISOString())} ${new Date(subs.lastCheck).toLocaleTimeString("ar-u-nu-latn", { hour: "2-digit", minute: "2-digit" })}` : ""}`
                    : "أضف برامج للمفضلة لتصلك حلقاتها الجديدة"}
                </small>
              </span>
              <button className={`icon-btn ${subs.checking ? "spinning" : ""}`} aria-label="افحص الآن" onClick={subs.checkNow} disabled={!favPodcasts.length}>
                <IconRefresh size={20} />
              </button>
            </div>
            <label className="subs-row toggle">
              <span>
                <strong>تحميل الحلقات الجديدة تلقائياً</strong>
                <small>للاستماع لاحقاً بدون إنترنت</small>
              </span>
              <input type="checkbox" checked={subs.autoDownload} onChange={(e) => subs.setAutoDownload(e.target.checked)} />
            </label>
          </div>
          {subs.inbox.length ? (
            <>
              <div className="list-head">
                <span className="muted">{subs.inbox.length} حلقة جديدة</span>
                <button className="btn ghost small" onClick={subs.clearInbox}>تمييز الكل كمُشاهد</button>
              </div>
              <div className="episode-list">
                {subs.inbox.map((e) => <EpisodeRow key={e.id} episode={e} queue={subs.inbox} showArtwork showPodcast />)}
              </div>
            </>
          ) : (
            <Empty icon={<IconBell size={40} />} title="لا توجد حلقات جديدة">
              عند نزول حلقة جديدة من برامجك المفضلة ستظهر هنا، وستصلك إشعارات بها.
            </Empty>
          )}
        </>
      )}

      {tab === "shows" &&
        (favPodcasts.length ? (
          <div className="grid">{favPodcasts.map((p) => <PodcastCard key={p.id} podcast={p} />)}</div>
        ) : (
          <Empty icon={<IconHeart size={40} />} title="لا توجد برامج مفضّلة بعد">
            اضغط «أضف للمفضلة» في صفحة أي بودكاست ليظهر هنا.
          </Empty>
        ))}

      {tab === "episodes" &&
        (favEpisodes.length ? (
          <div className="episode-list">
            {favEpisodes.map((e) => <EpisodeRow key={e.id} episode={e} queue={favEpisodes} showArtwork showPodcast />)}
          </div>
        ) : (
          <Empty icon={<IconHeart size={40} />} title="لا توجد حلقات مفضّلة">
            اضغط على القلب بجانب أي حلقة لحفظها هنا.
          </Empty>
        ))}

      {tab === "history" &&
        (history.length ? (
          <>
            <div className="list-head">
              <span className="muted">{history.length} حلقة</span>
              <button className="btn ghost small" onClick={() => confirm("مسح سجل الاستماع؟") && clearHistory()}>
                مسح السجل
              </button>
            </div>
            <div className="episode-list">
              {history.map((e) => <EpisodeRow key={e.id} episode={e} showArtwork showPodcast />)}
            </div>
          </>
        ) : (
          <Empty icon="🎧" title="لم تستمع لأي حلقة بعد" />
        ))}
    </div>
  );
}
