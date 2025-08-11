package com.cinemamanager.manager.showtime;
import com.cinemamanager.exception.BusinessRuleException;
import com.cinemamanager.exception.ShowtimeNotFoundException;
import com.cinemamanager.manager.movie.MovieManager;
import com.cinemamanager.manager.room.RoomManager;
import com.cinemamanager.manager.seat.SeatManager;
import com.cinemamanager.manager.timeslot.ScheduleManager;
import com.cinemamanager.model.cine.Showtime;
import com.cinemamanager.util.common.ConsoleUtil;
import com.cinemamanager.util.common.JsonUtil;
import com.cinemamanager.util.common.StorageManager;
import com.cinemamanager.util.common.enums.CollectionType;
import com.cinemamanager.util.common.exception.DuplicateElementException;
import com.cinemamanager.util.showtime.ShowtimeFactory;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public final class ShowtimeManager {
    private final StorageManager <Integer, Showtime> showtimeStorageManager;
    private final static String SHOWTIME_FILE_PATH = "showtimes.json";
    private int nextId;

    private final RoomManager roomManager;
    private final ScheduleManager scheduleManager;
    private final MovieManager movieManager;
    private final SeatManager seatManager;

    public ShowtimeManager (RoomManager roomManager,
                            ScheduleManager scheduleManager,
                            MovieManager movieManager,
                            SeatManager seatManager) {
        this.showtimeStorageManager = new StorageManager<>(CollectionType.ARRAY_LIST);
        this.roomManager = roomManager;
        this.scheduleManager = scheduleManager;
        this.movieManager = movieManager;
        this.seatManager = seatManager;

        loadFromFile();

        OptionalInt maxId = showtimeStorageManager.findAll().stream()
                .mapToInt(Showtime::getId)
                .max();
        this.nextId = maxId.isPresent() ? maxId.getAsInt() +1 : 1;
    }

    public void addShowTime () {
        try {
            Showtime showtime = ShowtimeFactory.createShowtime(nextId, this, movieManager, scheduleManager, roomManager);
            nextId++;
            saveToFile();
            System.out.println("\nShowtime registered successfully!\n");
        } catch (BusinessRuleException e) {
            System.err.println("\nError registering the showtime: " + e.getMessage());;
        }
    }

    public boolean deleteShowtimeById () {
        int id = ConsoleUtil.readInt("Enter the ID of the showtime you want to delete: ");
        try {
            Showtime showtime = findShowtimeById(id);
            if (showtime.hasAtLeastOneSale()) {
                System.out.println("You cannot delete a showtime with sold tickets.");
                return false;
            }
            if (!ConsoleUtil.confirm("This will permanently delete the showtime from the system.")) {
                return false;
            }
            showtimeStorageManager.delete(id);
            saveToFile();
            System.out.println("Showtime successfully deleted.");
            return true;
        } catch (ShowtimeNotFoundException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public void cancelShowtime () {
        int id = ConsoleUtil.readInt("Enter the ID of the showtime you want to cancel: ");
        try {
            Showtime showtime = findShowtimeById(id);
            if (showtime.hasAtLeastOneSale()) {
                System.out.println("You cannot delete a showtime with sold tickets.");
            }
            if (ConsoleUtil.confirm("This will cancel the current showtime.")) {
                showtime.cancel();
                saveToFile();
                System.out.println("Showtime canceled.");
            }
        } catch (ShowtimeNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    public Showtime findShowtimeById (int id) throws ShowtimeNotFoundException {
        return showtimeStorageManager.findById(id).orElseThrow(() -> new ShowtimeNotFoundException("Showtime with ID: " + id + " not found."));
    }

    public List <Showtime> findAllShowtimes () {
        return showtimeStorageManager.findAll();
    }

    public List <Showtime> findAvailableShowtimes () {
        return showtimeStorageManager.findBy(Showtime::isAvailable);
    }

    private void loadFromFile () {
        Type type = new TypeToken<List <Showtime> >() {}.getType();
        List <Showtime> loaded = JsonUtil.read(SHOWTIME_FILE_PATH, type, ArrayList::new);
        showtimeStorageManager.clear();
        for (Showtime st : loaded) {
            try {
                showtimeStorageManager.add(st, true);
            } catch (DuplicateElementException ignored) {}
        }
    }

    private void saveToFile () {
        List <Showtime> list = new ArrayList<>(showtimeStorageManager.findAll());
        JsonUtil.write(SHOWTIME_FILE_PATH, list);
    }

}
