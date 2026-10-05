import { useSearchParams } from "react-router-dom";
import { EpisodeRow } from "../components/EpisodeRow";
import { IconHeart } from "../components/Icons";
import { PodcastCard } from "../components/PodcastCard";
import { Empty } from "../components/States";
import { useLibrary } from "../store/library";

const TABS = [
  { id: "shows", label: "البرامج" },
  { id: "episodes", label: "حلقات مفضّلة" },
  { id: "history", label: "سجل الاستماع" },
] as const;
type TabId = (typeof TABS)[number]["id"];

export function Library() {
  const [params, setParams] = useSearchParams();
  const tab = (params.get("t") as TabId) || "shows";
  const { favPodcasts, favEpisodes, history, clearHistory } = useLibrary();

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
          </button>
        ))}
      </div>

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
