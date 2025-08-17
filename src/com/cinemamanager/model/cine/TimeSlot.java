package com.cinemamanager.model.cine;
import com.cinemamanager.util.common.ConsoleUtil;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class TimeSlot {

    private final Duration cleaningDuration;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public TimeSlot(LocalTime startTime, Duration movieDuration) {
        this.cleaningDuration = Duration.ofMinutes(30);
        this.startTime = startTime;
        this.endTime = startTime.plus(movieDuration.plus(cleaningDuration));
    }

    public Duration getCleaningDuration() {
        return cleaningDuration;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return  "Start time: " + ConsoleUtil.formatTime(startTime) + ".\n" +
                "End time: " + ConsoleUtil.formatTime(endTime) + ".\n";
    }

}
