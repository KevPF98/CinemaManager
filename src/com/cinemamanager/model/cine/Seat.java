package com.cinemamanager.model.cine;

public final class Seat {

    private int number;
    private boolean occupied;

    public Seat (int number) {
        this.number = number;
        this.occupied = false;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    @Override
    public String toString() {
        return  "--------------------------\n" +
                "Number: " + number + ".\n" +
                (occupied ? "Seat is occupied" : "Seat is available") + ".\n";
    }

}

