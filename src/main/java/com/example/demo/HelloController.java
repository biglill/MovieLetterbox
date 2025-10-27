package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class HelloController {

    private FirebaseService firebaseService;
    private File selectedPhotoFile;

    @FXML
    private VBox signInPane;
    @FXML
    private VBox signUpPane;

    @FXML
    private Label feedbackLabel;

    @FXML
    private TextField signInUsernameField;
    @FXML
    private PasswordField signInPasswordField;

    @FXML
    private TextField signUpNameField;
    @FXML
    private TextField signUpEmailField;
    @FXML
    private PasswordField signUpPasswordField;
    @FXML
    private TextField signUpUsernameField;
    @FXML
    private TextField signUpAgeField;

    // +++++++++ NEW FXML VAR +++++++++
    @FXML
    private PasswordField signUpConfirmPasswordField;
    // ++++++++++++++++++++++++++++++++

    @FXML
    private ImageView profileImageView;
    @FXML
    private Label profilePhotoLabel;

    @FXML
    public void initialize() {
        try {
            firebaseService = new FirebaseService();
            System.out.println("Firebase initialized successfully.");
            feedbackLabel.setText("Firebase connected.");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            feedbackLabel.setText("Connection Error: Check environment variable setup.");
        }
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("placeholder.png"));
            profileImageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Warning: placeholder.png not found.");
        }
    }

    /**
     * Handles the action of clicking the "Sign In" button.
     */
    @FXML
    void handleSignInAction(ActionEvent event) {
        // (This method is unchanged from the last step)
        if (firebaseService == null) {
            feedbackLabel.setText("Error: Database service is not available.");
            return;
        }

        String username = signInUsernameField.getText();
        String password = signInPasswordField.getText();

        if (username.isBlank() || password.isBlank()) {
            feedbackLabel.setText("Please enter both username and password.");
            return;
        }

        try {
            Map<String, Object> userData = firebaseService.getUserByUsername(username);

            if (userData == null) {
                feedbackLabel.setText("Sign-in failed: User not found.");
                return;
            }

            String storedPassword = (String) userData.get("password");

            if (password.equals(storedPassword)) {
                feedbackLabel.setText("Sign-in successful! Welcome, " + userData.get("name") + ".");
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

        // +++++++++ GET CONFIRM PASSWORD VALUE +++++++++
        String confirmPassword = signUpConfirmPasswordField.getText();
        // ++++++++++++++++++++++++++++++++++++++++++++++

        String username = signUpUsernameField.getText();
        String age = signUpAgeField.getText();

        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() || username.isBlank() || age.isBlank()) {
            feedbackLabel.setText("Please fill in all sign-up fields.");
            return;
        }

        // +++++++++ ADD PASSWORD VALIDATION +++++++++
        if (!password.equals(confirmPassword)) {
            feedbackLabel.setText("Passwords do not match. Please try again.");
            return;
        }
        // +++++++++++++++++++++++++++++++++++++++++++

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("password", password); // WARNING: Hash this in a real app
        userData.put("username", username);
        userData.put("age", age);
        userData.put("profilePhotoUrl", null);

        try {
            String newUserId = firebaseService.saveUserDetails(userData);
            System.out.println("Successfully saved user data with ID: " + newUserId);

            if (selectedPhotoFile != null) {
                try {
                    firebaseService.uploadProfilePhoto(selectedPhotoFile, newUserId);
                    System.out.println("Photo uploaded and linked successfully.");
                } catch (Exception e) {
                    System.err.println("Error uploading photo: " + e.getMessage());
                    e.printStackTrace();
                    feedbackLabel.setText("Account created, but photo upload failed. See console.");
                }
            }

            feedbackLabel.setText("Account created successfully! Please sign in.");
            showSignInPane(null);

        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Error saving user data to Firestore.");
            feedbackLabel.setText("Error: Could not create account. See console for details.");
            e.printStackTrace();
        }
    }

    /**
     * Opens a FileChooser to select a profile photo.
     */
    @FXML
    void handleChoosePhotoAction(ActionEvent event) {
        // (This method is unchanged)
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                selectedPhotoFile = file;
                profilePhotoLabel.setText(file.getName());
                Image image = new Image(file.toURI().toString());
                profileImageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading image preview: " + e.getMessage());
                profilePhotoLabel.setText("Error: Could not load image.");
            }
        }
    }

    @FXML
    void showSignInPane(ActionEvent event) {
        signInPane.setVisible(true);
        signUpPane.setVisible(false);
        clearSignUpForm();
    }

    @FXML
    void showSignUpPane(ActionEvent event) {
        signUpPane.setVisible(true);
        signInPane.setVisible(false);
    }

    /**
     * Helper method to clear all sign-up fields.
     */
    private void clearSignUpForm() {
        signUpNameField.clear();
        signUpEmailField.clear();
        signUpPasswordField.clear();

        // +++++++++ CLEAR NEW FIELD +++++++++
        signUpConfirmPasswordField.clear();
        // +++++++++++++++++++++++++++++++++++

        signUpUsernameField.clear();
        signUpAgeField.clear();

        selectedPhotoFile = null;
        profilePhotoLabel.setText("No photo selected.");
        try {
            profileImageView.setImage(new Image(getClass().getResourceAsStream("placeholder.png")));
        } catch (Exception e) {
            // placeholder.png not found, do nothing
        }
        feedbackLabel.setText("");

        signInUsernameField.clear();
        signInPasswordField.clear();
    }
}