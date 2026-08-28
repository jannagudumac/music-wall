export interface Artist {
  id: number | null;
  name: string;
}

export interface Genre {
  id: number | null;
  name: string;
}

export interface Album {
  id: number;
  title: string;
  releaseYear: number | null;
  coverUrl: string | null;
  artistId: number | null;
  artistName: string;
  genres: Genre[];
  tracks: Track[];
}

export interface Track {
  id: number | null;
  title: string;
  durationSeconds: number | null;
  artistId: number | null;
  artistName: string;
  albumId: number | null;
  albumTitle: string | null;
  albumCoverUrl: string | null;
  genres: Genre[];
}

export interface ArtistDetail {
  artist: Artist;
  albums: Album[];
  tracks: Track[];
}

export interface CatalogSearchResult {
  artists: Artist[];
  albums: Album[];
  tracks: Track[];
  genres: Genre[];
  warnings: string[];
}

export interface CatalogSuggestion {
  id: string;
  type: 'ARTIST' | 'ALBUM' | 'TRACK';
  title: string;
  subtitle: string;
}
