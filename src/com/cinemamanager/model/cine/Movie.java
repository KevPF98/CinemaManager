package com.cinemamanager.model.cine;

import com.cinemamanager.enums.*;
import com.cinemamanager.iface.Identifiable;
import com.cinemamanager.util.ConsoleUtil;

import java.time.Duration;
import java.util.Objects;

public final class Movie implements Identifiable <Integer> {

    private final int movieId;
    private String title;
    private Language audio;
    private Language subtitles;
    private Duration duration;
    private String producer;
    private String director;
    private int releaseYear;
    private Country country;
    private AgeRating ageRating;
    private MovieGenre genre;
    private MovieStatus status;

    public Movie (int movieId, String title, Language audio, Language subtitles, Duration duration, String producer, String director, int releaseYear, Country country, AgeRating ageRating, MovieGenre genre, MovieStatus status) {
        this.movieId = movieId;
        this.title = title;
        this.audio = audio;
        this.subtitles = subtitles;
        this.duration = duration;
        this.producer = producer;
        this.director = director;
        this.releaseYear = releaseYear;
        this.country = country;
        this.ageRating = ageRating;
        this.genre = genre;
        this.status = status;
    }

    @Override
    public Integer getId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Language getAudio() {
        return audio;
    }

    public void setAudio(Language audio) {
        this.audio = audio;
    }

    public Language getSubtitles() {
        return subtitles;
    }

    public void setSubtitles(Language subtitles) {
        this.subtitles = subtitles;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public AgeRating getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(AgeRating ageRating) {
        this.ageRating = ageRating;
    }

    public MovieGenre getGenre() {
        return genre;
    }

    public void setGenre(MovieGenre genre) {
        this.genre = genre;
    }

    public MovieStatus getStatus() {
        return status;
    }

    public void setStatus(MovieStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return movieId == movie.movieId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(movieId);
    }

    @Override
    public String toString() {
        return "--------------------------\n" +
                "Title: " + title + ".\n" +
                "Audio: " + ConsoleUtil.formatEnumName(audio.name()) + ".\n" +
                "Subtitles: " + ConsoleUtil.formatEnumName(subtitles.name()) + ".\n" +
                "Duration: " + formatDuration (duration) + ".\n" +
                "Producer: " + producer + ".\n" +
                "Director: " + director + ".\n" +
                "Year: " + releaseYear + ".\n" +
                "Country of origin: " + ConsoleUtil.formatEnumName(country.name()) + ".\n" +
                "Age Rating: " + ConsoleUtil.formatEnumName(ageRating.name()) + ".\n" +
                "Genre: " + ConsoleUtil.formatEnumName(genre.name()) + ".\n" +
                "Status: " + ConsoleUtil.formatEnumName(status.name()) + ".\n" +
                "--------------------------\n";
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        return String.format("%dh %02dmin", hours, minutes);
    }

}
