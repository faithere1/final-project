package com.aclsim;

import com.aclsim.database.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

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

            // ---NEW FOR 4.3.1: BRANDED TASKBAR/WINDOW ICON---
            try{
                InputStream iconStream = getClass().getResourceAsStream("/com/aclsim/logo.png");
                if(iconStream != null){
                    Image brandIcon = new Image(iconStream);
                    primaryStage.getIcons().add(brandIcon);
                    System.out.println(">>> Branded identity icon loaded successfully.");
                } else {
                    System.err.println(">>> WARNING: Branded logopng not found in resources folder.");
                }
            } catch (Exception e) {
                System.err.println(">>> ERROR: Failed to load branded icon." + e.getMessage());
                e.printStackTrace();
            }

            
            //--- 4.3.1: MACOS DOCK ICON FIX---
            if (System.getProperty("os.name").toLowerCase().contains("mac")){
                try{
                    //Use AWT Taskbar API to set macOS Dock Icon
                    InputStream macStream = getClass().getResourceAsStream("/com/aclsim/logo.png");
                    if(macStream != null){
                        java.awt.image.BufferedImage awtImage = javax.imageio.ImageIO.read(macStream);
                        java.awt.Taskbar.getTaskbar().setIconImage(awtImage);
                            System.out.println(">>> macOS Dock icon set successfully.");
                    }
                } catch (Exception e) {
                    System.err.println(">>> Note: macOS Dock icon bypass used (AWT Desktop environment check).");
                }
            }

            

            //-------------------------------------------------------------
            
            // 4. Configure and show the Stage (the window)
            primaryStage.setTitle("ACL Simulator Dashboard");
            primaryStage.setScene(scene);
            
            // Set a minimum size so the layout doesn't break
            primaryStage.setMinWidth(1100);
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