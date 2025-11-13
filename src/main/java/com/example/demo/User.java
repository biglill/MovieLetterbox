package com.example.demo;

public class User {
    private int userId;           // Primary Key
    private String email;
    private String password;
    private String username;
    private String bio;
    private String profilePic;
    private String registered;
    private String lastLogin;
    private int totalLikes;

    // Constructors
    public User() {}

    public User(
            int userId,
            String email,
            String password,
            String username,
            String bio,
            String profilePic,
            String registered,
            String lastLogin,
            int totalLikes
    ) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.username = username;
        this.bio = bio;
        this.profilePic = profilePic;
        this.registered = registered;
        this.lastLogin = lastLogin;
        this.totalLikes = totalLikes;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfilePic() { return profilePic; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }

    public String getRegistered() { return registered; }
    public void setRegistered(String registered) { this.registered = registered; }

    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }

    public int getTotalLikes() { return totalLikes; }
    public void setTotalLikes(int totalLikes) { this.totalLikes = totalLikes; }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                ", bio='" + bio + '\'' +
                ", profilePic='" + profilePic + '\'' +
                ", registered='" + registered + '\'' +
                ", lastLogin='" + lastLogin + '\'' +
                ", totalLikes=" + totalLikes +
                '}';
    }
}
