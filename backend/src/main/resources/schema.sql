CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- The normal catalogue is provider-independent. These idempotent statements
-- also clean databases created by the former runtime importer.
ALTER TABLE IF EXISTS artist
    DROP COLUMN IF EXISTS musicbrainz_id,
    DROP COLUMN IF EXISTS catalog_imported;

ALTER TABLE IF EXISTS album
    DROP COLUMN IF EXISTS musicbrainz_id,
    DROP COLUMN IF EXISTS musicbrainz_release_id;

ALTER TABLE IF EXISTS track
    DROP COLUMN IF EXISTS musicbrainz_id;

CREATE INDEX IF NOT EXISTS idx_artist_name_trgm
    ON artist USING gin (lower(name) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_album_title_trgm
    ON album USING gin (lower(title) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_track_title_trgm
    ON track USING gin (lower(title) gin_trgm_ops);
