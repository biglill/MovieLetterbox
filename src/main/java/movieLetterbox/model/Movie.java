package movieLetterbox.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Data model class representing a Movie.
 * Stores information fetched from TMDB as well as local community data (ratings/favorites).
 */
public class Movie {
    private String movieId;
    private String name;
    private int year;
    private String ageRating;
    private String release;
    private String runtime;
    private String genre;
    private String plot;
    private String language;
    private String country;
    private String rewards;
    private String posterPic;
    private int favoriteCount;
    private int ratingCount;
    private double ratingTotal;
    private double tmdbRating;

    // Base URL for fetching images from TMDB
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    public Movie() {}

    /**
     * Constructor for manually creating a movie object (e.g. for testing).
     */
    public Movie(String movieId, String name, int year, String posterPic) {
        this.movieId = movieId;
        this.name = name;
        this.year = year;
        this.posterPic = posterPic;
    }

    /**
     * Constructor that parses a TMDB JSON Object to populate movie fields.
     * Handles both 'Search Results' and 'Movie Details' JSON structures.
     */
    public Movie(JsonObject json) {
        // Handle ID
        if (json.has("id")) {
            this.movieId = String.valueOf(json.get("id").getAsInt());
        }

        // Title
        this.name = json.has("title") ? json.get("title").getAsString() : "Unknown Title";

        // Year / Release Date parsing
        if (json.has("release_date") && !json.get("release_date").getAsString().isEmpty()) {
            String date = json.get("release_date").getAsString(); // Format: YYYY-MM-DD
            this.release = date;
            try {
                this.year = Integer.parseInt(date.substring(0, 4));
            } catch (Exception e) {
                this.year = 0;
            }
        } else {
            this.year = 0;
            this.release = "N/A";
        }

        // Poster Path Construction
        if (json.has("poster_path") && !json.get("poster_path").isJsonNull()) {
            this.posterPic = IMAGE_BASE_URL + json.get("poster_path").getAsString();
        } else {
            this.posterPic = "N/A";
        }

        // Plot / Overview
        this.plot = json.has("overview") ? json.get("overview").getAsString() : "No description available.";

        // Runtime (Only available in Details endpoint, not Search)
        if (json.has("runtime") && !json.get("runtime").isJsonNull()) {
            this.runtime = json.get("runtime").getAsInt() + " min";
        } else {
            this.runtime = "N/A";
        }

        // Genre Parsing (handles array of objects vs array of IDs if needed)
        if (json.has("genres")) {
            JsonArray genres = json.getAsJsonArray("genres");
            List<String> genreList = new ArrayList<>();
            for (JsonElement g : genres) {
                genreList.add(g.getAsJsonObject().get("name").getAsString());
            }
            this.genre = String.join(", ", genreList);
        } else {
            this.genre = "N/A";
        }

        // Rating (Vote Average from TMDB 0-10)
        this.tmdbRating = json.has("vote_average") ? json.get("vote_average").getAsDouble() : 0.0;

        // Default / Missing Fields
        this.language = json.has("original_language") ? json.get("original_language").getAsString() : "en";
        this.country = "N/A";
        this.rewards = "N/A";
        this.ageRating = "N/A";

        // Initialize counters
        this.favoriteCount = 0;
        this.ratingCount = 0;
        this.ratingTotal = 0;
    }

    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getAgeRating() { return ageRating; }
    public void setAgeRating(String ageRating) { this.ageRating = ageRating; }

    public String getRelease() { return release; }
    public void setRelease(String release) { this.release = release; }

    public String getRuntime() { return runtime; }
    public void setRuntime(String runtime) { this.runtime = runtime; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getPlot() { return plot; }
    public void setPlot(String plot) { this.plot = plot; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getRewards() { return rewards; }
    public void setRewards(String rewards) { this.rewards = rewards; }

    public String getPosterPic() { return posterPic; }
    public void setPosterPic(String posterPic) { this.posterPic = posterPic; }

    public int getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(int favoriteCount) { this.favoriteCount = favoriteCount; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    public double getRatingTotal() { return ratingTotal; }
    public void setRatingTotal(double ratingTotal) { this.ratingTotal = ratingTotal; }

    public double getTmdbRating() { return tmdbRating; }
    public void setTmdbRating(double tmdbRating) { this.tmdbRating = tmdbRating; }
}