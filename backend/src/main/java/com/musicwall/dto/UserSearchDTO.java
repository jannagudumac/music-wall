package com.musicwall.dto;

public class UserSearchDTO {
    private String username;

    public UserSearchDTO() {
    }

    public UserSearchDTO(String username) {
        this.username = username;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
