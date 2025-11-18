package com.cinemamanager.manager.cine.movie;
import com.cinemamanager.util.common.enums.CollectionType;
import com.cinemamanager.enums.cine.movie.*;
import com.cinemamanager.util.common.exception.DuplicateElementException;
import com.cinemamanager.model.cine.Movie;
import com.cinemamanager.util.common.ConsoleUtil;
import com.cinemamanager.util.common.JsonUtil;
import com.cinemamanager.util.cine.movie.MovieFactory;
import com.cinemamanager.util.common.StorageManager;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class MovieManager {
    private final StorageManager <Integer, Movie> movieStorageManager;
    private static final String MOVIE_FILE_PATH = "movies.json";
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
            System.out.println("\nError registering the movie: " + e.getMessage());
        }
    }

    public void deleteMovieById () {
        List <Movie> notShowingMovies = getNotShowingMovies();

        if (notShowingMovies.isEmpty()) {
            System.out.println("\nThere are no movies available to delete.\n");
            return;
        }

        System.out.println("\nOnly movies that are not currently showing can be deleted: \n");

        Optional <Movie> optionalMovie = selectMovieByIdFromList(notShowingMovies);
        optionalMovie.ifPresent(movie -> {
            movieStorageManager.delete(movie);
            saveToFile();
        });
    }

    public Optional <Movie> findMovieById (int id) {
        return movieStorageManager.findById(id);
    }

    public Optional <Movie> findMovieById (int id, List <Movie> movieList) {
        return movieList.stream()
                .filter(m -> m.getId() == id)
                .findFirst();
    }

    public List <Movie> findAllMovies () {
        return movieStorageManager.findAll();
    }

    public void displayMovieList (List <Movie> movieList) {
        if (movieList.isEmpty()) {
            System.out.println("No movies available.");
        }
        for (Movie movie : movieList) {
            System.out.println(movie);
        }
    }

    public void displayMovieListWithId (List <Movie> movieList) {
        if (movieList.isEmpty()) {
            System.out.println("No movies available.");
        }
        for (Movie movie : movieList) {
            System.out.println("ID: " + movie.getId());
            System.out.println(movie);
        }
    }

    public List <Movie> getMovieListings () {
        return movieStorageManager.findBy(m -> m.getStatus().equals(MovieStatus.NOW_SHOWING));
    }

    public void showAllMovies () {
        displayMovieList(findAllMovies());
    }

    public void showMovieListings () {
        displayMovieList(getMovieListings());
    }

    public void updateMovie () {
        List <Movie> movies = findAllMovies();
        Optional <Movie> optionalMovieToUpdate = selectMovieByIdFromList(movies);
        if (optionalMovieToUpdate.isPresent()) {

            Movie movieToUpdate = optionalMovieToUpdate.get();

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
                            [11] Change all.
                            
                            [0] Back.
                            >""" + " ";

            Set <String> validOptions = new HashSet<>();
            for (int i = 0; i <= 11; i++) {
                validOptions.add(String.valueOf(i));
            }
            String chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            switch (chosenOption) {
                case "1"  -> {
                    changeTitle(movieToUpdate);
                    System.out.println("\nTitle changed successfully!\n");
                    saveToFile();
                }
                case "2"  -> {
                    changeAudioLanguage(movieToUpdate);
                    System.out.println("\nLanguage changed successfully!\n");
                    saveToFile();
                }
                case "3"  -> {
                    changeSubLanguage(movieToUpdate);
                    System.out.println("\nSubtitle changed successfully!\n");
                    saveToFile();
                }
                case "4"  -> {
                    changeDuration(movieToUpdate);
                    System.out.println("\nDuration of the movie changed successfully!\n");
                    saveToFile();
                }
                case "5"  -> {
                    changeProducer(movieToUpdate);
                    System.out.println("\nProducer changed successfully!\n");
                    saveToFile();
                }
                case "6"  -> {
                    changeDirector(movieToUpdate);
                    System.out.println("\nDirector changed successfully!\n");
                    saveToFile();
                }
                case "7"  -> {
                    changeYear(movieToUpdate);
                    System.out.println("\nRelease year changed successfully!\n");
                    saveToFile();
                }
                case "8"  -> {
                    changeCountry(movieToUpdate);
                    System.out.println("\nCountry of origin changed successfully!\n");
                    saveToFile();
                }
                case "9"  -> {
                    changeAgeRating(movieToUpdate);
                    System.out.println("\nAge rating changed successfully!\n");
                    saveToFile();
                }
                case "10" -> {
                    changeGenre(movieToUpdate);
                    System.out.println("\nGenre changed successfully!\n");
                    saveToFile();
                }
                case "11" -> changeAll(movieToUpdate);
                case "0"  -> {}
            }
        }
    }

    public Optional <Movie> selectMovieByIdFromList(List <Movie> movieList) {
        if (movieList.isEmpty()) {
            System.out.println("\nNo movies available to select.\n");
            return Optional.empty();
        }

        while (true) {
            displayMovieListWithId(movieList);
            int id = ConsoleUtil.readInt("\nEnter the ID of the movie to select: ");
            Optional <Movie> optionalSelected = findMovieById (id, movieList);

            if (optionalSelected.isPresent()) return optionalSelected;

            System.out.println("\nThe selected movie ID is not in the current list.");
            if (!ConsoleUtil.confirm("Do you want to search with a different ID?")) return Optional.empty();
        }
    }

    public void searchByIdAndDisplay() {
        int id = ConsoleUtil.readInt("\nEnter the movie ID you wish to find: ");
        displaySearchResult(findMovieById(id).stream().toList(), System.out::println, "\nMovie not found.\n");
    }

    public void searchByTitleAndDisplay() {
        String title = ConsoleUtil.readCapitalizedString("\nEnter the movie title you wish to find: ");
        displaySearchResult(searchMoviesByTitleRegex(title), System.out::println, "\nNo movies were found.\n");
    }

    public void searchByAudioAndDisplay() {
        Language audio = ConsoleUtil.readEnum(Language.class, "\nSelect the audio language: ");
        displaySearchResult(searchMoviesByAudio(audio), System.out::println, "\nNo movies were found.\n");
    }

    public void searchBySubsAndDisplay() {
        Language subs = ConsoleUtil.readEnum(Language.class, "\nSelect the subtitle language: ");
        displaySearchResult(searchMoviesBySubs(subs), System.out::println, "\nNo movies were found.\n");
    }

    public void searchWithMinDurationAndDisplay() {
        Duration min = ConsoleUtil.readDuration("\nEnter the minimum duration: ");
        displaySearchResult(searchMoviesWithMinDuration(min), System.out::println, "\nNo movies were found.\n");
    }

    public void searchWithMaxDurationAndDisplay() {
        Duration max = ConsoleUtil.readDuration("\nEnter the maximum duration: ");
        displaySearchResult(searchMoviesWithMaxDuration(max), System.out::println, "\nNo movies were found.\n");
    }

    public void searchByProducerAndDisplay() {
        String producer = ConsoleUtil.readCapitalizedString("\nEnter the producer name: ");
        displaySearchResult(searchMoviesByProducerRegex(producer), System.out::println, "\nNo movies were found.\n");
    }

    public void searchByDirectorAndDisplay() {
        String director = ConsoleUtil.readCapitalizedString("\nEnter the director name: ");
        displaySearchResult(searchMoviesByDirectorRegex(director), System.out::println, "\nNo movies were found.\n");
    }

    public void searchReleasedFromAndDisplay() {
        int year = ConsoleUtil.readInt("\nEnter the starting release year: ");
        displaySearchResult(searchMoviesReleasedFrom(year), System.out::println, "\nNo movies were found.\n");
    }

    public void searchFromCountryAndDisplay() {
        Country country = ConsoleUtil.readEnum(Country.class, "Select the country");
        displaySearchResult(searchMoviesFrom(country), System.out::println, "\nNo movies were found.\n");
    }

    public void searchByAgeRatingAndDisplay() {
        AgeRating ageRating = ConsoleUtil.readEnum(AgeRating.class, "Select the age rating");
        displaySearchResult(searchMoviesByAgeRating(ageRating), System.out::println, "\nNo movies were found.\n");
    }

    public void searchByGenreAndDisplay() {
        MovieGenre genre = ConsoleUtil.readEnum(MovieGenre.class, "Select the genre");
        displaySearchResult(searchMoviesByGenre(genre), System.out::println, "\nNo movies were found.\n");
    }

    public void searchByStatusAndDisplay() {
        MovieStatus status = ConsoleUtil.readEnum(MovieStatus.class, "Select the movie status");
        displaySearchResult(searchMoviesByStatus(status), System.out::println, "\nNo movies were found.\n");
    }

    private List <Movie> getNotShowingMovies () {
        List <Movie> notShowingMovies = new ArrayList<>(searchMoviesByStatus(MovieStatus.UNAVAILABLE));
        notShowingMovies.addAll(searchMoviesByStatus(MovieStatus.COMING_SOON));
        return notShowingMovies;
    }

    private List <Movie> searchMoviesByTitleRegex (String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return movieStorageManager.findBy(movie -> pattern.matcher(movie.getTitle()).find());
    }

    private List <Movie> searchMoviesByAudio (Language desiredAudio) {
        return movieStorageManager.findBy(m -> m.getAudio().equals(desiredAudio));
    }

    private List <Movie> searchMoviesBySubs (Language desiredSubs) {
        return movieStorageManager.findBy(m -> m.getSubtitles().equals(desiredSubs));
    }

    private List <Movie> searchMoviesWithMinDuration (Duration minDuration) {
        return movieStorageManager.findBy(m -> m.getDuration().compareTo(minDuration) >= 0);
    }

    private List <Movie> searchMoviesWithMaxDuration (Duration maxDuration) {
        return movieStorageManager.findBy(m -> m.getDuration().compareTo(maxDuration) <= 0);
    }

    private List <Movie> searchMoviesByProducerRegex (String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return movieStorageManager.findBy(movie -> pattern.matcher(movie.getProducer()).find());
    }

    private List <Movie> searchMoviesByDirectorRegex (String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return movieStorageManager.findBy(movie -> pattern.matcher(movie.getDirector()).find());
    }

    private List <Movie> searchMoviesReleasedFrom (int year) {
        return movieStorageManager.findBy(m -> m.getReleaseYear() >= year);
    }

    private List <Movie> searchMoviesFrom (Country country) {
        return movieStorageManager.findBy(m -> m.getCountry().equals(country));
    }

    private List <Movie> searchMoviesByAgeRating (AgeRating ageRating) {
        return movieStorageManager.findBy(m -> m.getAgeRating().equals(ageRating));
    }

    private List <Movie> searchMoviesByGenre (MovieGenre movieGenre) {
        return movieStorageManager.findBy(m -> m.getGenre().equals(movieGenre));
    }

    private List <Movie> searchMoviesByStatus (MovieStatus movieStatus) {
        return movieStorageManager.findBy(m -> m.getStatus().equals(movieStatus));
    }

    private <T> void displaySearchResult (Collection<T> results, Consumer<T> onFound, String emptyMessage) {
        if (results == null || results.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        results.forEach(onFound);
    }

    private void changeTitle (Movie movieToUpdate) {
        String newTitle = ConsoleUtil.readCapitalizedString("\nEnter the new title: ");
        movieToUpdate.setTitle(newTitle);
    }

    private void changeAudioLanguage (Movie movieToUpdate) {
        Language newAudio = ConsoleUtil.readEnum(Language.class, "Select the audio language");
        movieToUpdate.setAudio(newAudio);
    }

    private void changeSubLanguage (Movie movieToUpdate) {
        Language newSub = ConsoleUtil.readEnum(Language.class, "Select the subtitle language");
        movieToUpdate.setSubtitles(newSub);
    }

    private void changeDuration (Movie movieToUpdate) {
        Duration newDuration = ConsoleUtil.readDuration("\nEnter the new movie duration: ");
        movieToUpdate.setDuration(newDuration);
    }

    private void changeProducer (Movie movieToUpdate) {
        String newProducer = ConsoleUtil.readCapitalizedString("\nEnter the new producer name: ");
        movieToUpdate.setProducer(newProducer);
    }

    private void changeDirector (Movie movieToUpdate) {
        String newDirector = ConsoleUtil.readCapitalizedString("\nEnter the new director name: ");
        movieToUpdate.setDirector(newDirector);
    }

    private void changeYear (Movie movieToUpdate) {
        int newYear = ConsoleUtil.readInt("\nEnter the new year: ");
        movieToUpdate.setReleaseYear(newYear);
    }

    private void changeCountry (Movie movieToUpdate) {
        Country newCountry = ConsoleUtil.readEnum(Country.class, "Select the country");
        movieToUpdate.setCountry(newCountry);
    }

    private void changeAgeRating (Movie movieToUpdate) {
        AgeRating newAgeRating = ConsoleUtil.readEnum(AgeRating.class, "Select the age rating");
        movieToUpdate.setAgeRating(newAgeRating);
    }

    private void changeGenre (Movie movieToUpdate) {
        MovieGenre newMovieGenre = ConsoleUtil.readEnum(MovieGenre.class, "Select the genre");
        movieToUpdate.setGenre(newMovieGenre);
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
        System.out.println("\nYour updated movie:\n" + movieToUpdate);
        if (ConsoleUtil.confirm("\nDo you want to save these changes?")) {
            System.out.println("\nEverything changed successfully!\n");
            saveToFile();
        }
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

    public void saveToFile () {
        List <Movie> list = new ArrayList<>(movieStorageManager.findAll());
        JsonUtil.write(MOVIE_FILE_PATH, list);
    }

}
