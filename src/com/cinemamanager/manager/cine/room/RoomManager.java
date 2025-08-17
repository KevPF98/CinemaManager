package com.cinemamanager.manager.cine.room;
import com.cinemamanager.enums.cine.room.RoomType;
import com.cinemamanager.util.common.ConsoleUtil;
import com.cinemamanager.util.common.enums.CollectionType;
import com.cinemamanager.util.common.exception.DuplicateElementException;
import com.cinemamanager.model.cine.Room;
import com.cinemamanager.util.common.JsonUtil;
import com.cinemamanager.util.common.StorageManager;
import com.cinemamanager.util.cine.room.RoomFactory;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public final class RoomManager {
    private final StorageManager <Integer, Room> roomStorageManager;
    private static final String ROOM_FILE_PATH = "rooms.json";

    public RoomManager() {
        this.roomStorageManager = new StorageManager<>(CollectionType.TREE_SET);
        loadFromFile();
    }

    public void addRoom () {
        Room newRoom = RoomFactory.createRoom();
        try {
            roomStorageManager.add (newRoom, false);
            saveToFile();
            System.out.println("\nRoom registered successfully!\n");
        } catch (DuplicateElementException e) {
            System.err.println("\nTo add this room to the system, you must first remove the one associated with the number: " + newRoom.getId() + ".\n");
        }
    }

    public void deleteRoom (int id) {
        roomStorageManager.delete(id);
        saveToFile();
    }

    public void deleteRoom (Room room) {
        roomStorageManager.delete(room);
        saveToFile();
    }

    public void deactivateRoom (Room room) {
        room.deactivate();
        System.out.println("\nRoom has been disabled successfully.\n");
        saveToFile();
    }

    public void activateRoom (Room room) {
        if (room.isActive()) {
            System.out.println("\nThe room is already active.\n");
            return;
        }

        room.activate();
        System.out.println("\nRoom has been activated successfully.\n");
        saveToFile();
    }

    public Optional <Room> findRoomByIdInSet (TreeSet <Room> roomSet, int roomNumber) {
        Room key = new Room(roomNumber, null, 0);
        Room found = roomSet.floor(key);
        if (found != null && found.getId() == roomNumber) {
            return Optional.of(found);
        }
        return Optional.empty();
    }

    public TreeSet <Room> findAllRooms () {
        return new TreeSet<> (roomStorageManager.findAll());
    }

    public TreeSet <Room> getActiveRooms () {
        List <Room> activeRooms = roomStorageManager.findBy(Room::isActive);
        return new TreeSet<> (activeRooms);
    }

    public TreeSet <Room> getInactiveRooms () {
        List <Room> inactiveRooms =  roomStorageManager.findBy(r -> !r.isActive());
        return new TreeSet<> (inactiveRooms);
    }

    public TreeSet <Room> getRoomsByCapacity () {
        int[] range = ConsoleUtil.readSeatRange();
        int min = range[0];
        int max = range[1];
        List <Room> listByCapacity = roomStorageManager.findBy(r -> r.getTotalSeats() >= min && r.getTotalSeats() <= max);
        return new TreeSet<>(listByCapacity);
    }

    public void showRooms (TreeSet <Room> rooms) {
        for (Room room : rooms) {
            System.out.println(room);
        }
    }

    public void updateRoom (Room roomToUpdate) {

        String prompt = """
                        
                        What do you want to do?
                        
                        [1]  Change the room type.
                        [2]  Change the room capacity.
                
                        [0] Back.
                        >""" + " ";

        Set<String> validOptions = Set.of("0", "1", "2");
        String chosenOption = ConsoleUtil.readOption(prompt, validOptions);

        switch (chosenOption) {
            case "1" -> changeRoomType(roomToUpdate);
            case "2" -> changeRoomCapacity(roomToUpdate);

            case "0" -> {}
        }
    }

    public void displayRoomsOrMessage(TreeSet<Room> rooms) {
        if (rooms.isEmpty()) {
            System.out.println("\nNo auditoriums found.\n");
        } else {
            showRooms(rooms);
        }
    }

    public Optional <Room> selectRoomByIdFromSet (TreeSet <Room> roomSet) {
        if (roomSet.isEmpty()) {
            System.out.println("\nNo rooms available to select.\n");
            return Optional.empty();
        }

        System.out.println("\nAuditoriums:");
        showRooms(roomSet);

        return selectValidRoomFromSet(roomSet);
    }

    public Optional <Room> selectValidRoomFromSet (TreeSet <Room> roomSet) {
        while (true) {
            int roomNumber = ConsoleUtil.readInt("\nEnter the number of the room to select: ");
            Optional <Room> optionalRoomFound = findRoomByIdInSet(roomSet, roomNumber);
            if (optionalRoomFound.isPresent()) {
                return optionalRoomFound;
            } else {
                System.err.println("\nRoom with number " + roomNumber + " not found.\n");
                if (!ConsoleUtil.confirm("\nDo you want to try with another room number?")) return Optional.empty();
            }
        }
    }

    private void changeRoomType (Room roomToUpdate) {
        RoomType newType = ConsoleUtil.readEnum(RoomType.class, "Select the new room type");
        roomToUpdate.setType(newType);
        System.out.println("\nRoom type updated successfully!\n");
        saveToFile();
    }

    private void changeRoomCapacity (Room roomToUpdate) {
        int newCapacity = ConsoleUtil.readPositiveSeats("Enter the new number of seats in the room: ");
        roomToUpdate.setTotalSeats(newCapacity);
        System.out.println("\nRoom capacity updated successfully!\n");
        saveToFile();
    }

    private void loadFromFile () {
        Type type = new TypeToken<TreeSet <Room> >() {}.getType();
        TreeSet <Room> loaded = JsonUtil.read(ROOM_FILE_PATH, type, TreeSet::new);
        roomStorageManager.clear();
        for (Room r : loaded) {
            try {
                roomStorageManager.add(r, true);
            } catch (DuplicateElementException ignored) {}
        }
    }

    private void saveToFile () {
        TreeSet <Room> treeSet = new TreeSet<>(roomStorageManager.findAll());
        JsonUtil.write(ROOM_FILE_PATH, treeSet);
    }

}
