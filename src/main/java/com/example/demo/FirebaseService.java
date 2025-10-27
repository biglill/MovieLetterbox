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

    // !!! IMPORTANT: Find this value in your Firebase Console !!!
    // Go to Storage -> Files -> It looks like "your-project-id.appspot.com"
    private final String BUCKET_NAME = "movieletterbox.firebasestorage.app";

    public FirebaseService() throws IOException {
        // STEP 1: Read the path to the service account key from an environment variable.
        String serviceAccountPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

        if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
            throw new IOException("Error: The GOOGLE_APPLICATION_CREDENTIALS environment variable is not set. Please set it to the path of your serviceAccountKey.json file.");
        }

        // STEP 2: Initialize Firebase using the path from the environment variable.
        FileInputStream serviceAccountStream = new FileInputStream(serviceAccountPath);

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                .setStorageBucket(BUCKET_NAME) // <-- ADD THIS
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        db = FirestoreClient.getFirestore();

        // STEP 3: Initialize Google Cloud Storage
        // We need to "reset" the stream to use it again for storage credentials
        FileInputStream serviceAccountStreamForStorage = new FileInputStream(serviceAccountPath);
        storage = StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountStreamForStorage))
                .build()
                .getService();
    }

    /**
     * Saves a user's details to the "users" collection in Firestore.
     * @param userData A map containing the user's data (e.g., name, email).
     * @return The auto-generated document ID of the new user.
     * @throws ExecutionException If the database operation fails.
     * @throws InterruptedException If the thread is interrupted.
     */
    public String saveUserDetails(Map<String, Object> userData) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("users").document();
        ApiFuture<WriteResult> future = docRef.set(userData);
        System.out.println("User data saved successfully at: " + future.get().getUpdateTime());
        return docRef.getId();
    }

    /**
     * Uploads a profile photo to Firebase Storage and updates the user's Firestore document
     * with the public URL.
     * @param file The image file to upload.
     * @param userId The unique ID of the user (from Firestore) to link the photo.
     * @throws IOException If the file read fails.
     */
    public void uploadProfilePhoto(File file, String userId) throws IOException, ExecutionException, InterruptedException {
        String fileExtension = getFileExtension(file.getName());
        String blobName = "profile-images/" + userId + "." + fileExtension; // e.g., "profile-images/abc123xyz.jpg"

        BlobId blobId = BlobId.of(BUCKET_NAME, blobName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(Files.probeContentType(file.toPath()))
                .build();

        // Upload the file
        Blob blob = storage.create(blobInfo, Files.readAllBytes(file.toPath()));

        // Make the file public
        blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

        // Get the public URL
        String publicUrl = blob.getMediaLink();

        // Update the user's document in Firestore with the new URL
        DocumentReference userDoc = db.collection("users").document(userId);
        ApiFuture<WriteResult> updateFuture = userDoc.update("profilePhotoUrl", publicUrl);
        updateFuture.get(); // Wait for the update to complete
        System.out.println("Successfully uploaded photo and updated user document.");
    }

    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // No extension
        }
        return fileName.substring(lastIndexOf + 1);
    }

    /**
     * Retrieves a user document from Firestore based on their email.
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