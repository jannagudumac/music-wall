// Keep background names consistent with the backend enum and available images.
export type WallWallpaper =
  'NONE' |
  'IMAGE_1' |
  'IMAGE_2' |
  'IMAGE_3' |
  'IMAGE_4' |
  'IMAGE_5' |
  'IMAGE_6' |
  'IMAGE_7' |
  'IMAGE_8' |
  'IMAGE_9';

// The backend supplies id and ownerUsername; requests send only editable wall fields.
export interface MusicWall {
  id: number;
  name: string;
  ownerUsername: string;
  wallpaper: WallWallpaper;
  wallColor: string;
}

export type MusicItemType = 'TRACK' | 'ALBUM';
export type ListeningStatus = 'TO_LISTEN' | 'LISTENED';
export type SectionNoteColor = 'CREAM' | 'ROSE' | 'PEACH' | 'MINT' | 'SKY' | 'LAVENDER';

// The backend supplies title/artist/type and links the item to a catalogue track or album.
export interface MusicItem {
  id: number;
  title: string;
  artist: string;
  itemType: MusicItemType;
  status: ListeningStatus;
  catalogTrackId: number | null;
  catalogAlbumId: number | null;
}

export interface MusicSection {
  id: number;
  name: string;
  noteColor: SectionNoteColor;
  items: MusicItem[];
}

// Wall details include sections; wall list responses leave them out.
export interface MusicWallDetail extends MusicWall {
  sections: MusicSection[];
}

export interface User {
  username: string;
}
