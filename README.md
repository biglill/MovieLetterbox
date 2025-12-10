# MovieLetterbox 🎬

**MovieLetterbox** is a desktop-based social platform for movie enthusiasts. Built with JavaFX and powered by the Firebase backend and The Movie Database (TMDB) API, it allows users to discover movies, leave reviews, rate films, and connect with a community of cinephiles.

---

## 🚀 Features

* **User Authentication**: Secure sign-up and login system utilizing Firebase Authentication.
* **Movie Discovery**: 
    * View trending movies on the dashboard.
    * Search for movies using the massive TMDB database.
    * View detailed movie information including plot, release year, and cast.
* **Social & Community**:
    * **Profiles**: Customize your profile with a bio and profile picture (with crop/zoom functionality).
    * **Follow System**: Follow other users to build your network.
    * **User Search**: Find friends and other users by username.
* **Ratings & Reviews**:
    * Rate movies on a 5-star scale.
    * Write and publish written reviews.
    * View community ratings alongside official TMDB statistics.
* **Favorites**: Curate a list of your top favorite films displayed directly on your profile.

---

## 🛠 Tech Stack

* **Language**: Java (JDK 21)
* **Frontend**: JavaFX (FXML for UI design)
* **Build Tool**: Maven
* **Backend / Database**: Google Firebase (Firestore)
* **File Storage**: Google Cloud Storage (for Profile Pictures)
* **External API**: The Movie Database (TMDB) API
* **Libraries**: 
    * Gson (JSON Parsing)
    * Java.net.http (API Requests)

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed:
* [Java Development Kit (JDK) 21](https://www.oracle.com/java/technologies/downloads/#java21) or higher.
* [IntelliJ IDEA](https://www.jetbrains.com/idea/) (Recommended) or any Java IDE.
* A Google Firebase Project (for the database connection).

---

## ⚙️ Installation & Setup

### 1. Clone the Repository
```bash
git clone [https://github.com/yourusername/MovieLetterbox.git](https://github.com/yourusername/MovieLetterbox.git)
cd MovieLetterbox
