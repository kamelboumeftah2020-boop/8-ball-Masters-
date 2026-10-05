import { Link } from "react-router-dom";
import type { Podcast } from "../lib/types";
import { Artwork } from "./Artwork";

export function PodcastCard({ podcast, rank }: { podcast: Podcast; rank?: number }) {
  return (
    <Link to={`/podcast/${podcast.id}`} state={{ podcast }} className="card">
      <div className="card-art">
        <Artwork src={podcast.artwork} alt={podcast.title} />
        {rank != null && <span className="rank">{rank}</span>}
      </div>
      <div className="card-title">{podcast.title}</div>
      <div className="card-sub">{podcast.author}</div>
    </Link>
  );
}

export function Shelf({ title, action, children }: { title: string; action?: React.ReactNode; children: React.ReactNode }) {
  return (
    <section className="shelf">
      <header className="section-head">
        <h2>{title}</h2>
        {action}
      </header>
      <div className="shelf-row">{children}</div>
    </section>
  );
}
