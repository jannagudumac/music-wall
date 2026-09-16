package com.musicwall.exception;

// Reports a missing resource or wrong parent id; the error handler returns HTTP 404.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
