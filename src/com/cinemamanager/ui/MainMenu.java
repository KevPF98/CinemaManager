package com.cinemamanager.ui;
import com.cinemamanager.auth.LoginService;
import com.cinemamanager.enums.user.Role;
import com.cinemamanager.manager.cine.movie.MovieManager;
import com.cinemamanager.manager.user.UserManager;
import com.cinemamanager.model.cine.Movie;
import com.cinemamanager.model.people.User;
import com.cinemamanager.util.common.ConsoleUtil;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class MainMenu {
    private final UserManager userManager;
    private final LoginService loginService;
    private final MovieManager movieManager;

    /// Main:
    public MainMenu() {
        this.userManager = new UserManager();
        this.loginService = new LoginService(userManager);
        this.movieManager = new MovieManager();
    }

    public void displayMainMenu(){

        String chosenOption;

        do {
            String prompt =
            "\n" +
            """
            Welcome to our Cinema Manager!
            How can we help you?
            [1] I want to log in (only for authorized personnel).
            [2] Show me the movie listings.
            
            [0] Exit.
            >""";

            Set<String> validOptions = Set.of("0", "1", "2");
            chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            switch (chosenOption) {
                case "0" -> System.out.println ("\nGoodbye!\n");
                case "1" -> {
                    String nickname = ConsoleUtil.readInputOrEsc("Please enter the nickname, or type 'ESC' to return to the previous menu: ");
                    if (nickname == null) break;

                    String password = ConsoleUtil.readString ("Please enter the password: ");
                    boolean success = loginService.login(nickname, password);

                    String message = success
                            ? "\nWelcome, " + loginService.getActiveUser().getAccount().getNickname() + ".\n"
                            : "Incorrect nickname or password.";
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
            System.out.println("You must change your personal data before continuing.\n");
            userManager.forcePersonalDataChange(activeUser);
        }

        if (activeUser.getAccount().isMustChangePassword()) {
            System.out.println("You must change your password before continuing.\n");
            userManager.forcePasswordChange(activeUser);
        }

        String chosenOption;

        do {
            System.out.println("\nHello, " + activeUser.getAccount().getNickname());
            String prompt =
                            """
                            What do you want to do?
                            
                            [1] Manage users.
                            [2] Manage cinema.
                            [3] Show the movie listings.
                            
                            [0] Log-out.
                            """;

            Set<String> validOptions = Set.of("0", "1", "2", "3");
            chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            switch (chosenOption) {
                case "0" -> {
                    System.out.println("Logging out...");
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
                    [1] Add a new user.
                    [2] Modify user by ID.
                    [3] Delete user by ID.
                    [4] Reactivate account by ID.
                    [5] Find user by ID and show data.
                    [6] List all users.
                    [7] Grant admin privileges by ID.
                    [8] Revoke admin permissions by ID.
                    
                    [0] Return to the previous menu.
                    """;

            Set<String> validOptions = Set.of("0", "1", "2", "3", "4", "5", "6", "7", "8");
            chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            boolean isNotFounder = !roleActiveSession.equals(Role.FOUNDER);
            boolean isNotAdmin = !roleActiveSession.equals(Role.ADMIN);
            boolean targetIsFounder;
            boolean targetIsAdmin;
            Role targetRole;

            switch (chosenOption) {
                case "0" -> System.out.println("Returning to the previous menu...");
                case "1" -> userManager.addUser();
                case "2" -> {
                    userManager.showAllUsers();
                    int idToModify = ConsoleUtil.readInt("Enter the ID of the user you want to edit.");
                    Optional <User> optionalUserToModify = userManager.findUserById(idToModify);
                    if (optionalUserToModify.isEmpty()) break;
                    User userToModify = optionalUserToModify.get();
                    targetRole = userToModify.getAccount().getRole();

                    targetIsFounder = targetRole.equals(Role.FOUNDER);
                    targetIsAdmin = targetRole.equals(Role.ADMIN);
                    boolean isNotEditingItself = !Objects.equals(userToModify.getId(), activeUser.getId());

                    if (isNotFounder) {
                        if (targetIsFounder) {
                            System.out.println("Only the founder can modify their own account.");
                            break;
                        }

                        if (targetIsAdmin && isNotEditingItself) {
                            System.out.println("An admin can only be modified by themselves or by the founder account.");
                            break;
                        }
                    }

                    userManager.updateUser(userToModify.getId());
                }
                case "3" -> showUserDeletionMenu (isNotFounder, isNotAdmin, activeUser);
                case "4" -> {
                    if (isNotFounder && isNotAdmin) {
                        System.out.println("Only an admin can reactivate an account.");
                        break;
                    }

                    userManager.showAllUsers();

                    int idToReactivate = ConsoleUtil.readInt("Enter the ID of the user account you want to reactivate.");

                    Optional <User> optionalUserToReactivate = userManager.findUserById (idToReactivate);
                    if (optionalUserToReactivate.isEmpty()) break;

                    User userToReactivate = optionalUserToReactivate.get();
                    targetRole = userToReactivate.getAccount().getRole();

                    targetIsAdmin = targetRole.equals (Role.ADMIN);

                    if (isNotFounder && targetIsAdmin) {
                        System.out.println("Only the founder is allowed to reactivate admin accounts.");
                        break;
                    }

                    userManager.reactivateUser (userToReactivate);

                }
                case "5" -> {
                    int idToSearch = ConsoleUtil.readInt("Enter the user ID to search.");
                    Optional <User> optionalUserFound = userManager.findUserById (idToSearch);
                    if (optionalUserFound.isEmpty()) break;
                    User userFound = optionalUserFound.get();
                    System.out.println(userFound);
                }
                case "6" -> userManager.findAllUsers();
                case "7" -> {
                    if (isNotFounder) {
                        System.out.println("Only the Founder can grant admin privileges.");
                        break;
                    }
                    userManager.showAllUsers();
                    int idToGrantPrivileges = ConsoleUtil.readInt("Enter the user ID to grant privileges.");
                    Optional <User> optionalUserToGrantPrivileges = userManager.findUserById (idToGrantPrivileges);
                    if (optionalUserToGrantPrivileges.isEmpty()) break;
                    User userToGrantPrivileges = optionalUserToGrantPrivileges.get();
                    userManager.grantPrivileges (userToGrantPrivileges);
                }
                case "8" -> {
                    if (isNotFounder) {
                        System.out.println("Only the Founder can revoke admin privileges.");
                        break;
                    }
                    userManager.showAllUsers();
                    int idToRevokePrivileges = ConsoleUtil.readInt("Enter the user ID to revoke privileges.");
                    Optional <User> optionalUserToRevokePrivileges = userManager.findUserById (idToRevokePrivileges);
                    if (optionalUserToRevokePrivileges.isEmpty()) break;
                    User userToRevokePrivileges = optionalUserToRevokePrivileges.get();
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
                    >""";

            Set<String> validOptions = Set.of("0", "1", "2");
            chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            switch (chosenOption) {
                case "0" -> System.out.println ("Returning to the previous menu...");
                case "1" -> deleteUser(isNotFounder, isNotAdmin, activeUser, false);
                case "2" -> deleteUser(isNotFounder, isNotAdmin, activeUser, true);
            }
        } while (!chosenOption.equals("0"));
    }

    private void deleteUser (boolean isNotFounder,
                             boolean isNotAdmin,
                             User activeUser,
                             boolean isPermanent) {

        if (isNotFounder && isNotAdmin) {
            System.out.println("Only an admin can deactivate other user accounts.");
            return;
        }

        userManager.showAllUsers();

        int idToDelete = ConsoleUtil.readInt("Enter the ID of the user account you want to deactivate.");
        Optional <User> optionalUserToDelete = userManager.findUserById(idToDelete);
        if (optionalUserToDelete.isEmpty()) return;

        User userToDelete = optionalUserToDelete.get();
        Role targetRole = userToDelete.getAccount().getRole();

        boolean targetIsFounder = targetRole.equals(Role.FOUNDER);

        if (targetIsFounder) {
            System.out.println("The founder account cannot be deactivated.");
            return;
        }

        boolean targetIsAdmin = targetRole.equals(Role.ADMIN);
        boolean isNotDeletingItself = !Objects.equals(userToDelete.getId(), activeUser.getId());

        if (isNotFounder && targetIsAdmin && isNotDeletingItself) {
            System.out.println("An admin can only be deactivated by themselves or by the founder.");
            return;
        }

        if (isPermanent) {
            if (userToDelete.getAccount().isActive()) {
                System.out.println("Only disabled accounts can be permanently deleted from the system.");
                return;
            }

            if (!ConsoleUtil.confirm("WARNING: this action is permanent and cannot be reversed.")) return;
            userManager.deleteUserById(idToDelete);
            System.out.println("The user account has been permanently deleted.");

        } else {
            userManager.deactivateUser(userToDelete);
            System.out.println("User '" + userToDelete.getAccount().getNickname() + "' has been deactivated.");
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
                            [2] Manage theaters.
                            [3] Manage showtimes.
                            [4] Sell tickets.
                            
                            [0] Return to the previous menu.
                            """;

            Set<String> validOptions = Set.of("0", "1", "2", "3", "4");
            chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            switch (chosenOption) {
                case "0" -> System.out.println("Returning to the previous menu...");
                case "1" -> showMovieMenu();
//                case "2" -> ;
//                case "3" -> ;
//                case "4" -> ;
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
                    [3] Show all movies.
                    [4] Find a movie.
                    [5] Update a movie.
                    
                    [0] Return to the previous menu.
                    """;

            Set<String> validOptions = Set.of("0", "1", "2", "3", "4", "5");
            chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            switch (chosenOption) {
                case "0" -> System.out.println("Returning to the previous menu...");
                case "1" -> movieManager.addMovie();
                case "2" -> movieManager.deleteMovieById();
                case "3" -> movieManager.findAllMovies();
                case "4" -> showSearchingMovieMenu();
                case "5" -> movieManager.updateMovie();
            }
        } while (!chosenOption.equals("0"));
    }

    private void showSearchingMovieMenu () {
        String chosenOption;

        do {
            String prompt =
                    """
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
                    """;

            Set<String> validOptions = IntStream.rangeClosed(0, 13)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.toSet());
            chosenOption = ConsoleUtil.readOption(prompt, validOptions);

            switch (chosenOption) {
                case "0" -> System.out.println("Returning to the previous menu...");
                case "1" -> {
                    int id = ConsoleUtil.readInt("Enter the movie ID you wish to find.");
                    Optional <Movie> optionalFound = movieManager.findMovieById(id);
                    if (optionalFound.isEmpty()) {
                        System.out.println("Movie not found.");
                        break;
                    }
                    Movie found = optionalFound.get();
                    System.out.println(found);
                }
                case "2" -> {
                    String title = ConsoleUtil.capitalizeEachWord("Enter the movie title you wish to find.");
                    List <Movie> results = movieManager.searchMoviesByTitleRegex(title);
                    if (results.isEmpty()) {
                        System.out.println("No movies were found");
                        break;
                    }
                    movieManager.displayMovieList(results);
                }
                case "3" -> {
                    List <Movie> results = movieManager.findAllMovies();
                    if (results.isEmpty()) {
                        System.out.println("No movies were found");
                        break;
                    }
                    movieManager.displayMovieList(results);
                }
//                case "4" -> ;
            }
        } while (!chosenOption.equals("0"));
    }

}
