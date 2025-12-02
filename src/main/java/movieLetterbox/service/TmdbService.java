package movieLetterbox.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TmdbService {
    // TMDB Base URL
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    // Replace with your actual TMDB API Key
    private final String API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJiNWM2OWRiYjFkZGIzMDE0YWZiMjI4YzVlYzk5MGNmOCIsIm5iZiI6MTc1Nzg3MTE3OS4yMTgsInN1YiI6IjY4YzZmYzRiZDk3MjAzOTVmYWYyMWQ3ZCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.bUfEg5f-_0AMYquE-A2VLraxDY-1rqsJ-T9GP4E_lMc";
    private final HttpClient client;

    public TmdbService() {
        this.client = HttpClient.newHttpClient();
    }

    private JsonObject sendRequest(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("TMDB API Error: " + response.statusCode() + " " + response.body());
            throw new IOException("HTTP Error: " + response.statusCode());
        }

        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    public JsonObject searchMovies(String query) throws IOException, InterruptedException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = BASE_URL + "/search/movie?api_key=" + API_KEY + "&query=" + encodedQuery + "&language=en-US&page=1&include_adult=false";
        return sendRequest(url);
    }

    public JsonObject getMovieById(String movieId) throws IOException, InterruptedException {
        // TMDB ID is usually an integer, but we treat it as a string for compatibility
        String url = BASE_URL + "/movie/" + movieId + "?api_key=" + API_KEY + "&language=en-US";
        return sendRequest(url);
    }
}