package com.musicwall.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MusicBrainzImporterTest {

    @Test
    void keepsAlbumsAndEpsButExcludesCompilations() throws Exception {
        String json = """
                {
                  "release-groups": [
                    {"id":"album-1","title":"First","primary-type":"Album","first-release-date":"1970-01-01"},
                    {"id":"single-1","title":"Single","primary-type":"Single","first-release-date":"1969-01-01"},
                    {"id":"compilation-1","title":"Best Of","primary-type":"Album","secondary-types":["Compilation"]},
                    {"id":"ep-1","title":"Small","primary-type":"EP","first-release-date":"1971-01-01"}
                  ]
                }
                """;

        var groups = MusicBrainzImporter.selectAlbumGroups(
                new ObjectMapper().readTree(json), 5
        );

        assertEquals(2, groups.size());
        assertEquals("album-1", groups.get(0).path("id").asText());
        assertEquals("ep-1", groups.get(1).path("id").asText());
    }

    @Test
    void normalizesGenresWithoutPersistingProviderMetadata() {
        assertEquals("Classical", MusicBrainzImporter.normalizeGenre("modern classical"));
        assertEquals("Progressive Rock", MusicBrainzImporter.normalizeGenre("prog rock"));
        assertEquals("Jazz", MusicBrainzImporter.normalizeGenre("jazz"));
    }
}
