package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class MainMenuController {

    @FXML
    private Label welcomeLabel;

    private Map<String, Object> userData;

    /**
     * This method is called by the HelloController to pass in the user's
     * data after a successful login.
     */
    public void setUserData(Map<String, Object> userData) {
        this.userData = userData;
        // Update the welcome label with the user's name
        welcomeLabel.setText("Welcome, " + userData.get("name") + "!");
    }

    /**
     * Handles the "Log Out" button action.
     * Loads the login.fxml screen.
     */
    @FXML
    void handleLogoutAction(ActionEvent event) {
        try {
            // Load the login screen
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
            // Set the scene dimensions back to your 750x750 size
            Scene scene = new Scene(fxmlLoader.load(), 750, 750);
            scene.getStylesheets().add(HelloApplication.class.getResource("Style.css").toExternalForm());

            // Get the current stage (window) from any control, like the label
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();

            // Set the new scene on the stage
            stage.setScene(scene);
            stage.setTitle("MovieLetterbox");
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading login screen.");
            e.printStackTrace();
        }
    }
}