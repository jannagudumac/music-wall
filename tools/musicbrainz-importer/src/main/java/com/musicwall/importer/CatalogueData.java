package com.musicwall.importer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class CatalogueData {

    record Artist(long id, String name) { }
    record Genre(long id, String name) { }
    record Album(long id, String title, Integer releaseYear, String coverUrl,
                 Artist artist, Set<Genre> genres) { }
    record Track(long id, String title, Integer durationSeconds, Artist artist,
                 Album album, Set<Genre> genres) { }

    private final Map<String, Artist> artists = new LinkedHashMap<>();
    private final Map<String, Genre> genres = new LinkedHashMap<>();
    private final Map<String, Album> albums = new LinkedHashMap<>();
    private final Map<String, Track> tracks = new LinkedHashMap<>();

    Artist artist(String name) {
        String cleanName = required(name);
        return artists.computeIfAbsent(key(cleanName), ignored ->
                new Artist(artists.size() + 1L, cleanName));
    }

    Set<Genre> genres(List<String> names) {
        Set<Genre> result = new LinkedHashSet<>();
        for (String name : names) {
            String cleanName = required(name);
            result.add(genres.computeIfAbsent(key(cleanName), ignored ->
                    new Genre(genres.size() + 1L, cleanName)));
        }
        return result;
    }

    Album album(String title, Integer releaseYear, String coverUrl,
                Artist artist, Set<Genre> albumGenres) {
        String cleanTitle = required(title);
        String localKey = artist.id() + "|" + key(cleanTitle);
        return albums.computeIfAbsent(localKey, ignored ->
                new Album(albums.size() + 1L, cleanTitle, releaseYear, coverUrl,
                        artist, new LinkedHashSet<>(albumGenres)));
    }

    Track track(String title, Integer durationSeconds, Artist artist,
                Album album, Set<Genre> trackGenres) {
        String cleanTitle = required(title);
        String albumKey = album == null ? "no-album" : Long.toString(album.id());
        String localKey = artist.id() + "|" + albumKey + "|" + key(cleanTitle)
                + "|" + durationSeconds;
        return tracks.computeIfAbsent(localKey, ignored ->
                new Track(tracks.size() + 1L, cleanTitle, durationSeconds, artist,
                        album, new LinkedHashSet<>(trackGenres)));
    }

    List<Artist> artists() { return new ArrayList<>(artists.values()); }
    List<Genre> genres() { return new ArrayList<>(genres.values()); }
    List<Album> albums() { return new ArrayList<>(albums.values()); }
    List<Track> tracks() { return new ArrayList<>(tracks.values()); }

    private static String key(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static String required(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Catalogue names and titles must not be blank");
        }
        return value.trim();
    }
}
