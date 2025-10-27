package com.example.demo;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.cloud.storage.*;
import com.google.cloud.storage.Blob;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirebaseService {

    private final Firestore db;
    private final Storage storage;

    // Make sure this is set to your project's bucket name
    private final String BUCKET_NAME = "movieletterbox.firebasestorage.app"; // <-- Ensure this is correct!

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

    public String saveUserDetails(Map<String, Object> userData) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("users").document();
        ApiFuture<WriteResult> future = docRef.set(userData);
        System.out.println("User data saved successfully at: " + future.get().getUpdateTime());
        return docRef.getId();
    }

    public void uploadProfilePhoto(File file, String userId) throws IOException, ExecutionException, InterruptedException {
        String fileExtension = getFileExtension(file.getName());
        String blobName = "profile-images/" + userId + "." + fileExtension;

        BlobId blobId = BlobId.of(BUCKET_NAME, blobName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(Files.probeContentType(file.toPath()))
                .build();

        Blob blob = storage.create(blobInfo, Files.readAllBytes(file.toPath()));
        blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
        String publicUrl = blob.getMediaLink();

        DocumentReference userDoc = db.collection("users").document(userId);
        ApiFuture<WriteResult> updateFuture = userDoc.update("profilePhotoUrl", publicUrl);
        updateFuture.get();
        System.out.println("Successfully uploaded photo and updated user document.");
    }

    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return fileName.substring(lastIndexOf + 1);
    }

    // +++++++++ NEW METHOD START +++++++++
    /**
     * Retrieves a user document from Firestore based on their username.
     * @param username The username of the user to search for.
     * @return A Map containing the user's data if found, otherwise null.
     * @throws ExecutionException If the database operation fails.
     * @throws InterruptedException If the thread is interrupted.
     */
    public Map<String, Object> getUserByUsername(String username) throws ExecutionException, InterruptedException {
        // Create a query to search for a user with the matching username.
        CollectionReference users = db.collection("users");
        Query query = users.whereEqualTo("username", username);

        // Execute the query.
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        // Check if any documents were returned.
        if (!querySnapshot.get().getDocuments().isEmpty()) {
            // Get the first document (usernames should be unique).
            QueryDocumentSnapshot document = querySnapshot.get().getDocuments().get(0);
            System.out.println("Found user with username: " + username);
            return document.getData();
        } else {
            System.out.println("No user found with username: " + username);
            return null;
        }
    }
    // +++++++++ NEW METHOD END +++++++++

    /**
     * Retrieves a user document from Firestore based on their email.
     * (This method is no longer used for sign-in but could be useful elsewhere)
     * @param email The email of the user to search for.
     * @return A Map containing the user's data if found, otherwise null.
     * @throws ExecutionException If the database operation fails.
     * @throws InterruptedException If the thread is interrupted.
     */
    public Map<String, Object> getUserByEmail(String email) throws ExecutionException, InterruptedException {
        CollectionReference users = db.collection("users");
        Query query = users.whereEqualTo("email", email);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        if (!querySnapshot.get().getDocuments().isEmpty()) {
            QueryDocumentSnapshot document = querySnapshot.get().getDocuments().get(0);
            System.out.println("Found user with email: " + email);
            return document.getData();
        } else {
            System.out.println("No user found with email: " + email);
            return null;
        }
    }
}