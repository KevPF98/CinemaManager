package com.cinemamanager.menu;

import com.cinemamanager.manager.UserManager;
import com.cinemamanager.util.ConsoleUtil;

import java.util.Set;

public final class MainMenu {
    private final UserManager userManager;

    public MainMenu() {
        this.userManager = new UserManager();
    }

    public void displayMainMenu(){
        String chosenOption;
        String prompt;
        Set <String> validOptions = Set.of("1", "2", "3");
        do {
            prompt = ("""
                      Welcome to our Cinema Manager!
                      How can we help you?
                      [1] I want to log in (only for authorized personal).
                      [2] Show me the movie listings.
                      
                      [3] Exit.
                      > """);
            chosenOption = ConsoleUtil.readString (prompt);
            if (validOptions.contains(chosenOption)) {
                break;
            }
            System.out.println("\nPlease, choose a valid option.\n");
        } while (true);

        switch (chosenOption) {
            case "1":
                break;
            case "2":
                break;
            case "3":
                break;
        }

    }

}
