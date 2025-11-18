package com.cinemamanager.manager.user;
import com.cinemamanager.model.people.PersonalData;
import com.cinemamanager.util.common.ConsoleUtil;
import com.cinemamanager.util.common.JsonUtil;
import com.cinemamanager.util.common.StorageManager;
import com.cinemamanager.util.common.enums.CollectionType;
import com.cinemamanager.util.common.exception.DuplicateElementException;
import com.cinemamanager.util.people.personalData.PersonalDataFactory;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public final class PersonalDataManager {
    private final StorageManager <String, PersonalData> personalDataStorageManager;
    private static final String PERSONAL_DATA_FILE_PATH = "personalData.json";

    public PersonalDataManager () {
        this.personalDataStorageManager = new StorageManager<>(CollectionType.HASH_MAP);
        loadFromFile();
    }

    public Optional<PersonalData> registerPersonalData(String nationalId) {
//        if (!isPersonalDataUnique(nationalId, userManager)) {
//            return Optional.empty();
//        }
//        LA VALIDACION DE SI DNI ESTA YA O NO, SE HARA DESDE EL MENU, ANTES DE LLAMAR A ESTE METODO
        PersonalData data = PersonalDataFactory.createPersonalData(this, nationalId);
        try {
            personalDataStorageManager.add(data, false);
            saveToFile();
            System.out.println("Personal data registered successfully.");
            return Optional.of(data);
        } catch (DuplicateElementException e) {
            System.out.println("\nError registering personal data: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional <PersonalData> findPersonalDataByNationalId (String nationalId) {
        return personalDataStorageManager.findById (nationalId);
    }

    public List <PersonalData> getAllPersonalDataUsers() {
        return personalDataStorageManager.findAll();
    }

    void updatePersonalData (PersonalData personalDataToUpdate) {
        String prompt = """
                        
                        What do you want to do?
                        [1] Change full name.
                        [2] Change email.
                        [3] Change phone seatNumber.
                        [4] Change all.
                        
                        [0] Back.
                        >""" + " ";
        Set <String> validOptions = Set.of("0", "1", "2", "3", "4");
        String chosenOption = ConsoleUtil.readOption(prompt, validOptions);

        switch (chosenOption) {
            case "1" -> {
                changeFullName(personalDataToUpdate);
                saveToFile();
            }
            case "2" -> {
                if (changeEmail(personalDataToUpdate)) saveToFile();
            }
            case "3" -> {
                if (changePhoneNumber(personalDataToUpdate)) saveToFile();
            }
            case "4" -> {
                // start transaction
                PersonalData backup = new PersonalData(
                        personalDataToUpdate.getId(),
                        personalDataToUpdate.getName(),
                        personalDataToUpdate.getLastName(),
                        personalDataToUpdate.getEmail(),
                        personalDataToUpdate.getPhoneNumber(),
                        personalDataToUpdate.isMustCompleteProfile()
                );
                if (!changeAll(personalDataToUpdate)) {
                    // rollback
                    personalDataToUpdate.setName(backup.getName());
                    personalDataToUpdate.setLastName(backup.getLastName());
                    personalDataToUpdate.setEmail(backup.getEmail());
                    personalDataToUpdate.setPhoneNumber(backup.getPhoneNumber());
                    System.out.println("\nNo changes were applied.\n");
                    break;
                }
                // commit
                saveToFile();
            }
            case "0" -> {}
        }
    }

    private void changeFullName (PersonalData personalDataToUpdate) {
        String newFirstName = ConsoleUtil.readValidName("the new first name");
        String newLastName = ConsoleUtil.readValidName("the new last name");
        personalDataToUpdate.setName(newFirstName);
        personalDataToUpdate.setLastName(newLastName);
        System.out.println("\nFull name successfully updated.\n");
    }

    private boolean changeEmail (PersonalData personalDataToUpdate) {
        String newEmail = ConsoleUtil.readValidEmail("new email");
        if (emailAlreadyExists(newEmail)) {
            System.out.println("\nThe email is already in use.\n");
            ConsoleUtil.showAbortMessage();
            return false;
        }
        else {
            personalDataToUpdate.setEmail(newEmail);
            System.out.println("\nEmail successfully updated.\n");
            return true;
        }
    }

    private boolean changePhoneNumber (PersonalData personalDataToUpdate) {
        String newPhoneNumber = ConsoleUtil.readValidPhone("new phone number");
        if (phoneNumberAlreadyExists(newPhoneNumber)) {
            System.out.println("\nThis phone number is already in use.\n");
            ConsoleUtil.showAbortMessage();
            return false;
        }
        else {
            personalDataToUpdate.setPhoneNumber(newPhoneNumber);
            System.out.println("\nPhone Number successfully updated.\n");
            return true;
        }
    }

    private boolean changeAll (PersonalData personalDataToUpdate) {
        changeFullName(personalDataToUpdate);
        if (!changeEmail(personalDataToUpdate)) return false;
        if (!changePhoneNumber(personalDataToUpdate)) return false;
        System.out.println("\nThe updated personal data:\n" + personalDataToUpdate);
        if (!ConsoleUtil.confirm("\nDo you want to save these changes?")) {
            return false;
        }
        System.out.println("\nAll personal data successfully updated.\n");
        return true;
    }

    public void forceProfileUpdate (PersonalData personalData) {
        boolean isValidEmail;
        boolean isValidNumber;
        String nationalId = ConsoleUtil.readValidNationalId ("new national ID");
        personalData.setId(nationalId);
        changeFullName (personalData);
        do {
            isValidEmail = changeEmail(personalData);
        } while (!isValidEmail);
        do {
            isValidNumber = changePhoneNumber(personalData);
        } while (!isValidNumber);
        personalData.setMustCompleteProfile(false);
        saveToFile();
        System.out.println("\nPersonal data updated successfully.\n");
    }

    public boolean nationalIdAlreadyExists(String newNationalId) {
        Optional <PersonalData> optionalPersonalData = personalDataStorageManager.findFirstBy(pd -> pd.getId().equals(newNationalId));
        return optionalPersonalData.isPresent();
    }

    public boolean emailAlreadyExists (String newEmail) {
        Optional <PersonalData> optionalPersonalData = personalDataStorageManager.findFirstBy(pd -> pd.getEmail().equals(newEmail));
        return optionalPersonalData.isPresent();
    }

    public boolean phoneNumberAlreadyExists(String newPhoneNumber) {
        Optional <PersonalData> optionalPersonalData = personalDataStorageManager.findFirstBy(pd -> pd.getPhoneNumber().equals(newPhoneNumber));
        return optionalPersonalData.isPresent();
    }

    public boolean isPersonalDataUnique (String nationalId, UserManager userManager) {
        Optional <PersonalData> optionalPersonalData = findPersonalDataByNationalId (nationalId);
        optionalPersonalData.ifPresent(personalData -> System.out.println("\nNational ID already exists\n" + personalData + "\n"));
        return optionalPersonalData.isEmpty();
    }

    void loadFromFile () {
        Type type = new TypeToken <Map <String, PersonalData> >() {}.getType();
        Map <String, PersonalData> loaded = JsonUtil.read(PERSONAL_DATA_FILE_PATH, type, HashMap::new);
        personalDataStorageManager.clear();
        for (PersonalData c : loaded.values()) {
            try {
                personalDataStorageManager.add(c, true);
            } catch (DuplicateElementException ignored) {}
        }
    }

    void saveToFile() {
        Map<String, PersonalData> map = personalDataStorageManager.findAll().stream()
                .collect(Collectors.toMap(PersonalData::getId, pd -> pd));
        JsonUtil.write(PERSONAL_DATA_FILE_PATH, map);
    }

}
