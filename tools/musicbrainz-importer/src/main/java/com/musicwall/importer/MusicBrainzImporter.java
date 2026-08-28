package com.musicwall.importer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Development-only utility that transforms selected MusicBrainz data into a
 * provider-independent PostgreSQL seed. It is not part of the Spring backend.
 */
public final class MusicBrainzImporter {

    record CuratedArtist(String name, String musicBrainzId, int maxAlbums) { }

    private static final String DEFAULT_BASE_URL = "https://musicbrainz.org";
    private static final long REQUEST_INTERVAL_MILLISECONDS = 1_100L;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String baseUrl;
    private final String userAgent;
    private long lastRequestTime;

    MusicBrainzImporter(String baseUrl, String userAgent) {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.baseUrl = baseUrl;
        this.userAgent = userAgent;
    }

    public static void main(String[] args) throws Exception {
        Path artistsFile = args.length > 0 ? Path.of(args[0]) : Path.of("artists.json");
        Path outputFile = args.length > 1
                ? Path.of(args[1])
                : Path.of("../../database/catalogue_seed.sql");
        String userAgent = System.getenv("MUSICBRAINZ_USER_AGENT");
        if (userAgent == null || userAgent.isBlank()) {
            throw new IllegalArgumentException(
                    "Set MUSICBRAINZ_USER_AGENT to an identifiable application/contact value"
            );
        }

        MusicBrainzImporter importer = new MusicBrainzImporter(DEFAULT_BASE_URL, userAgent);
        CatalogueData catalogue = importer.importArtists(importer.readArtists(artistsFile));
        new SqlSeedWriter().write(catalogue, outputFile);
        System.out.printf(
                "Wrote %d artists, %d albums, %d tracks and %d genres to %s%n",
                catalogue.artists().size(), catalogue.albums().size(),
                catalogue.tracks().size(), catalogue.genres().size(), outputFile.toAbsolutePath()
        );
    }

    List<CuratedArtist> readArtists(Path path) throws IOException {
        return objectMapper.readValue(path.toFile(), new TypeReference<>() { });
    }

    CatalogueData importArtists(List<CuratedArtist> configuredArtists) throws Exception {
        CatalogueData catalogue = new CatalogueData();
        for (CuratedArtist configured : configuredArtists) {
            importArtist(configured, catalogue);
        }
        return catalogue;
    }

    private void importArtist(CuratedArtist configured, CatalogueData catalogue) throws Exception {
        CatalogueData.Artist artist = catalogue.artist(configured.name());
        JsonNode response = get("/ws/2/release-group?artist="
                + encode(configured.musicBrainzId()) + "&fmt=json&limit=100");

        for (JsonNode summary : selectAlbumGroups(response, configured.maxAlbums())) {
            String releaseGroupId = text(summary, "id");
            if (releaseGroupId == null) continue;
            try {
                importReleaseGroup(releaseGroupId, artist, catalogue);
            } catch (IOException exception) {
                System.err.println("Skipped " + configured.name() + " / "
                        + text(summary, "title") + ": " + exception.getMessage());
            }
        }
    }

    private void importReleaseGroup(String releaseGroupId, CatalogueData.Artist artist,
                                    CatalogueData catalogue) throws Exception {
        JsonNode group = get("/ws/2/release-group/" + encode(releaseGroupId)
                + "?fmt=json&inc=releases%2Bgenres");
        JsonNode releaseSummary = firstOfficialRelease(group.path("releases"));
        if (releaseSummary == null) throw new IOException("no release was available");

        String releaseId = text(releaseSummary, "id");
        JsonNode release = get("/ws/2/release/" + encode(releaseId)
                + "?fmt=json&inc=recordings%2Bartist-credits%2Brelease-groups");
        if (!hasTracks(release)) throw new IOException("the selected release had no tracks");

        Set<CatalogueData.Genre> genres = catalogue.genres(topGenres(group.path("genres")));
        CatalogueData.Album album = catalogue.album(
                text(group, "title"),
                parseYear(text(group, "first-release-date")),
                "https://coverartarchive.org/release-group/" + releaseGroupId + "/front-250",
                artist,
                genres
        );

        for (JsonNode medium : iterable(release.path("media"))) {
            for (JsonNode trackNode : iterable(medium.path("tracks"))) {
                JsonNode recording = trackNode.path("recording");
                String title = text(trackNode, "title");
                if (title == null) title = text(recording, "title");
                if (title == null) continue;
                JsonNode length = trackNode.path("length");
                if (!length.canConvertToInt()) length = recording.path("length");
                Integer duration = length.canConvertToInt() ? length.asInt() / 1_000 : null;
                catalogue.track(title, duration, artist, album, genres);
            }
        }
    }

    static List<JsonNode> selectAlbumGroups(JsonNode response, int maxAlbums) {
        List<JsonNode> groups = new ArrayList<>();
        for (JsonNode group : iterable(response.path("release-groups"))) {
            String primaryType = text(group, "primary-type");
            if (!"Album".equalsIgnoreCase(primaryType)
                    && !"EP".equalsIgnoreCase(primaryType)) continue;
            if (hasExcludedSecondaryType(group.path("secondary-types"))) continue;
            groups.add(group);
        }
        groups.sort(Comparator
                .comparing((JsonNode node) -> text(node, "first-release-date"),
                        Comparator.nullsLast(String::compareTo))
                .thenComparing(node -> text(node, "title"),
                        Comparator.nullsLast(String::compareToIgnoreCase)));
        return groups.stream().limit(Math.max(0, maxAlbums)).toList();
    }

    static String normalizeGenre(String value) {
        if (value == null || value.isBlank()) return null;
        String clean = value.trim().toLowerCase(Locale.ROOT);
        return switch (clean) {
            case "classical", "modern classical", "romanticism", "neoclassicism" -> "Classical";
            case "progressive rock", "prog rock", "art rock" -> "Progressive Rock";
            case "hard rock" -> "Hard Rock";
            case "heavy metal", "metal" -> "Metal";
            case "progressive metal" -> "Progressive Metal";
            case "electronic", "electronica" -> "Electronic";
            case "synth-pop", "synthpop" -> "Synth-pop";
            case "folk rock" -> "Folk Rock";
            case "indie rock", "alternative rock" -> "Alternative Rock";
            case "jazz" -> "Jazz";
            case "funk" -> "Funk";
            case "folk" -> "Folk";
            case "celtic" -> "Celtic";
            case "pop", "italian pop" -> "Pop";
            case "ambient" -> "Ambient";
            case "world", "world music" -> "World";
            case "african" -> "African";
            default -> Character.toUpperCase(clean.charAt(0)) + clean.substring(1);
        };
    }

    private List<String> topGenres(JsonNode genreNodes) {
        List<JsonNode> sorted = new ArrayList<>();
        iterable(genreNodes).forEach(sorted::add);
        sorted.sort(Comparator.comparingInt(node -> -node.path("count").asInt(0)));
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (JsonNode node : sorted.stream().limit(3).toList()) {
            String name = normalizeGenre(text(node, "name"));
            if (name != null) names.add(name);
        }
        return new ArrayList<>(names);
    }

    private synchronized JsonNode get(String pathAndQuery) throws Exception {
        long remaining = REQUEST_INTERVAL_MILLISECONDS
                - (System.currentTimeMillis() - lastRequestTime);
        if (remaining > 0) Thread.sleep(remaining);

        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + pathAndQuery))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .header("User-Agent", userAgent)
                .GET()
                .build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } finally {
            lastRequestTime = System.currentTimeMillis();
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("MusicBrainz returned HTTP " + response.statusCode());
        }
        return objectMapper.readTree(response.body());
    }

    private static JsonNode firstOfficialRelease(JsonNode releasesNode) {
        List<JsonNode> releases = new ArrayList<>();
        iterable(releasesNode).forEach(releases::add);
        releases.sort(Comparator
                .comparing((JsonNode node) -> !"Official".equalsIgnoreCase(text(node, "status")))
                .thenComparing(node -> text(node, "date"), Comparator.nullsLast(String::compareTo)));
        return releases.isEmpty() ? null : releases.get(0);
    }

    private static boolean hasTracks(JsonNode release) {
        for (JsonNode medium : iterable(release.path("media"))) {
            if (medium.path("tracks").isArray() && !medium.path("tracks").isEmpty()) return true;
        }
        return false;
    }

    private static boolean hasExcludedSecondaryType(JsonNode types) {
        for (JsonNode type : iterable(types)) {
            String value = type.asText("");
            if ("Compilation".equalsIgnoreCase(value)
                    || "DJ-mix".equalsIgnoreCase(value)
                    || "Mixtape/Street".equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    private static Iterable<JsonNode> iterable(JsonNode value) {
        return value != null && value.isArray() ? value : List.of();
    }

    private static String text(JsonNode node, String field) {
        if (node == null || node.path(field).isMissingNode() || node.path(field).isNull()) return null;
        String value = node.path(field).asText().trim();
        return value.isEmpty() ? null : value;
    }

    private static Integer parseYear(String date) {
        if (date == null || date.length() < 4) return null;
        try {
            return Integer.valueOf(date.substring(0, 4));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
