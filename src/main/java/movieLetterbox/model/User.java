package movieLetterbox.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userId;
    private String email;
    private String password;
    private String username;
    private String bio;
    private String profilePhotoUrl;
    private String registered;
    private String lastLogin;
    private int totalLikes;

    private String name;
    private String age;
    private String phone;

    private List<String> favorites = new ArrayList<>();
    // NEW: List to store IDs of people this user follows
    private List<String> following = new ArrayList<>();

    public User() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public String getRegistered() { return registered; }
    public void setRegistered(String registered) { this.registered = registered; }

    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }

    public int getTotalLikes() { return totalLikes; }
    public void setTotalLikes(int totalLikes) { this.totalLikes = totalLikes; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public List<String> getFavorites() {
        if (favorites == null) favorites = new ArrayList<>();
        return favorites;
    }
    public void setFavorites(List<String> favorites) { this.favorites = favorites; }

    // NEW: Getter and Setter for Following
    public List<String> getFollowing() {
        if (following == null) following = new ArrayList<>();
        return following;
    }
    public void setFollowing(List<String> following) { this.following = following; }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", favorites=" + favorites +
                ", following=" + following +
                '}';
    }
}