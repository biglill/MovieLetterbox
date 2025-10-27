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

    private FirebaseService firebaseService;

    @FXML
    private VBox signInPane;
    @FXML
    private VBox signUpPane;

    @FXML
    private Label feedbackLabel;

    @FXML
    private TextField signInEmailField;
    @FXML
    private PasswordField signInPasswordField;

    @FXML
    private TextField signUpNameField;
    @FXML
    private TextField signUpEmailField;
    @FXML
    private PasswordField signUpPasswordField;

    // +++++++++ NEW FIELDS START +++++++++
    @FXML
    private TextField signUpUsernameField;
    @FXML
    private TextField signUpAgeField;
    // +++++++++ NEW FIELDS END +++++++++


    @FXML
    public void initialize() {
        try {
            firebaseService = new FirebaseService();
            System.out.println("Firebase initialized successfully.");
            feedbackLabel.setText("Firebase connected.");
        } catch (IOException e) {
            // This error now reflects the environment variable requirement.
            System.err.println(e.getMessage());
            e.printStackTrace();
            feedbackLabel.setText("Connection Error: Check environment variable setup.");
        }
    }

    /**
     * Handles the action of clicking the "Sign In" button.
     * It retrieves user data from Firestore and validates the password.
     */
    @FXML
    void handleSignInAction(ActionEvent event) {
        if (firebaseService == null) {
            feedbackLabel.setText("Error: Database service is not available.");
            return;
        }

        String email = signInEmailField.getText();
        String password = signInPasswordField.getText();

        if (email.isBlank() || password.isBlank()) {
            feedbackLabel.setText("Please enter both email and password.");
            return;
        }

        try {
            Map<String, Object> userData = firebaseService.getUserByEmail(email);

            if (userData == null) {
                feedbackLabel.setText("Sign-in failed: User not found.");
                return;
            }

            // In a real app, this would be a hashed password comparison.
            String storedPassword = (String) userData.get("password");

            if (password.equals(storedPassword)) {
                feedbackLabel.setText("Sign-in successful! Welcome, " + userData.get("name") + ".");
                // TODO: Add logic to navigate to the main part of your application.
            } else {
                feedbackLabel.setText("Sign-in failed: Incorrect password.");
            }

        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Error during sign-in process.");
            feedbackLabel.setText("Error: Could not sign in. See console for details.");
            e.printStackTrace();
        }
    }

    /**
     * Handles the action of clicking the "Sign Up" button.
     */
    @FXML
    void handleSignUpAction(ActionEvent event) {
        if (firebaseService == null) {
            feedbackLabel.setText("Error: Database service is not available.");
            return;
        }

        String name = signUpNameField.getText();
        String email = signUpEmailField.getText();
        String password = signUpPasswordField.getText();

        // +++++++++ GET NEW FIELD VALUES +++++++++
        String username = signUpUsernameField.getText();
        String age = signUpAgeField.getText();

        // +++++++++ UPDATE VALIDATION +++++++++
        if (name.isBlank() || email.isBlank() || password.isBlank() || username.isBlank() || age.isBlank()) {
            feedbackLabel.setText("Please fill in all sign-up fields.");
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        // WARNING: In a production application, you should ALWAYS hash the password.
        userData.put("password", password);

        // +++++++++ ADD NEW DATA TO MAP +++++++++
        userData.put("username", username);
        userData.put("age", age); // Stored as a String. See note below.

        try {
            String newUserId = firebaseService.saveUserDetails(userData);
            System.out.println("Successfully saved user data with ID: " + newUserId);

            feedbackLabel.setText("Account created successfully! Please sign in.");
            showSignInPane(null);
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Error saving user data to Firestore.");
            feedbackLabel.setText("Error: Could not create account. See console for details.");
            e.printStackTrace();
        }
    }

    @FXML
    void showSignInPane(ActionEvent event) {
        signInPane.setVisible(true);
        signUpPane.setVisible(false);
    }

    @FXML
    void showSignUpPane(ActionEvent event) {
        signUpPane.setVisible(true);
        signInPane.setVisible(false);
    }
}