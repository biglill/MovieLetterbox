module com.example.demo {
    // Required for JavaFX UI components
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics; // <-- ADD THIS for Image and ImageView

    // Required for the Firebase Admin SDK
    requires firebase.admin;

    // Required for Google Cloud authentication and Firestore services
    requires com.google.auth;
    requires com.google.auth.oauth2;
    requires google.cloud.firestore;
    requires com.google.api.apicommon;
    requires google.cloud.core;
    requires google.cloud.storage; // <-- ADD THIS for Firebase Storage
    requires java.net.http;
    requires com.google.gson;
    requires java.desktop;

    // Opens your package to the FXML loader so it can access your controller
    opens com.example.demo to javafx.fxml;

    // Exports your package so the JavaFX application can launch
    exports com.example.demo;
}