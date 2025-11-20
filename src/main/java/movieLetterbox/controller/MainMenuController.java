package movieLetterbox.controller;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
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
    @FXML private TilePane movieGrid; // The grid where movies go

    private User user;
    private final OmdbService omdbService = new OmdbService();

    // Specific movies from your screenshot to populate the dashboard
    private final String[] DEFAULT_MOVIES = {
            "Ferris Bueller's Day Off",
            "Interstellar",
            "Pulp Fiction",
            "Morbius",
            "Man of Steel", // Superman image in screenshot corresponds to this or generic Superman
            "How to Lose a Guy in 10 Days"
    };

    public void setUserData(User user) {
        this.user = user;
        // Just showing first name or username for clean UI
        welcomeLabel.setText(user.getUsername());
        loadDashboardMovies();
    }

    @FXML
    public void initialize() {
        // Setup search listener to trigger on Enter key
        searchField.setOnAction(event -> searchMovies(searchField.getText()));
    }

    private void loadDashboardMovies() {
        movieGrid.getChildren().clear();
        for (String title : DEFAULT_MOVIES) {
            fetchAndAddMovie(title);
        }
    }

    private void searchMovies(String query) {
        if (query == null || query.isBlank()) return;
        movieGrid.getChildren().clear();
        // Basic search: just trying to find the exact match for now to show the card
        fetchAndAddMovie(query);
    }

    private void fetchAndAddMovie(String title) {
        // Run API calls in background thread to prevent freezing UI
        CompletableFuture.runAsync(() -> {
            try {
                JsonObject movieData = omdbService.GetMovieByTitle(title);

                // JavaFX UI updates must happen on the JavaFX Application Thread
                Platform.runLater(() -> {
                    if (movieData != null && movieData.has("Poster") && !movieData.get("Poster").getAsString().equals("N/A")) {
                        VBox card = createMovieCard(
                                movieData.get("Title").getAsString(),
                                movieData.get("Poster").getAsString(),
                                4 // Defaulting to 4 stars for visual demo
                        );
                        movieGrid.getChildren().add(card);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // --- DYNAMIC UI GENERATION ---
    private VBox createMovieCard(String title, String posterUrl, int rating) {
        VBox card = new VBox();
        card.getStyleClass().add("movie-card");

        // 1. Poster Image
        ImageView poster = new ImageView(new Image(posterUrl, true)); // background loading
        poster.setFitWidth(180);
        poster.setFitHeight(270);
        poster.setPreserveRatio(true);
        poster.getStyleClass().add("movie-poster");

        // 2. "Average Rating" Label
        Label ratingText = new Label("Average Rating");
        ratingText.getStyleClass().add("rating-label");

        // 3. Star HBox
        HBox stars = new HBox();
        stars.getStyleClass().add("star-container");

        // SVG Path for a Star shape
        String starPath = "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z";

        for (int i = 0; i < 5; i++) {
            SVGPath star = new SVGPath();
            star.setContent(starPath);
            star.setScaleX(0.8);
            star.setScaleY(0.8);
            // Fill yellow if i < rating, else white
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