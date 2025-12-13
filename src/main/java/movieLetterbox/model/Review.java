package movieLetterbox.model;

/**
 * Data model representing a user review for a specific movie.
 * Stored in Firestore within a sub-collection of the movie.
 */
public class Review {
    private String reviewId;
    private String movieId;
    private String userId;
    private String username;
    private int rating;      // 1-5 stars
    private String reviewText;
    private String timestamp;

    // Required empty constructor for Firestore serialization
    public Review() {}

    public Review(String userId, String username, String movieId, int rating, String reviewText) {
        this.userId = userId;
        this.username = username;
        this.movieId = movieId;
        this.rating = rating;
        this.reviewText = reviewText;
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}