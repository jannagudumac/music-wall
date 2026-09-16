package com.musicwall.exception;
// Reports a missing permission; the error handler returns HTTP 403.
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) { super(message); }
}
