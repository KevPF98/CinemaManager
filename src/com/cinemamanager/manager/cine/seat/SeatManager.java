package com.cinemamanager.manager.cine.seat;
import com.cinemamanager.model.cine.Seat;
import com.cinemamanager.util.common.ConsoleUtil;

import java.util.Optional;
import java.util.TreeSet;

public final class SeatManager {

    public static Optional <Seat> selectSeat(TreeSet <Seat> seats) {
        while (true) {
            int number = ConsoleUtil.readValidSeat("Enter the number of the seat: ");
            Optional<Seat> optionalSelectedSeat = findSeatByNumber(seats, number);
            if (optionalSelectedSeat.isPresent()) {
                if (!optionalSelectedSeat.get().isOccupied()) {
                    return optionalSelectedSeat;
                }
                System.out.println("The seat you are trying to select is occupied.");
            } else {
                System.out.println("Seat number not found.");
            }
            if (!ConsoleUtil.confirm("You can try a different seat number.")) return Optional.empty();
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
                        System.out.println("Error: the seat is already occupied.");
                    } else {
                        seat.setOccupied(true);
                        System.out.println("Seat " + seatNumber + " has been occupied.");
                    }
                },
                () -> System.out.println("Error: seat not found.")
        );
    }

    public static void freeSeat (TreeSet <Seat> seats, int seatNumber) {
        findSeatByNumber(seats, seatNumber).ifPresentOrElse(
                seat -> {
                    if (!seat.isOccupied()) {
                        System.out.println("Error: the seat is already free.");
                    } else {
                        seat.setOccupied(false);
                        System.out.println("Seat " + seatNumber + " has been freed.");
                    }
                },
                () -> System.out.println("Error: seat not found.")
        );
    }

}
