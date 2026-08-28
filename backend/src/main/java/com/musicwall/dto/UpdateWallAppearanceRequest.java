package com.musicwall.dto;

import com.musicwall.entity.Wallpaper;
import jakarta.validation.constraints.Pattern;

public class UpdateWallAppearanceRequest {

    private Wallpaper wallpaper = Wallpaper.NONE;

    @Pattern(
            regexp = "#[0-9a-fA-F]{6}",
            message = "Wall color must be a hexadecimal color"
    )
    private String wallColor = "#FFFFFF";

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
