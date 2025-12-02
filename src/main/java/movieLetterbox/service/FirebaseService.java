package movieLetterbox.service;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.Acl;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import movieLetterbox.model.Movie;
import movieLetterbox.model.User;
import movieLetterbox.model.Review;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FirebaseService {

    public final Firestore db;
    public final Storage storage;

    private final String BUCKET_NAME = "movieletterbox.firebasestorage.app";

    public FirebaseService() throws IOException {
        String serviceAccountPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

        if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
            throw new IOException("Error: The GOOGLE_APPLICATION_CREDENTIALS environment variable is not set. Please set it to the path of your serviceAccountKey.json file.");
        }

        FileInputStream serviceAccountStream = new FileInputStream(serviceAccountPath);

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                .setStorageBucket(BUCKET_NAME)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        db = FirestoreClient.getFirestore();

        FileInputStream serviceAccountStreamForStorage = new FileInputStream(serviceAccountPath);
        storage = StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountStreamForStorage))
                .build()
                .getService();
    }

    // --- USER METHODS ---

    public String saveUserDetails(User user) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("users").document();
        user.setUserId(docRef.getId());
        ApiFuture<WriteResult> future = docRef.set(user);
        return docRef.getId();
    }

    public void updateUser(User user) throws ExecutionException, InterruptedException {
        if (user.getUserId() == null) return;
        DocumentReference docRef = db.collection("users").document(user.getUserId());
        docRef.set(user);
    }

    public String uploadProfilePhoto(File file, String userId) throws IOException, ExecutionException, InterruptedException {
        String fileExtension = getFileExtension(file.getName());
        String blobName = "profile-images/" + userId + "-" + System.currentTimeMillis() + "." + fileExtension;

        BlobId blobId = BlobId.of(BUCKET_NAME, blobName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(Files.probeContentType(file.toPath()))
                .build();

        Blob blob = storage.create(blobInfo, Files.readAllBytes(file.toPath()));
        blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
        String publicUrl = blob.getMediaLink();

        DocumentReference userDoc = db.collection("users").document(userId);
        userDoc.update("profilePhotoUrl", publicUrl).get();
        return publicUrl;
    }

    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        return (lastIndexOf == -1) ? "" : fileName.substring(lastIndexOf + 1);
    }

    public User getUserByUsername(String username) throws ExecutionException, InterruptedException {
        CollectionReference users = db.collection("users");
        Query query = users.whereEqualTo("username", username);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        if (!querySnapshot.get().getDocuments().isEmpty()) {
            return querySnapshot.get().getDocuments().get(0).toObject(User.class);
        }
        return null;
    }

    public User getUserByEmail(String email) throws ExecutionException, InterruptedException {
        CollectionReference users = db.collection("users");
        Query query = users.whereEqualTo("email", email);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        if (!querySnapshot.get().getDocuments().isEmpty()) {
            return querySnapshot.get().getDocuments().get(0).toObject(User.class);
        }
        return null;
    }

    public User getUserById(String userId) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = db.collection("users").document(userId).get().get();
        if (document.exists()) {
            return document.toObject(User.class);
        }
        return null;
    }

    public List<User> searchUsers(String usernameQuery) throws ExecutionException, InterruptedException {
        String end = usernameQuery + "\uf8ff";
        Query query = db.collection("users")
                .orderBy("username")
                .startAt(usernameQuery)
                .endAt(end);

        ApiFuture<QuerySnapshot> future = query.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<User> users = new ArrayList<>();
        for (DocumentSnapshot doc : documents) {
            users.add(doc.toObject(User.class));
        }
        return users;
    }

    // --- FRIEND / FOLLOW METHODS ---

    public void followUser(User currentUser, String targetUserId) {
        try {
            if (currentUser.getFollowing() == null) currentUser.setFollowing(new ArrayList<>());
            if (!currentUser.getFollowing().contains(targetUserId)) {
                currentUser.getFollowing().add(targetUserId);
            }

            DocumentReference userRef = db.collection("users").document(currentUser.getUserId());
            userRef.update("following", FieldValue.arrayUnion(targetUserId));
            System.out.println("Followed user: " + targetUserId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unfollowUser(User currentUser, String targetUserId) {
        try {
            if (currentUser.getFollowing() != null) {
                currentUser.getFollowing().remove(targetUserId);
            }

            DocumentReference userRef = db.collection("users").document(currentUser.getUserId());
            userRef.update("following", FieldValue.arrayRemove(targetUserId));
            System.out.println("Unfollowed user: " + targetUserId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<User> getFollowers(String userId) throws ExecutionException, InterruptedException {
        Query query = db.collection("users").whereArrayContains("following", userId);
        ApiFuture<QuerySnapshot> future = query.get();
        List<User> users = new ArrayList<>();
        for (DocumentSnapshot doc : future.get().getDocuments()) {
            users.add(doc.toObject(User.class));
        }
        return users;
    }

    public List<User> getUsersByIds(List<String> userIds) throws ExecutionException, InterruptedException {
        List<User> users = new ArrayList<>();
        if (userIds == null || userIds.isEmpty()) return users;

        for (String id : userIds) {
            User u = getUserById(id);
            if (u != null) users.add(u);
        }
        return users;
    }

    // --- FAVORITES METHODS ---

    public void addFavorite(User user, String movieId) {
        try {
            if (user.getFavorites() == null) user.setFavorites(new ArrayList<>());
            if (!user.getFavorites().contains(movieId)) {
                user.getFavorites().add(movieId);
            }
            DocumentReference userRef = db.collection("users").document(user.getUserId());
            userRef.update("favorites", FieldValue.arrayUnion(movieId));
            DocumentReference movieRef = db.collection("movies").document(movieId);
            movieRef.update("favoriteCount", FieldValue.increment(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeFavorite(User user, String movieId) {
        try {
            if (user.getFavorites() != null) {
                user.getFavorites().remove(movieId);
            }
            DocumentReference userRef = db.collection("users").document(user.getUserId());
            userRef.update("favorites", FieldValue.arrayRemove(movieId));
            DocumentReference movieRef = db.collection("movies").document(movieId);
            movieRef.update("favoriteCount", FieldValue.increment(-1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- MOVIE & REVIEW METHODS ---

    public void addReview(Movie movie, Review review) {
        DocumentReference movieRef = db.collection("movies").document(movie.getMovieId());
        try {
            db.runTransaction(transaction -> {
                DocumentSnapshot snapshot = transaction.get(movieRef).get();
                if (!snapshot.exists()) {
                    movie.setRatingCount(0);
                    movie.setRatingTotal(0);
                    transaction.set(movieRef, movie);
                }
                DocumentReference newReviewRef = movieRef.collection("reviews").document();
                review.setReviewId(newReviewRef.getId());
                transaction.set(newReviewRef, review);
                transaction.update(movieRef, "ratingCount", FieldValue.increment(1));
                transaction.update(movieRef, "ratingTotal", FieldValue.increment(review.getRating()));
                return null;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public List<Review> getReviews(String movieId) {
        List<Review> reviews = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> future = db.collection("movies")
                    .document(movieId)
                    .collection("reviews")
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                reviews.add(doc.toObject(Review.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public Review getUserReview(String movieId, String userId) {
        try {
            Query query = db.collection("movies")
                    .document(movieId)
                    .collection("reviews")
                    .whereEqualTo("userId", userId)
                    .limit(1);
            ApiFuture<QuerySnapshot> future = query.get();
            List<QueryDocumentSnapshot> docs = future.get().getDocuments();
            if (!docs.isEmpty()) {
                return docs.get(0).toObject(Review.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}