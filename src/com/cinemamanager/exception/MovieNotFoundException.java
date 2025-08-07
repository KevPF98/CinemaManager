package com.cinemamanager.exception;

public final class MovieNotFoundException extends Exception {
    public MovieNotFoundException(String message) {
        super(message);
    }
}
