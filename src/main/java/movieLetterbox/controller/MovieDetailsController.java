package movieLetterbox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import movieLetterbox.MainApplication;
import movieLetterbox.model.User;

import java.io.IOException;

public class MovieDetailsController {

    @FXML private Label movieTitleLabel;
    @FXML private ImageView posterImageView;
    @FXML private HBox ratingStarContainer;
    @FXML private TextArea userReviewArea;
    @FXML private Label descriptionLabel;

    private User currentUser;

    public void setUserData(User user) {
        this.currentUser = user;
    }

    @FXML
    void handleReturnAction(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-menu.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), MainApplication.WINDOW_WIDTH, MainApplication.WINDOW_HEIGHT);

            if (MainApplication.class.getResource("Style.css") != null) {
                scene.getStylesheets().add(MainApplication.class.getResource("Style.css").toExternalForm());
            }

            // Get the MainMenuController and pass the user back so they stay logged in
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
            System.err.println("Failed to return to main menu.");
        }
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
        movieTitleLabel.setText("Loading details for ID: " + movieId);
    }
}