package com.cinemamanager.manager.sale;
import com.cinemamanager.manager.cine.movie.MovieManager;
import com.cinemamanager.manager.cine.seat.SeatManager;
import com.cinemamanager.manager.cine.showtime.ShowtimeManager;
import com.cinemamanager.model.cine.Movie;
import com.cinemamanager.model.cine.Seat;
import com.cinemamanager.model.cine.Showtime;
import com.cinemamanager.model.sale.Ticket;
import com.cinemamanager.util.common.ConsoleUtil;
import com.cinemamanager.util.common.JsonUtil;
import com.cinemamanager.util.common.StorageManager;
import com.cinemamanager.util.common.enums.CollectionType;
import com.cinemamanager.util.common.exception.DuplicateElementException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public final class TicketManager {
    private final StorageManager <Integer, Ticket> ticketStorageManager;
    private final static String TICKET_FILE_PATH = "tickets.json";
    private int nextId;

    private final MovieManager movieManager;
    private final ShowtimeManager showtimeManager;
    private final SeatManager seatManager;

    public TicketManager (MovieManager movieManager,
                          ShowtimeManager showtimeManager,
                          SeatManager seatManager) {
        this.ticketStorageManager = new StorageManager<>(CollectionType.ARRAY_LIST);
        this.movieManager = movieManager;
        this.showtimeManager = showtimeManager;
        this.seatManager = seatManager;

        loadFromFile();

        OptionalInt maxId = ticketStorageManager.findAll().stream()
                .mapToInt(Ticket::getId)
                .max();
        this.nextId = maxId.isPresent() ? maxId.getAsInt() +1 : 1;
    }

    public List <Ticket> createTickets () {
        List <Ticket> tickets = new ArrayList<>();

        do {
            System.out.println("Generating new tickets...");
            List <Ticket> newTickets = createTicketsForShowtime();
            tickets.addAll(newTickets);
        } while (ConsoleUtil.confirm("Do you want to reserve seats for another showtime?"));

        for (Ticket ticket : tickets) {
            try {
                ticketStorageManager.add(ticket, false);
            } catch (DuplicateElementException e) {
                System.err.println(e.getMessage());;
            }
        }

        System.out.println("A total of " + tickets.size() + " tickets have been generated.");
        for (Ticket ticket : tickets) {
            System.out.println(ticket);
        }
        return tickets;
    }

    private List <Ticket> createTicketsForShowtime() {
        List <Ticket> tickets = new ArrayList<>();
        Optional <Movie> optionalSelectedMovie = movieManager.selectMovieByIdFromList(movieManager.findAllMovies());
        if (optionalSelectedMovie.isEmpty()) return tickets;

        Movie selectedMovie = optionalSelectedMovie.get();
        List <Showtime> showtimesByMovie = showtimeManager.getAvailableShowtimesByMovie(selectedMovie);
        Showtime selectedSt = showtimeManager.selectShowtimeByIdFromList(showtimesByMovie);
        if (selectedSt == null) return tickets;

        while (true) {
            Optional <Seat> seatOptional = showtimeManager.reserveSeat(selectedSt);
            if (seatOptional.isEmpty()) break;

            Ticket newTicket = new Ticket(nextId++, selectedSt, seatOptional.get().getId());
            tickets.add(newTicket);

            if (!ConsoleUtil.confirm("Please confirm if you want to reserve another seat")) break;
        }

        return tickets;
    }

    void loadFromFile () {
        Type type = new TypeToken<List <Ticket> >() {}.getType();
        List <Ticket> loaded = JsonUtil.read(TICKET_FILE_PATH, type, ArrayList::new);
        ticketStorageManager.clear();
        for (Ticket ticket : loaded) {
            try {
                ticketStorageManager.add(ticket, true);
            } catch (DuplicateElementException ignored) {}
        }
    }

    void saveToFile () {
        List <Ticket> list = new ArrayList<>(ticketStorageManager.findAll());
        JsonUtil.write(TICKET_FILE_PATH, list);
    }

}
