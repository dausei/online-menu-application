package com.onlinemenu.api.common.exception;

/** Thrown when a request conflicts with existing data (e.g. duplicate slug). Mapped to HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
