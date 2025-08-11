package com.cinemamanager.util.room;

import com.cinemamanager.util.common.ConsoleUtil;

public final class RoomValidator {

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

}
