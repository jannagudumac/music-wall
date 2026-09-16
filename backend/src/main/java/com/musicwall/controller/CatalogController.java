package com.musicwall.controller;

import com.musicwall.dto.AlbumDTO;
import com.musicwall.dto.ArtistDTO;
import com.musicwall.dto.ArtistDetailDTO;
import com.musicwall.dto.CatalogSearchDTO;
import com.musicwall.dto.CatalogSuggestionDTO;
import com.musicwall.dto.TrackDTO;
import com.musicwall.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// The controller receives catalogue requests and delegates searches and details to the service.
@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    // Read the search text from the URL; an empty query displays the full catalogue.
    @GetMapping("/search")
    public CatalogSearchDTO search(
            @RequestParam(defaultValue = "") String query
    ) {
        return catalogService.search(query);
    }

    // Return a short suggestion list for autocomplete, not the full search results.
    @GetMapping("/suggestions")
    public List<CatalogSuggestionDTO> suggestions(
            @RequestParam String query
    ) {
        return catalogService.getSuggestions(query);
    }

    @GetMapping("/artists/{id}")
    public ArtistDetailDTO getArtist(@PathVariable Long id) {
        return catalogService.getArtist(id);
    }

    @GetMapping("/albums/{id}")
    public AlbumDTO getAlbum(@PathVariable Long id) {
        return catalogService.getAlbum(id);
    }

    @GetMapping("/tracks/{id}")
    public TrackDTO getTrack(@PathVariable Long id) {
        return catalogService.getTrack(id);
    }

}
