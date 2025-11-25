package movieLetterbox.controller;

import movieLetterbox.service.FirebaseService;
import movieLetterbox.MainApplication;
import movieLetterbox.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea; // Imported TextArea
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
    @FXML private TextField signUpAgeField;
    @FXML private TextField signUpPhoneField;
    @FXML private PasswordField signUpConfirmPasswordField;

    // NEW: Bio Field
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

        // --- 1. SETUP CIRCULAR CROP ---
        if (imageCropContainer != null) {
            Circle clip = new Circle(50);
            clip.setCenterX(50);
            clip.setCenterY(50);
            imageCropContainer.setClip(clip);

            // --- 2. SETUP DRAG (PAN) LOGIC ---
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

        // --- 3. SETUP ZOOM LOGIC ---
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

        try {
            User user = firebaseService.getUserByUsername(username);

            if (user == null) {
                feedbackLabel.setText("Sign-in failed: User not found.");
                return;
            }

            String storedPassword = user.getPassword();

            if (password.equals(storedPassword)) {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-menu.fxml"));
                    Scene scene = new Scene(fxmlLoader.load(), 800, 800);

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

            } else {
                feedbackLabel.setText("Sign-in failed: Incorrect password.");
            }

        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Error during sign-in process.");
            feedbackLabel.setText("Error: Could not sign in. See console for details.");
            e.printStackTrace();
        }
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
        String age = signUpAgeField.getText();
        String phone = signUpPhoneField.getText();
        String bio = signUpBioArea.getText(); // Get bio text

        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() ||
                username.isBlank() || age.isBlank() || phone.isBlank()) {
            feedbackLabel.setText("Please fill in all sign-up fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            feedbackLabel.setText("Passwords do not match. Please try again.");
            return;
        }

        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setUsername(username);
        newUser.setAge(age);
        newUser.setPhone(phone);
        newUser.setBio(bio); // Set the bio
        newUser.setProfilePhotoUrl(null);

        try {
            String newUserId = firebaseService.saveUserDetails(newUser);
            System.out.println("Successfully saved user data with ID: " + newUserId);

            if (profileImageView.getImage() != null && selectedPhotoFile != null) {
                try {
                    SnapshotParameters params = new SnapshotParameters();
                    params.setFill(Color.TRANSPARENT);
                    var croppedImage = imageCropContainer.snapshot(params, null);

                    File tempFile = File.createTempFile("profile_crop", ".png");
                    ImageIO.write(SwingFXUtils.fromFXImage(croppedImage, null), "png", tempFile);

                    firebaseService.uploadProfilePhoto(tempFile, newUserId);
                    System.out.println("Photo uploaded and linked successfully.");

                    tempFile.delete();

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
        signUpAgeField.clear();
        signUpPhoneField.clear();
        signUpBioArea.clear(); // Clear bio

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