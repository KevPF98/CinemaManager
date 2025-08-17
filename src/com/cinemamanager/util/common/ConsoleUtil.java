package com.cinemamanager.util.common;
import com.cinemamanager.manager.user.UserManager;
import com.cinemamanager.util.user.UserValidator;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public final class ConsoleUtil {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static Scanner getScanner() {
        return SCANNER;
    }
    public static void closeScanner() {
        SCANNER.close();
    }

    // General:

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = SCANNER.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("\nInvalid input. Please enter a valid integer.\n");
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
                System.out.println("\nInvalid input. Please enter a valid decimal number.\n");
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

    public static boolean confirm (String message) {
        String input;
        do {
            input = readStringToLower(message + " (yes/no): ");
            System.out.println("\n");

            if (input.equals("no")) {
                System.out.println ("\nOperation cancelled.\n");
                return false;
            } else if (input.equals("yes")) {
                return true;
            }

            System.out.println ("\nPlease, choose a valid option.\n");
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

            String input = readStringToUpper("\nEnter your choice: ")
                    .replace(' ', '_');;

            try {
                return Enum.valueOf(enumClass, input);
            } catch (IllegalArgumentException e) {
                System.out.println("\nInvalid choice. Please try again.\n");
            }
        }
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

    // Date and time:

    public static String formatTime(LocalTime time) {
        return time.format(TIME_FORMATTER);
    }

    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    public static Duration readDuration(String prompt) {
        while (true) {
            System.out.print(prompt + " (format HH:mm:ss): ");
            String input = SCANNER.nextLine().trim();

            try {
                String[] parts = input.split(":");
                if (parts.length != 3) {
                    throw new IllegalArgumentException("\nIncorrect format.\n");
                }

                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                int seconds = Integer.parseInt(parts[2]);

                return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
            } catch (Exception e) {
                System.out.println("\nInvalid duration format. Please enter in HH:mm:ss format.\n");
            }
        }
    }

    public static LocalTime readTime(String prompt) {
        while (true) {
            System.out.print(prompt + " (format HH:mm): ");
            String input = SCANNER.nextLine().trim();
            try {
                return LocalTime.parse(input, TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("\nInvalid time format. Please enter in HH:mm format.\n");
            }
        }
    }

    public static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (format dd/MM/yyyy): ");
            String input = SCANNER.nextLine().trim();
            try {
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("\nInvalid date format. Please enter in dd/MM/yyyy format.\n");
            }
        }
    }

    public static LocalDateTime readDateTime(String prompt) {
        while (true) {
            System.out.print(prompt + " (format dd/MM/yyyy HH:mm): ");
            String input = SCANNER.nextLine().trim();
            try {
                return LocalDateTime.parse(input, DATE_TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("\nInvalid datetime format. Please enter in dd/MM/yyyy HH:mm format.\n");
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
            nickname = ConsoleUtil.readString("\nEnter " + label + " (3-20 characters, letters, numbers, underscores): ");
            if (!UserValidator.isValidNickname(nickname)) {
                System.out.println("\nInvalid nickname. Only letters, numbers and underscores allowed (3-20 characters).\n");
            }
        } while (!UserValidator.isValidNickname(nickname));
        return nickname;
    }

    public static String readValidPassword (String label) {
        String password;
        do {
            password = ConsoleUtil.readString ("\nEnter " + label + " (at least 6 characters): ");
            if (!UserValidator.isValidPassword(password)) {
                System.out.println ("\nPassword too weak. Must be at least 6 characters, include uppercase, lowercase and a number.\n");
            }
        } while (!UserValidator.isValidPassword(password));
        return password;
    }

    public static String readValidName(String label) {
        String name;
        do {
            name = ConsoleUtil.readCapitalizedString("\nEnter " + label + ": ");
            if (!UserValidator.isValidName(name)) {
                System.out.println("\nInvalid " + label + ". Only letters, spaces and hyphens allowed.\n");
            }
        } while (!UserValidator.isValidName(name));
        return name;
    }

    public static String readValidNationalId (String label) {
        String nationalId;
        do {
            nationalId = ConsoleUtil.readString ("\nEnter " + label + " (7-10 digits): ");
            if (!UserValidator.isValidNationalId(nationalId)) {
                System.out.println ("\nInvalid National Identification format. Please enter only digits, length 7 to 10.\n");
            }
        } while (!UserValidator.isValidNationalId(nationalId));
        return nationalId;
    }

    public static String readValidEmail (String label) {
        String email;
        do {
            email = ConsoleUtil.readString ("\nEnter " + label + ": ");
            if (!UserValidator.isValidEmail(email)) {
                System.out.println ("\nInvalid email format. Please try again.\n");
            }
        } while (!UserValidator.isValidEmail(email));
        return email;
    }

    public static String readValidPhone(String label) {
        String phone;
        do {
            phone = ConsoleUtil.readString("\nEnter " + label + " (digits, spaces, dashes, optional '+'): ");
            if (!UserValidator.isValidPhone(phone)) {
                System.out.println("\nInvalid phone number format. Please try again.\n");
            }
        } while (!UserValidator.isValidPhone(phone));
        return phone;
    }

    public static String readUniqueNickname(UserManager userManager) {
        while (true) {
            String nickname = ConsoleUtil.readValidNickname("nickname");

            if (userManager.nickNameAlreadyExists(nickname)) {
                System.out.println("\nThis nickname is already in use. Please enter another one.\n");
            } else {
                return nickname;
            }
        }
    }

    public static String readUniqueNationalId(UserManager userManager) {
        while (true) {
            String nationalId = ConsoleUtil.readValidNationalId("National ID");

            if (userManager.nationalIdAlreadyExists(nationalId)) {
                System.out.println("\nThere is already a user associated with this National ID.\n");
            } else {
                return nationalId;
            }
        }
    }

    public static String readUniqueEmail(UserManager userManager) {
        while (true) {
            String email = ConsoleUtil.readValidEmail("email");

            if (userManager.emailAlreadyExists(email)) {
                System.out.println("\nThis email is already in use. Please enter another one.\n");
            } else {
                return email;
            }
        }
    }

    public static String readUniquePhoneNumber(UserManager userManager) {
        while (true) {
            String phoneNumber = ConsoleUtil.readValidPhone("phone number");

            if (userManager.phoneNumberAlreadyExists(phoneNumber)) {
                System.out.println("\nThis phone number is already in use. Please enter another one.\n");
            } else {
                return phoneNumber;
            }
        }
    }

    // Movie:

    public static int readValidReleaseYear(String prompt) {
        final int MIN_YEAR = 1935;
        final int MAX_YEAR = LocalDate.now().getYear() + 1;

        while (true) {
            int year = readInt(prompt);
            if (year >= MIN_YEAR && year <= MAX_YEAR) {
                return year;
            } else {
                System.out.println("\nPlease enter a valid year between " + MIN_YEAR + " and " + MAX_YEAR + ".\n");
            }
        }
    }

    // Seat:

    public static int readValidSeat(String prompt) {
        int seatNumber;
        do {
            seatNumber = readInt(prompt);
            if (seatNumber <= 0) {
                System.out.println("\nSeat number cannot be negative or zero.\n");
            } else {
                System.out.println("\nSelected seat number: " + seatNumber);
                return seatNumber;
            }
        } while (true);
    }

    // Room:

    public static int readPositiveSeats(String prompt) {
        int seats;
        do {
            seats = ConsoleUtil.readInt(prompt);
            if (seats <= 0) {
                System.out.println("Error: The number of seats must be a positive integer.");
            }
        } while (seats <= 0);
        return seats;
    }

    public static int[] readSeatRange() {
        int minSeats;
        int maxSeats;

        do {
            minSeats = readPositiveSeats("Enter minimum number of seats: ");
            maxSeats = readInt("Enter maximum number of seats: ");

            if (maxSeats < minSeats) {
                System.out.println("\nMaximum seats cannot be less than minimum seats. Please try again.\n");
            }
        } while (maxSeats < minSeats);

        return new int[]{minSeats, maxSeats};
    }

    // Ticket:

    public static double readValidPrice(String prompt) {
        double price;
        do {
            price = readDouble(prompt);
            if (price <= 0) {
                System.out.println("\nPrice must be greater than $0.00. Please try again.\n");
            } else {
                System.out.println("\nPrice entered: $" + String.format("%.2f", price) + ".\n");
                return price;
            }
        } while (true);
    }

}
