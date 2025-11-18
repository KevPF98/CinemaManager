package com.cinemamanager.util.people.personalData;
import com.cinemamanager.manager.user.PersonalDataManager;
import com.cinemamanager.model.people.PersonalData;
import com.cinemamanager.util.common.ConsoleUtil;

public final class PersonalDataFactory {

    public static PersonalData createPersonalData(PersonalDataManager personalDataManager, String nationalId) {
        String name = ConsoleUtil.readValidName("first name");
        String lastName = ConsoleUtil.readValidName("last name");
        if (nationalId == null) {
            nationalId = ConsoleUtil.readUniqueNationalId(personalDataManager);
        }
        String email = ConsoleUtil.readUniqueEmail(personalDataManager);
        String phone = ConsoleUtil.readUniquePhoneNumber(personalDataManager);
        return new PersonalData(nationalId, name, lastName, email, phone, false);
    }

}
