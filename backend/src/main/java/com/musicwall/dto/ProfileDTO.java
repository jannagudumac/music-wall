package com.musicwall.dto;

import jakarta.validation.constraints.Size;

public class ProfileDTO {
    private String username;

    @Size(max = 300, message = "Bio must be at most 300 characters")
    private String bio;

    private String avatarUrl;
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
