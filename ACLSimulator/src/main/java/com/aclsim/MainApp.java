package com.aclsim;

import com.aclsim.database.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. Initialize the database (this creates the table if it doesn't exist)
        DatabaseManager.initializeDatabase();

        try {
            // 2. Load the FXML file from the resources folder
            // Path: src/main/resources/com/aclsim/main_view.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aclsim/main_view.fxml"));
            Parent root = loader.load();

            // 3. Create the Scene with the loaded layout
            Scene scene = new Scene(root);

            // ---FORCE LOAD THE CSS VIA CODE (Bypasses FXML path bugs)---
            String cssPath = getClass().getResource("/com/aclsim/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            System.out.println(">>> Sentinel Theme Applied: " + cssPath);
            
            // 4. Configure and show the Stage (the window)
            primaryStage.setTitle("ACL Simulator Dashboard");
            primaryStage.setScene(scene);
            
            // Set a minimum size so the layout doesn't break
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(750);
            
            primaryStage.show();
            System.out.println(">>> ACL Simulator GUI started successfully.");

        } catch (IOException e) {
            System.err.println("CRITICAL ERROR: Could not load main_view.fxml");
            System.err.println("Check if the file is in: src/main/resources/com/aclsim/main_view.fxml");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}