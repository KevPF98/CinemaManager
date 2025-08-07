package com.cinemamanager.util;

import com.cinemamanager.manager.UserManager;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public final class ConsoleUtil {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static Scanner getScanner() {
        return SCANNER;
    }

    // General:

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = SCANNER.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = SCANNER.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid decimal number.");
            }
        }
    }

    public static String readString(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine().trim();
    }

    public static String readCapitalizedString(String prompt) {
        System.out.print(prompt);
        String input = SCANNER.nextLine().trim();
        return capitalizeEachWord(input);
    }

    public static String readStringToLower(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine().trim().toLowerCase();
    }

    public static String readStringToUpper(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine().trim().toUpperCase();
    }

    private static String capitalizeEachWord(String input) {
        String[] words = input.trim().toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    public static boolean confirm (String warningMessage) {
        String input;
        do {
            System.err.println (warningMessage);
            input = readStringToLower("Do you want to proceed? (yes/no): ");

            if (input.equals("no")) {
                System.out.println ("Operation cancelled.");
                return false;
            } else if (input.equals("yes")) {
                return true;
            }

            System.out.println ("Please, choose a valid option.");
        } while (true);
    }

    public static String formatEnumName(String enumName) {
        if (enumName == null || enumName.isEmpty()) {
            return enumName;
        }

        String withSpaces = enumName.toLowerCase().replace('_', ' ');

        return capitalizeEachWord(withSpaces);
    }

    public static <T extends Enum<T>> T readEnum (Class <T> enumClass, String prompt) {
        List<String> options = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .toList();

        while (true) {
            System.out.println(prompt + ":");
            for (T constant : enumClass.getEnumConstants()) {
                System.out.println(" - " + formatEnumName(constant.name()));
            }

            String input = readStringToUpper("Enter your choice: ");

            try {
                return Enum.valueOf(enumClass, input);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public static Duration readDuration(String prompt) {
        while (true) {
            System.out.print(prompt + " (format HH:mm:ss): ");
            String input = SCANNER.nextLine().trim();

            try {
                String[] parts = input.split(":");
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Incorrect format");
                }

                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                int seconds = Integer.parseInt(parts[2]);

                return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
            } catch (Exception e) {
                System.out.println("Invalid duration format. Please enter in HH:mm:ss format.");
            }
        }
    }

    public static LocalTime readTime(String prompt) {
        while (true) {
            System.out.print(prompt + " (format HH:mm): ");
            String input = SCANNER.nextLine().trim();

            try {
                return LocalTime.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid time format. Please enter in HH:mm format.");
            }
        }
    }

    // Menu:

    public static String readOption(String prompt, Set<String> validOptions) {
        String input;
        do {
            input = ConsoleUtil.readString(prompt);
            if (validOptions.contains(input)) {
                return input;
            }
            System.out.println("\nPlease, choose a valid option.\n");
        } while (true);
    }

    public static String readInputOrEsc(String prompt) {
        String input = readString(prompt);

        if (input.equalsIgnoreCase("esc")) {
            return null;
        }

        return input;
    }

    // User:

    public static String readValidNickname(String label) {
        String nickname;
        do {
            nickname = ConsoleUtil.readString("Enter " + label + " (3-20 characters, letters, numbers, underscores): ");
            if (!UserValidator.isValidNickname(nickname)) {
                System.out.println("Invalid nickname. Only letters, numbers and underscores allowed (3-20 characters).");
            }
        } while (!UserValidator.isValidNickname(nickname));
        return nickname;
    }

    public static String readValidPassword (String label) {
        String password;
        do {
            password = ConsoleUtil.readString ("Enter " + label + " (at least 6 characters): ");
            if (!UserValidator.isValidPassword(password)) {
                System.out.println ("Password too weak. Must be at least 6 characters, include uppercase, lowercase and a number.");
            }
        } while (!UserValidator.isValidPassword(password));
        return password;
    }

    public static String readValidName(String label) {
        String name;
        do {
            name = ConsoleUtil.readCapitalizedString("Enter " + label + ": ");
            if (!UserValidator.isValidName(name)) {
                System.out.println("Invalid " + label + ". Only letters, spaces and hyphens allowed.");
            }
        } while (!UserValidator.isValidName(name));
        return name;
    }

    public static String readValidNationalId (String label) {
        String nationalId;
        do {
            nationalId = ConsoleUtil.readString ("Enter " + label + " (7-10 digits): ");
            if (!UserValidator.isValidNationalId(nationalId)) {
                System.out.println ("Invalid National Identification format. Please enter only digits, length 7 to 10.");
            }
        } while (!UserValidator.isValidNationalId(nationalId));
        return nationalId;
    }

    public static String readValidEmail (String label) {
        String email;
        do {
            email = ConsoleUtil.readString ("Enter " + label + ": ");
            if (!UserValidator.isValidEmail(email)) {
                System.out.println ("Invalid email format. Please try again.");
            }
        } while (!UserValidator.isValidEmail(email));
        return email;
    }

    public static String readValidPhone(String label) {
        String phone;
        do {
            phone = ConsoleUtil.readString("Enter " + label + " (digits, spaces, dashes, optional '+'): ");
            if (!UserValidator.isValidPhone(phone)) {
                System.out.println("Invalid phone number format. Please try again.");
            }
        } while (!UserValidator.isValidPhone(phone));
        return phone;
    }

    public static String readUniqueNickname(UserManager userManager) {
        while (true) {
            String nickname = ConsoleUtil.readValidNickname("nickname");

            if (userManager.nickNameAlreadyExists(nickname)) {
                System.out.println("This nickname is already in use. Please enter another one.");
            } else {
                return nickname;
            }
        }
    }

    public static String readUniqueNationalId(UserManager userManager) {
        while (true) {
            String nationalId = ConsoleUtil.readValidNationalId("National ID");

            if (userManager.nationalIdAlreadyExists(nationalId)) {
                System.out.println("There is already a user associated with this National ID.");
            } else {
                return nationalId;
            }
        }
    }

    public static String readUniqueEmail(UserManager userManager) {
        while (true) {
            String email = ConsoleUtil.readValidEmail("email");

            if (userManager.emailAlreadyExists(email)) {
                System.out.println("This email is already in use. Please enter another one.");
            } else {
                return email;
            }
        }
    }

    public static String readUniquePhoneNumber(UserManager userManager) {
        while (true) {
            String phoneNumber = ConsoleUtil.readValidPhone("phone number");

            if (userManager.phoneNumberAlreadyExists(phoneNumber)) {
                System.out.println("This phone number is already in use. Please enter another one.");
            } else {
                return phoneNumber;
            }
        }
    }

}
