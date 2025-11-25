package movieLetterbox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class MovieDetailsController {

    @FXML private Label movieTitleLabel;
    @FXML private ImageView posterImageView;
    @FXML private HBox ratingStarContainer;
    @FXML private TextArea userReviewArea;
    @FXML private Label descriptionLabel;

    @FXML
    void handleReturnAction(ActionEvent event) {
        System.out.println("Return button clicked");
        // Logic to go back to the main menu will go here
    }

    @FXML
    void handleSetRating(MouseEvent event) {
        System.out.println("A star was clicked");
        // Logic to determine which star was clicked and update UI/backend
    }

    @FXML
    void handleSubmitReviewAction(ActionEvent event) {
        String reviewText = userReviewArea.getText();
        System.out.println("Submitting review: " + reviewText);
        // Logic to send review to backend
    }

    // Method intended for the MainMenuController to call to pass movie data
    public void setMovieData(String movieId) {
        // Backend team will implement fetching data here
        System.out.println("Loading details for movie ID: " + movieId);
        movieTitleLabel.setText("Loading " + movieId + "...");
    }
}