package movieLetterbox.controller;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import movieLetterbox.MainApplication;
import movieLetterbox.model.Movie;
import movieLetterbox.model.User;
import movieLetterbox.service.FirebaseService;
import movieLetterbox.service.OmdbService;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class ProfileController {

    // --- VIEW 1: PROFILE PAGE FIELDS ---
    @FXML private ScrollPane profileViewPane;
    @FXML private ImageView viewProfileImage;
    @FXML private Label viewUsernameLabel;
    @FXML private Label viewBioLabel;
    @FXML private HBox favoritesContainer;
    @FXML private Button editProfileButton;
    @FXML private Button followButton; // NEW

    // --- VIEW 2: EDIT PAGE FIELDS ---
    @FXML private VBox editProfilePane;
    @FXML private ImageView profileImageView;
    @FXML private StackPane imageCropContainer;
    @FXML private Slider zoomSlider;

    @FXML private TextField usernameField;
    @FXML private TextArea bioArea;
    @FXML private Label statusLabel;

    // --- VIEW 3: CHANGE PASSWORD FIELDS ---
    @FXML private VBox changePasswordPane;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmNewPasswordField;
    @FXML private Label passwordStatusLabel;

    // currentUser = The logged-in user
    private User currentUser;
    // profileUser = The user whose profile we are looking at
    private User profileUser;

    private FirebaseService firebaseService;
    private final OmdbService omdbService = new OmdbService();
    private File selectedPhotoFile;

    private double startX, startY;
    private double initialTranslateX, initialTranslateY;

    @FXML
    public void initialize() {
        firebaseService = MainApplication.firebaseService;
        profileViewPane.setVisible(true);
        editProfilePane.setVisible(false);
        if (changePasswordPane != null) changePasswordPane.setVisible(false);

        // Setup image crop interactions
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

    /**
     * Sets the logged-in user.
     * By default, this assumes the user is viewing their OWN profile.
     */
    public void setUserData(User user) {
        this.currentUser = user;
        this.profileUser = user; // Default to self-view
        updateUI();
    }

    /**
     * Call this when viewing ANOTHER user's profile.
     */
    public void setProfileData(User currentUser, User targetUser) {
        this.currentUser = currentUser;
        this.profileUser = targetUser;
        updateUI();
    }

    private void updateUI() {
        if (profileUser == null) return;

        // 1. Load Profile Details
        viewUsernameLabel.setText(profileUser.getUsername());
        viewBioLabel.setText("Bio: " + (profileUser.getBio() != null ? profileUser.getBio() : ""));

        // Load image
        if (profileUser.getProfilePhotoUrl() != null && !profileUser.getProfilePhotoUrl().isBlank()) {
            Image image = new Image(profileUser.getProfilePhotoUrl(), true);
            viewProfileImage.setImage(image);
            profileImageView.setImage(image);
        } else {
            try {
                if (MainApplication.class.getResource("placeholder.png") != null) {
                    Image placeholder = new Image(MainApplication.class.getResource("placeholder.png").toExternalForm());
                    viewProfileImage.setImage(placeholder);
                    profileImageView.setImage(placeholder);
                }
            } catch (Exception e) {}
        }
        resetImageAdjustment();

        // 2. Handle "My Profile" vs "Friend's Profile"
        if (currentUser.getUserId().equals(profileUser.getUserId())) {
            // Viewing Self
            editProfileButton.setVisible(true);
            editProfileButton.setManaged(true);
            followButton.setVisible(false);
            followButton.setManaged(false);

            // Populate edit fields
            usernameField.setText(currentUser.getUsername());
            bioArea.setText(currentUser.getBio() != null ? currentUser.getBio() : "");
        } else {
            // Viewing Someone Else
            editProfileButton.setVisible(false);
            editProfileButton.setManaged(false);
            followButton.setVisible(true);
            followButton.setManaged(true);

            updateFollowButtonState();
        }

        // 3. Load Favorites (of the profileUser, not currentUser)
        loadFavorites();
    }

    private void updateFollowButtonState() {
        boolean isFollowing = currentUser.getFollowing() != null && currentUser.getFollowing().contains(profileUser.getUserId());
        if (isFollowing) {
            followButton.setText("Unfollow");
            followButton.setStyle("-fx-background-color: #E53E3E;"); // Red for unfollow
        } else {
            followButton.setText("Follow");
            followButton.setStyle("-fx-background-color: #4C51BF;"); // Standard purple
        }
    }

    @FXML
    void handleFollowAction(ActionEvent event) {
        boolean isFollowing = currentUser.getFollowing() != null && currentUser.getFollowing().contains(profileUser.getUserId());

        if (isFollowing) {
            firebaseService.unfollowUser(currentUser, profileUser.getUserId());
        } else {
            firebaseService.followUser(currentUser, profileUser.getUserId());
        }
        // Update UI immediately
        updateFollowButtonState();
    }

    private void loadFavorites() {
        favoritesContainer.getChildren().clear();

        if (profileUser.getFavorites() == null || profileUser.getFavorites().isEmpty()) {
            Label placeholder = new Label("No favorite movies yet.");
            placeholder.setStyle("-fx-text-fill: #888888; -fx-font-style: italic;");
            favoritesContainer.getChildren().add(placeholder);
            return;
        }

        for (String movieId : profileUser.getFavorites()) {
            CompletableFuture.runAsync(() -> {
                try {
                    JsonObject json = omdbService.GetMovieByID(movieId);
                    Movie movie = new Movie(json);
                    Platform.runLater(() -> favoritesContainer.getChildren().add(createFavoriteCard(movie)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private VBox createFavoriteCard(Movie movie) {
        VBox card = new VBox();
        card.getStyleClass().add("movie-card");
        card.setPrefWidth(200);
        card.setPrefHeight(300);
        card.setAlignment(javafx.geometry.Pos.CENTER);

        ImageView poster = new ImageView();
        poster.setFitWidth(180);
        poster.setFitHeight(270);
        poster.setPreserveRatio(true);
        poster.getStyleClass().add("movie-poster");

        if (movie.getPosterPic() != null && !movie.getPosterPic().equals("N/A")) {
            poster.setImage(new Image(movie.getPosterPic(), true));
        }

        card.getChildren().addAll(poster);

        card.setOnMouseClicked(e -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("movie-details.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), MainApplication.WINDOW_WIDTH, MainApplication.WINDOW_HEIGHT);
                if (MainApplication.class.getResource("Style.css") != null) {
                    scene.getStylesheets().add(MainApplication.class.getResource("Style.css").toExternalForm());
                }
                MovieDetailsController controller = fxmlLoader.getController();
                controller.setMovieData(movie.getMovieId());
                controller.setUserData(currentUser); // Keep logged-in user

                Stage stage = (Stage) favoritesContainer.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle(movie.getName() + " - Details");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        return card;
    }

    private void resetImageAdjustment() {
        if (profileImageView != null) {
            profileImageView.setTranslateX(0);
            profileImageView.setTranslateY(0);
            profileImageView.setScaleX(1);
            profileImageView.setScaleY(1);
        }
        if (zoomSlider != null) {
            zoomSlider.setValue(1);
            zoomSlider.setDisable(profileImageView.getImage() == null);
        }
    }

    // --- NAVIGATION ACTIONS ---
    @FXML
    void handleBackAction(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-menu.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), MainApplication.WINDOW_WIDTH, MainApplication.WINDOW_HEIGHT);
            if (MainApplication.class.getResource("Style.css") != null) {
                scene.getStylesheets().add(MainApplication.class.getResource("Style.css").toExternalForm());
            }
            MainMenuController controller = fxmlLoader.getController();
            controller.setUserData(currentUser);
            Stage stage = (Stage) profileViewPane.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleEditProfileAction(ActionEvent event) {
        profileViewPane.setVisible(false);
        editProfilePane.setVisible(true);
        statusLabel.setText("");
        selectedPhotoFile = null;
    }

    @FXML
    void handleCancelEditAction(ActionEvent event) {
        editProfilePane.setVisible(false);
        profileViewPane.setVisible(true);
        updateUI();
    }

    // --- EDIT ACTIONS ---
    @FXML
    void handleChangePhotoAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select New Profile Photo");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(editProfilePane.getScene().getWindow());
        if (file != null) {
            selectedPhotoFile = file;
            try {
                Image image = new Image(file.toURI().toString());
                profileImageView.setImage(image);
                resetImageAdjustment();
                zoomSlider.setDisable(false);
                statusLabel.setText("Photo selected. Use drag & slider to adjust.");
                statusLabel.setStyle("-fx-text-fill: blue;");
            } catch (Exception e) {
                statusLabel.setText("Error loading image.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    @FXML
    void handleSaveAction(ActionEvent event) {
        String newUsername = usernameField.getText();
        String newBio = bioArea.getText();
        if (newUsername.isBlank()) {
            statusLabel.setText("Username cannot be empty.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        currentUser.setUsername(newUsername);
        currentUser.setBio(newBio);
        statusLabel.setText("Saving...");
        new Thread(() -> {
            try {
                if (selectedPhotoFile != null && profileImageView.getImage() != null) {
                    javafx.application.Platform.runLater(() -> {
                        try {
                            SnapshotParameters params = new SnapshotParameters();
                            params.setFill(Color.TRANSPARENT);
                            var croppedImage = imageCropContainer.snapshot(params, null);
                            File tempFile = File.createTempFile("profile_crop_update", ".png");
                            ImageIO.write(SwingFXUtils.fromFXImage(croppedImage, null), "png", tempFile);
                            new Thread(() -> {
                                try {
                                    String newUrl = firebaseService.uploadProfilePhoto(tempFile, currentUser.getUserId());
                                    currentUser.setProfilePhotoUrl(newUrl);
                                    tempFile.delete();
                                    finalizeSave();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    showError("Error uploading photo.");
                                }
                            }).start();
                        } catch (Exception e) {
                            e.printStackTrace();
                            showError("Error preparing photo.");
                        }
                    });
                } else {
                    finalizeSave();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error saving profile.");
            }
        }).start();
    }

    private void finalizeSave() throws java.util.concurrent.ExecutionException, InterruptedException {
        firebaseService.updateUser(currentUser);
        javafx.application.Platform.runLater(() -> {
            updateUI();
            statusLabel.setText("Saved!");
            editProfilePane.setVisible(false);
            profileViewPane.setVisible(true);
        });
    }

    private void showError(String msg) {
        javafx.application.Platform.runLater(() -> {
            statusLabel.setText(msg);
            statusLabel.setStyle("-fx-text-fill: red;");
        });
    }

    @FXML
    void handleChangePasswordAction(ActionEvent event) {
        editProfilePane.setVisible(false);
        changePasswordPane.setVisible(true);
        passwordStatusLabel.setText("");
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmNewPasswordField.clear();
    }

    @FXML
    void handleCancelPasswordAction(ActionEvent event) {
        changePasswordPane.setVisible(false);
        editProfilePane.setVisible(true);
    }

    @FXML
    void handleUpdatePasswordAction(ActionEvent event) {
        String currentPass = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmNewPasswordField.getText();
        if (currentPass.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
            passwordStatusLabel.setText("All fields are required.");
            passwordStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        if (!currentPass.equals(currentUser.getPassword())) {
            passwordStatusLabel.setText("Incorrect current password.");
            passwordStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            passwordStatusLabel.setText("New passwords do not match.");
            passwordStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        currentUser.setPassword(newPass);
        new Thread(() -> {
            try {
                firebaseService.updateUser(currentUser);
                javafx.application.Platform.runLater(() -> {
                    passwordStatusLabel.setText("Password updated successfully!");
                    passwordStatusLabel.setStyle("-fx-text-fill: green;");
                    new Thread(() -> {
                        try {
                            Thread.sleep(1500);
                            javafx.application.Platform.runLater(() -> {
                                changePasswordPane.setVisible(false);
                                editProfilePane.setVisible(true);
                            });
                        } catch (InterruptedException e) {}
                    }).start();
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    passwordStatusLabel.setText("Error updating password.");
                    passwordStatusLabel.setStyle("-fx-text-fill: red;");
                });
            }
        }).start();
    }
}