package com.musicwall.dto;

import com.musicwall.entity.Wallpaper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateMusicWallRequest {

    @NotBlank(message = "Wall name is required")
    @Size(max = 100, message = "Wall name is too long")
    private String name;

    private Wallpaper wallpaper = Wallpaper.NONE;

    @Pattern(
            regexp = "#[0-9a-fA-F]{6}",
            message = "Wall color must be a hexadecimal color"
    )
    private String wallColor = "#FFFFFF";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
