package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class MainMenuController {

    @FXML
    private Label welcomeLabel;

    private User user;

    public void setUserData(User user) {
        this.user = user;
        welcomeLabel.setText("Welcome, " + user.getName() + "!");
    }

    @FXML
    void handleLogoutAction(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 750, 750);
            scene.getStylesheets().add(HelloApplication.class.getResource("Style.css").toExternalForm());

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();

            stage.setScene(scene);
            stage.setTitle("MovieLetterbox");
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading login screen.");
            e.printStackTrace();
        }
    }
}