package movieLetterbox.controller;

import javafx.application.Platform;
import movieLetterbox.service.FirebaseService;
import movieLetterbox.MainApplication;
import movieLetterbox.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.scene.SnapshotParameters;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import javafx.scene.shape.Circle;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class HelloController {

    private FirebaseService firebaseService;
    private File selectedPhotoFile;

    @FXML private VBox signInPane;
    @FXML private VBox signUpPane;
    @FXML private Label feedbackLabel;

    @FXML private TextField signInUsernameField;
    @FXML private PasswordField signInPasswordField;

    @FXML private TextField signUpNameField;
    @FXML private TextField signUpEmailField;
    @FXML private PasswordField signUpPasswordField;
    @FXML private TextField signUpUsernameField;
    @FXML private TextField signUpDobField; // Renamed from signUpAgeField
    @FXML private TextField signUpPhoneField;
    @FXML private PasswordField signUpConfirmPasswordField;

    @FXML private TextArea signUpBioArea;

    @FXML private ImageView profileImageView;
    @FXML private Label profilePhotoLabel;

    @FXML private StackPane imageCropContainer;
    @FXML private Slider zoomSlider;

    private double startX, startY;
    private double initialTranslateX, initialTranslateY;

    @FXML
    public void initialize() {
        firebaseService = MainApplication.firebaseService;

        if (imageCropContainer != null) {
            Circle clip = new Circle(50);
            clip.setCenterX(50);
            clip.setCenterY(50);
            imageCropContainer.setClip(clip);

            imageCropContainer.setOnMousePressed(e -> {
                startX = e.getSceneX();
                startY = e.getSceneY();
                initialTranslateX = profileImageView.getTranslateX();
                initialTranslateY = profileImageView.getTranslateY();
            });

            imageCropContainer.setOnMouseDragged(e -> {
                double deltaX = e.getSceneX() - startX;
                double deltaY = e.getSceneY() - startY;
                profileImageView.setTranslateX(initialTranslateX + deltaX);
                profileImageView.setTranslateY(initialTranslateY + deltaY);
            });
        }

        if (zoomSlider != null) {
            zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                profileImageView.setScaleX(newVal.doubleValue());
                profileImageView.setScaleY(newVal.doubleValue());
            });
        }
    }

    @FXML
    void handleSignInAction(ActionEvent event) {
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

        feedbackLabel.setText("Signing in..."); // Show loading state

        // RUN ASYNC to prevent UI Freeze
        CompletableFuture.runAsync(() -> {
            try {
                User user = firebaseService.getUserByUsername(username);

                if (user == null) {
                    Platform.runLater(() -> feedbackLabel.setText("Sign-in failed: User not found."));
                    return;
                }

                String storedPassword = user.getPassword();

                if (password.equals(storedPassword)) {
                    // --- NEW LOGIC: Update Last Login Time ---
                    try {
                        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        user.setLastLogin(currentTime);
                        firebaseService.updateUser(user); // Save to Firestore
                    } catch (Exception e) {
                        System.err.println("Failed to update last login: " + e.getMessage());
                        // We continue anyway, as login shouldn't fail just because timestamp failed
                    }

                    // Load Main Menu on UI Thread
                    Platform.runLater(() -> {
                        try {
                            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-menu.fxml"));
                            Scene scene = new Scene(fxmlLoader.load(), MainApplication.WINDOW_WIDTH, MainApplication.WINDOW_HEIGHT);

                            if (MainApplication.class.getResource("Style.css") != null) {
                                scene.getStylesheets().add(MainApplication.class.getResource("Style.css").toExternalForm());
                            }

                            MainMenuController controller = fxmlLoader.getController();
                            controller.setUserData(user);

                            Stage stage = (Stage) signInPane.getScene().getWindow();
                            stage.setScene(scene);
                            stage.setTitle("Main Menu");
                            stage.show();

                        } catch (IOException e) {
                            System.err.println("Failed to load main menu screen.");
                            e.printStackTrace();
                            feedbackLabel.setText("Login successful, but failed to load main menu.");
                        }
                    });

                } else {
                    Platform.runLater(() -> feedbackLabel.setText("Sign-in failed: Incorrect password."));
                }

            } catch (ExecutionException | InterruptedException e) {
                System.err.println("Error during sign-in process.");
                Platform.runLater(() -> {
                    feedbackLabel.setText("Error: Could not sign in. See console for details.");
                });
                e.printStackTrace();
            }
        });
    }

    @FXML
    void handleSignUpAction(ActionEvent event) {
        if (firebaseService == null) {
            feedbackLabel.setText("Error: Database service is not available.");
            return;
        }

        String name = signUpNameField.getText();
        String email = signUpEmailField.getText();
        String password = signUpPasswordField.getText();
        String confirmPassword = signUpConfirmPasswordField.getText();
        String username = signUpUsernameField.getText();
        String dob = signUpDobField.getText(); // Changed from age to dob
        String phone = signUpPhoneField.getText();
        String bio = signUpBioArea.getText();

        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() ||
                username.isBlank() || dob.isBlank() || phone.isBlank()) {
            feedbackLabel.setText("Please fill in all sign-up fields.");
            return;
        }

        // Validate Date Format (YYYY-MM-DD)
        try {
            LocalDate.parse(dob, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            feedbackLabel.setText("Date of birth must be YYYY-MM-DD (e.g., 1990-01-01).");
            return;
        }

        if (!password.equals(confirmPassword)) {
            feedbackLabel.setText("Passwords do not match. Please try again.");
            return;
        }

        feedbackLabel.setText("Creating account..."); // Loading state

        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setUsername(username);
        newUser.setDob(dob); // Set DOB instead of Age
        newUser.setPhone(phone);
        newUser.setBio(bio);
        newUser.setProfilePhotoUrl(null);
        // Set registered time
        newUser.setRegistered(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        // RUN ASYNC
        CompletableFuture.runAsync(() -> {
            try {
                String newUserId = firebaseService.saveUserDetails(newUser);
                System.out.println("Successfully saved user data with ID: " + newUserId);

                if (profileImageView.getImage() != null && selectedPhotoFile != null) {
                    try {
                        // Snapshot must be on UI thread
                        final File[] tempFileContainer = {null};

                        // We need to wait for the UI operation to finish before uploading
                        CompletableFuture<Void> snapshotFuture = new CompletableFuture<>();

                        Platform.runLater(() -> {
                            try {
                                SnapshotParameters params = new SnapshotParameters();
                                params.setFill(Color.TRANSPARENT);
                                var croppedImage = imageCropContainer.snapshot(params, null);

                                File tempFile = File.createTempFile("profile_crop", ".png");
                                ImageIO.write(SwingFXUtils.fromFXImage(croppedImage, null), "png", tempFile);
                                tempFileContainer[0] = tempFile;
                                snapshotFuture.complete(null);
                            } catch (Exception ex) {
                                snapshotFuture.completeExceptionally(ex);
                            }
                        });

                        snapshotFuture.join(); // Wait for snapshot

                        if (tempFileContainer[0] != null) {
                            firebaseService.uploadProfilePhoto(tempFileContainer[0], newUserId);
                            System.out.println("Photo uploaded and linked successfully.");
                            tempFileContainer[0].delete();
                        }

                    } catch (Exception e) {
                        System.err.println("Error uploading photo: " + e.getMessage());
                        e.printStackTrace();
                        Platform.runLater(() -> feedbackLabel.setText("Account created, but photo upload failed."));
                    }
                }

                Platform.runLater(() -> {
                    feedbackLabel.setText("Account created successfully! Please sign in.");
                    showSignInPane(null);
                });

            } catch (ExecutionException | InterruptedException e) {
                System.err.println("Error saving user data to Firestore.");
                Platform.runLater(() -> feedbackLabel.setText("Error: Could not create account."));
                e.printStackTrace();
            }
        });
    }

    @FXML
    void handleChoosePhotoAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                profileImageView.setTranslateX(0);
                profileImageView.setTranslateY(0);
                profileImageView.setScaleX(1);
                profileImageView.setScaleY(1);

                if (zoomSlider != null) {
                    zoomSlider.setValue(1);
                    zoomSlider.setDisable(false);
                }

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

    private void clearSignUpForm() {
        signUpNameField.clear();
        signUpEmailField.clear();
        signUpPasswordField.clear();
        signUpConfirmPasswordField.clear();
        signUpUsernameField.clear();
        signUpDobField.clear(); // Clear DOB field
        signUpPhoneField.clear();
        signUpBioArea.clear();

        selectedPhotoFile = null;
        profilePhotoLabel.setText("No photo selected.");
        profileImageView.setImage(null);

        profileImageView.setTranslateX(0);
        profileImageView.setTranslateY(0);
        profileImageView.setScaleX(1);
        profileImageView.setScaleY(1);
        if (zoomSlider != null) {
            zoomSlider.setValue(1);
            zoomSlider.setDisable(true);
        }

        feedbackLabel.setText("");
        signInUsernameField.clear();
        signInPasswordField.clear();
    }
}