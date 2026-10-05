import { useParams } from "react-router-dom";
import { BackButton } from "../components/BackButton";
import { PodcastCard } from "../components/PodcastCard";
import { Empty, ErrorState, SkeletonGrid } from "../components/States";
import { topPodcasts } from "../lib/api";
import { COUNTRIES, genreById } from "../lib/genres";
import { useAsync } from "../lib/useAsync";
import { useLibrary } from "../store/library";

/** Top chart for a genre (/genre/:id) or overall (/top). */
export function Chart() {
  const { id } = useParams();
  const { country } = useLibrary();
  const genre = id ? genreById(id) : undefined;
  const { data, loading, error, retry } = useAsync(() => topPodcasts(country, id, 100), [country, id]);
  const countryName = COUNTRIES.find((c) => c.code === country)?.name;

  return (
    <div className="page">
      <header
        className="genre-head"
        style={genre ? ({ "--c1": genre.colors[0], "--c2": genre.colors[1] } as React.CSSProperties) : undefined}
      >
        <BackButton />
        <div>
          <h1>{genre ? `${genre.emoji} ${genre.name}` : "🏆 الأكثر استماعاً"}</h1>
          <p>أفضل 100 بودكاست في {countryName}</p>
        </div>
      </header>
      {loading ? (
        <SkeletonGrid count={12} />
      ) : error ? (
        <ErrorState onRetry={retry} error={error} />
      ) : !data?.length ? (
        <Empty title="لا توجد نتائج في هذا البلد">جرّب تغيير البلد من الصفحة الرئيسية.</Empty>
      ) : (
        <div className="grid">
          {data.map((p, i) => <PodcastCard key={p.id} podcast={p} rank={i + 1} />)}
        </div>
      )}
    </div>
  );
}
