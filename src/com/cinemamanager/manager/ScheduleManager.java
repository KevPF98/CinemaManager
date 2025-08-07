package com.cinemamanager.manager;
import com.cinemamanager.model.cine.Showtime;
import com.cinemamanager.util.ConsoleUtil;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ScheduleManager {
    private final Map <DayOfWeek, List<Showtime>> weeklySchedule;
    private final LocalTime openingTime;
    private final LocalTime closingTime;

    // ANTES DE CREAR EL OBJETO, VALIDAR QUE HORARIO APERTURA NO SEA ANTES A HORARIO CIERRE
    public ScheduleManager (LocalTime openingTime, LocalTime closingTime) {
        this.weeklySchedule = new HashMap<>();
        this.openingTime = openingTime;
        this.closingTime = closingTime;

        for (DayOfWeek day : DayOfWeek.values()) {
            weeklySchedule.put(day, new ArrayList<>());
        }
    }

    public Showtime createShowtime (DayOfWeek day, Duration movieDuration) {
        try {
            // Ask the user for the start time
            LocalTime startTime = ConsoleUtil.readTime("Enter start time");

            // Validate that the time is within opening and closing hours
            if (startTime.isBefore(openingTime) || startTime.isAfter(closingTime)) {
                throw new IllegalArgumentException("Start time must be between " + openingTime + " and " + closingTime);
            }

            // Create the showtime
            Showtime newShowtime = new Showtime(startTime, movieDuration);

            // Validate that the showtime does not exceed closing time
            if (newShowtime.getEndTime().isAfter(closingTime)) {
                throw new IllegalArgumentException("The showtime ends after closing time: " + closingTime);
            }

            // Validate that the showtime does not overlap with existing ones
            List <Showtime> dayShowtimes = getShowtimesForDay(day);
            for (Showtime existing : dayShowtimes) {
                if (overlaps(existing, newShowtime)) {
                    System.out.println("This showtime overlaps with an existing one.");
                    return null;
                }
            }

            System.out.println("Showtime successfully created: " +
                    newShowtime.getStartTime() + " - " + newShowtime.getEndTime());
            return newShowtime;

        } catch (Exception e) {
            System.out.println("Error creating showtime: " + e.getMessage());
            return null;
        }
    }

// ADD A NEW SHOWTIME TO A SPECIFIC DAY ------------------------------------------------------

    public void addShowtime (DayOfWeek day, Showtime newShowtime) {
        List <Showtime> dayShowtimes = getShowtimesForDay(day);
        dayShowtimes.add(newShowtime);
    }

// CHECK IF TWO SHOWTIMES OVERLAP ------------------------------------------------------------

    public boolean overlaps (Showtime s1, Showtime s2) {
        LocalTime start1 = s1.getStartTime();
        LocalTime end1 = s1.getEndTime();
        LocalTime start2 = s2.getStartTime();
        LocalTime end2 = s2.getEndTime();

        return start1.isBefore(end2) && start2.isBefore(end1);
    }

// GET SHOWTIMES FOR A SPECIFIC DAY ----------------------------------------------------------

    public List <Showtime> getShowtimesForDay(DayOfWeek day) {
        return weeklySchedule.get(day);
    }

// LIST SHOWTIMES FOR A SELECTED DAY ---------------------------------------------------------

    public void listShowtimes() {
        DayOfWeek selectedDay = ConsoleUtil.readEnum(DayOfWeek.class, "Select the day to display showtimes");

        List <Showtime> showtimes = getShowtimesForDay(selectedDay);
        if (showtimes.isEmpty()) {
            System.out.println("No showtimes scheduled for " + ConsoleUtil.formatEnumName(selectedDay.name()) + ".");
        } else {
            int count = 1;
            for (Showtime showtime : showtimes) {
                System.out.println("Showtime " + count++ + ": " + showtime);
            }
        }
    }

}
