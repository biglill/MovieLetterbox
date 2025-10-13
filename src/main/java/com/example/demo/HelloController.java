package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class HelloController {

    // Panes for switching views
    @FXML
    private VBox signInPane;
    @FXML
    private VBox signUpPane;

    // Sign-In Fields from FXML
    @FXML
    private TextField signInEmailField;
    @FXML
    private PasswordField signInPasswordField;

    // Sign-Up Fields from FXML
    @FXML
    private TextField signUpFirstNameField;
    @FXML
    private TextField signUpLastNameField;
    @FXML
    private TextField signUpEmailField;
    @FXML
    private PasswordField signUpPasswordField;

    /**
     * This method is called when the "Sign In" button is clicked.
     */
    @FXML
    void handleSignInAction(ActionEvent event) {
        String email = signInEmailField.getText();
        String password = signInPasswordField.getText();
        System.out.println("Attempting to sign in with Email: " + email);
        // Add your authentication logic here
    }

    /**
     * This method is called when the "Sign Up" button is clicked.
     */
    @FXML
    void handleSignUpAction(ActionEvent event) {
        String firstName = signUpFirstNameField.getText();
        String lastName = signUpLastNameField.getText();
        String email = signUpEmailField.getText();
        String password = signUpPasswordField.getText();

        System.out.println("Signing up new user:");
        System.out.println("First Name: " + firstName);
        System.out.println("Email: " + email);
        // Add your user creation logic here
    }

    /**
     * Makes the Sign-In pane visible and hides the Sign-Up pane.
     * Called by the "Already have an account? Sign In" hyperlink.
     */
    @FXML
    void showSignInPane(ActionEvent event) {
        signInPane.setVisible(true);
        signUpPane.setVisible(false);
    }

    /**
     * Makes the Sign-Up pane visible and hides the Sign-In pane.
     * Called by the "Don't have an account? Sign Up" hyperlink.
     */
    @FXML
    void showSignUpPane(ActionEvent event) {
        signUpPane.setVisible(true);
        signInPane.setVisible(false);
    }
}