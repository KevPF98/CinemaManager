package com.cinemamanager.auth;

import com.cinemamanager.manager.UserManager;
import com.cinemamanager.model.people.User;

import java.util.Optional;

public final class LoginService {

    private final UserManager userManager;

    public LoginService(UserManager userManager) {
        this.userManager = userManager;
    }

    public boolean login(String nickname, String password) {

        Optional<User> optionalUser = userManager.findAllUsers().stream()
                .filter(u -> u.getAccount().getNickname().equals(nickname)
                        && u.getAccount().getPassword().equals(password))
                .findFirst();

        if (optionalUser.isEmpty()) return false;

        User user = optionalUser.get();

        if (!user.getAccount().isActive()) {
            System.out.println("Login failed: account is inactive.");
            return false;
        }

        Session.startSession(user);
        return true;
    }

    public void logout() {
        Session.endSession();
    }

    public boolean isSessionActive() {
        return Session.isSessionActive();
    }

    public User getActiveUser() {
        return Session.getActiveUser();
    }

}
