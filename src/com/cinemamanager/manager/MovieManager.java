package com.cinemamanager.manager;
import com.cinemamanager.enums.*;
import com.cinemamanager.exception.DuplicateElementException;
import com.cinemamanager.exception.MovieNotFoundException;
import com.cinemamanager.model.cine.Movie;
import com.cinemamanager.util.ConsoleUtil;
import com.cinemamanager.util.JsonUtil;
import com.cinemamanager.util.MovieFactory;
import com.cinemamanager.util.StorageManager;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

public final class MovieManager {
    private final StorageManager <Integer, Movie> movieStorageManager;
    private static final String MOVIE_FILE_PATH = "movie.json";
    private int nextId;

    public MovieManager () {
        this.movieStorageManager = new StorageManager<>(CollectionType.ARRAY_LIST);
        loadFromFile();

        OptionalInt maxId = movieStorageManager.findAll().stream()
                .mapToInt(Movie::getId)
                .max();
        this.nextId = maxId.isPresent() ? maxId.getAsInt() +1 : 1;
    }

    public void addMovie () {
        Movie newMovie = MovieFactory.createMovie (nextId);
        try {
            movieStorageManager.add(newMovie, false);
            nextId++;
            saveToFile();
            System.out.println("\nMovie registered successfully!\n");
        } catch (DuplicateElementException e) {
            System.out.println("Error registering the movie: " + e.getMessage());
        }
    }

    public void deleteMovieById (int id) {
        movieStorageManager.delete(id);
        saveToFile();
    }

    public Movie findMovieById (int id) throws MovieNotFoundException {
        return movieStorageManager.findById(id).orElseThrow(() -> new MovieNotFoundException("Movie with ID: " + id + " not found."));
    }

    public List <Movie> searchMoviesByTitleRegex (String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return movieStorageManager.findBy(movie -> pattern.matcher(movie.getTitle()).find());
    }

    public List <Movie> searchMoviesByAudio (Language desiredAudio) {
        return movieStorageManager.findBy(m -> m.getAudio().equals(desiredAudio));
    }

    public List <Movie> searchMoviesBySubs (Language desiredSubs) {
        return movieStorageManager.findBy(m -> m.getSubtitles().equals(desiredSubs));
    }

    public List<Movie> searchMoviesWithMinDuration (Duration minDuration) {
        return movieStorageManager.findBy(m -> m.getDuration().compareTo(minDuration) >= 0);
    }

    public List<Movie> searchMoviesWithMaxDuration (Duration maxDuration) {
        return movieStorageManager.findBy(m -> m.getDuration().compareTo(maxDuration) <= 0);
    }

    public List <Movie> searchMoviesByProducerRegex (String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return movieStorageManager.findBy(movie -> pattern.matcher(movie.getProducer()).find());
    }

    public List <Movie> searchMoviesByDirectorRegex (String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return movieStorageManager.findBy(movie -> pattern.matcher(movie.getDirector()).find());
    }

    public List <Movie> searchMoviesReleasedFrom (int year) {
        return movieStorageManager.findBy(m -> m.getReleaseYear() >= year);
    }

    public List <Movie> searchMoviesFrom (Country country) {
        return movieStorageManager.findBy(m -> m.getCountry().equals(country));
    }

    public List <Movie> searchMoviesByAgeRating (AgeRating ageRating) {
        return movieStorageManager.findBy(m -> m.getAgeRating().equals(ageRating));
    }

    public List <Movie> searchMoviesByGenre (MovieGenre movieGenre) {
        return movieStorageManager.findBy(m -> m.getGenre().equals(movieGenre));
    }

    public List <Movie> searchMoviesByStatus (MovieStatus movieStatus) {
        return movieStorageManager.findBy(m -> m.getStatus().equals(movieStatus));
    }

    public List <Movie> findAllMovies () {
        return movieStorageManager.findAll();
    }

    public void displayMovieList (List <Movie> movieList) {
        for (Movie movie : movieList) {
            System.out.println(movie);
        }
    }

    public List <Movie> getMovieListings () {
        return movieStorageManager.findBy(m -> m.getStatus().equals(MovieStatus.NOW_SHOWING));
    }

    public void showMovieListings () {
        displayMovieList(getMovieListings());
    }

    public void updateMovie () {
        List <Movie> movies = findAllMovies();
        Movie movieToUpdate = selectMovieByIdFromList(movies);
        if (movieToUpdate != null) {

            String prompt = """
                            What do you want to do?
                            [1]  Change the title.
                            [2]  Change the audio language.
                            [3]  Change the subtitle language.
                            [4]  Change the duration.
                            [5]  Change the producer's name.
                            [6]  Change the director's name.
                            [7]  Change the release year.
                            [8]  Change the country of origin.
                            [9]  Change the age rating.
                            [10] Change the genre.
                            [11] Change the status.
                            [12] Change all.
                            
                            [0] Back.
                            """;

            Set <String> validOptions = new HashSet<>();
            for (int i = 0; i <= 12; i++) {
                validOptions.add(String.valueOf(i));
            }
            String chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            switch (chosenOption) {
                case "1"  -> changeTitle(movieToUpdate);
                case "2"  -> changeAudioLanguage(movieToUpdate);
                case "3"  -> changeSubLanguage(movieToUpdate);
                case "4"  -> changeDuration(movieToUpdate);
                case "5"  -> changeProducer(movieToUpdate);
                case "6"  -> changeDirector(movieToUpdate);
                case "7"  -> changeYear(movieToUpdate);
                case "8"  -> changeCountry(movieToUpdate);
                case "9"  -> changeAgeRating(movieToUpdate);
                case "10" -> changeGenre(movieToUpdate);
                case "11" -> changeStatus(movieToUpdate);
                case "12" -> changeAll(movieToUpdate);
                case "0"  -> {}
            }
        }
    }

    public Movie selectMovieByIdFromList(List <Movie> movieList) {
        if (movieList.isEmpty()) {
            System.out.println("No movies available to select.");
            return null;
        }

        for (Movie movie : movieList) {
            System.out.println("ID: " + movie.getId());
            System.out.println(movie);
        }

        while (true) {
            int id = ConsoleUtil.readInt("Enter the ID of the movie to select: ");

            try {
                Movie selected = findMovieById(id);

                if (movieList.contains(selected)) {
                    return selected;
                } else {
                    System.out.println("The selected movie ID is not in the current list.");
                }
            } catch (MovieNotFoundException e) {
                System.err.println (e.getMessage());
            }
        }
    }

    private void changeTitle (Movie movieToUpdate) {
        String newTitle = ConsoleUtil.readCapitalizedString("Enter the new title: ");
        movieToUpdate.setTitle(newTitle);
        System.out.println("Title changed successfully!");
        saveToFile();
    }

    private void changeAudioLanguage (Movie movieToUpdate) {
        Language newAudio = ConsoleUtil.readEnum(Language.class, "Select the audio language");
        movieToUpdate.setAudio(newAudio);
        System.out.println("Language changed successfully!");
        saveToFile();
    }

    private void changeSubLanguage (Movie movieToUpdate) {
        Language newSub = ConsoleUtil.readEnum(Language.class, "Select the subtitle language");
        movieToUpdate.setAudio(newSub);
        System.out.println("Subtitle changed successfully!");
        saveToFile();
    }

    private void changeDuration (Movie movieToUpdate) {
        Duration newDuration = ConsoleUtil.readDuration("Enter the new movie duration: ");
        movieToUpdate.setDuration(newDuration);
        System.out.println("Duration of the movie changed successfully!");
        saveToFile();
    }

    private void changeProducer (Movie movieToUpdate) {
        String newProducer = ConsoleUtil.readCapitalizedString("Enter the new producer name: ");
        movieToUpdate.setProducer(newProducer);
        System.out.println("Producer changed successfully!");
        saveToFile();
    }

    private void changeDirector (Movie movieToUpdate) {
        String newDirector = ConsoleUtil.readCapitalizedString("Enter the new director name: ");
        movieToUpdate.setDirector(newDirector);
        System.out.println("Director changed successfully!");
        saveToFile();
    }

    private void changeYear (Movie movieToUpdate) {
        int newYear = ConsoleUtil.readInt("Enter the new year: ");
        movieToUpdate.setReleaseYear(newYear);
        System.out.println("Release year changed successfully!");
        saveToFile();
    }

    private void changeCountry (Movie movieToUpdate) {
        Country newCountry = ConsoleUtil.readEnum(Country.class, "Select the country");
        movieToUpdate.setCountry(newCountry);
        System.out.println("Country of origin changed successfully!");
        saveToFile();
    }

    private void changeAgeRating (Movie movieToUpdate) {
        AgeRating newAgeRating = ConsoleUtil.readEnum(AgeRating.class, "Select the age rating");
        movieToUpdate.setAgeRating(newAgeRating);
        System.out.println("Age rating changed successfully!");
        saveToFile();
    }

    private void changeGenre (Movie movieToUpdate) {
        MovieGenre newMovieGenre = ConsoleUtil.readEnum(MovieGenre.class, "Select the genre");
        movieToUpdate.setGenre(newMovieGenre);
        System.out.println("Genre changed successfully!");
        saveToFile();
    }

    private void changeStatus (Movie movieToUpdate) {
        // Necesita validaciones más complejas para garantizar la consistencia y seguridad entre estados.
        // Debe acceder al gestor de funciones; si está en una función, no se puede cambiar su status de "NOW_SHOWING"
        // Y no se puede poner en "NOW_SHOWING" de forma manual; se cambiará a tal estado cuando una función tenga la peli.
    }

    private void changeAll (Movie movieToUpdate) {
        changeTitle(movieToUpdate);
        changeAudioLanguage(movieToUpdate);
        changeSubLanguage(movieToUpdate);
        changeDuration(movieToUpdate);
        changeProducer(movieToUpdate);
        changeDirector(movieToUpdate);
        changeYear(movieToUpdate);
        changeCountry(movieToUpdate);
        changeAgeRating(movieToUpdate);
        changeGenre(movieToUpdate);
        changeStatus(movieToUpdate);
    }

    private void loadFromFile () {
        Type type = new TypeToken <List <Movie> >() {}.getType();
        List <Movie> loaded = JsonUtil.read(MOVIE_FILE_PATH, type, ArrayList::new);
        movieStorageManager.clear();
        for (Movie m : loaded) {
            try {
                movieStorageManager.add(m, true);
            } catch (DuplicateElementException ignored) {}
        }
    }

    private void saveToFile () {
        List <Movie> list = new ArrayList<>(movieStorageManager.findAll());
        JsonUtil.write(MOVIE_FILE_PATH, list);
    }

}
