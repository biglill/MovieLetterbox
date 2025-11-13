module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires firebase.admin;

    requires com.google.auth;
    requires com.google.auth.oauth2;
    requires google.cloud.firestore;
    requires com.google.api.apicommon;
    requires google.cloud.core;
    requires google.cloud.storage;
    requires java.net.http;
    requires com.google.gson;
    requires java.desktop;

    // Opens your package to the FXML loader
    opens com.example.demo to javafx.fxml, google.cloud.firestore;

    // Exports your package so the JavaFX application can launch
    exports com.example.demo;
}