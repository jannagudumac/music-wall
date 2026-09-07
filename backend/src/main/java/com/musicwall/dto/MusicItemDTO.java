package com.musicwall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MusicItemDTO {

    private Long id;
    private String title;
    private String artist;
    private String itemType;

    @NotBlank(message = "Listening status is required")
    @Pattern(regexp = "TO_LISTEN|LISTENED", message = "Invalid listening status")
    private String status;

    private Long catalogTrackId;
    private Long catalogAlbumId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public Long getCatalogTrackId() { return catalogTrackId; }
    public void setCatalogTrackId(Long catalogTrackId) { this.catalogTrackId = catalogTrackId; }
    public Long getCatalogAlbumId() { return catalogAlbumId; }
    public void setCatalogAlbumId(Long catalogAlbumId) { this.catalogAlbumId = catalogAlbumId; }
}
