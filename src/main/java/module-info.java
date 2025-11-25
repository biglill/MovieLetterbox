module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;

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
    opens movieLetterbox to javafx.fxml, google.cloud.firestore;

    // Exports your package so the JavaFX application can launch
    exports movieLetterbox;
    exports movieLetterbox.controller;
    opens movieLetterbox.controller to google.cloud.firestore, javafx.fxml;
    exports movieLetterbox.model;
    opens movieLetterbox.model to google.cloud.firestore, javafx.fxml;
    exports movieLetterbox.service;
    opens movieLetterbox.service to google.cloud.firestore, javafx.fxml;
}