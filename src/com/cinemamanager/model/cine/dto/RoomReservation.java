package com.cinemamanager.model.cine.dto;
import com.cinemamanager.model.cine.Room;
import com.cinemamanager.model.cine.TimeSlot;
import java.time.DayOfWeek;

public final class RoomReservation {
    private final DayOfWeek day;
    private final TimeSlot timeSlot;
    private final Room room;

    public RoomReservation(DayOfWeek day, TimeSlot timeSlot, Room room) {
        this.day = day;
        this.timeSlot = timeSlot;
        this.room = room;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public Room getRoom() {
        return room;
    }
}
