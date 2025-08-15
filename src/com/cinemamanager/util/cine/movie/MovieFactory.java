package com.cinemamanager.util.cine.movie;

import com.cinemamanager.enums.cine.movie.*;
import com.cinemamanager.model.cine.Movie;
import com.cinemamanager.util.common.ConsoleUtil;

import java.time.Duration;

public final class MovieFactory {

    public static Movie createMovie (int movieId) {
        System.out.println("Registering a new movie...");
        String title = ConsoleUtil.readCapitalizedString("Enter the movie title: ");
        Language audio = ConsoleUtil.readEnum(Language.class, "Select the audio language");
        Language subtitles = ConsoleUtil.readEnum(Language.class, "Select the subtitles language");
        Duration duration = ConsoleUtil.readDuration("Enter the movie duration");
        String producer = ConsoleUtil.readCapitalizedString("Enter the producer name: ");
        String director = ConsoleUtil.readCapitalizedString("Enter the director name: ");
        int releaseYear = ConsoleUtil.readValidReleaseYear("Enter the release year: ");
        Country country = ConsoleUtil.readEnum(Country.class, "Select the country");
        AgeRating ageRating = ConsoleUtil.readEnum(AgeRating.class, "Select the age rating");
        MovieGenre movieGenre = ConsoleUtil.readEnum(MovieGenre.class, "Select the genre");
        return new Movie(movieId, title, audio, subtitles, duration, producer, director, releaseYear, country, ageRating, movieGenre);
    }

}
