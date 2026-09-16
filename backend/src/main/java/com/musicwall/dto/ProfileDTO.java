package com.musicwall.dto;

import jakarta.validation.constraints.Size;

// DTO receives biography edits and returns profile details; username and avatar URL are not
// editable here.
public class ProfileDTO {
    private String username;

    // The biography is optional; @Size limits its length when text is supplied.
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
