package com.cinemamanager.manager;

import com.cinemamanager.model.cine.Seat;

import java.util.ArrayList;
import java.util.Optional;

public final class SeatManager {

    public Optional <Seat> findSeatByNumber (ArrayList <Seat> seats, int number) {
        return seats.stream()
                .filter(seat -> seat.getNumber() == number)
                .findFirst();
    }

    public void occupySeat (ArrayList <Seat> seats, int number) {
        findSeatByNumber (seats, number).ifPresentOrElse(
                seat -> {
                    if (seat.isOccupied()) {
                        System.out.println("Error: the seat is already occupied.");
                    } else {
                        seat.setOccupied(true);
                        System.out.println("Seat " + number + " has been occupied.");
                    }
                },
                () -> System.out.println("Error: seat not found.")
        );
    }

    public void freeSeat (ArrayList <Seat> seats, int number) {
        findSeatByNumber(seats, number).ifPresentOrElse(
                seat -> {
                    if (!seat.isOccupied()) {
                        System.out.println("Error: the seat is already free.");
                    } else {
                        seat.setOccupied(false);
                        System.out.println("Seat " + number + " has been freed.");
                    }
                },
                () -> System.out.println("Error: seat not found.")
        );
    }

}
