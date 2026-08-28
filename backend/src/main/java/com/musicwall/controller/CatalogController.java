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

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/search")
    public CatalogSearchDTO search(
            @RequestParam(defaultValue = "") String query
    ) {
        return catalogService.search(query);
    }

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
