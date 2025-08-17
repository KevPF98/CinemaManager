package com.cinemamanager.manager.cine.seat;
import com.cinemamanager.model.cine.Seat;
import com.cinemamanager.util.common.ConsoleUtil;

import java.util.Optional;
import java.util.TreeSet;

public final class SeatManager {

    public static Optional <Seat> selectSeat(TreeSet <Seat> seats) {
        while (true) {
            int number = ConsoleUtil.readValidSeat("\nEnter the number of the seat: ");
            Optional<Seat> optionalSelectedSeat = findSeatByNumber(seats, number);
            if (optionalSelectedSeat.isPresent()) {
                if (!optionalSelectedSeat.get().isOccupied()) {
                    return optionalSelectedSeat;
                }
                System.out.println("\nThe seat you are trying to select is occupied.\n");
            } else {
                System.out.println("\nSeat number not found.\n");
            }
            if (!ConsoleUtil.confirm("\nDo you want to try with another seat number?")) return Optional.empty();
        }
    }

    public static Optional <Seat> findSeatByNumber (TreeSet <Seat> seats, int seatNumber) {
        Seat key = new Seat(seatNumber);
        Seat found = seats.floor(key);
        if (found != null && found.getId().equals(seatNumber)) {
            return Optional.of(found);
        }
        return Optional.empty();
    }

    public static void occupySeat (TreeSet <Seat> seats, int seatNumber) {
        findSeatByNumber (seats, seatNumber).ifPresentOrElse(
                seat -> {
                    if (seat.isOccupied()) {
                        System.out.println("\nError: the seat is already occupied.\n");
                    } else {
                        seat.setOccupied(true);
                        System.out.println("\nSeat " + seatNumber + " has been occupied.\n");
                    }
                },
                () -> System.out.println("\nError: seat not found.\n")
        );
    }

    public static void freeSeat (TreeSet <Seat> seats, int seatNumber) {
        findSeatByNumber(seats, seatNumber).ifPresentOrElse(
                seat -> {
                    if (!seat.isOccupied()) {
                        System.out.println("\nError: the seat is already free.\n");
                    } else {
                        seat.setOccupied(false);
                        System.out.println("\nSeat " + seatNumber + " has been freed.\n");
                    }
                },
                () -> System.out.println("\nError: seat not found.\n")
        );
    }

}
