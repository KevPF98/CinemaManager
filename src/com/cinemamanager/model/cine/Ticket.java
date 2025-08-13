package com.cinemamanager.model.cine;

import com.cinemamanager.iface.Identifiable;
import com.cinemamanager.util.common.ConsoleUtil;

import java.util.Objects;

public final class Ticket implements Identifiable <Integer> {

    private final int ticketId;
    private final Showtime showtime;
    private final int seatNumber;

    public Ticket (int ticketId, Showtime showtime, int seatNumber) {
        this.ticketId = ticketId;
        this.showtime = showtime;
        this.seatNumber = seatNumber;
    }

    public Integer getId() {
        return ticketId;
    }

    public Showtime getShowtime() {
        return showtime;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return ticketId == ticket.ticketId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticketId);
    }

    @Override
    public String toString() {
        return "\nTicket number: " + ticketId +
                "\nMovie: " + showtime.getMovie().getTitle() +
                "\nSchedule: " + ConsoleUtil.formatTime(showtime.getTimeSlot().getStartTime()) +
                "\nSeat number: " + seatNumber +
                "\n----------------------------------------";
    }

}

