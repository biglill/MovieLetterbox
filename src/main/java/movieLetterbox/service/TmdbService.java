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

/**
 * Service class that handles all HTTP communication with The Movie Database (TMDB) API.
 * Responsible for searching movies, fetching movie details, and getting trending lists.
 */
public class TmdbService {
    private static final String BASE_URL = "https://api.themoviedb.org/3";

    // Bearer Token for API Authentication
    private final String BEARER_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJiNWM2OWRiYjFkZGIzMDE0YWZiMjI4YzVlYzk5MGNmOCIsIm5iZiI6MTc1Nzg3MTE3OS4yMTgsInN1YiI6IjY4YzZmYzRiZDk3MjAzOTVmYWYyMWQ3ZCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.bUfEg5f-_0AMYquE-A2VLraxDY-1rqsJ-T9GP4E_lMc";

    private final HttpClient client;

    public TmdbService() {
        this.client = HttpClient.newHttpClient();
    }

    /**
     * Helper method to send a GET request to the provided URL.
     * Includes the necessary Authorization headers.
     */
    private JsonObject sendRequest(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + BEARER_TOKEN)
                .header("accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("TMDB API Error: " + response.statusCode() + " " + response.body());
            throw new IOException("HTTP Error: " + response.statusCode());
        }

        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    /**
     * Searches TMDB for movies matching the given query string.
     */
    public JsonObject searchMovies(String query) throws IOException, InterruptedException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = BASE_URL + "/search/movie?query=" + encodedQuery + "&language=en-US&page=1&include_adult=false";
        return sendRequest(url);
    }

    /**
     * Fetches detailed information for a specific movie ID.
     */
    public JsonObject getMovieById(String movieId) throws IOException, InterruptedException {
        String url = BASE_URL + "/movie/" + movieId + "?language=en-US";
        return sendRequest(url);
    }

    /**
     * Retrieves the list of movies currently trending for the week.
     */
    public JsonObject getTrendingMovies() throws IOException, InterruptedException {
        String url = BASE_URL + "/trending/movie/week?language=en-US";
        return sendRequest(url);
    }
}