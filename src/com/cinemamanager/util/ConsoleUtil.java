package com.cinemamanager.util;

import java.util.Scanner;

public final class ConsoleUtil {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static Scanner getScanner() {
        return SCANNER;
    }

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
        return SCANNER.nextLine().trim().toLowerCase();
    }

    public static String readStringToLower(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine().trim().toLowerCase();
    }

    public static String readStringToUpper(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine().trim().toUpperCase();
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

}
