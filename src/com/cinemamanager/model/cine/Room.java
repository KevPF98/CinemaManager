package com.cinemamanager.model.cine;
import com.cinemamanager.enums.cine.room.RoomType;
import com.cinemamanager.iface.Identifiable;
import com.cinemamanager.util.common.ConsoleUtil;

import java.util.Objects;

public final class Room implements Comparable <Room>, Identifiable <Integer> {

    private final int roomNumber;
    private boolean active;
    private RoomType type;
    private int totalSeats;

    public Room (int roomNumber, RoomType type, int totalSeats) {
        this.roomNumber = roomNumber;
        this.active = true;
        this.type = type;
        this.totalSeats = totalSeats;
    }

    @Override
    public Integer getId() {
        return roomNumber;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats (int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public boolean is3D () {
        return type.equals(RoomType.THREE_D);
    }

    public boolean is2D () {
        return type.equals(RoomType.TWO_D);
    }

    public boolean isATMOS () {
        return type.equals(RoomType.ATMOS);
    }

    public void activate () {
        this.active = true;
    }

    public void deactivate () {
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public int compareTo(Room other) {
        return Integer.compare(this.roomNumber, other.roomNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return roomNumber == room.roomNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(roomNumber);
    }

    @Override
    public String toString() {
        return "-----------------\n" +
                "Room number: " + roomNumber + ".\n" +
                "Room type: " + ConsoleUtil.formatEnumName(type.name()) + ".\n" +
                "Maximum capacity: " + totalSeats + ".\n" +
                "Status: " + (isActive() ? "active." : "inactive.") +
                "\n-----------------\n";
    }

}

