package com.cinemamanager.auth;

import com.cinemamanager.model.people.User;

public class Session {
    private static User activeUser;

    public static void startSession(User user) {
        activeUser = user;
    }

    public static User getActiveUser() {
        return activeUser;
    }

    public static void endSession() {
        activeUser = null;
    }

    public static boolean isSessionActive() {
        return activeUser != null;
    }
}
