package movieLetterbox.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
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
import movieLetterbox.service.FirebaseService;
import movieLetterbox.service.OmdbService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MainMenuController {

    @FXML private Label welcomeLabel;
    @FXML private TextField searchField;
    @FXML private TilePane movieGrid;
    @FXML private ComboBox<String> searchTypeCombo; // NEW: To switch modes

    @FXML private ImageView userProfileImage;
    @FXML private SVGPath defaultProfileIcon;

    private User user;
    private final OmdbService omdbService = new OmdbService();
    private final FirebaseService firebaseService = MainApplication.firebaseService;

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
            searchField.setOnAction(event -> handleSearch());
        }

        // NEW: Fix for ComboBox text visibility
        // Explicitly set the "Button Cell" (the part visible when closed) to show the text
        if (searchTypeCombo != null) {
            searchTypeCombo.setButtonCell(new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        // Force styles: Purple text, Bold, and some padding
                        setStyle("-fx-text-fill: #4C51BF; -fx-font-weight: bold; -fx-padding: 0 0 0 5;");
                    }
                }
            });

            // Ensure "Movies" is selected by default if nothing is set
            if (searchTypeCombo.getValue() == null) {
                searchTypeCombo.getSelectionModel().select("Movies");
            }
        }
    }

    @FXML
    void handleHomeAction(MouseEvent event) {
        if (searchField != null) {
            searchField.clear();
        }
        loadDashboardMovies();
    }

    // Unified Search Handler
    private void handleSearch() {
        String query = searchField.getText();
        if (query == null || query.isBlank()) return;

        String type = searchTypeCombo.getValue();

        if ("People".equals(type)) {
            searchUsers(query);
        } else {
            searchMovies(query);
        }
    }

    // --- USER SEARCH LOGIC ---
    private void searchUsers(String query) {
        movieGrid.getChildren().clear();
        CompletableFuture.runAsync(() -> {
            try {
                List<User> users = firebaseService.searchUsers(query);
                Platform.runLater(() -> {
                    if (users.isEmpty()) {
                        Label noRes = new Label("No users found.");
                        noRes.setStyle("-fx-font-size: 18px; -fx-text-fill: #555;");
                        movieGrid.getChildren().add(noRes);
                    } else {
                        for (User u : users) {
                            movieGrid.getChildren().add(createUserCard(u));
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private VBox createUserCard(User targetUser) {
        VBox card = new VBox();
        card.getStyleClass().add("movie-card"); // Reuse card style
        card.setAlignment(javafx.geometry.Pos.CENTER);

        // Circular Profile Image
        ImageView profileImg = new ImageView();
        profileImg.setFitWidth(150);
        profileImg.setFitHeight(150);
        profileImg.setPreserveRatio(true);

        if (targetUser.getProfilePhotoUrl() != null && !targetUser.getProfilePhotoUrl().isBlank()) {
            profileImg.setImage(new Image(targetUser.getProfilePhotoUrl(), true));
            Circle clip = new Circle(75, 75, 75);
            profileImg.setClip(clip);
        } else {
            try {
                if (MainApplication.class.getResource("placeholder.png") != null) {
                    profileImg.setImage(new Image(MainApplication.class.getResource("placeholder.png").toExternalForm()));
                }
            } catch (Exception e) {}
        }

        Label nameLabel = new Label(targetUser.getUsername());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 0 0 0;");

        card.getChildren().addAll(profileImg, nameLabel);

        // Click to Open Profile
        card.setOnMouseClicked(e -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("profile-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), MainApplication.WINDOW_WIDTH, MainApplication.WINDOW_HEIGHT);
                if (MainApplication.class.getResource("Style.css") != null) {
                    scene.getStylesheets().add(MainApplication.class.getResource("Style.css").toExternalForm());
                }

                ProfileController controller = fxmlLoader.getController();
                // We are 'user', looking at 'targetUser'
                controller.setProfileData(this.user, targetUser);

                Stage stage = (Stage) welcomeLabel.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Profile - " + targetUser.getUsername());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        return card;
    }

    // --- MOVIE SEARCH LOGIC (Existing) ---
    private void loadDashboardMovies() {
        movieGrid.getChildren().clear();
        for (String title : RECENT_MOVIES) {
            fetchAndAddMovieByTitle(title);
        }
    }

    private void searchMovies(String query) {
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
                    Platform.runLater(() -> {
                        Label noRes = new Label("No movies found.");
                        noRes.setStyle("-fx-font-size: 18px; -fx-text-fill: #555;");
                        movieGrid.getChildren().add(noRes);
                    });
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

            String movieId = "";
            if (movieData.has("imdbID")) {
                movieId = movieData.get("imdbID").getAsString();
            }

            VBox card = createMovieCard(movieId, title, posterUrl, rating);
            movieGrid.getChildren().add(card);
        }
    }

    private VBox createMovieCard(String movieId, String title, String posterUrl, int rating) {
        VBox card = new VBox();
        card.getStyleClass().add("movie-card");
        Image image;
        if (posterUrl == null || posterUrl.equals("N/A")) {
            try {
                image = new Image(MainApplication.class.getResource("poster_not_found.png").toExternalForm());
            } catch (Exception e) {
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

        card.setOnMouseClicked(e -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("movie-details.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), MainApplication.WINDOW_WIDTH, MainApplication.WINDOW_HEIGHT);

                if (MainApplication.class.getResource("Style.css") != null) {
                    scene.getStylesheets().add(MainApplication.class.getResource("Style.css").toExternalForm());
                }

                MovieDetailsController controller = fxmlLoader.getController();
                controller.setMovieData(movieId);
                controller.setUserData(this.user);

                Stage stage = (Stage) welcomeLabel.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle(title + " - Details");
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        return card;
    }

    @FXML
    void handleProfileViewAction(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("profile-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), MainApplication.WINDOW_WIDTH, MainApplication.WINDOW_HEIGHT);

            if (MainApplication.class.getResource("Style.css") != null) {
                scene.getStylesheets().add(MainApplication.class.getResource("Style.css").toExternalForm());
            }

            ProfileController controller = fxmlLoader.getController();
            controller.setUserData(this.user);

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Edit Profile");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogoutAction(MouseEvent event) {
        handleProfileViewAction(event);
    }
}