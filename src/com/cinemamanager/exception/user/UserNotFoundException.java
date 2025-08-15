package com.cinemamanager.exception.user;

public final class UserNotFoundException extends Exception {
    public UserNotFoundException(String message) {
        super(message);
    }
}
