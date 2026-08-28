package com.musicwall.dto;
public class WallMemberDTO {
    private String username;
    public WallMemberDTO() {}
    public WallMemberDTO(String username) {
        this.username = username;
    }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
