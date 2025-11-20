package movieLetterbox.service;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.cloud.storage.*;
import com.google.cloud.storage.Blob;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import movieLetterbox.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
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

    /**
     * Saves a User object to the 'users' collection.
     * The User's userId field will be populated with the Firestore Document ID.
     * @param user The User object to save.
     * @return The new Document ID.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public String saveUserDetails(User user) throws ExecutionException, InterruptedException {
        // 1. Create a document reference with a new auto-generated ID
        DocumentReference docRef = db.collection("users").document();

        // 2. Get that ID and set it on the user object
        user.setUserId(docRef.getId());

        // 3. Now, save the user object (which includes its own ID)
        ApiFuture<WriteResult> future = docRef.set(user);

        System.out.println("User data saved successfully at: " + future.get().getUpdateTime());

        // 4. Return the ID
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

    public User getUserByUsername(String username) throws ExecutionException, InterruptedException {
        CollectionReference users = db.collection("users");
        Query query = users.whereEqualTo("username", username);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        if (!querySnapshot.get().getDocuments().isEmpty()) {
            QueryDocumentSnapshot document = querySnapshot.get().getDocuments().get(0);
            System.out.println("Found user with username: " + username);
            return document.toObject(User.class);
        } else {
            System.out.println("No user found with username: " + username);
            return null;
        }
    }

    public User getUserByEmail(String email) throws ExecutionException, InterruptedException {
        CollectionReference users = db.collection("users");
        Query query = users.whereEqualTo("email", email);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        if (!querySnapshot.get().getDocuments().isEmpty()) {
            QueryDocumentSnapshot document = querySnapshot.get().getDocuments().get(0);
            System.out.println("Found user with email: " + email);
            return document.toObject(User.class);
        } else {
            System.out.println("No user found with email: " + email);
            return null;
        }
    }
}