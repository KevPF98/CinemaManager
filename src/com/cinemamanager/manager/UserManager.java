package com.cinemamanager.manager;
import com.cinemamanager.enums.CollectionType;
import com.cinemamanager.enums.Role;
import com.cinemamanager.exception.DuplicateElementException;
import com.cinemamanager.exception.UserNotFoundException;
import com.cinemamanager.model.people.Account;
import com.cinemamanager.model.people.PersonalData;
import com.cinemamanager.model.people.User;
import com.cinemamanager.util.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public final class UserManager {
    private final StorageManager <Integer, User> userStorageManager;
    private static final String USER_FILE_PATH = "user.json";
    private int nextId;

    public UserManager () {
        this.userStorageManager = new StorageManager<>(CollectionType.HASH_MAP);
        loadFromFile();

        OptionalInt maxId = userStorageManager.findAll().stream()
                .mapToInt(User :: getId)
                .max();
        this.nextId = maxId.isPresent() ? maxId.getAsInt() +1 : 1;
    }

    public void addUser () {
        User newUser = UserFactory.createUser(nextId, this);
        try {
            userStorageManager.add(newUser, false);
            nextId++;
            saveToFile();
            System.out.println("\nUser created successfully!\n");
        } catch (DuplicateElementException e) {
            System.out.println("Error adding the user: " + e.getMessage());
        }
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
            System.out.println("Permissions cannot be revoked to a deactivated account.");
            return;
        }
        if (isNotAdmin) {
            System.out.println("The user is not an admin.");
            return;
        }
        accountToRevokePermissions.setRole(Role.EMPLOYEE);
        System.out.println("Permissions revoked successfully.");
        saveToFile();
    }

    public User findUserById (int id) throws UserNotFoundException{
        return userStorageManager.findById(id).orElseThrow(() -> new UserNotFoundException("User with ID: " + id + " not found."));
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
            confirm = ConsoleUtil.readString("Confirm new password: ");
            if (!newPassword.equals(confirm)) {
                System.out.println("\nPasswords do not match. Try again.\n");
            }
        } while (!newPassword.equals(confirm));

        user.getAccount().setPassword(newPassword);
        user.getAccount().setMustChangePassword(false);
        saveToFile();
        System.out.println("\nPassword updated successfully.\n");
    }

    public void forcePersonalDataChange (User user) {
        String nationalId = ConsoleUtil.readValidNationalId ("new national ID");
        user.getPersonalData().setId(nationalId);
        changeAll (user);
        user.getPersonalData().setMustCompleteProfile(false);
        saveToFile();
        System.out.println("\nPersonal data updated successfully.\n");
    }

    public void updateUser (int id) {
        try {
            User userToUpdate = findUserById(id);
            boolean isDeactivated = !userToUpdate.getAccount().isActive();

            if (isDeactivated) {
                System.out.println("A deactivated user cannot be modified.");
                return;
            }

            System.out.println("User found:\n" + userToUpdate);
            String prompt = """
                            What do you want to do?
                            [1] Update account data.
                            [2] Update personal data.
                            
                            [0] Back.
                            """;

            Set<String> validOptions = Set.of("0", "1", "2");
            String chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            if (chosenOption.equals("1")) {
                updateAccountData(userToUpdate);
            }
            else if (chosenOption.equals("2")) {
                updatePersonalData(userToUpdate);
            }

        } catch (UserNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    public boolean nickNameAlreadyExists(String newNickName) {
        Optional<User> userWithNickName = userStorageManager.findFirstBy(u -> u.getAccount().getNickname().equals(newNickName));
        return userWithNickName.isPresent();
    }

    public boolean nationalIdAlreadyExists(String newNationalId) {
        Optional<User> userWithNationalId = userStorageManager.findFirstBy(u -> u.getPersonalData().getId().equals(newNationalId));
        return userWithNationalId.isPresent();
    }

    public boolean emailAlreadyExists(String newEmail) {
        Optional<User> userWithEmail = userStorageManager.findFirstBy(u -> u.getPersonalData().getEmail().equals(newEmail));
        return userWithEmail.isPresent();
    }

    public boolean phoneNumberAlreadyExists(String newPhoneNumber) {
        Optional<User> userWithPhoneNumber = userStorageManager.findFirstBy(u -> u.getPersonalData().getPhoneNumber().equals(newPhoneNumber));
        return userWithPhoneNumber.isPresent();
    }

    private void updateAccountData(User userToUpdate) {
        String prompt = """
                        What do you want to do?
                        [1] Change nickname.
                        [2] Change password.
                        
                        [0] Back.
                        """;
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
            System.out.println("The nickname is already in use.");
        } else {
            userToUpdate.getAccount().setNickname(newNickname);
            System.out.println("Nickname successfully changed to: " + newNickname);
            saveToFile();
        }
    }

    private void changePassword(User userToUpdate) {
        while (true) {
            String input = ConsoleUtil.readString("Please enter the current password, or type 'ESC' to cancel.");
            if (input.equalsIgnoreCase("ESC")) {
                break;
            }
            if (input.equals(userToUpdate.getAccount().getPassword())) {
                String newPassword = ConsoleUtil.readValidPassword("new password");
                userToUpdate.getAccount().setPassword(newPassword);
                System.out.println("Password successfully changed.");
                saveToFile();
                break;
            } else {
                System.out.println("Incorrect password. Please try again.");
            }
        }
    }

    private void updatePersonalData (User userToUpdate) {
        String prompt = """
                        What do you want to do?
                        [1] Change full name.
                        [2] Change email.
                        [3] Change phone number.
                        [4] Change all.
                        
                        [0] Back.
                        """;
        Set<String> validOptions = Set.of("0", "1", "2", "3", "4");
        String chosenOption = ConsoleUtil.readOption(prompt, validOptions);

        switch (chosenOption) {
            case "1" -> changeFullName (userToUpdate);
            case "2" -> changeEmail (userToUpdate);
            case "3" -> changePhoneNumber (userToUpdate);
            case "4" -> changeAll (userToUpdate);
            case "0" -> {}
        }
    }

    private void changeFullName (User userToUpdate) {
        String newFirstName = ConsoleUtil.readValidName("the new first name");
        String newLastName = ConsoleUtil.readValidName("the new last name");
        userToUpdate.getPersonalData().setName(newFirstName);
        userToUpdate.getPersonalData().setLastName(newLastName);
        System.out.println("Full name successfully changed.\n");
        saveToFile();
    }

    private void changeEmail (User userToUpdate) {
        String newEmail = ConsoleUtil.readValidEmail("new email");
        if (emailAlreadyExists(newEmail)) {
            System.out.println("The email is already in use.");
        }
        else {
            userToUpdate.getPersonalData().setEmail(newEmail);
            System.out.println("Email successfully changed.\n");
            saveToFile();
        }
    }

    private void changePhoneNumber (User userToUpdate) {
        String newPhoneNumber = ConsoleUtil.readValidPhone("new phone numer");
        if (phoneNumberAlreadyExists(newPhoneNumber)) {
            System.out.println("There is already a user associated with this phone number.");
        }
        else {
            userToUpdate.getPersonalData().setPhoneNumber(newPhoneNumber);
            System.out.println("Phone number successfully changed.\n");
            saveToFile();
        }
    }

    private void changeAll (User userToUpdate) {
        changeFullName(userToUpdate);
        changeEmail(userToUpdate);
        changePhoneNumber(userToUpdate);
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
        Map<Integer, User> map = userStorageManager.findAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        JsonUtil.write(USER_FILE_PATH, map);
    }

    private void createDefaultFounderUser() {
        Account founderAccount = new Account("founder", "founder123", Role.FOUNDER);

        PersonalData data = new PersonalData(" ", " ", " ", " ", " ", true);

        User founder = new User(1, founderAccount, data);

        try {
            userStorageManager.add(founder, true);
            System.out.println("\nDefault founder account created. Please log in with:");
            System.out.println("Nickname: founder");
            System.out.println("Password: founder123\n");
        } catch (DuplicateElementException e) {
            System.err.println("Unexpected duplicate while creating founder: " + e.getMessage());
        }
    }

}
