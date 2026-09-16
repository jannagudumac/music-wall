package com.musicwall.dto;

import jakarta.validation.constraints.NotBlank;

// DTO shares a username for member searches, member lists and adding a member; @NotBlank requires
// input.
public class UserDTO {

    @NotBlank(message = "Username is required")
    private String username;

    public UserDTO() {
    }

    public UserDTO(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
