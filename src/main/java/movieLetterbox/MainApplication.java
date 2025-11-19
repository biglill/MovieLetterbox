package movieLetterbox;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import movieLetterbox.service.FirebaseService;

import java.io.IOException;

public class MainApplication extends Application {
    public FirebaseService firebaseService;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 800);
        scene.getStylesheets().add(MainApplication.class.getResource("Style.css").toExternalForm());
        stage.setTitle("MovieLetterbox");
        stage.setScene(scene);
        stage.show();
    }
}
