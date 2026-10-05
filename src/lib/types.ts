export interface Podcast {
  id: string;
  title: string;
  author: string;
  artwork: string;
  genre?: string;
  summary?: string;
  feedUrl?: string;
  episodeCount?: number;
  link?: string;
}

export interface Episode {
  id: string;
  podcastId: string;
  podcastTitle: string;
  title: string;
  description: string;
  audioUrl: string;
  artwork: string;
  releaseDate: string;
  durationMs: number;
  fileExtension?: string;
}
