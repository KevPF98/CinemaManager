package com.cinemamanager.util.user;

public final class UserValidator {

    public static boolean isValidNickname(String nickname) {
        return nickname != null && !nickname.trim().isEmpty();
    }

    public static boolean isValidPassword(String password) {
        return password.length() >= 6
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*\\d.*");
    }

    public static boolean isValidName(String name) {
        return name != null && name.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{1,50}$");
    }

    public static boolean isValidNationalId (String nationalId) {
        return nationalId.matches ("\\d{7,10}");
    }

    public static boolean isValidEmail (String email) {
        return email.matches ("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public static boolean isValidPhone(String phone) {
        return phone.matches("^\\+?[0-9\\- ]{7,15}$");
    }

}
