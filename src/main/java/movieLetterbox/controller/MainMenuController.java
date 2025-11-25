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
import javafx.scene.shape.Circle;
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

    @FXML private ImageView userProfileImage;
    @FXML private SVGPath defaultProfileIcon;

    private User user;
    private final OmdbService omdbService = new OmdbService();

    private final String[] RECENT_MOVIES = {
            "Dune: Part Two", "Civil War", "The Fall Guy", "Challengers",
            "Kingdom of the Planet of the Apes", "Godzilla x Kong: The New Empire",
            "Furiosa: A Mad Max Saga", "Kung Fu Panda 4", "Inside Out 2"
    };

    public void setUserData(User user) {
        this.user = user;
        if (user != null) {
            welcomeLabel.setText(user.getUsername());

            if (user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isBlank()) {
                Image image = new Image(user.getProfilePhotoUrl(), true);
                userProfileImage.setImage(image);

                Circle clip = new Circle(20, 20, 20);
                userProfileImage.setClip(clip);

                userProfileImage.setVisible(true);
                userProfileImage.setManaged(true);
                defaultProfileIcon.setVisible(false);
                defaultProfileIcon.setManaged(false);
            } else {
                userProfileImage.setVisible(false);
                userProfileImage.setManaged(false);
                defaultProfileIcon.setVisible(true);
                defaultProfileIcon.setManaged(true);
            }
        }
        loadDashboardMovies();
    }

    @FXML
    public void initialize() {
        if (searchField != null) {
            searchField.setOnAction(event -> searchMovies(searchField.getText()));
        }
    }

    @FXML
    void handleHomeAction(MouseEvent event) {
        if (searchField != null) {
            searchField.clear();
        }
        loadDashboardMovies();
    }

    private void loadDashboardMovies() {
        movieGrid.getChildren().clear();
        for (String title : RECENT_MOVIES) {
            fetchAndAddMovieByTitle(title);
        }
    }

    private void searchMovies(String query) {
        if (query == null || query.isBlank()) return;
        movieGrid.getChildren().clear();
        CompletableFuture.runAsync(() -> {
            try {
                JsonObject searchResult = omdbService.SearchMovie(query);
                if (searchResult != null && searchResult.has("Response")
                        && "True".equalsIgnoreCase(searchResult.get("Response").getAsString())) {
                    JsonArray searchList = searchResult.getAsJsonArray("Search");
                    for (JsonElement element : searchList) {
                        JsonObject simpleMovie = element.getAsJsonObject();
                        if (simpleMovie.has("imdbID")) {
                            String imdbID = simpleMovie.get("imdbID").getAsString();
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
                    rating = (int) Math.round(imdbScore / 2.0);
                } catch (NumberFormatException e) {
                }
            }
            VBox card = createMovieCard(title, posterUrl, rating);
            movieGrid.getChildren().add(card);
        }
    }

    private VBox createMovieCard(String title, String posterUrl, int rating) {
        VBox card = new VBox();
        card.getStyleClass().add("movie-card");
        Image image;
        if (posterUrl == null || posterUrl.equals("N/A")) {
            try {
                image = new Image(MainApplication.class.getResource("poster_not_found.png").toExternalForm());
            } catch (Exception e) {
                System.err.println("Could not find poster_not_found.png in resources.");
                image = null;
            }
        } else {
            image = new Image(posterUrl, true);
        }
        ImageView poster = new ImageView(image);
        poster.setFitWidth(180);
        poster.setFitHeight(270);
        if (image == null) {
            poster.setFitWidth(180);
            poster.setFitHeight(270);
        }
        poster.setPreserveRatio(true);
        poster.getStyleClass().add("movie-poster");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("movie-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(180);
        Label ratingText = new Label("Average Rating");
        ratingText.getStyleClass().add("rating-label");
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
        card.getChildren().addAll(poster, titleLabel, ratingText, stars);
        card.setOnMouseClicked(e -> System.out.println("Clicked on: " + title));
        return card;
    }

    @FXML
    void handleProfileViewAction(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("profile-view.fxml"));
            // Use constants from MainApplication
            Scene scene = new Scene(fxmlLoader.load(), MainApplication.WINDOW_WIDTH, MainApplication.WINDOW_HEIGHT);

            if (MainApplication.class.getResource("Style.css") != null) {
                scene.getStylesheets().add(MainApplication.class.getResource("Style.css").toExternalForm());
            }

            ProfileController controller = fxmlLoader.getController();
            controller.setUserData(this.user); // Pass the current user data to the profile screen

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Edit Profile");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Kept for backward compatibility if FXML still references this name
    @FXML
    void handleLogoutAction(MouseEvent event) {
        handleProfileViewAction(event);
    }
}