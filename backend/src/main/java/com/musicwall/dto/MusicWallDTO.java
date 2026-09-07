package com.musicwall.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.musicwall.entity.Wallpaper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class MusicWallDTO {

    private Long id;

    @NotBlank(message = "Wall name is required")
    @Size(max = 100, message = "Wall name is too long")
    private String name;

    private String ownerUsername;

    private Wallpaper wallpaper = Wallpaper.NONE;

    @Pattern(
            regexp = "#[0-9a-fA-F]{6}",
            message = "Wall color must be a hexadecimal color"
    )
    private String wallColor = "#FFFFFF";

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<MusicSectionDTO> sections;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public Wallpaper getWallpaper() {
        return wallpaper;
    }

    public void setWallpaper(Wallpaper wallpaper) {
        this.wallpaper = wallpaper;
    }

    public String getWallColor() {
        return wallColor;
    }

    public void setWallColor(String wallColor) {
        this.wallColor = wallColor;
    }

    public List<MusicSectionDTO> getSections() {
        return sections;
    }

    public void setSections(List<MusicSectionDTO> sections) {
        this.sections = sections;
    }
}
