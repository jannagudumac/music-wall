package com.musicwall.controller;

import com.musicwall.dto.MusicItemDTO;
import com.musicwall.service.MusicItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

// The controller delegates item requests to the service, which checks the wall and section.
@RestController
@RequestMapping("/api/walls/{wallId}/sections/{sectionId}/items")
public class MusicItemController {

    private final MusicItemService musicItemService;

    public MusicItemController(MusicItemService musicItemService) {
        this.musicItemService = musicItemService;
    }

    // Add a catalogue track or album with a listening status; the service supplies its title and
    // artist.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MusicItemDTO createItem(
            @PathVariable Long wallId,
            @PathVariable Long sectionId,
            Authentication authentication,
            @Valid @RequestBody MusicItemDTO request
    ) {
        return musicItemService.createItem(
                authentication.getName(),
                wallId,
                sectionId,
                request
        );
    }

    // Use the item id from the URL, not an id sent in the request body.
    @PutMapping("/{itemId}")
    public MusicItemDTO updateItem(
            @PathVariable Long wallId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            Authentication authentication,
            @Valid @RequestBody MusicItemDTO request
    ) {
        return musicItemService.updateItem(
                authentication.getName(),
                wallId,
                sectionId,
                itemId,
                request
        );
    }

    // Return HTTP 204 with no body after the service confirms deletion.
    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(
            @PathVariable Long wallId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            Authentication authentication
    ) {
        musicItemService.deleteItem(
                authentication.getName(),
                wallId,
                sectionId,
                itemId
        );
    }
}
