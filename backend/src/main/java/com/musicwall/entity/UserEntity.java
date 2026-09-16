package com.musicwall.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// JPA stores registered accounts in app_user; API responses exclude passwords.
@Entity
@Table(name = "app_user")
public class UserEntity {

    // The database generates the id when an account is saved.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Require a unique username to distinguish accounts.
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    // Store a BCrypt password hash, never the plain password.
    private String password;

    @Column(nullable = false)
    private String role;


    @Column(length = 300)
    private String bio;

    // PostgreSQL BYTEA stores the uploaded image bytes; the file type is saved separately.
    @Column(columnDefinition = "bytea")
    private byte[] avatarImage;

    private String avatarContentType;

    public UserEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public byte[] getAvatarImage() { return avatarImage; }
    public void setAvatarImage(byte[] avatarImage) { this.avatarImage = avatarImage; }
    public String getAvatarContentType() { return avatarContentType; }
    public void setAvatarContentType(String avatarContentType) { this.avatarContentType = avatarContentType; }
}
