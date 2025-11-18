package com.cinemamanager.ui;
import com.cinemamanager.auth.LoginService;
import com.cinemamanager.enums.user.Role;
import com.cinemamanager.manager.cine.movie.MovieManager;
import com.cinemamanager.manager.cine.room.RoomManager;
import com.cinemamanager.manager.cine.showtime.ShowtimeManager;
import com.cinemamanager.manager.cine.timeslot.ScheduleManager;
import com.cinemamanager.manager.sale.InvoiceManager;
import com.cinemamanager.manager.sale.TicketManager;
import com.cinemamanager.manager.user.PersonalDataManager;
import com.cinemamanager.manager.user.UserManager;
import com.cinemamanager.model.cine.Movie;
import com.cinemamanager.model.cine.Room;
import com.cinemamanager.model.cine.Showtime;
import com.cinemamanager.model.people.PersonalData;
import com.cinemamanager.model.people.User;
import com.cinemamanager.model.sale.Invoice;
import com.cinemamanager.util.common.ConsoleUtil;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class MainMenu {
    private final PersonalDataManager personalDataManager;
    private final UserManager userManager;
    private final LoginService loginService;
    private final MovieManager movieManager;
    private final RoomManager roomManager;
    private final ScheduleManager scheduleManager;
    private final ShowtimeManager showtimeManager;
    private final TicketManager ticketManager;
    private final InvoiceManager invoiceManager;

    /// Main:
    public MainMenu() {
        this.personalDataManager = new PersonalDataManager();
        this.userManager = new UserManager(personalDataManager);
        this.loginService = new LoginService(userManager);
        this.movieManager = new MovieManager();
        this.roomManager = new RoomManager();
        this.scheduleManager = new ScheduleManager(LocalTime.of(8, 0), LocalTime.of(23, 30));
        this.showtimeManager = new ShowtimeManager(roomManager, scheduleManager, movieManager);
        this.ticketManager = new TicketManager(movieManager, showtimeManager);
        this.invoiceManager = new InvoiceManager(ticketManager, /*userManager*/ personalDataManager);
    }

    public void displayMainMenu(){

        String chosenOption;

        do {
            String prompt =
            """
            
            Welcome to our Cinema Manager!
            How can we help you?
            
            [1] I want to log in (only for authorized personnel).
            [2] Show me the movie listings.
            
            [0] Exit.
            >""" + " ";

            Set<String> validOptions = Set.of("0", "1", "2");
            chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            switch (chosenOption) {
                case "0" -> {
                    System.out.println("\nGoodbye!\n");
                    ConsoleUtil.closeScanner();
                }
                case "1" -> {
                    String nickname = ConsoleUtil.readInputOrEsc("\nPlease enter the nickname, or type 'ESC' to return to the previous menu: ");
                    if (nickname == null) break;

                    String password = ConsoleUtil.readString ("\nPlease enter the password: ");
                    boolean success = loginService.login(nickname, password);

                    String message = success
                            ? "\nWelcome, " + loginService.getActiveUser().getAccount().getNickname() + ".\n"
                            : "\nIncorrect nickname or password.\n";
                    System.out.println(message);

                    if (success) {
                        showLoggedInMenu();
                    }
                }
                case "2" -> movieManager.showMovieListings();
            }
        } while (!chosenOption.equals("0"));

    }

    private void showLoggedInMenu() {
        User activeUser = loginService.getActiveUser();

        if (activeUser.getPersonalData().isMustCompleteProfile()) {
            System.out.println("\nYou must change your personal data before continuing.\n");
            personalDataManager.forceProfileUpdate(activeUser.getPersonalData());
        }

        if (activeUser.getAccount().isMustChangePassword()) {
            System.out.println("\nYou must change your password before continuing.\n");
            userManager.forcePasswordChange(activeUser);
        }

        String chosenOption;

        do {
            String prompt =
                            """
                            
                            What do you want to do?
                            
                            [1] Manage users.
                            [2] Manage cinema.
                            [3] Show the movie listings.
                            
                            [0] Log-out.
                            >""" + " ";

            Set <String> validOptions = Set.of("0", "1", "2", "3");
            chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            switch (chosenOption) {
                case "0" -> {
                    System.out.println("\nLogging out...\n");
                    loginService.logout();
                }
                case "1" -> showUserMenu();
                case "2" -> showCinemaMenu();
                case "3" -> movieManager.showMovieListings();
            }
        } while (!chosenOption.equals("0"));

    }

    /// Users:
    private void showUserMenu(){
        User activeUser = loginService.getActiveUser();
        Role roleActiveSession = activeUser.getAccount().getRole();
        String chosenOption;

        do {
            String prompt =
                    """
                    
                    What do you want to do?
                    
                    [1] Add a new client.
                    [2] Add a new employee.
                    [3] Modify user by ID.
                    [4] Delete user by ID.
                    [5] Reactivate account by ID.
                    [6] Find user by ID and show data.
                    [7] List all users.
                    [8] Grant admin privileges by ID.
                    [9] Revoke admin permissions by ID.
                    
                    [0] Return to the previous menu.
                    >""" + " ";

            Set <String> validOptions = Set.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
            chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            boolean isNotFounder = !roleActiveSession.equals(Role.FOUNDER);
            boolean isNotAdmin = !roleActiveSession.equals(Role.ADMIN);
            boolean targetIsFounder;
            boolean targetIsAdmin;
            Role targetRole;

            switch (chosenOption) {
                case "0" -> System.out.println("\nReturning to the previous menu...\n");
                case "1" -> {
                    String nationalId = ConsoleUtil.readValidNationalId("the National ID");
                    Optional <PersonalData> data = Optional.empty();
                    if (personalDataManager.isPersonalDataUnique(nationalId, userManager)) {
                        data = personalDataManager.registerPersonalData(nationalId);
                    }
                    if (data.isPresent()) {
                        break;
                    }
                    System.out.println("Operation canceled.");
                }
                case "2" -> {
                    String nationalId = ConsoleUtil.readValidNationalId("the National ID");
                    Optional<User> created = userManager.addUser(nationalId);
                    if (created.isEmpty()) {
                        System.out.println("Operation canceled.");
                    }
                }
                case "3" -> {
                    userManager.showAllUsers();
                    int idToModify = ConsoleUtil.readInt("\nEnter the ID of the user you want to edit.\n");
                    Optional <User> optionalUserToModify = userManager.findUserById(idToModify);
                    if (optionalUserToModify.isEmpty()) break;
                    User userToModify = optionalUserToModify.get();
                    targetRole = userToModify.getAccount().getRole();

                    targetIsFounder = targetRole.equals(Role.FOUNDER);
                    targetIsAdmin = targetRole.equals(Role.ADMIN);
                    boolean isNotEditingItself = !Objects.equals(userToModify.getId(), activeUser.getId());

                    if (isNotFounder) {
                        if (targetIsFounder) {
                            System.out.println("\nOnly the founder can modify their own account.\n");
                            break;
                        }

                        if (targetIsAdmin && isNotEditingItself) {
                            System.out.println("\nAn admin can only be modified by themselves or by the founder account.\n");
                            break;
                        }
                    }

                    userManager.updateUser(userToModify.getId());
                }
                case "4" -> showUserDeletionMenu (isNotFounder, isNotAdmin, activeUser);
                case "5" -> {
                    if (isNotFounder && isNotAdmin) {
                        System.out.println("\nOnly an admin can reactivate an account.\n");
                        break;
                    }

                    userManager.showAllUsers();

                    int idToReactivate = ConsoleUtil.readInt("\nEnter the ID of the user account you want to reactivate.\n");

                    Optional <User> optionalUserToReactivate = userManager.findUserById (idToReactivate);
                    if (optionalUserToReactivate.isEmpty()) break;

                    User userToReactivate = optionalUserToReactivate.get();
                    targetRole = userToReactivate.getAccount().getRole();

                    targetIsAdmin = targetRole.equals (Role.ADMIN);

                    if (isNotFounder && targetIsAdmin) {
                        System.out.println("\nOnly the founder is allowed to reactivate admin accounts.\n");
                        break;
                    }

                    System.out.println("\nUser '" + userToReactivate.getAccount().getNickname() + "' has been reactivated.\n");
                    userManager.reactivateUser (userToReactivate);

                }
                case "6" -> {
                    int idToSearch = ConsoleUtil.readInt("\nEnter the user ID to search.\n");
                    Optional <User> optionalUserFound = userManager.findUserById (idToSearch);
                    if (optionalUserFound.isEmpty()) break;
                    User userFound = optionalUserFound.get();
                    System.out.println(userFound);
                }
                case "7" -> userManager.showAllUsers();
                case "8" -> {
                    if (isNotFounder) {
                        System.out.println("\nOnly the Founder can grant admin privileges.\n");
                        break;
                    }
                    userManager.showAllUsers();
                    int idToGrantPrivileges = ConsoleUtil.readInt("\nEnter the user ID to grant privileges.\n");
                    Optional <User> optionalUserToGrantPrivileges = userManager.findUserById (idToGrantPrivileges);
                    if (optionalUserToGrantPrivileges.isEmpty()) break;
                    User userToGrantPrivileges = optionalUserToGrantPrivileges.get();
                    userManager.grantPrivileges (userToGrantPrivileges);
                }
                case "9" -> {
                    if (isNotFounder) {
                        System.out.println("\nOnly the Founder can revoke admin privileges.\n");
                        break;
                    }
                    userManager.showAllUsers();
                    int idToRevokePrivileges = ConsoleUtil.readInt("\nEnter the user ID to revoke privileges.\n");
                    Optional <User> optionalUserToRevokePrivileges = userManager.findUserById (idToRevokePrivileges);
                    if (optionalUserToRevokePrivileges.isEmpty()) break;

                    User userToRevokePrivileges = optionalUserToRevokePrivileges.get();
                    targetRole = userToRevokePrivileges.getAccount().getRole();
                    targetIsFounder = targetRole.equals(Role.FOUNDER);

                    if (targetIsFounder) {
                        System.out.println("\nFounder privileges cannot be revoked.\n");
                        break;
                    }
                    userManager.revokePrivileges(userToRevokePrivileges);
                }
            }
        } while (!chosenOption.equals("0"));

    }

    private void showUserDeletionMenu (boolean isNotFounder,
                                       boolean isNotAdmin,
                                       User activeUser) {
        String chosenOption;

        do {
            String prompt =
                    """
                    
                    What do you want to do?
                    [1] Deactivate user (can be reactivated later).
                    [2] Permanently delete user (cannot be undone).
                    
                    [0] Return to the previous menu.
                    >""" + " ";

            Set<String> validOptions = Set.of("0", "1", "2");
            chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            switch (chosenOption) {
                case "0" -> System.out.println ("\nReturning to the previous menu...\n");
                case "1" -> deleteUser(isNotFounder, isNotAdmin, activeUser, false);
                case "2" -> deleteUser(isNotFounder, isNotAdmin, activeUser, true);
            }
        } while (!chosenOption.equals("0"));
    }

    private void deleteUser (boolean isNotFounder,
                             boolean isNotAdmin,
                             User activeUser,
                             boolean isPermanent)  {

        if (isNotFounder && isNotAdmin) {
            System.out.println("\nOnly an admin can deactivate other user accounts.\n");
            return;
        }

        userManager.showAllUsers();

        int idToDelete = ConsoleUtil.readInt("\nEnter the ID of the user account you want to deactivate.\n");
        Optional <User> optionalUserToDelete = userManager.findUserById(idToDelete);
        if (optionalUserToDelete.isEmpty()) return;

        User userToDelete = optionalUserToDelete.get();
        Role targetRole = userToDelete.getAccount().getRole();

        boolean targetIsFounder = targetRole.equals(Role.FOUNDER);

        if (targetIsFounder) {
            System.out.println("\nThe founder account cannot be deactivated.\n");
            return;
        }

        boolean targetIsAdmin = targetRole.equals(Role.ADMIN);
        boolean isNotDeletingItself = !Objects.equals(userToDelete.getId(), activeUser.getId());

        if (isNotFounder && targetIsAdmin && isNotDeletingItself) {
            System.out.println("\nAn admin can only be deactivated by themselves or by the founder.\n");
            return;
        }

        if (isPermanent) {
            if (userToDelete.getAccount().isActive()) {
                System.out.println("\nOnly disabled accounts can be permanently deleted from the system.\n");
                return;
            }

            if (!ConsoleUtil.confirm("\nWARNING: this action is permanent and cannot be reversed. Do you want to proceed?\n")) return;
            userManager.deleteUserById(idToDelete);
            System.out.println("\nThe user account has been permanently deleted.\n");

        } else {
            userManager.deactivateUser(userToDelete);
            System.out.println("\nUser '" + userToDelete.getAccount().getNickname() + "' has been deactivated.\n");
        }
    }

    /// Cinema:
    private void showCinemaMenu (){
        String chosenOption;

        do {
            String prompt =
                            """
                            
                            What do you want to do?
                            
                            [1] Manage movies.
                            [2] Manage auditoriums.
                            [3] Manage showtimes.
                            [4] Sell tickets.
                            
                            [0] Return to the previous menu.
                            >""" + " ";

            Set<String> validOptions = Set.of("0", "1", "2", "3", "4");
            chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            switch (chosenOption) {
                case "0" -> System.out.println("\nReturning to the previous menu...\n");
                case "1" -> showMovieMenu();
                case "2" -> showRoomMenu();
                case "3" -> displayShowtimeMenu();
                case "4" -> {
                    Optional <Invoice> optionalInvoice = invoiceManager.addInvoice();
                    if (optionalInvoice.isEmpty()) {
                        System.out.println("No invoice generated.");
                        break;
                    }
                    showtimeManager.saveToFile(); // Updates the seats
                    System.out.println(optionalInvoice.get());
                }
            }
        } while (!chosenOption.equals("0"));
    }

    // Movie
    private void showMovieMenu () {
        String chosenOption;

        do {
            String prompt =
                    """
                    
                    What do you want to do?
                    
                    [1] Add a new movie.
                    [2] Delete a movie.
                    [3] Update a movie.
                    [4] Find a movie.
                    [5] Show all movies.
                    [6] Show the movie listings.
                    
                    [0] Return to the previous menu.
                    >""" + " ";

            Set<String> validOptions = Set.of("0", "1", "2", "3", "4", "5", "6");
            chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            switch (chosenOption) {
                case "0" -> System.out.println("\nReturning to the previous menu...\n");
                case "1" -> movieManager.addMovie();
                case "2" -> movieManager.deleteMovieById();
                case "3" -> movieManager.updateMovie();
                case "4" -> showSearchingMovieMenu();
                case "5" -> movieManager.showAllMovies();
                case "6" -> movieManager.showMovieListings();
            }
        } while (!chosenOption.equals("0"));
    }

    private void showSearchingMovieMenu() {
        String chosenOption;

        do {
            String prompt = """
                            
                            Which parameter would you like to search by?
                            
                            [1]  ID.
                            [2]  Title.
                            [3]  Audio language.
                            [4]  Subtitle language.
                            [5]  Min. duration.
                            [6]  Max. duration.
                            [7]  Producer.
                            [8]  Director.
                            [9]  With a release date later than a certain year.
                            [10] Country.
                            [11] Age rating.
                            [12] Genre.
                            [13] Status.
                            
                            [0] Return to the previous menu.
                            >""" + " ";

            Set<String> validOptions = IntStream.rangeClosed(0, 13)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.toSet());

            chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            switch (chosenOption) {
                case "0" -> System.out.println("\nReturning to the previous menu...\n");
                case "1" -> movieManager.searchByIdAndDisplay();
                case "2" -> movieManager.searchByTitleAndDisplay();
                case "3" -> movieManager.searchByAudioAndDisplay();
                case "4" -> movieManager.searchBySubsAndDisplay();
                case "5" -> movieManager.searchWithMinDurationAndDisplay();
                case "6" -> movieManager.searchWithMaxDurationAndDisplay();
                case "7" -> movieManager.searchByProducerAndDisplay();
                case "8" -> movieManager.searchByDirectorAndDisplay();
                case "9" -> movieManager.searchReleasedFromAndDisplay();
                case "10" -> movieManager.searchFromCountryAndDisplay();
                case "11" -> movieManager.searchByAgeRatingAndDisplay();
                case "12" -> movieManager.searchByGenreAndDisplay();
                case "13" -> movieManager.searchByStatusAndDisplay();
            }

        } while (!chosenOption.equals("0"));
    }

    // Room
    private void showRoomMenu () {
        String chosenOption;

        do {
            String prompt =
                    """
                    
                    What do you want to do?
                    
                    [1] Add a new auditorium.
                    [2] Delete auditorium by number.
                    [3] Reactivate auditorium.
                    [4] Update an auditorium.
                    [5] Find auditorium.
                    [6] Show all auditoriums.
                    
                    [0] Return to the previous menu.
                    >""" + " ";

            Set<String> validOptions = Set.of("0", "1", "2", "3", "4", "5", "6");
            chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            switch (chosenOption) {
                case "0" -> System.out.println("\nReturning to the previous menu...\n");
                case "1" -> roomManager.addRoom();
                case "2" -> showRoomDeletionMenu();
                case "3" -> {
                    TreeSet <Room> rooms = roomManager.findAllRooms();
                    Optional<Room> optionalRoom = roomManager.selectRoomByIdFromSet(rooms);
                    if (optionalRoom.isEmpty()) return;
                    Room found = optionalRoom.get();
                    roomManager.activateRoom(found);
                }
                case "4" -> {
                    TreeSet <Room> rooms = roomManager.findAllRooms();
                    Optional <Room> optionalRoom = roomManager.selectRoomByIdFromSet(rooms);
                    if (optionalRoom.isEmpty()) return;
                    Room found = optionalRoom.get();
                    if (showtimeManager.roomHasShowtimes(found)) {
                        System.out.println("\nWARNING: The auditorium you are trying to modify has active showtimes.\n");
                    }
                    roomManager.updateRoom(found);
                }
                case "5" -> showSearchingRoomMenu();
                case "6" -> roomManager.displayRoomsOrMessage(roomManager.findAllRooms());
            }
        } while (!chosenOption.equals("0"));
    }

    private void showRoomDeletionMenu () {
        String chosenOption;

        do {
            String prompt =
                    """
                    
                    What do you want to do?
                    [1] Deactivate room (can be reactivated later).
                    [2] Permanently delete room (cannot be undone).
                    
                    [0] Return to the previous menu.
                    >""" + " ";

            Set <String> validOptions = Set.of("0", "1", "2");
            chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            switch (chosenOption) {
                case "0" -> System.out.println ("\nReturning to the previous menu...\n");
                case "1" -> deleteRoom (false);
                case "2" -> deleteRoom(true);
            }
        } while (!chosenOption.equals("0"));
    }

    private void deleteRoom (boolean isPermanent) {
        TreeSet<Room> rooms = roomManager.findAllRooms();
        Optional<Room> optionalRoom = roomManager.selectRoomByIdFromSet(rooms);

        if (optionalRoom.isEmpty()) return;
        Room found = optionalRoom.get();

        if (!found.isActive() && !isPermanent) {
            System.out.println("\nThe auditorium has already been deactivated.\n");
            return;
        }

        if (showtimeManager.roomHasShowtimes(found)) {
            System.out.println("\nYou cannot delete or disable an auditorium that has active showtimes.\n");
            return;
        }

        if (isPermanent && found.isActive()) {
            System.out.println("\nOnly disabled auditoriums can be permanently deleted from the system.\n");
            return;
        }

        if (isPermanent) {
            if (!ConsoleUtil.confirm("\nWARNING: this action is permanent and cannot be reversed. Do you want to proceed?\n"))
                return;

            roomManager.deleteRoom(found);
            System.out.println("\nThe auditorium has been permanently deleted.\n");
        } else {
            roomManager.deactivateRoom(found);
            System.out.println("\nAuditorium: " + found.getId() + " has been deactivated.\n");
        }
    }

    private void showSearchingRoomMenu() {
        String chosenOption;

        do {
            String prompt = """
                            
                            Which parameter would you like to search by?
                            
                            [1]  Find by number.
                            [2]  Find by capacity.
                            [3]  Show active auditoriums.
                            [4]  Show inactive auditoriums.
                            [5]  Show rooms with active showtimes.
                            [6]  Show rooms without active showtimes.
                            
                            [0] Return to the previous menu.
                            >""" + " ";

            Set <String> validOptions = IntStream.rangeClosed(0, 6)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.toSet());

            chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            switch (chosenOption) {
                case "0" -> System.out.println("\nReturning to the previous menu...\n");
                case "1" -> roomManager.selectRoomByIdFromSet(roomManager.findAllRooms())
                        .ifPresent(System.out::println);
                case "2" -> roomManager.displayRoomsOrMessage(roomManager.getRoomsByCapacity());
                case "3" -> roomManager.displayRoomsOrMessage(roomManager.getActiveRooms());
                case "4" -> roomManager.displayRoomsOrMessage(roomManager.getInactiveRooms());
                case "5" -> roomManager.displayRoomsOrMessage(showtimeManager.getRoomsWithActiveShows());
                case "6" -> roomManager.displayRoomsOrMessage(showtimeManager.getRoomsWithoutActiveShows());
            }
        } while (!chosenOption.equals("0"));
    }

    // Showtime
    private void displayShowtimeMenu () {
        String chosenOption;

        do {
            String prompt =
                    """
                    
                    What do you want to do?
                    
                    [1]  Add a new showtime.
                    [2]  Cancel a showtime.
                    [3]  Delete a showtime.
                    [4]  Update a showtime.
                    [5]  Display showtimes.
                    
                    [0] Return to the previous menu.
                    >""" + " ";

            Set <String> validOptions = IntStream.rangeClosed(0, 5)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.toSet());
            chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            switch (chosenOption) {
                case "0" -> System.out.println("\nReturning to the previous menu...\n");
                case "1" -> showtimeManager.addShowTime();
                case "2" -> showtimeManager.cancelShowtime();
                case "3" -> showtimeManager.deleteShowtimeById();
                case "4" -> showtimeManager.updateShowtime();
                case "5" -> displaySearchingShowtimeMenu();
            }
        } while (!chosenOption.equals("0"));
    }

    private void displaySearchingShowtimeMenu() {
        String chosenOption;

        do {
            String prompt = """
                            
                            Choose an option to filter showtimes:
                            
                            [1]  Find by ID.
                            [2]  Display all showtimes.
                            [3]  View showtime seats.
                            [4]  View showtimes with available seats.
                            [5]  View showtimes with available seats by movie.
                            
                            [0] Return to the previous menu.
                            >""" + " ";

            Set <String> validOptions = IntStream.rangeClosed(0, 5)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.toSet());

            chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            switch (chosenOption) {
                case "0" -> System.out.println("\nReturning to the previous menu...\n");
                case "1" -> {
                    int id = ConsoleUtil.readInt("\nEnter the ID of the showtime to select: ");

                    Optional <Showtime> optionalShowtime = showtimeManager.findShowtimeById(id);

                    if (optionalShowtime.isEmpty()) {
                        System.out.println("Showtime with ID: " + id + " not found.");
                        break;
                    }
                    Showtime found = optionalShowtime.get();
                    System.out.println(found);
                }
                case "2" -> {
                    List <Showtime> showtimeList = showtimeManager.findAllShowtimes();
                    if (showtimeList.isEmpty()) {
                        System.out.println("No showtimes found.");
                        break;
                    }
                    showtimeManager.displayShowtimes(showtimeList);
                }
                case "3" -> showtimeManager.showSeatsForShowtimeById();
                case "4" -> showtimeManager.showAvailableShowtimes();
                case "5" -> {
                    Optional <Movie> optionalMovie = movieManager.selectMovieByIdFromList(movieManager.getMovieListings());
                    if (optionalMovie.isEmpty()) break;
                    List <Showtime> showtimeList = showtimeManager.getAvailableShowtimesByMovie(optionalMovie.get());
                    if (showtimeList.isEmpty()) {
                        System.out.println("No showtimes found.");
                        break;
                    }
                    showtimeManager.displayShowtimes(showtimeList);
                }
            }
        } while (!chosenOption.equals("0"));
    }

}
