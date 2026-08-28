package com.musicwall.importer;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CatalogueDataTest {

    @Test
    void deduplicatesLocallyWithoutTreatingArtistAndTitleAsUniversalTrackIdentity() {
        CatalogueData data = new CatalogueData();
        var artist = data.artist("Example Artist");
        var album = data.album("Studio", 2000, null, artist, Set.of());
        var liveAlbum = data.album("Live", 2002, null, artist, Set.of());

        var studioTrack = data.track("Same title", 180, artist, album, Set.of());
        var duplicateStudioTrack = data.track(" same TITLE ", 180, artist, album, Set.of());
        var liveTrack = data.track("Same title", 230, artist, liveAlbum, Set.of());

        assertEquals(studioTrack.id(), duplicateStudioTrack.id());
        assertNotEquals(studioTrack.id(), liveTrack.id());
        assertEquals(2, data.tracks().size());
    }

    @Test
    void escapesSqlText() {
        assertEquals("'Rock ''n'' Roll'", SqlSeedWriter.literal("Rock 'n' Roll"));
        assertEquals("NULL", SqlSeedWriter.literal(null));
    }
}
