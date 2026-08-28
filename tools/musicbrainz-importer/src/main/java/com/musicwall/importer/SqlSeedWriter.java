package com.musicwall.importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class SqlSeedWriter {

    void write(CatalogueData catalogue, Path output) throws IOException {
        List<String> sql = new ArrayList<>();
        sql.add("-- Generated catalogue reference data only.");
        sql.add("-- Load into an empty Music Wall catalogue schema.");
        sql.add("BEGIN;");

        for (var artist : catalogue.artists()) {
            sql.add("INSERT INTO artist (id, name) VALUES (" + artist.id() + ", "
                    + literal(artist.name()) + ");");
        }
        for (var genre : catalogue.genres()) {
            sql.add("INSERT INTO genre (id, name) VALUES (" + genre.id() + ", "
                    + literal(genre.name()) + ");");
        }
        for (var album : catalogue.albums()) {
            sql.add("INSERT INTO album (id, title, release_year, cover_url, artist_id) VALUES ("
                    + album.id() + ", " + literal(album.title()) + ", "
                    + number(album.releaseYear()) + ", " + literal(album.coverUrl()) + ", "
                    + album.artist().id() + ");");
        }
        for (var track : catalogue.tracks()) {
            sql.add("INSERT INTO track (id, title, duration_seconds, artist_id, album_id) VALUES ("
                    + track.id() + ", " + literal(track.title()) + ", "
                    + number(track.durationSeconds()) + ", " + track.artist().id() + ", "
                    + (track.album() == null ? "NULL" : track.album().id()) + ");");
        }
        for (var album : catalogue.albums()) {
            for (var genre : album.genres()) {
                sql.add("INSERT INTO album_genre (album_id, genre_id) VALUES ("
                        + album.id() + ", " + genre.id() + ");");
            }
        }
        for (var track : catalogue.tracks()) {
            for (var genre : track.genres()) {
                sql.add("INSERT INTO track_genre (track_id, genre_id) VALUES ("
                        + track.id() + ", " + genre.id() + ");");
            }
        }

        addSequenceReset(sql, "artist", catalogue.artists().size());
        addSequenceReset(sql, "genre", catalogue.genres().size());
        addSequenceReset(sql, "album", catalogue.albums().size());
        addSequenceReset(sql, "track", catalogue.tracks().size());
        sql.add("COMMIT;");
        sql.add("");

        Path parent = output.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        Files.write(output, sql, StandardCharsets.UTF_8);
    }

    static String literal(String value) {
        return value == null ? "NULL" : "'" + value.replace("'", "''") + "'";
    }

    private static String number(Integer value) {
        return value == null ? "NULL" : value.toString();
    }

    private static void addSequenceReset(List<String> sql, String table, int count) {
        if (count > 0) {
            sql.add("SELECT setval(pg_get_serial_sequence('" + table + "', 'id'), "
                    + "(SELECT max(id) FROM " + table + "));" );
        }
    }
}
