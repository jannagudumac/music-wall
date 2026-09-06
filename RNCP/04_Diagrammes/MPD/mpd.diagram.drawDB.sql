CREATE TABLE IF NOT EXISTS "app_user" (
	"id" BIGINT NOT NULL,
	"avatar_content_type" VARCHAR(255),
	"avatar_image" BYTEA,
	"bio" VARCHAR(300),
	"password" VARCHAR(255) NOT NULL,
	"role" VARCHAR(255) NOT NULL,
	"username" VARCHAR(255) NOT NULL,
	PRIMARY KEY("id"),
	CONSTRAINT "uk3k4cplvh82srueuttfkwnylq0" UNIQUE ("username")
);

CREATE TABLE IF NOT EXISTS "artist" (
	"id" BIGINT NOT NULL,
	"name" VARCHAR(150) NOT NULL,
	PRIMARY KEY("id"),
	CONSTRAINT "uk_artist_name" UNIQUE ("name")
);

CREATE TABLE IF NOT EXISTS "genre" (
	"id" BIGINT NOT NULL,
	"name" VARCHAR(80) NOT NULL,
	PRIMARY KEY("id"),
	CONSTRAINT "uk_genre_name" UNIQUE ("name")
);

CREATE TABLE IF NOT EXISTS "music_wall" (
	"id" BIGINT NOT NULL,
	"name" VARCHAR(100) NOT NULL,
	"wall_color" VARCHAR(7),
	"wallpaper" VARCHAR(20),
	"owner_id" BIGINT NOT NULL,
	PRIMARY KEY("id")
);

CREATE TABLE IF NOT EXISTS "wall_members" (
	"wall_id" BIGINT NOT NULL,
	"user_id" BIGINT NOT NULL,
	PRIMARY KEY("wall_id", "user_id")
);

CREATE TABLE IF NOT EXISTS "music_section" (
	"id" BIGINT NOT NULL,
	"name" VARCHAR(80) NOT NULL,
	"note_color" VARCHAR(20),
	"wall_id" BIGINT NOT NULL,
	PRIMARY KEY("id")
);

CREATE TABLE IF NOT EXISTS "album" (
	"id" BIGINT NOT NULL,
	"cover_url" VARCHAR(500),
	"release_year" INTEGER,
	"title" VARCHAR(180) NOT NULL,
	"artist_id" BIGINT NOT NULL,
	PRIMARY KEY("id"),
	CONSTRAINT "uk_album_artist_title" UNIQUE ("artist_id", "title")
);

CREATE TABLE IF NOT EXISTS "track" (
	"id" BIGINT NOT NULL,
	"duration_seconds" INTEGER,
	"title" VARCHAR(180) NOT NULL,
	"album_id" BIGINT,
	"artist_id" BIGINT NOT NULL,
	PRIMARY KEY("id")
);

CREATE TABLE IF NOT EXISTS "album_genre" (
	"album_id" BIGINT NOT NULL,
	"genre_id" BIGINT NOT NULL,
	PRIMARY KEY("album_id", "genre_id")
);

CREATE TABLE IF NOT EXISTS "track_genre" (
	"track_id" BIGINT NOT NULL,
	"genre_id" BIGINT NOT NULL,
	PRIMARY KEY("track_id", "genre_id")
);

CREATE TABLE IF NOT EXISTS "music_item" (
	"id" BIGINT NOT NULL,
	"artist" VARCHAR(150) NOT NULL,
	"item_type" VARCHAR(255) NOT NULL,
	"status" VARCHAR(255) NOT NULL,
	"title" VARCHAR(180) NOT NULL,
	"catalog_album_id" BIGINT,
	"catalog_track_id" BIGINT,
	"section_id" BIGINT NOT NULL,
	PRIMARY KEY("id")
);

ALTER TABLE "music_wall"
ADD FOREIGN KEY("owner_id") REFERENCES "app_user"("id")
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE "wall_members"
ADD FOREIGN KEY("user_id") REFERENCES "app_user"("id")
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE "wall_members"
ADD FOREIGN KEY("wall_id") REFERENCES "music_wall"("id")
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE "music_section"
ADD FOREIGN KEY("wall_id") REFERENCES "music_wall"("id")
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE "album"
ADD FOREIGN KEY("artist_id") REFERENCES "artist"("id")
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE "track"
ADD FOREIGN KEY("album_id") REFERENCES "album"("id")
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE "track"
ADD FOREIGN KEY("artist_id") REFERENCES "artist"("id")
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE "album_genre"
ADD FOREIGN KEY("album_id") REFERENCES "album"("id")
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE "album_genre"
ADD FOREIGN KEY("genre_id") REFERENCES "genre"("id")
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE "track_genre"
ADD FOREIGN KEY("genre_id") REFERENCES "genre"("id")
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE "track_genre"
ADD FOREIGN KEY("track_id") REFERENCES "track"("id")
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE "music_item"
ADD FOREIGN KEY("catalog_album_id") REFERENCES "album"("id")
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE "music_item"
ADD FOREIGN KEY("section_id") REFERENCES "music_section"("id")
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE "music_item"
ADD FOREIGN KEY("catalog_track_id") REFERENCES "track"("id")
ON UPDATE NO ACTION ON DELETE NO ACTION;