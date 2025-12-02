package movieLetterbox;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import movieLetterbox.service.FirebaseService;
import movieLetterbox.service.MovieService;
import movieLetterbox.service.TmdbService;

import java.io.IOException;

public class MainApplication extends Application {
    public static FirebaseService firebaseService;
    public static TmdbService tmdbService; // CHANGED
    public static MovieService movieService;

    // Defined constants for uniform window size across the application
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 800;

    private boolean firebaseInit () throws IOException {
        try {
            firebaseService = new FirebaseService();
            System.out.println("Firebase initialized successfully.");
            return true;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void start(Stage stage) throws IOException {
        if (!firebaseInit()) {
            Platform.exit();
            System.err.println("Failed to initialize firebase service.");
            return;
        }

        tmdbService = new TmdbService(); // CHANGED
        movieService = new MovieService();

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(MainApplication.class.getResource("Style.css").toExternalForm());
        stage.setTitle("MovieLetterbox");
        stage.setScene(scene);
        stage.show();
    }
}