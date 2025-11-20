package movieLetterbox;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import movieLetterbox.service.FirebaseService;
import movieLetterbox.service.MovieService;
import movieLetterbox.service.OmdbService;

import java.io.IOException;


public class MainApplication extends Application {
    public static FirebaseService firebaseService;
    public static OmdbService omdbService;
    public static MovieService movieService;

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

        omdbService = new OmdbService();
        movieService = new MovieService();

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 800);
        scene.getStylesheets().add(MainApplication.class.getResource("Style.css").toExternalForm());
        stage.setTitle("MovieLetterbox");
        stage.setScene(scene);
        stage.show();

        movieService.CreateMovie("tt6718170");

    }
}
