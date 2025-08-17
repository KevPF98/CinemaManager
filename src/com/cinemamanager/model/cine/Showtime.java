package com.cinemamanager.model.cine;
import com.cinemamanager.enums.cine.showtime.ShowtimeStatus;
import com.cinemamanager.iface.Identifiable;
import com.cinemamanager.util.common.ConsoleUtil;
import java.time.DayOfWeek;
import java.util.Objects;
import java.util.TreeSet;

public final class Showtime implements Identifiable <Integer> {

    private final int showtimeId;
    private Movie movie;
    private Room room;
    private TimeSlot timeSlot;
    private DayOfWeek showDay;
    private double price;
    private ShowtimeStatus status;
    private final TreeSet <Seat> seats;

    public Showtime(int showtimeId, Movie movie, Room room, TimeSlot timeSlot, DayOfWeek showDay, double price) {
        this.showtimeId = showtimeId;
        this.movie = movie;
        this.room = room;
        this.timeSlot = timeSlot;
        this.showDay = showDay;
        this.price = price;
        this.status = ShowtimeStatus.AVAILABLE;
        this.seats = new TreeSet<>();
        loadSeats();
    }

    public void loadSeats() {
        seats.clear();
        for (int i = 1; i <= room.getTotalSeats(); i++) {
            this.seats.add(new Seat(i));
        }
    }

    public Integer getId() {
        return showtimeId;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public DayOfWeek getShowDay() {
        return showDay;
    }

    public void setShowDay(DayOfWeek showDay) {
        this.showDay = showDay;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public ShowtimeStatus getStatus() {
        return status;
    }

    public void setStatus(ShowtimeStatus status) {
        this.status = status;
    }

    public TreeSet <Seat> getSeats() {
        return seats;
    }

    // Methods related to seats availability:

    public boolean hasAvailableSeats() {
        for (Seat seat : seats) {
            if (!seat.isOccupied()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAtLeastOneSale() {
        for (Seat seat : seats) {
            if (seat.isOccupied()) {
                return true;
            }
        }
        return false;
    }

    public int countAvailableSeats() {
        int count = 0;
        for (Seat seat : seats) {
            if (!seat.isOccupied()) {
                count++;
            }
        }
        return count;
    }

    public void occupySeat(Seat targetSeat) {
        for (Seat seat : seats) {
            if (seat.equals(targetSeat)) {
                seat.setOccupied(true);
            }
        }
    }

    public void freeSeat(Seat targetSeat) {
        for (Seat seat : seats) {
            if (seat.equals(targetSeat)) {
                seat.setOccupied(false);
            }
        }
    }

    // Status change helpers:

    public void markSoldOut() {
        setStatus(ShowtimeStatus.SOLD_OUT);
    }

    public void cancel() {
        setStatus(ShowtimeStatus.CANCELLED);
    }

    public void makeAvailable() {
        setStatus(ShowtimeStatus.AVAILABLE);
    }

    public boolean isAvailable() {
        return status == ShowtimeStatus.AVAILABLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Showtime showtime = (Showtime) o;

        return showtimeId == showtime.showtimeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(showtimeId);
    }

    @Override
    public String toString() {
        return  "Movie: " + movie.getTitle() + ".\n" +
                "Room type: " + ConsoleUtil.formatEnumName(room.getType().name()) + ".\n" +
                "Room number: " + room.getId() + ".\n" +
                 timeSlot +
                "Day: " + ConsoleUtil.formatEnumName(showDay.name()) + ".\n" +
                "Price: " + price + ".\n" +
                "Current status: " + ConsoleUtil.formatEnumName(status.name()) + ".\n" +
                "\n-----------------\n";
    }

}
