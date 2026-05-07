package com.aclsim;

import com.aclsim.database.DatabaseManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize the database when the app starts
        DatabaseManager.initializeDatabase();
        
        primaryStage.setTitle("ACL Simulator");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}