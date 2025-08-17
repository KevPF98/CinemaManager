package com.cinemamanager.util.user;

import com.cinemamanager.enums.user.Role;
import com.cinemamanager.manager.user.UserManager;
import com.cinemamanager.model.people.Account;
import com.cinemamanager.model.people.PersonalData;
import com.cinemamanager.model.people.User;
import com.cinemamanager.util.common.ConsoleUtil;

public final class UserFactory {

    public static User createUser(int userId, UserManager userManager) {
        return createUserInternal(userId, userManager, null);
    }

    public static User createUser(int userId, UserManager userManager, String nationalId) {
        return createUserInternal(userId, userManager, nationalId);
    }

    private static User createUserInternal(int userId, UserManager userManager, String nationalId) {
        System.out.println("\nCreating a new user...\n");

        Account account = createAccount(userManager);
        PersonalData personalData = createPersonalData(userManager, nationalId);

        return new User(userId, account, personalData);
    }

    private static Account createAccount(UserManager userManager) {
        String nickname = ConsoleUtil.readUniqueNickname(userManager);
        String password = ConsoleUtil.readValidPassword("password");
        return new Account(nickname, password, Role.EMPLOYEE);
    }

    private static PersonalData createPersonalData(UserManager userManager, String nationalId) {
        String name = ConsoleUtil.readValidName("first name");
        String lastName = ConsoleUtil.readValidName("last name");
        if (nationalId == null) {
            nationalId = ConsoleUtil.readUniqueNationalId(userManager);
        }
        String email = ConsoleUtil.readUniqueEmail(userManager);
        String phone = ConsoleUtil.readUniquePhoneNumber(userManager);
        return new PersonalData(nationalId, name, lastName, email, phone, false);
    }

}
