package com.musicwall.service;

// Carries the image bytes and file type from the service to the controller.
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
