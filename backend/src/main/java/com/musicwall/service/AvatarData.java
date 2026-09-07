package com.musicwall.service;

public class AvatarData {

    private final byte[] image;
    private final String contentType;

    public AvatarData(byte[] image, String contentType) {
        this.image = image;
        this.contentType = contentType;
    }

    public byte[] getImage() {
        return image;
    }

    public String getContentType() {
        return contentType;
    }
}
