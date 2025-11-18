package com.cinemamanager.util.people.account;

public final class AccountValidator {

    public static boolean isValidNickname(String nickname) {
        return nickname != null && !nickname.trim().isEmpty();
    }

    public static boolean isValidPassword(String password) {
        return password.length() >= 6
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*\\d.*");
    }

}
