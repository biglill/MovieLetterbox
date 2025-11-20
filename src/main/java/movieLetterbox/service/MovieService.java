package movieLetterbox.service;

import com.google.gson.GsonBuilder;
import movieLetterbox.MainApplication;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MovieService {
    private FirebaseService firebaseService;
    private OmdbService omdbService;

    public MovieService() {
        firebaseService = MainApplication.firebaseService;
        omdbService = MainApplication.omdbService;
    }

    public void CreateMovie(String movieID) {
        try {
            JsonObject movieJson = omdbService.GetMovieByID(movieID);

            if (movieJson.get("Response").getAsBoolean()) {
                System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(movieJson));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
