package com.cinemamanager.manager.room;
import com.cinemamanager.enums.room.RoomType;
import com.cinemamanager.util.common.ConsoleUtil;
import com.cinemamanager.util.common.enums.CollectionType;
import com.cinemamanager.util.common.exception.DuplicateElementException;
import com.cinemamanager.model.cine.Room;
import com.cinemamanager.util.common.JsonUtil;
import com.cinemamanager.util.common.StorageManager;
import com.cinemamanager.util.room.RoomFactory;
import com.cinemamanager.util.room.RoomValidator;
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
            System.out.println("Room registered successfully!");
        } catch (DuplicateElementException e) {
            System.err.println("To add this room to the system, you must first remove the one associated with the number: " + newRoom.getId());
        }
    }

    public void deleteRoomById (int id) {
        // VALIDAR PRIMERO QUE LA SALA NO TENGA FUNCIONES.
        roomStorageManager.delete(id);
        saveToFile();
    }

    public void deactivateRoom (Room room) {
        if (!room.isActive()) {
            System.out.println("The room has already been deactivated.");
            return;
        }

        // VALIDAR PRIMERO QUE LA SALA NO TENGA FUNCIONES.
        room.deactivate();
        System.out.println("Room has been disabled successfully.");
        saveToFile();
    }

    public void activateRoom (Room room) {
        if (room.isActive()) {
            System.out.println("The room is already active.");
            return;
        }

        room.activate();
        System.out.println("Room has been activated successfully.");
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

    public List <Room> findAllRooms () {
        return roomStorageManager.findAll();
    }

    public TreeSet <Room> getActiveRooms () {
        List <Room> activeRooms = roomStorageManager.findBy(Room::isActive);
        return new TreeSet<> (activeRooms);
    }

    public TreeSet <Room> getInactiveRooms () {
        List <Room> inactiveRooms =  roomStorageManager.findBy(r -> !r.isActive());
        return new TreeSet<> (inactiveRooms);
    }

    public void showRooms (TreeSet <Room> rooms) {
        for (Room room : rooms) {
            System.out.println(room);
        }
    }

    public void showActiveRooms () {
        showRooms(getActiveRooms());
    }

    public void showInactiveRooms () {
        showRooms(getInactiveRooms());
    }

    public void updateRoom () {
        TreeSet <Room> rooms = new TreeSet<> (getActiveRooms());

        Room roomToUpdate = selectRoomByIdFromSet(rooms);
        if (roomToUpdate == null) return;

//        if (gestorFunciones.salaEnUso(sala)) {
//            System.out.println("ATENCION: La sala que desea modificar tiene funciones activas");
//        }

        String prompt = """
        What do you want to do?
        [1]  Change the room type.
        [2]  Change the room capacity.

        [0] Back.
        """;

        Set<String> validOptions = Set.of("0", "1", "2");
        String chosenOption = ConsoleUtil.readOption(prompt, validOptions);

        switch (chosenOption) {
            case "1" -> changeRoomType(roomToUpdate);
            case "2" -> changeRoomCapacity(roomToUpdate);

            case "0" -> {}
        }
    }

    public Room selectRoomByIdFromSet (TreeSet <Room> roomSet) {
        if (roomSet.isEmpty()) {
            System.out.println("No rooms available to select.");
            return null;
        }

        System.out.println("Available rooms:");
        for (Room room : roomSet) {
            System.out.println(room);
        }

        while (true) {
            int roomNumber = ConsoleUtil.readInt("Enter the number of the room to select: ");
            Optional<Room> found = findRoomByIdInSet(roomSet, roomNumber);
            if (found.isPresent()) {
                return found.get();
            } else {
                System.err.println("Room with number " + roomNumber + " not found.");
            }
        }
    }

    private void changeRoomType (Room roomToUpdate) {
        RoomType newType = ConsoleUtil.readEnum(RoomType.class, "Select the new room type");
        roomToUpdate.setType(newType);
        System.out.println("Room type updated successfully!");
        saveToFile();
    }

    private void changeRoomCapacity (Room roomToUpdate) {
        int newCapacity = RoomValidator.readPositiveSeats("Enter the new number of seats in the room: ");
        roomToUpdate.setTotalSeats(newCapacity);
        System.out.println("Room capacity updated successfully!");
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
