package com.cinemamanager.util.people.user;

import com.cinemamanager.enums.user.Role;
import com.cinemamanager.manager.user.PersonalDataManager;
import com.cinemamanager.manager.user.UserManager;
import com.cinemamanager.model.people.Account;
import com.cinemamanager.model.people.PersonalData;
import com.cinemamanager.model.people.User;
import com.cinemamanager.util.common.ConsoleUtil;
import com.cinemamanager.util.people.personalData.PersonalDataFactory;

public final class UserFactory {

    public static User createNewUser(int userId, UserManager userManager, PersonalDataManager personalDataManager) {
        return createUserInternal(userId, userManager, personalDataManager, null, null);
    }

    public static User createUserWithNationalId(int userId, UserManager userManager, PersonalDataManager personalDataManager, String nationalId) {
        return createUserInternal(userId, userManager, personalDataManager, nationalId, null);
    }

    public static User createUserWithCompleteData(int userId, UserManager userManager, PersonalDataManager personalDataManager, String nationalId, PersonalData personalData) {
        return createUserInternal(userId, userManager, personalDataManager, nationalId, personalData);
    }

    public static User createUserFromPersonalData(int userId, UserManager userManager, PersonalDataManager personalDataManager, PersonalData personalData) {
        return createUserInternal(userId, userManager, personalDataManager, null, personalData);
    }

    private static User createUserInternal(int userId, UserManager userManager, PersonalDataManager personalDataManager, String nationalId, PersonalData personalData) {

        Account account = createAccount(userManager);
        if (personalData == null) {
            personalData = PersonalDataFactory.createPersonalData(personalDataManager, nationalId);
        }

        return new User(userId, account, personalData);
    }

    private static Account createAccount(UserManager userManager) {
        String nickname = ConsoleUtil.readUniqueNickname(userManager);
        String password = ConsoleUtil.readValidPassword("password");
        return new Account(nickname, password, Role.EMPLOYEE);
    }

}
