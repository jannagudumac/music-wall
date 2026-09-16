package com.musicwall.exception;

// Reports a rejected business rule, such as a duplicate username; the error handler returns HTTP
// 400.
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
