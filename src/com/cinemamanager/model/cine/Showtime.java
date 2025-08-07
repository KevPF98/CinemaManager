package com.cinemamanager.model.cine;
import java.time.Duration;
import java.time.LocalTime;

public final class Showtime {

    private Duration cleaningDuration;
    private LocalTime startTime;
    private LocalTime endTime;

    public Showtime(LocalTime startTime, Duration movieDuration) {
        this.cleaningDuration = Duration.ofMinutes(30);
        this.startTime = startTime;
        this.endTime = startTime.plus(movieDuration.plus(cleaningDuration));
    }

    public Duration getCleaningDuration() {
        return cleaningDuration;
    }

    public void setCleaningDuration(Duration cleaningDuration) {
        this.cleaningDuration = cleaningDuration;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return  "--------------------------\n" +
                "Start time: " + startTime + ".\n" +
                "End time: " + endTime + ".\n";
    }

}
