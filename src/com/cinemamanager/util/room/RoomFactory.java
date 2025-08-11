package com.cinemamanager.util.room;
import com.cinemamanager.enums.room.RoomType;
import com.cinemamanager.model.cine.Room;
import com.cinemamanager.util.common.ConsoleUtil;

public final class RoomFactory {

    public static Room createRoom () {
        int roomNumber = ConsoleUtil.readInt("Enter the room number: ");
        RoomType roomType = ConsoleUtil.readEnum(RoomType.class, "Enter the room type");
        int totalSeats = RoomValidator.readPositiveSeats("Enter the number of seats in the room: ");
        return new Room(roomNumber, roomType, totalSeats);
    }

}
