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

    // Default movies to show on load
    private final String[] DEFAULT_MOVIES = {
            "Ferris Bueller's Day Off",
            "Interstellar",
            "Pulp Fiction",
            "Morbius",
            "Man of Steel",
            "How to Lose a Guy in 10 Days"
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
        // Listen for the "Enter" key in the search box
        if (searchField != null) {
            searchField.setOnAction(event -> searchMovies(searchField.getText()));
        }
    }

    private void loadDashboardMovies() {
        movieGrid.getChildren().clear();
        for (String title : DEFAULT_MOVIES) {
            fetchAndAddMovieByTitle(title);
        }
    }

    /**
     * NEW SEARCH LOGIC:
     * 1. Searches for a list of movies matching the query (e.g. "Batman" returns "Batman Begins", "The Batman", etc.)
     * 2. Fetches details for each to get the rating.
     * 3. Adds them to the grid.
     */
    private void searchMovies(String query) {
        if (query == null || query.isBlank()) return;

        // Clear the grid immediately to show new results are coming
        movieGrid.getChildren().clear();

        CompletableFuture.runAsync(() -> {
            try {
                // 1. Perform the broad search to get the list of "similar titles"
                JsonObject searchResult = omdbService.SearchMovie(query);

                if (searchResult != null && searchResult.has("Response")
                        && "True".equalsIgnoreCase(searchResult.get("Response").getAsString())) {

                    JsonArray searchList = searchResult.getAsJsonArray("Search");

                    // 2. Iterate through the results
                    for (JsonElement element : searchList) {
                        JsonObject simpleMovie = element.getAsJsonObject();

                        if (simpleMovie.has("imdbID")) {
                            String imdbID = simpleMovie.get("imdbID").getAsString();

                            // 3. Fetch full details for this specific ID (to get the Rating and proper Poster)
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

    // Helper for the default dashboard load (still uses Title)
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

    // Shared helper to actually create the card and add it to the view
    private void addMovieToGrid(JsonObject movieData) {
        if (movieData != null && movieData.has("Poster") && !movieData.get("Poster").getAsString().equals("N/A")) {
            String title = movieData.has("Title") ? movieData.get("Title").getAsString() : "Unknown";
            String posterUrl = movieData.get("Poster").getAsString();

            // Calculate rating from IMDb score (1-10) to Stars (1-5)
            int rating = 0;
            if (movieData.has("imdbRating") && !movieData.get("imdbRating").getAsString().equals("N/A")) {
                try {
                    double imdbScore = movieData.get("imdbRating").getAsDouble();
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

        // Poster
        ImageView poster = new ImageView(new Image(posterUrl, true));
        poster.setFitWidth(180);
        poster.setFitHeight(270);
        poster.setPreserveRatio(true);
        poster.getStyleClass().add("movie-poster");

        // Label
        Label ratingText = new Label("Average Rating");
        ratingText.getStyleClass().add("rating-label");

        // Stars
        HBox stars = new HBox();
        stars.getStyleClass().add("star-container");
        String starPath = "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z";

        for (int i = 0; i < 5; i++) {
            SVGPath star = new SVGPath();
            star.setContent(starPath);
            star.setScaleX(0.8);
            star.setScaleY(0.8);
            star.getStyleClass().add(i < rating ? "star-filled" : "star-empty");
            stars.getChildren().add(star);
        }

        card.getChildren().addAll(poster, ratingText, stars);
        return card;
    }

    @FXML
    void handleLogoutAction(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 800);
            scene.getStylesheets().add(MainApplication.class.getResource("Style.css").toExternalForm());
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}