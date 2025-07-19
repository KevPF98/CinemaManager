package com.cinemamanager.util;

import com.cinemamanager.enums.Role;
import com.cinemamanager.model.people.Account;
import com.cinemamanager.model.people.PersonalData;
import com.cinemamanager.model.people.User;

public final class UserFactory {

    public static User createUser (int userId, int accountId) {
        System.out.println ("Creating a new user...");
        Account account = createAccount (accountId);
        PersonalData personalData = createPersonalData ();
        return new User (userId, account, personalData);
    }

    private static Account createAccount(int id) {
        String nickname = ConsoleUtil.readString ("Enter nickname: ");
        String password = readValidPassword ();

        Role role;
        String option;
        do {
            option = ConsoleUtil.readString ("Enter role:" +
                    "\n1) ADMIN" +
                    "\n2) EMPLOYEE." +
                    "\n> ");
            if (option.equals("1")) {
                role = Role.ADMIN;
                break;
            } else if (option.equals("2")) {
                role = Role.EMPLOYEE;
                break;
            }
            System.out.println ("Invalid option.");
        } while (true);

        return new Account(id, nickname, password, role);
    }

    private static PersonalData createPersonalData () {
        String name = ConsoleUtil.readString ("Enter first name: ");
        String lastName = ConsoleUtil.readString ("Enter last name: ");
        String nationalId = readValidNationalId ();
        String email = readValidEmail ();
        String phone = readValidPhone ();

        return new PersonalData (nationalId, name, lastName, email, phone);
    }

    private static String readValidEmail () {
        String email;
        do {
            email = ConsoleUtil.readString ("Enter email: ");
            if (!isValidEmail(email)) {
                System.out.println ("Invalid email format. Please try again.");
            }
        } while (!isValidEmail(email));
        return email;
    }

    private static String readValidNationalId () {
        String nationalId;
        do {
            nationalId = ConsoleUtil.readString ("Enter national ID (7-10 digits): ");
            if (!isValidNationalId(nationalId)) {
                System.out.println ("Invalid National Identification format. Please enter only digits, length 7 to 10.");
            }
        } while (!isValidNationalId(nationalId));
        return nationalId;
    }

    private static String readValidPassword () {
        String password;
        do {
            password = ConsoleUtil.readString ("Enter password (at least 6 characters): ");
            if (!isValidPassword(password)) {
                System.out.println ("Password too weak. Must be at least 6 characters, include uppercase, lowercase and a number.");
            }
        } while (!isValidPassword(password));
        return password;
    }

    private static String readValidPhone() {
        String phone;
        do {
            phone = ConsoleUtil.readString("Enter phone number (digits, spaces, dashes, optional '+'): ");
            if (!isValidPhone(phone)) {
                System.out.println("Invalid phone number format. Please try again.");
            }
        } while (!isValidPhone(phone));
        return phone;
    }

    private static boolean isValidEmail (String email) {
        return email.matches ("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private static boolean isValidNationalId (String nationalId) {
        return nationalId.matches ("\\d{7,10}");
    }

    private static boolean isValidPassword(String password) {
        return password.length() >= 6
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*\\d.*");
    }

    private static boolean isValidPhone(String phone) {
        return phone.matches("^\\+?[0-9\\- ]{7,15}$");
    }

}
