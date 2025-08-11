package com.cinemamanager.model.cine;

import com.cinemamanager.iface.Identifiable;

public final class Seat implements Comparable <Seat>, Identifiable <Integer> {

    private int seatNumber;
    private boolean occupied;

    public Seat (int seatNumber) {
        this.seatNumber = seatNumber;
        this.occupied = false;
    }

    public Integer getId() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    @Override
    public int compareTo(Seat other) {
        return Integer.compare(this.seatNumber, other.seatNumber);
    }

    @Override
    public String toString() {
        return  "--------------------------\n" +
                "Number: " + seatNumber + ".\n" +
                (occupied ? "Seat is occupied" : "Seat is available") + ".\n";
    }

}

