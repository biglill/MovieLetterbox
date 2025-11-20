package movieLetterbox.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import movieLetterbox.MainApplication;
import movieLetterbox.model.User;
import movieLetterbox.service.OmdbService;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class MainMenuController {

    @FXML private Label welcomeLabel;
    @FXML private TextField searchField;
    @FXML private TilePane movieGrid;

    private User user;
    private final OmdbService omdbService = new OmdbService();

    // Recent blockbuster movies (2024-2025) to show on startup
    private final String[] RECENT_MOVIES = {
            "Dune: Part Two",
            "Civil War",
            "The Fall Guy",
            "Challengers",
            "Kingdom of the Planet of the Apes",
            "Godzilla x Kong: The New Empire",
            "Furiosa: A Mad Max Saga",
            "Kung Fu Panda 4",
            "Inside Out 2"
    };

    public void setUserData(User user) {
        this.user = user;
        if (user != null) {
            welcomeLabel.setText(user.getUsername());
        }
        loadDashboardMovies();
    }

    @FXML
    public void initialize() {
        // Setup search listener to trigger on Enter key
        if (searchField != null) {
            searchField.setOnAction(event -> searchMovies(searchField.getText()));
        }
    }

    /**
     * Triggered when clicking "Latest Movies".
     * Clears the search bar and reloads the initial recent movies list.
     */
    @FXML
    void handleHomeAction(MouseEvent event) {
        if (searchField != null) {
            searchField.clear();
        }
        loadDashboardMovies();
    }

    private void loadDashboardMovies() {
        movieGrid.getChildren().clear();
        // Loop through the curated recent movies list
        for (String title : RECENT_MOVIES) {
            fetchAndAddMovieByTitle(title);
        }
    }

    /**
     * Search Logic:
     * 1. Calls OMDb search API to get a list of matching movies.
     * 2. Iterates through results and fetches full details for each to get the rating.
     * 3. Adds cards to the grid.
     */
    private void searchMovies(String query) {
        if (query == null || query.isBlank()) return;

        // Clear grid to show new results
        movieGrid.getChildren().clear();

        CompletableFuture.runAsync(() -> {
            try {
                // 1. Perform broad search to get list of titles/IDs
                JsonObject searchResult = omdbService.SearchMovie(query);

                if (searchResult != null && searchResult.has("Response")
                        && "True".equalsIgnoreCase(searchResult.get("Response").getAsString())) {

                    JsonArray searchList = searchResult.getAsJsonArray("Search");

                    // 2. Iterate through results
                    for (JsonElement element : searchList) {
                        JsonObject simpleMovie = element.getAsJsonObject();

                        if (simpleMovie.has("imdbID")) {
                            String imdbID = simpleMovie.get("imdbID").getAsString();

                            // 3. Fetch full details to ensure we have the Rating and Plot if needed
                            JsonObject fullMovie = omdbService.GetMovieByID(imdbID);

                            Platform.runLater(() -> {
                                addMovieToGrid(fullMovie);
                            });
                        }
                    }
                } else {
                    System.out.println("No movies found for query: " + query);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // Helper to fetch a single specific movie by title (used for dashboard loading)
    private void fetchAndAddMovieByTitle(String title) {
        CompletableFuture.runAsync(() -> {
            try {
                JsonObject movieData = omdbService.GetMovieByTitle(title);
                Platform.runLater(() -> addMovieToGrid(movieData));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // Core method to validate data and add the card to the UI
    private void addMovieToGrid(JsonObject movieData) {
        if (movieData != null && movieData.has("Title")) {
            String title = movieData.get("Title").getAsString();

            String posterUrl = "N/A";
            if (movieData.has("Poster")) {
                posterUrl = movieData.get("Poster").getAsString();
            }

            int rating = 0;
            if (movieData.has("imdbRating") && !movieData.get("imdbRating").getAsString().equals("N/A")) {
                try {
                    double imdbScore = movieData.get("imdbRating").getAsDouble();
                    // Convert 10-point scale to 5-point scale
                    rating = (int) Math.round(imdbScore / 2.0);
                } catch (NumberFormatException e) {
                    // Ignore parsing errors, leave rating as 0
                }
            }

            VBox card = createMovieCard(title, posterUrl, rating);
            movieGrid.getChildren().add(card);
        }
    }

    private VBox createMovieCard(String title, String posterUrl, int rating) {
        VBox card = new VBox();
        card.getStyleClass().add("movie-card");

        // --- 1. Poster Image Logic ---
        Image image;
        if (posterUrl == null || posterUrl.equals("N/A")) {
            // Load the custom 404 image if poster is missing
            try {
                // Ensure 'poster_not_found.png' is in src/main/resources/movieLetterbox/
                image = new Image(MainApplication.class.getResource("poster_not_found.png").toExternalForm());
            } catch (Exception e) {
                System.err.println("Could not find poster_not_found.png in resources.");
                image = null;
            }
        } else {
            image = new Image(posterUrl, true); // true = load in background
        }

        ImageView poster = new ImageView(image);
        poster.setFitWidth(180);
        poster.setFitHeight(270);

        // Safety check if image failed to load entirely
        if (image == null) {
            // Keeps the layout structure even if image is missing
            poster.setFitWidth(180);
            poster.setFitHeight(270);
        }

        poster.setPreserveRatio(true);
        poster.getStyleClass().add("movie-poster");

        // --- 2. Movie Title ---
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("movie-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(180);

        // --- 3. Rating Label ---
        Label ratingText = new Label("Average Rating");
        ratingText.getStyleClass().add("rating-label");

        // --- 4. Stars ---
        HBox stars = new HBox();
        stars.getStyleClass().add("star-container");

        // SVG Data for a Star shape
        String starPath = "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z";

        for (int i = 0; i < 5; i++) {
            SVGPath star = new SVGPath();
            star.setContent(starPath);
            star.setScaleX(0.8);
            star.setScaleY(0.8);
            // Fill yellow if index is less than rating, else white
            star.getStyleClass().add(i < rating ? "star-filled" : "star-empty");
            stars.getChildren().add(star);
        }

        // Assemble the card
        card.getChildren().addAll(poster, titleLabel, ratingText, stars);

        // Click listener (Placeholder for future functionality like opening details)
        card.setOnMouseClicked(e -> System.out.println("Clicked on: " + title));

        return card;
    }

    @FXML
    void handleLogoutAction(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 800);
            // Ensure styles are loaded
            if (MainApplication.class.getResource("Style.css") != null) {
                scene.getStylesheets().add(MainApplication.class.getResource("Style.css").toExternalForm());
            }
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}