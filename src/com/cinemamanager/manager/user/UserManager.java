package com.cinemamanager.manager.user;
import com.cinemamanager.auth.Session;
import com.cinemamanager.util.common.enums.CollectionType;
import com.cinemamanager.enums.user.Role;
import com.cinemamanager.util.common.exception.DuplicateElementException;
import com.cinemamanager.model.people.Account;
import com.cinemamanager.model.people.PersonalData;
import com.cinemamanager.model.people.User;
import com.cinemamanager.util.common.ConsoleUtil;
import com.cinemamanager.util.common.JsonUtil;
import com.cinemamanager.util.common.StorageManager;
import com.cinemamanager.util.people.user.UserFactory;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public final class UserManager {
    private final StorageManager <Integer, User> userStorageManager;
    private static final String USER_FILE_PATH = "users.json";
    private int nextId;

    private final PersonalDataManager personalDataManager;

    public UserManager (PersonalDataManager personalDataManager) {
        this.userStorageManager = new StorageManager<>(CollectionType.HASH_MAP);
        this.personalDataManager = personalDataManager;
        loadFromFile();

        nextId = userStorageManager.findAll().stream()
                .mapToInt(User::getId)
                .max()
                .orElse(0) + 1;
    }

    public Optional<User> addUser(String nationalId) {

        Optional<PersonalData> personalData = personalDataManager.findPersonalDataByNationalId(nationalId);

        // Caso: no existe → se crea personalData + usuario
        if (personalData.isEmpty()) {
            User newUser = UserFactory.createUserWithNationalId(nextId, this, personalDataManager, nationalId);
            return persistNewUser(newUser, "\nUser created successfully!\n");
        }

        // Caso: existe y ya es empleado → se cancela
        if (personalDataAlreadyAssociatedWithEmployee(nationalId)) {
            return Optional.empty();
        }

        // Caso: existe pero es cliente → preguntar si quiere convertir
        if (!ConsoleUtil.confirm("\nPersonal data already exists.\nConvert this client into an employee?")) {
            return Optional.empty();
        }

        return convertClientToEmployee(personalData.get());
    }


    public Optional<User> convertClientToEmployee(PersonalData data) {

        User newUser = new User(
                this.getNextId(),
                new Account("newUser", "password123", Role.EMPLOYEE),
                data
        );

        return persistNewUser(newUser, "\nClient converted to employee successfully!\n");
    }


    public void deleteUserById (int id) {
        userStorageManager.delete(id);
        saveToFile();
    }

    public void deactivateUser (User userToDeactivate) {
        userToDeactivate.getAccount().deactivate();
        saveToFile();
    }

    public void reactivateUser (User userToReactivate) {
        userToReactivate.getAccount().activate();
        saveToFile();
    }

    public void grantPrivileges (User userToGrantPermissions) {
        Account accountToGrantPermissions = userToGrantPermissions.getAccount();
        boolean isDeactivated = !accountToGrantPermissions.isActive();
        boolean isAlreadyAdmin = !accountToGrantPermissions.getRole().equals(Role.EMPLOYEE);
        if (isDeactivated) {
            System.out.println("Permissions cannot be granted to a deactivated account.");
            return;
        }
        if (isAlreadyAdmin) {
            System.out.println("The user already has admin privileges.");
            return;
        }
        accountToGrantPermissions.setRole(Role.ADMIN);
        System.out.println("Permissions granted successfully.");
        saveToFile();
    }

    public void revokePrivileges (User userToRevokePermissions) {
        Account accountToRevokePermissions = userToRevokePermissions.getAccount();
        boolean isDeactivated = !accountToRevokePermissions.isActive();
        boolean isNotAdmin = accountToRevokePermissions.getRole().equals(Role.EMPLOYEE);
        if (isDeactivated) {
            System.out.println("\nPermissions cannot be revoked to a deactivated account.\n");
            return;
        }
        if (isNotAdmin) {
            System.out.println("\nThe user is not an admin.\n");
            return;
        }
        accountToRevokePermissions.setRole(Role.EMPLOYEE);
        System.out.println("\nPermissions revoked successfully.\n");
        saveToFile();
    }

    public Optional <User> findUserById (int id) {
        return userStorageManager.findById(id);
    }

    public Optional <User> findUserByNationalId (String nationalId) {
        return userStorageManager.findFirstBy(u -> u.getPersonalData().getId().equals(nationalId));
    }

    public Optional <User> getOrCreateUser() {
        String nationalId = ConsoleUtil.readValidNationalId("National ID");
        return findUserByNationalId(nationalId)
                .or(() -> ConsoleUtil.confirm(
                                "\nNo user found.\nDo you want to create a new user with that National ID?"
                        )
                                ? addUser(nationalId)
                                : Optional.empty()
                );
    }

    public List <User> findAllUsers () {
        return userStorageManager.findAll();
    }

    public void showList (List <User> userList) {
        for (User user : userList) {
            System.out.println(user);
        }
    }

    public void showAllUsers () {
        showList (findAllUsers());
    }

    public void forcePasswordChange (User user) {
        String newPassword;
        String confirm;
        do {
            newPassword = ConsoleUtil.readValidPassword("new password");
            confirm = ConsoleUtil.readString("\nConfirm new password: ");
            if (!newPassword.equals(confirm)) {
                System.out.println("\nPasswords do not match. Try again.\n");
            }
        } while (!newPassword.equals(confirm));

        user.getAccount().setPassword(newPassword);
        user.getAccount().setMustChangePassword(false);
        saveToFile();
        System.out.println("\nPassword updated successfully.\n");
    }

    public void updateUser (int id) {
        Optional <User> optionalUserToUpdate = findUserById(id);
        if (optionalUserToUpdate.isEmpty()) {
            System.out.println("\nUser not found.\n");
            return;
        }

        User userToUpdate = optionalUserToUpdate.get();
        boolean isDeactivated = !userToUpdate.getAccount().isActive();

        if (isDeactivated) {
            System.out.println("\nA deactivated user cannot be modified.\n");
            return;
        }

        System.out.println("\nUser found:\n" + userToUpdate);
        String prompt = """
                        
                        What do you want to do?
                        
                        [1] Update account data.
                        [2] Update personal data.
                        
                        [0] Back.
                        >""" + " ";

        Set<String> validOptions = Set.of("0", "1", "2");
        String chosenOption = ConsoleUtil.readOption(prompt, validOptions);

        if (chosenOption.equals("1")) {
            updateAccountData(userToUpdate);
        }
        else if (chosenOption.equals("2")) {
            personalDataManager.updatePersonalData(userToUpdate.getPersonalData());
        }
    }

    public boolean nickNameAlreadyExists(String newNickName) {
        Optional<User> userWithNickName = userStorageManager.findFirstBy(u -> u.getAccount().getNickname().equals(newNickName));
        return userWithNickName.isPresent();
    }

    public Integer getNextId () {
        return nextId;
    }

    private boolean personalDataAlreadyAssociatedWithEmployee(String nationalId) {
        boolean employeeExists = userStorageManager.findFirstBy(
                                    u -> u.getPersonalData().getId().equals(nationalId)
                                ).isPresent();
        if (employeeExists) {
            System.out.println("An employee is already associated with this National ID.");
        }
        return employeeExists;
    }

    private Optional<User> persistNewUser(User newUser, String successMessage) {
        try {
            userStorageManager.add(newUser, false);
            nextId++;
            saveToFile();
            System.out.println(successMessage);
            return Optional.of(newUser);
        } catch (DuplicateElementException e) {
            System.out.println("\nError: " + e.getMessage());
            return Optional.empty();
        }
    }


    private void updateAccountData(User userToUpdate) {
        String prompt = """
                        
                        What do you want to do?
                        [1] Change nickname.
                        [2] Change password.
                        
                        [0] Back.
                        >""" + " ";
        Set<String> validOptions = Set.of("0", "1", "2");
        String chosenOption = ConsoleUtil.readOption(prompt, validOptions);

        switch (chosenOption) {
            case "1" -> changeNickname(userToUpdate);
            case "2" -> changePassword(userToUpdate);
            case "0" -> {}
        }
    }

    private void changeNickname(User userToUpdate) {
        String newNickname = ConsoleUtil.readValidNickname("new nickname");
        if (nickNameAlreadyExists(newNickname)) {
            System.out.println("\nThe nickname is already in use.");
            System.out.println("Operation aborted.\n");
        } else {
            userToUpdate.getAccount().setNickname(newNickname);
            System.out.println("\nNickname successfully changed to: " + newNickname + ".\n");
            saveToFile();
        }
    }

    private void changePassword(User userToUpdate) {
        boolean isSelf = Session.getActiveUser().equals(userToUpdate);

        if (isSelf) {
            while (true) {
                String input = ConsoleUtil.readString(
                        "\nPlease enter your current password, or type 'ESC' to cancel: "
                );
                if (input.equalsIgnoreCase("ESC")) return;

                if (input.equals(userToUpdate.getAccount().getPassword())) break;
                System.out.println("\nIncorrect password. Please try again.\n");
            }
        }

        String newPassword = ConsoleUtil.readValidPassword("new password");
        userToUpdate.getAccount().setPassword(newPassword);

        System.out.println("\nPassword successfully changed.\n");
        saveToFile();
    }

    private void createDefaultFounderUser() {
        Account founderAccount = new Account("founder", "founder123", Role.FOUNDER);

        PersonalData data = new PersonalData(" ", " ", " ", " ", " ", true);

        User founder = new User(1, founderAccount, data);

        try {
            userStorageManager.add(founder, true);
            System.out.println("\nDefault founder account created. Please log in with:");
            System.out.println("\nNickname: founder\n");
            System.out.println("\nPassword: founder123\n");
        } catch (DuplicateElementException e) {
            System.err.println("\nUnexpected duplicate while creating founder: " + e.getMessage() + ".\n");
        }
    }

    private void loadFromFile () {
        Type type = new TypeToken <Map <Integer, User> >() {}.getType();
        Map<Integer, User> loaded = JsonUtil.read(USER_FILE_PATH, type, HashMap::new);
        userStorageManager.clear();
        for (User u : loaded.values()) {
            try {
                userStorageManager.add(u, true);
            } catch (DuplicateElementException ignored) {}
        }

        if (userStorageManager.findAll().isEmpty()) {
            createDefaultFounderUser();
            saveToFile();
        }

    }

    private void saveToFile () {
        Map <Integer, User> map = userStorageManager.findAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        JsonUtil.write(USER_FILE_PATH, map);
    }

}
