package com.cinemamanager.manager.showtime;
import com.cinemamanager.exception.BusinessRuleException;
import com.cinemamanager.exception.ShowtimeNotFoundException;
import com.cinemamanager.manager.movie.MovieManager;
import com.cinemamanager.manager.room.RoomManager;
import com.cinemamanager.manager.seat.SeatManager;
import com.cinemamanager.manager.timeslot.ScheduleManager;
import com.cinemamanager.model.cine.*;
import com.cinemamanager.model.cine.dto.RoomReservation;
import com.cinemamanager.util.common.ConsoleUtil;
import com.cinemamanager.util.common.JsonUtil;
import com.cinemamanager.util.common.StorageManager;
import com.cinemamanager.util.common.enums.CollectionType;
import com.cinemamanager.util.common.exception.DuplicateElementException;
import com.cinemamanager.util.showtime.ShowtimeFactory;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.DayOfWeek;
import java.util.*;

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
            Showtime showtime = ShowtimeFactory.createShowtime(nextId++, this, movieManager, scheduleManager, roomManager);
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

    public void updateShowtime () {
        Showtime showtimeToUpdate = selectShowtimeByIdFromList(findAllShowtimes());
        if (showtimeToUpdate == null) return;

        if (showtimeToUpdate.hasAtLeastOneSale()) {
            System.out.println("You cannot modify a showtime that already has sold tickets.");
            return;
        }

        String option = readUpdateOption();

        switch (option) {
            case "1" -> updateMovie(showtimeToUpdate);
            case "2" -> updateRoom(showtimeToUpdate);
            case "3" -> updateTimeSlot(showtimeToUpdate);
            case "4" -> updatePrice(showtimeToUpdate);
            case "0" -> System.out.println("Returning to the previous menu...");
        }
    }

    public Showtime findShowtimeById (int id) throws ShowtimeNotFoundException {
        return showtimeStorageManager.findById(id).orElseThrow(() -> new ShowtimeNotFoundException("Showtime with ID: " + id + " not found."));
    }

    public List <Showtime> findAllShowtimes () {
        return showtimeStorageManager.findAll();
    }

    public List <Showtime> findAvailableShowtimes () {
        return showtimeStorageManager.findBy(st -> st.isAvailable() && st.hasAvailableSeats());
    }

    public void displayShowtimes (List <Showtime> showtimeList) {
        for (Showtime showtime : showtimeList) {
            System.out.println(showtime);
        }
    }

    public void displayAvailableShowtimes () {
        List <Showtime> availables = findAvailableShowtimes();
        displayShowtimes(availables);
    }

    public Showtime selectShowtimeByIdFromList (List <Showtime> showtimeList) {
        if (showtimeList.isEmpty()) {
            System.out.println("No showtimes available to select.");
            return null;
        }

        for (Showtime showtime : showtimeList) {
            System.out.println("ID: " + showtime.getId());
            System.out.println(showtime);
        }

        while (true) {
            int id = ConsoleUtil.readInt("Enter the ID of the showtime to select: ");

            try {
                Showtime selected = findShowtimeById(id);

                if (showtimeList.contains(selected)) {
                    return selected;
                } else {
                    System.out.println("The selected showtime ID is not in the current list.");
                }
            } catch (ShowtimeNotFoundException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public Optional <Seat> reserveSeat (Showtime showtime) {
        if (!showtime.hasAvailableSeats()) {
            System.out.println("No more seats available for this showtime.");
            return Optional.empty();
        }
        displaySeats(showtime);
        Seat selected = seatManager.selectSeat(showtime.getSeats());
        seatManager.occupySeat(showtime.getSeats(), selected.getId());
        if (!showtime.hasAvailableSeats()) showtime.markSoldOut();
        return Optional.of(selected);
    }

    public List <Showtime> getAvailableShowtimesByMovie (Movie movie) {
        return showtimeStorageManager.findBy(st -> st.isAvailable() && st.hasAvailableSeats() && st.getMovie().equals(movie));
    }

    public void showSeatsForShowtimeById() {
        int id = ConsoleUtil.readInt("Enter the ID of the showtime to display seats: ");
        try {
            Showtime showtime = findShowtimeById(id);
            displaySeats(showtime);
        } catch (ShowtimeNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    private String readUpdateOption () {
        String prompt = """
            What do you want to update?
            [1] Change movie
            [2] Change room
            [3] Change time slot
            [4] Change price

            [0] Back
            """;

        Set <String> validOptions = Set.of("0", "1", "2", "3", "4");
        return ConsoleUtil.readOption(prompt, validOptions);
    }

    private void updateMovie(Showtime showtime) {
        while (true) {
            List <Movie> movies = movieManager.findAllMovies();
            Movie newMovie = movieManager.selectMovieByIdFromList(movies);
            if (newMovie != null) {
                System.out.println("The movie duration has changed. You need to enter a new schedule:");
                Optional <TimeSlot> newTimeSlotOpt = scheduleManager.createTimeSlot(showtime.getShowDay(), newMovie.getDuration());
                if (newTimeSlotOpt.isPresent()) {
                    showtime.setMovie(newMovie);
                    showtime.setTimeSlot(newTimeSlotOpt.get());
                    saveShowtimeAndNotify("Movie updated successfully!");
                    break;
                }
            }
        }
    }

    private void updateRoom(Showtime showtime) {
        Optional<RoomReservation> roomReservationOptional = ShowtimeFactory.reserveRoom(this, scheduleManager, roomManager, showtime.getMovie());
        if (roomReservationOptional.isPresent()) {
            Room newRoom = roomReservationOptional.get().getRoom();
            showtime.setRoom(newRoom);
            showtime.loadSeats();
            saveShowtimeAndNotify("Room updated successfully and seats reset!");
        } else {
            System.out.println("No rooms available.");
        }
    }

    private void updateTimeSlot(Showtime showtime) {
        DayOfWeek newDay = ConsoleUtil.readEnum(DayOfWeek.class, "Select a day for the show time");
        Optional<TimeSlot> newTimeSlotOpt = scheduleManager.createTimeSlot(newDay, showtime.getMovie().getDuration());
        if (newTimeSlotOpt.isPresent()) {
            showtime.setTimeSlot(newTimeSlotOpt.get());
            saveShowtimeAndNotify("Time slot updated successfully!");
        }
    }

    private void updatePrice(Showtime showtime) {
        double newPrice = ConsoleUtil.readValidPrice("Enter the new price: ");
        showtime.setPrice(newPrice);
        saveShowtimeAndNotify("Price updated successfully!");
    }

    private void saveShowtimeAndNotify(String message) {
        saveToFile();
        System.out.println(message);
    }

    private void displaySeats(Showtime showtime) {
        int totalSeats = showtime.getRoom().getTotalSeats();
        List <Seat> seatList = new ArrayList<>(showtime.getSeats())
                .stream()
                .sorted()
                .toList();

        // Calculate columns and rows using Euclidean algorithm
        int columns = (int) Math.sqrt(totalSeats);
        while (totalSeats % columns != 0) {
            columns--;
        }
        int rows = totalSeats / columns;

        System.out.println("-----------------------------------------");
        System.out.println("           🎥 Screen 🎥\n");

        for (int i = 0; i < totalSeats; i++) {
            Seat seat = seatList.get(i);

            if (seat.isOccupied()) {
                System.out.printf("❌NA [%03d] ", seat.getId()); // NA = Not Available
            } else {
                System.out.printf("🎟️AV [%03d] ", seat.getId()); // AV = Available
            }

            if ((i + 1) % columns == 0) {
                System.out.println();
            }
        }

        System.out.println("              🚪 Entrance 🚪");
        System.out.println("-----------------------------------------");
        System.out.println("Legend: 🎟️AV [N] Available | ❌NA [N] Occupied (Not Available)\n");
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
