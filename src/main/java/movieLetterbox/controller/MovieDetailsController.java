package movieLetterbox.controller;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import movieLetterbox.MainApplication;
import movieLetterbox.model.Movie;
import movieLetterbox.model.Review;
import movieLetterbox.model.User;
import movieLetterbox.service.FirebaseService;
import movieLetterbox.service.TmdbService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MovieDetailsController {

    @FXML private Label movieTitleLabel;
    @FXML private ImageView posterImageView;
    @FXML private HBox ratingStarContainer;
    @FXML private TextArea userReviewArea;
    @FXML private Label descriptionLabel;
    @FXML private VBox reviewsContainer;
    @FXML private SVGPath favoriteHeartIcon;

    private User currentUser;
    private Movie currentMovie;
    private int currentRating = 0;
    private Review existingUserReview = null;

    private final TmdbService tmdbService = MainApplication.tmdbService;
    private final FirebaseService firebaseService = MainApplication.firebaseService;

    public void setUserData(User user) {
        this.currentUser = user;
    }

    public void setMovieData(String movieId) {
        movieTitleLabel.setText("Loading...");

        CompletableFuture.runAsync(() -> {
            try {
                // Fetch full details from TMDB
                JsonObject json = tmdbService.getMovieById(movieId);
                this.currentMovie = new Movie(json);

                Platform.runLater(() -> {
                    updateUI(currentMovie);
                    loadReviews(movieId);

                    if (currentUser != null) {
                        checkUserReviewStatus(movieId);
                        updateFavoriteIcon();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> movieTitleLabel.setText("Error loading movie."));
            }
        });
    }

    private void updateUI(Movie movie) {
        movieTitleLabel.setText(movie.getName() + " (" + movie.getYear() + ")");
        descriptionLabel.setText(movie.getPlot());

        if (movie.getPosterPic() != null && !movie.getPosterPic().equals("N/A")) {
            posterImageView.setImage(new Image(movie.getPosterPic()));
        }else{
            try {
                if (MainApplication.class.getResource("/movieLetterbox/assets/movieDefault.png") != null) {
                    Image placeholder = new Image(MainApplication.class.getResource("/movieLetterbox/assets/movieDefault.png").toExternalForm());
                    posterImageView.setImage(placeholder);
                }
            } catch (Exception e) {}
        }
    }

    @FXML
    void handleToggleFavorite(MouseEvent event) {
        if (currentUser == null || currentMovie == null) return;
        boolean isFav = isMovieFavorite();
        if (isFav) {
            firebaseService.removeFavorite(currentUser, currentMovie.getMovieId());
        } else {
            firebaseService.addFavorite(currentUser, currentMovie.getMovieId());
        }
        updateFavoriteIcon();
    }

    private boolean isMovieFavorite() {
        if (currentUser.getFavorites() == null) return false;
        return currentUser.getFavorites().contains(currentMovie.getMovieId());
    }

    private void updateFavoriteIcon() {
        if (favoriteHeartIcon == null) return;
        favoriteHeartIcon.getStyleClass().removeAll("heart-empty", "heart-filled");
        if (isMovieFavorite()) {
            favoriteHeartIcon.getStyleClass().add("heart-filled");
        } else {
            favoriteHeartIcon.getStyleClass().add("heart-empty");
        }
    }

    private void checkUserReviewStatus(String movieId) {
        CompletableFuture.runAsync(() -> {
            Review review = firebaseService.getUserReview(movieId, currentUser.getUserId());
            if (review != null) {
                existingUserReview = review;
                Platform.runLater(() -> {
                    currentRating = review.getRating();
                    updateStarVisuals(currentRating);
                    userReviewArea.setText(review.getReviewText());
                });
            }
        });
    }

    private void loadReviews(String movieId) {
        CompletableFuture.runAsync(() -> {
            List<Review> reviews = firebaseService.getReviews(movieId);
            Platform.runLater(() -> populateReviews(reviews));
        });
    }

    private void populateReviews(List<Review> reviews) {
        reviewsContainer.getChildren().clear();
        for (Review r : reviews) {
            VBox reviewBox = new VBox();
            reviewBox.getStyleClass().add("content-box");
            reviewBox.setPadding(new Insets(10));
            reviewBox.setSpacing(5);

            Label reviewText = new Label("\"" + r.getReviewText() + "\"");
            reviewText.setWrapText(true);
            reviewText.getStyleClass().add("review-text");

            String stars = "★".repeat(r.getRating());
            Label ratingLabel = new Label(stars);
            ratingLabel.setStyle("-fx-text-fill: #F6E05E; -fx-font-size: 14px;");

            Label authorLabel = new Label("- " + r.getUsername());
            authorLabel.getStyleClass().add("review-author");
            authorLabel.setMaxWidth(Double.MAX_VALUE);
            authorLabel.setAlignment(javafx.geometry.Pos.BOTTOM_RIGHT);

            reviewBox.getChildren().addAll(ratingLabel, reviewText, authorLabel);
            reviewsContainer.getChildren().add(reviewBox);
        }
    }

    @FXML
    void handleSetRating(MouseEvent event) {
        Node source = (Node) event.getSource();
        int index = ratingStarContainer.getChildren().indexOf(source);
        if (index != -1) {
            currentRating = index + 1;
            updateStarVisuals(currentRating);
        }
    }

    private void updateStarVisuals(int rating) {
        for (int i = 0; i < ratingStarContainer.getChildren().size(); i++) {
            SVGPath star = (SVGPath) ratingStarContainer.getChildren().get(i);
            if (i < rating) {
                star.getStyleClass().remove("star-empty");
                if (!star.getStyleClass().contains("star-filled")) {
                    star.getStyleClass().add("star-filled");
                }
            } else {
                star.getStyleClass().remove("star-filled");
                if (!star.getStyleClass().contains("star-empty")) {
                    star.getStyleClass().add("star-empty");
                }
            }
        }
    }

    @FXML
    void handleSubmitReviewAction(ActionEvent event) {
        if (currentUser == null) {
            showAlert("Error", "You must be logged in to review.");
            return;
        }
        if (currentMovie == null) {
            showAlert("Error", "Movie data not loaded.");
            return;
        }
        if (currentRating == 0) {
            showAlert("Missing Rating", "Please click a star to rate the movie.");
            return;
        }
        String text = userReviewArea.getText();
        if (text.isBlank()) {
            showAlert("Missing Review", "Please write a review.");
            return;
        }

        Review newReview = new Review(
                currentUser.getUserId(),
                currentUser.getUsername(),
                currentMovie.getMovieId(),
                currentRating,
                text
        );

        CompletableFuture.runAsync(() -> {
            firebaseService.addReview(currentMovie, newReview);
            Platform.runLater(() -> {
                loadReviews(currentMovie.getMovieId());
                showAlert("Success", "Review submitted!");
            });
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void handleReturnAction(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-menu.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), MainApplication.WINDOW_WIDTH, MainApplication.WINDOW_HEIGHT);
            if (MainApplication.class.getResource("Style.css") != null) {
                scene.getStylesheets().add(MainApplication.class.getResource("Style.css").toExternalForm());
            }
            MainMenuController controller = fxmlLoader.getController();
            if (currentUser != null) {
                controller.setUserData(currentUser);
            }
            Stage stage = (Stage) movieTitleLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Main Menu");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}