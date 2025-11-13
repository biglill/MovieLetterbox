package com.example.demo;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.print.DocFlavor;

public class OmdbService {
    private static final String URL = "https://www.omdbapi.com/";
    private final String API_KEY = "86c4b2d9";
    private final HttpClient client;

    public OmdbService() {
        this.client = HttpClient.newHttpClient();
    }

    private JsonObject OmdbHttpRequest(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("HTTP Error: " + response.statusCode());
        }

        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    public JsonObject SearchMovie(String movieTitle) throws IOException, InterruptedException {
        String encodedTitle = URLEncoder.encode(movieTitle, StandardCharsets.UTF_8);
        String url = URL + "?s=" + encodedTitle + "&type=movie&apikey=" + API_KEY;

        return OmdbHttpRequest(url);
    }

    public JsonObject GetMovieByTitle(String movieTitle) throws IOException, InterruptedException {
        String encodedTitle = URLEncoder.encode(movieTitle, StandardCharsets.UTF_8);
        String url = URL + "?t=" + encodedTitle + "&type=movie&apikey=" + API_KEY;

        return OmdbHttpRequest(url);
    }

    public JsonObject GetMovieByID(String movieID) throws IOException, InterruptedException {
        String encodedTitle = URLEncoder.encode(movieID, StandardCharsets.UTF_8);
        String url = URL + "?i=" + encodedTitle + "&type=movie&apikey=" + API_KEY;

        return OmdbHttpRequest(url);
    }

    /*
    Usage Example:

    OmdbService omdbService = new OmdbService();

    try {
        JsonObject movie = omdbService.SearchMovie("mario");

        movie.get("imdbID").getAsString();
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(movie));
    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
    }
     */
}
