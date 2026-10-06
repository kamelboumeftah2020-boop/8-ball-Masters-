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
  /** "video" for video podcasts; audio otherwise. */
  mediaType?: "audio" | "video";
  /** Podcasting 2.0 JSON chapters file. */
  chaptersUrl?: string;
  /** Inline chapters (Podlove Simple Chapters in the RSS feed). */
  chapters?: Chapter[];
}

export interface Chapter {
  /** Start time in seconds. */
  start: number;
  title: string;
  img?: string;
}
