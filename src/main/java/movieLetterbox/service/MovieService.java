package movieLetterbox.service;

import com.google.gson.GsonBuilder;
import movieLetterbox.MainApplication;
import com.google.gson.JsonObject;
import java.io.IOException;

public class MovieService {
    private FirebaseService firebaseService;
    private TmdbService tmdbService;

    public MovieService() {
        firebaseService = MainApplication.firebaseService;
        tmdbService = MainApplication.tmdbService;
    }

    public void CreateMovie(String movieID) {
        try {
            JsonObject movieJson = tmdbService.getMovieById(movieID);

            if (movieJson != null && movieJson.has("id")) {
                System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(movieJson));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}