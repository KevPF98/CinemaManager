package com.cinemamanager.manager.cine.timeslot;
import com.cinemamanager.model.cine.TimeSlot;
import com.cinemamanager.util.common.ConsoleUtil;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

public final class ScheduleManager {
    private final Map <DayOfWeek, List <TimeSlot>> weeklySchedule;
    private final LocalTime openingTime;
    private final LocalTime closingTime;

    public ScheduleManager (LocalTime openingTime, LocalTime closingTime) {
        if(openingTime.isAfter(closingTime)){
            throw new IllegalArgumentException("\nThe opening time must precede the closing time.\n");
        }

        this.weeklySchedule = new EnumMap<>(DayOfWeek.class);
        this.openingTime = openingTime;
        this.closingTime = closingTime;

        for (DayOfWeek day : DayOfWeek.values()) {
            weeklySchedule.put(day, new ArrayList<>());
        }
    }

    public Optional <TimeSlot> createTimeSlot(DayOfWeek day, Duration movieDuration) {
        // Ask the user for the start time
        LocalTime startTime = ConsoleUtil.readTime("\nEnter start time");

        // Validate that the time is within opening and closing hours
        if (startTime.isBefore(openingTime) || startTime.isAfter(closingTime)) {
            System.out.println("\nStart time must be between " + openingTime + " and " + closingTime + ".\n");
            return Optional.empty();
        }

        // Create the time slot
        TimeSlot newTimeSlot = new TimeSlot(startTime, movieDuration);

        // Validate that the time slot does not exceed closing time
        if (newTimeSlot.getEndTime().isAfter(closingTime)) {
            System.out.println("\nThe time slot ends after closing time: " + closingTime + ".\n");
            return Optional.empty();
        }

        // Validate that the time slot does not overlap with existing ones
        List<TimeSlot> dayTimeSlots = getTimeSlotsForDay(day);
        for (TimeSlot existing : dayTimeSlots) {
            if (overlaps(existing, newTimeSlot)) {
                System.out.println("\nThis time slot overlaps with an existing one.\n");
                return Optional.empty();
            }
        }

        System.out.println("\nTime slot successfully created: " +
                newTimeSlot.getStartTime() + " - " + newTimeSlot.getEndTime() + ".\n");
        return Optional.of(newTimeSlot);
    }

// ADD A NEW TIME SLOT TO A SPECIFIC DAY ------------------------------------------------------

    public void addTimeSlot (DayOfWeek day, TimeSlot newTimeSlot) {
        List <TimeSlot> dayTimeSlots = getTimeSlotsForDay(day);
        dayTimeSlots.add(newTimeSlot);
        dayTimeSlots.sort(Comparator.comparing(TimeSlot::getStartTime));
    }

// CHECK IF TWO TIME SLOTS OVERLAP ------------------------------------------------------------

    public boolean overlaps (TimeSlot s1, TimeSlot s2) {
        LocalTime start1 = s1.getStartTime();
        LocalTime end1 = s1.getEndTime();
        LocalTime start2 = s2.getStartTime();
        LocalTime end2 = s2.getEndTime();

        return start1.isBefore(end2) && start2.isBefore(end1);
    }

// LIST TIME SLOTS FOR A SELECTED DAY ---------------------------------------------------------

    public void listTimeSlots() {
        DayOfWeek selectedDay = ConsoleUtil.readEnum(DayOfWeek.class, "\nSelect the day to display time slots");

        List <TimeSlot> timeSlots = getTimeSlotsForDay(selectedDay);
        if (timeSlots.isEmpty()) {
            System.out.println("\nNo time slots scheduled for " + ConsoleUtil.formatEnumName(selectedDay.name()) + ".\n");
        } else {
            System.out.println("\nTime slots for " + ConsoleUtil.formatEnumName(selectedDay.name()) + ":");
            int count = 1;
            for (TimeSlot timeslot : timeSlots) {
                System.out.println("\nTime slot " + count++ + ": " + timeslot + ".\n");
            }
        }
    }

// GET TIME SLOTS FOR A SPECIFIC DAY ----------------------------------------------------------

    private List <TimeSlot> getTimeSlotsForDay(DayOfWeek day) {
        return weeklySchedule.get(day);
    }

}
