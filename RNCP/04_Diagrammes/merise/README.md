# Merise diagrams — Music Wall

These diagrams are derived directly from the JPA entities currently present in
the backend. They do not represent DTOs, JWTs, CORS settings, or external
services because those elements are not part of the persisted business data
model.

## Files

- `mcd-music-wall.mmd`: Conceptual Data Model (MCD), with entities,
  associations, and Merise cardinalities;
- `mld-music-wall.mmd`: Logical Data Model (MLD), with primary keys, foreign
  keys, and junction tables, but without PostgreSQL-specific data types;
- `mpd-music-wall.mmd`: Physical Data Model (MPD), matching the tables created
  by JPA/Hibernate in PostgreSQL, including types, sizes, and nullability.

Each Mermaid source is rendered as both PNG and SVG. SVG is recommended for the
project report because it remains sharp when resized.

## Naming and correspondence with the project

The diagrams use the exact persisted table and column names from the current
project. No email, display name, invitation, friendship, or administrator field
has been added.

JPA converts Java camelCase fields to the corresponding database column names:

| Java field | Persisted column |
|---|---|
| `avatarImage` | `avatar_image` |
| `avatarContentType` | `avatar_content_type` |
| `wallColor` | `wall_color` |
| `noteColor` | `note_color` |
| `itemType` | `item_type` |
| `catalogTrack` | `catalog_track_id` |
| `catalogAlbum` | `catalog_album_id` |
| `releaseYear` | `release_year` |
| `coverUrl` | `cover_url` |
| `durationSeconds` | `duration_seconds` |

The remaining names (`id`, `username`, `password`, `role`, `bio`, `name`,
`wallpaper`, `title`, `artist`, and `status`) are persisted without a naming
change.

## Business rules supporting the cardinalities

1. A user may own zero to many walls; each wall has exactly one owner.
2. A user may participate in zero to many walls, and a wall may have zero to
   many members. The owner is not duplicated in the member collection.
3. A wall may contain zero to many sections; each section belongs to exactly
   one wall.
4. A section may contain zero to many music items; each item belongs to exactly
   one section.
5. Every album and every track has exactly one artist.
6. A track may exist without an album; an album may contain multiple tracks.
7. Albums and tracks may each be associated with multiple genres.
8. An item added to a wall references exactly one catalogue track or one
   catalogue album. Both foreign-key columns are nullable at database level,
   but `MusicItemService` enforces this XOR rule.

## Conversion from MCD to MLD

The many-to-many associations become the `wall_members`, `album_genre`, and
`track_genre` junction tables. One-to-many associations become foreign keys on
the many side: `owner_id`, `wall_id`, `section_id`, and `artist_id`. The
optional track-to-album association becomes the nullable `track.album_id`
foreign key.

## Constraints represented in the model

- `app_user.username`, `artist.name`, and `genre.name` are unique;
- the pair `(album.artist_id, album.title)` is unique;
- the three junction tables use composite primary keys;
- `music_item.item_type` stores `TRACK` or `ALBUM`;
- `music_item.status` stores `TO_LISTEN` or `LISTENED`;
- passwords are stored as BCrypt hashes, never as plaintext.

## PostgreSQL-specific MPD details

- entity identifiers use identity-generated `BIGINT` columns;
- the avatar is stored in a `BYTEA` column;
- `schema.sql` enables the `pg_trgm` extension;
- three GIN indexes accelerate searches on `lower(artist.name)`,
  `lower(album.title)`, and `lower(track.title)`;
- Java enum values are persisted as strings.
