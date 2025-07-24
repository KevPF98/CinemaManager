package com.cinemamanager.manager;

import com.cinemamanager.enums.CollectionType;
import com.cinemamanager.exception.DuplicateElementException;
import com.cinemamanager.exception.UserNotFoundException;
import com.cinemamanager.model.people.User;
import com.cinemamanager.util.JsonUtil;
import com.cinemamanager.util.StorageManager;
import com.cinemamanager.util.UserFactory;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public final class UserManager {
    private final StorageManager <Integer, User> userManager;
    private static final String USER_FILE_PATH = "user.json";
    private int nextId;

    public UserManager() {
        this.userManager = new StorageManager<>(CollectionType.HASH_MAP);
        loadFromFile();

        OptionalInt maxId = userManager.findAll().stream()
                .mapToInt(User :: getId)
                .max();
        this.nextId = maxId.isPresent() ? maxId.getAsInt() +1 : 1;
    }

    public void addUser() {
        loadFromFile();

        User newUser = UserFactory.createUser(nextId, nextId);
        try {
            userManager.add(newUser, false);
            nextId++;
            saveToFile();
        } catch (DuplicateElementException e) {
            System.out.println("Error adding the user: " + e.getMessage());
        }
    }

    public void deleteUserById(int id) {
        loadFromFile();
        userManager.delete(id);
        saveToFile();
    }

    public User findUserById(int id) throws UserNotFoundException{
        loadFromFile();
        return userManager.findById(id).orElseThrow(() -> new UserNotFoundException("User with ID: " + id + " not found."));
    }

    public List<User> findAllUsers() {
        loadFromFile();
        return userManager.findAll();
    }

    private void loadFromFile() {
        Type type = new TypeToken<Map<Integer, User>>() {}.getType();
        Map<Integer, User> loaded = JsonUtil.readMap(USER_FILE_PATH, type, HashMap::new);
        userManager.clear();
        for (User u : loaded.values()) {
            try {
                userManager.add(u, true);
            } catch (DuplicateElementException ignored) {}
        }
    }

    private void saveToFile() {
        Map<Integer, User> mapa = userManager.findAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        JsonUtil.write(USER_FILE_PATH, mapa);
    }

}
