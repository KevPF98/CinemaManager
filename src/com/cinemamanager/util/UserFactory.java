package com.cinemamanager.util;

import com.cinemamanager.enums.Role;
import com.cinemamanager.manager.UserManager;
import com.cinemamanager.model.people.Account;
import com.cinemamanager.model.people.PersonalData;
import com.cinemamanager.model.people.User;

public final class UserFactory {

    public static User createUser (int userId, UserManager userManager) {
        System.out.println ("Creating a new user...");
        Account account = createAccount (userManager);
        PersonalData personalData = createPersonalData (userManager);
        return new User (userId, account, personalData);
    }

    private static Account createAccount(UserManager userManager) {
        String nickname = ConsoleUtil.readUniqueNickname(userManager);
        String password = ConsoleUtil.readValidPassword ("password");
        return new Account(nickname, password, Role.EMPLOYEE);
    }

    private static PersonalData createPersonalData (UserManager userManager) {
        String name = ConsoleUtil.readValidName ("first name");
        String lastName = ConsoleUtil.readValidName ("last name");
        String nationalId = ConsoleUtil.readUniqueNationalId (userManager);
        String email = ConsoleUtil.readUniqueEmail (userManager);
        String phone = ConsoleUtil.readUniquePhoneNumber (userManager);
        return new PersonalData (nationalId, name, lastName, email, phone, false);
    }

}
