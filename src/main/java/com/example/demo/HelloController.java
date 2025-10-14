package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class HelloController {

    // Firebase Service to interact with the database
    private FirebaseService firebaseService;

    // Panes for switching between Sign-In and Sign-Up views
    @FXML
    private VBox signInPane;
    @FXML
    private VBox signUpPane;

    // UI element for user feedback (e.g., success or error messages)
    @FXML
    private Label feedbackLabel;

    // --- Sign-In Fields ---
    @FXML
    private TextField signInEmailField;
    @FXML
    private PasswordField signInPasswordField;

    // --- Sign-Up Fields ---
    @FXML
    private TextField signUpNameField;
    @FXML
    private TextField signUpEmailField;
    @FXML
    private PasswordField signUpPasswordField;

    /**
     * Initializes the controller. This method is called automatically after the FXML file has been loaded.
     * It sets up the connection to Firebase.
     */
    @FXML
    public void initialize() {
        try {
            firebaseService = new FirebaseService();
            System.out.println("Firebase initialized successfully.");
            feedbackLabel.setText(""); // Clear feedback label on start
        } catch (IOException e) {
            System.err.println("Failed to initialize Firebase. Make sure 'serviceAccountKey.json' is in the project root.");
            feedbackLabel.setText("Error: Could not connect to the database.");
        }
    }

    /**
     * Handles the action of clicking the "Sign In" button.
     */
    @FXML
    void handleSignInAction(ActionEvent event) {
        String email = signInEmailField.getText();
        String password = signInPasswordField.getText();
        System.out.println("Attempting to sign in with Email: " + email);
        // TODO: Add logic to authenticate the user against Firestore data.
        feedbackLabel.setText("Sign-in functionality is not yet implemented.");
    }

    /**
     * Handles the action of clicking the "Sign Up" button.
     * It validates input, saves the new user to Firestore, and switches to the sign-in view.
     */
    @FXML
    void handleSignUpAction(ActionEvent event) {
        String name = signUpNameField.getText();
        String email = signUpEmailField.getText();
        String password = signUpPasswordField.getText();

        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            // In a real app, you would show this error on the sign-up screen itself.
            System.err.println("Sign-up error: All fields are required.");
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        // WARNING: In a production application, you should ALWAYS hash the password before saving.
        // For example: userData.put("passwordHash", hashFunction(password));

        try {
            firebaseService.saveUserDetails(userData);
            feedbackLabel.setText("Account created successfully! Please sign in.");
            showSignInPane(null); // Switch back to the sign-in view
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Error saving user data to Firestore.");
            feedbackLabel.setText("Error: Could not create the account.");
        }
    }

    /**
     * Makes the Sign-In pane visible and hides the Sign-Up pane.
     */
    @FXML
    void showSignInPane(ActionEvent event) {
        signInPane.setVisible(true);
        signUpPane.setVisible(false);
    }

    /**
     * Makes the Sign-Up pane visible and hides the Sign-In pane.
     */
    @FXML
    void showSignUpPane(ActionEvent event) {
        signUpPane.setVisible(true);
        signInPane.setVisible(false);
    }
}
