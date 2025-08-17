package com.cinemamanager.manager.cine.showtime;
import com.cinemamanager.enums.cine.movie.MovieStatus;
import com.cinemamanager.enums.cine.showtime.ShowtimeStatus;
import com.cinemamanager.exception.BusinessRuleException;
import com.cinemamanager.manager.cine.movie.MovieManager;
import com.cinemamanager.manager.cine.room.RoomManager;
import com.cinemamanager.manager.cine.seat.SeatManager;
import com.cinemamanager.manager.cine.timeslot.ScheduleManager;
import com.cinemamanager.model.cine.*;
import com.cinemamanager.model.cine.dto.RoomReservation;
import com.cinemamanager.util.common.ConsoleUtil;
import com.cinemamanager.util.common.JsonUtil;
import com.cinemamanager.util.common.StorageManager;
import com.cinemamanager.util.common.enums.CollectionType;
import com.cinemamanager.util.common.exception.DuplicateElementException;
import com.cinemamanager.util.cine.showtime.ShowtimeFactory;
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

    public ShowtimeManager (RoomManager roomManager,
                            ScheduleManager scheduleManager,
                            MovieManager movieManager) {
        this.showtimeStorageManager = new StorageManager<>(CollectionType.ARRAY_LIST);
        this.roomManager = roomManager;
        this.scheduleManager = scheduleManager;
        this.movieManager = movieManager;

        loadFromFile();

        OptionalInt maxId = showtimeStorageManager.findAll().stream()
                .mapToInt(Showtime::getId)
                .max();
        this.nextId = maxId.isPresent() ? maxId.getAsInt() +1 : 1;
    }

    public void addShowTime () {
        try {
            Showtime showtime = ShowtimeFactory.createShowtime(nextId++, this, movieManager, scheduleManager, roomManager);
            showtimeStorageManager.add(showtime, false);
            saveToFile();
            System.out.println("\nShowtime registered successfully!\n");
        } catch (BusinessRuleException | DuplicateElementException e) {
            System.err.println("\nError registering the showtime: " + e.getMessage() + ".\n");
        }
    }

    public void deleteShowtimeById () {
        Optional <Showtime> optionalShowtime = selectShowtimeByIdFromList(findAllShowtimes());

        if (optionalShowtime.isEmpty()) return;

        Showtime showtime = optionalShowtime.get();

        if (showtime.hasAtLeastOneSale()) {
            System.out.println("\nYou cannot delete a showtime with sold tickets.\n");
            return;
        }
        if (!showtime.getStatus().equals(ShowtimeStatus.CANCELLED)) {
            System.out.println("\nOnly canceled showtimes can be deleted.\n");
            return;
        }
        if (!ConsoleUtil.confirm("\nThis will permanently delete the showtime from the system.\nDo you want to proceed?")) {
            return;
        }
        Movie movie = showtime.getMovie();
        if (!isMovieShowing(movie)) movie.setStatus(MovieStatus.UNAVAILABLE);
        showtimeStorageManager.delete(showtime);
        saveToFile();
        System.out.println("\nShowtime successfully deleted.\n");
    }

    public void cancelShowtime () {
        Optional <Showtime> optionalShowtime = selectShowtimeByIdFromList(findAllShowtimes());

        if (optionalShowtime.isEmpty()) return;

        Showtime showtime = optionalShowtime.get();
        if (showtime.getStatus().equals(ShowtimeStatus.CANCELLED)) {
            System.out.println("This showtime has already been canceled.");
            return;
        }
        if (showtime.hasAtLeastOneSale()) {
            System.out.println("\nYou cannot delete a showtime with sold tickets.\n");
            return;
        }
        if (ConsoleUtil.confirm("\nThis will cancel the current showtime.\nDo you want to proceed?")) {
            Movie movie = showtime.getMovie();
            if (!isMovieShowing(movie)) movie.setStatus(MovieStatus.UNAVAILABLE);
            showtime.cancel();
            saveToFile();
            System.out.println("\nShowtime canceled.\n");
        }
    }

    public void updateShowtime () {
        Optional <Showtime> optionalShowtime = selectShowtimeByIdFromList(findAllShowtimes());

        if (optionalShowtime.isEmpty()) {
            System.out.println("Showtime not found.");
            return;
        }

        Showtime showtimeToUpdate = optionalShowtime.get();

        if (showtimeToUpdate.hasAtLeastOneSale()) {
            System.out.println("\nYou cannot modify a showtime that already has sold tickets.\n");
            return;
        }

        String option = readUpdateOption();

        switch (option) {
            case "1" -> updateMovieOfShowtime(showtimeToUpdate);
            case "2" -> updateRoomOfShowtime(showtimeToUpdate);
            case "3" -> updateTimeSlotOfShowtime(showtimeToUpdate);
            case "4" -> updatePriceOfShowtime(showtimeToUpdate);
            case "0" -> System.out.println("\nReturning to the previous menu...\n");
        }
    }

    public Optional <Showtime> findShowtimeById (int id) {
        return showtimeStorageManager.findById(id);
    }

    public List <Showtime> findAllShowtimes () {
        return showtimeStorageManager.findAll();
    }

    public List <Showtime> findAvailableShowtimes () {
        return showtimeStorageManager.findBy(st -> st.isAvailable() && st.hasAvailableSeats());
    }

    public void showAvailableShowtimes () {
        List <Showtime> availableShowtimes = findAvailableShowtimes();
        if (availableShowtimes.isEmpty()) {
            System.out.println("No showtimes available.");
            return;
        }
        for (Showtime showtime : availableShowtimes) {
            System.out.println(showtime);
            System.out.println("These are the seats available for this showtime:");
            displaySeats(showtime);
        }
    }

    public boolean isMovieShowing(Movie movie) {
        return showtimeStorageManager
                .findFirstBy(showtime -> showtime.getMovie().equals(movie) && showtime.getStatus().equals(ShowtimeStatus.AVAILABLE))
                .isPresent();
    }

    public void displayShowtimes (List <Showtime> showtimeList) {
        for (Showtime showtime : showtimeList) {
            System.out.println(showtime);
        }
    }

    public Optional <Showtime> selectShowtimeByIdFromList (List <Showtime> showtimeList) {
        if (showtimeList.isEmpty()) {
            System.out.println("\nNo showtimes available to select.\n");
            return Optional.empty();
        }

        while (true) {
            displayShowtimesWithId(showtimeList);

            int id = ConsoleUtil.readInt("\nEnter the ID of the showtime to select: ");

            Optional <Showtime> optionalShowtime = findShowtimeById(id);

            if (optionalShowtime.isEmpty()) {
                System.out.println("Showtime with ID: " + id + " not found.");
            }
            else {
                Showtime selected = optionalShowtime.get();

                if (showtimeList.contains(selected)) {
                    return Optional.of(selected);
                } else {
                    System.out.println("\nThe selected showtime ID is not in the current list.\n");
                }
            }
            if (!ConsoleUtil.confirm("\nDo you want to try with another ID?")) {
                return Optional.empty();
            }
        }
    }

    public Optional <Seat> reserveSeat (Showtime showtime) {
        if (!showtime.hasAvailableSeats()) {
            System.out.println("\nNo more seats available for this showtime.\n");
            return Optional.empty();
        }
        displaySeats(showtime);
        Optional <Seat> optionalSelectedSea = SeatManager.selectSeat(showtime.getSeats());
        optionalSelectedSea.ifPresent(selected -> SeatManager.occupySeat(showtime.getSeats(), selected.getId()));

        if (!showtime.hasAvailableSeats()) showtime.markSoldOut();
        return optionalSelectedSea;
    }

    public List <Showtime> getAvailableShowtimesByMovie (Movie movie) {
        return showtimeStorageManager.findBy(st -> st.isAvailable() && st.hasAvailableSeats() && st.getMovie().equals(movie));
    }

    public void showSeatsForShowtimeById() {
        int id = ConsoleUtil.readInt("\nEnter the ID of the showtime to display seats: ");

        Optional <Showtime> optionalShowtime = findShowtimeById(id);

        if (optionalShowtime.isEmpty()) {
            System.out.println("Showtime with ID: " + id + " not found.");
            return;
        }

        Showtime showtime = optionalShowtime.get();

        displaySeats(showtime);

    }

    public boolean roomHasShowtimes (Room room) {
        return showtimeStorageManager.findFirstBy(st -> st.getRoom().equals(room)).isPresent();
    }

    public TreeSet <Room> getRoomsWithActiveShows () {
        List <Room> rooms = showtimeStorageManager.findAll()
                .stream()
                .map(Showtime::getRoom)
                .distinct()
                .toList();

        return new TreeSet<>(rooms);
    }

    public TreeSet<Room> getRoomsWithoutActiveShows() {
        TreeSet <Room> allRooms = roomManager.findAllRooms();
        TreeSet <Room> roomsWithShows = getRoomsWithActiveShows();

        List <Room> roomsWithoutShows = allRooms.stream()
                                        .filter(room -> !roomsWithShows.contains(room))
                                        .toList();

        return new TreeSet<>(roomsWithoutShows);
    }

    private void displayShowtimesWithId (List <Showtime> showtimeList) {
        for (Showtime showtime : showtimeList) {
            System.out.println("ID: " + showtime.getId());
            System.out.println(showtime);
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
            >""" + " ";

        Set <String> validOptions = Set.of("0", "1", "2", "3", "4");
        return ConsoleUtil.readOption(prompt, validOptions);
    }

    private void updateMovieOfShowtime(Showtime showtime) {
        List<Movie> movies = movieManager.findAllMovies();
        Optional<Movie> optionalNewMovie = movieManager.selectMovieByIdFromList(movies);

        if (optionalNewMovie.isEmpty()) {
            System.out.println("\nNo movie selected. Operation cancelled.\n");
            return;
        }

        Movie newMovie = optionalNewMovie.get();
        System.out.println("\nThe movie duration has changed. You need to enter a new schedule: ");

        Optional<TimeSlot> newTimeSlotOpt = scheduleManager.createTimeSlot(showtime.getShowDay(), newMovie.getDuration());
        if (newTimeSlotOpt.isEmpty()) {
            System.out.println("\nNo valid time slot selected. Operation cancelled.\n");
            return;
        }

        newTimeSlotOpt.ifPresent(timeSlot -> {
            showtime.setMovie(newMovie);
            showtime.setTimeSlot(timeSlot);
            saveShowtimeAndNotify("\nMovie updated successfully!\n");
        });
    }

    private void updateRoomOfShowtime(Showtime showtime) {
        Optional<RoomReservation> roomReservationOptional = ShowtimeFactory.reserveRoom(this, scheduleManager, roomManager, showtime.getMovie());
        if (roomReservationOptional.isPresent()) {
            Room newRoom = roomReservationOptional.get().getRoom();
            showtime.setRoom(newRoom);
            showtime.loadSeats();
            saveShowtimeAndNotify("\nRoom updated successfully and seats reset!\n");
        } else {
            System.out.println("\nNo rooms available.\n");
        }
    }

    private void updateTimeSlotOfShowtime(Showtime showtime) {
        DayOfWeek newDay = ConsoleUtil.readEnum(DayOfWeek.class, "\nSelect a day for the show time");
        Optional<TimeSlot> newTimeSlotOpt = scheduleManager.createTimeSlot(newDay, showtime.getMovie().getDuration());
        if (newTimeSlotOpt.isPresent()) {
            showtime.setTimeSlot(newTimeSlotOpt.get());
            saveShowtimeAndNotify("\nTime slot updated successfully!\n");
        }
    }

    private void updatePriceOfShowtime(Showtime showtime) {
        double newPrice = ConsoleUtil.readValidPrice("\nEnter the new price: ");
        showtime.setPrice(newPrice);
        saveShowtimeAndNotify("\nPrice updated successfully!\n");
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

    public void saveToFile () {
        List <Showtime> list = new ArrayList<>(showtimeStorageManager.findAll());
        JsonUtil.write(SHOWTIME_FILE_PATH, list);
    }

}
