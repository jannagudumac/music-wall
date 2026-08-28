package com.musicwall.dto;

import jakarta.validation.constraints.Size;

public class UpdateProfileDTO {
    @Size(max = 240, message = "Bio must be at most 240 characters")
    private String bio;

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
