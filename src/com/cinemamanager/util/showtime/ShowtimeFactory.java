package com.cinemamanager.util.showtime;
import com.cinemamanager.exception.BusinessRuleException;
import com.cinemamanager.manager.movie.MovieManager;
import com.cinemamanager.manager.room.RoomManager;
import com.cinemamanager.manager.showtime.ShowtimeManager;
import com.cinemamanager.manager.timeslot.ScheduleManager;
import com.cinemamanager.model.cine.Movie;
import com.cinemamanager.model.cine.Room;
import com.cinemamanager.model.cine.Showtime;
import com.cinemamanager.model.cine.TimeSlot;
import com.cinemamanager.model.cine.dto.RoomReservation;
import com.cinemamanager.util.common.ConsoleUtil;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

public final class ShowtimeFactory {

    public static Showtime createShowtime (int showTimeId,
                                           ShowtimeManager showtimeManager,
                                           MovieManager movieManager,
                                           ScheduleManager scheduleManager,
                                           RoomManager roomManager)
                                           throws BusinessRuleException
    {
        List<Movie> moviesNowShowing = movieManager.getMovieListings();
        Movie selectedMovie = movieManager.selectMovieByIdFromList(moviesNowShowing);

        if (selectedMovie == null) {
            throw new BusinessRuleException(
                    "There must be at least one movie currently showing to create a showtime."
            );
        }

        RoomReservation reservation = reserveRoom (
                showtimeManager,
                scheduleManager,
                roomManager,
                selectedMovie
        ).orElseThrow(() -> new BusinessRuleException("No rooms available."));

        double price = ConsoleUtil.readValidPrice("Enter the ticket price: ");

        return new Showtime(showTimeId, selectedMovie, reservation.getRoom(), reservation.getTimeSlot(), reservation.getDay(), price);
    }


    public static Optional <RoomReservation> reserveRoom(
            ShowtimeManager showtimeManager,
            ScheduleManager scheduleManager,
            RoomManager roomManager,
            Movie selectedMovie
    ) {
        while (true)
        {
            DayOfWeek showDay = ConsoleUtil.readEnum(DayOfWeek.class, "Select a day for the show time");
            Optional<TimeSlot> optionalTimeSlot = scheduleManager.createTimeSlot(showDay, selectedMovie.getDuration());

            if (optionalTimeSlot.isPresent()) {
                TimeSlot timeSlot = optionalTimeSlot.get();
                TreeSet<Room> availableRooms = getAvailableRoomsForTimeSlot(
                        showtimeManager, scheduleManager, roomManager, showDay, timeSlot
                );

                if (availableRooms.isEmpty()) return Optional.empty();


                Room selectedRoom = roomManager.selectRoomByIdFromSet(availableRooms);
                if (selectedRoom != null) {
                    System.out.println("Room successfully reserved for the day: " + showDay + " at " + ConsoleUtil.formatTime(timeSlot.getStartTime()));
                    return Optional.of(new RoomReservation(showDay, timeSlot, selectedRoom));
                }
                System.out.println("The room is unavailable for reservation at the selected date and time.");
            }

            System.out.println("Please, select another day and/or time slot.");
        }
    }

    private static TreeSet <Room> getAvailableRoomsForTimeSlot (ShowtimeManager showtimeManager, ScheduleManager scheduleManager, RoomManager roomManager, DayOfWeek day, TimeSlot timeSlot) {
        TreeSet <Room> availableRooms = roomManager.getActiveRooms();

        for (Showtime showtime : showtimeManager.findAllShowtimes()) {
            if (showtime.getShowDay().equals(day) && scheduleManager.overlaps(showtime.getTimeSlot(), timeSlot)) {
                availableRooms.remove(showtime.getRoom());
            }
        }

        return availableRooms;
    }

}
