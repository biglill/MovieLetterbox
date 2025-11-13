package com.example.demo;

public class Movie {
    private int movieId;
    private String name;
    private int year;
    private String ageRating;
    private String release;
    private int runtime;
    private String genre;
    private String plot;
    private String language;
    private String country;
    private String rewards;
    private String posterPic;
    private int favoriteCount;
    private int ratingCount;
    private double ratingTotal;

    public Movie() {}

    public Movie(
            int movieId,
            String name,
            int year,
            String ageRating,
            String release,
            int runtime,
            String genre,
            String plot,
            String language,
            String country,
            String rewards,
            String posterPic,
            int favoriteCount,
            int ratingCount,
            double ratingTotal
    ) {
        this.movieId = movieId;
        this.name = name;
        this.year = year;
        this.ageRating = ageRating;
        this.release = release;
        this.runtime = runtime;
        this.genre = genre;
        this.plot = plot;
        this.language = language;
        this.country = country;
        this.rewards = rewards;
        this.posterPic = posterPic;
        this.favoriteCount = favoriteCount;
        this.ratingCount = ratingCount;
        this.ratingTotal = ratingTotal;
    }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getAgeRating() { return ageRating; }
    public void setAgeRating(String ageRating) { this.ageRating = ageRating; }

    public String getRelease() { return release; }
    public void setRelease(String release) { this.release = release; }

    public int getRuntime() { return runtime; }
    public void setRuntime(int runtime) { this.runtime = runtime; }

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

    @Override
    public String toString() {
        return "Movie{" +
                "movieId=" + movieId +
                ", name='" + name + '\'' +
                ", year=" + year +
                ", ageRating='" + ageRating + '\'' +
                ", release='" + release + '\'' +
                ", runtime=" + runtime +
                ", genre='" + genre + '\'' +
                ", plot='" + plot + '\'' +
                ", language='" + language + '\'' +
                ", country='" + country + '\'' +
                ", rewards='" + rewards + '\'' +
                ", posterPic='" + posterPic + '\'' +
                ", favoriteCount=" + favoriteCount +
                ", ratingCount=" + ratingCount +
                ", ratingTotal=" + ratingTotal +
                '}';
    }
}