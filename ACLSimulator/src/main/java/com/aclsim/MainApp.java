package com.aclsim;

import com.aclsim.database.DatabaseManager;
import javafx.animation.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader; // FIXED IMPORT
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager.initializeDatabase();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aclsim/main_view.fxml"));
            Parent root = loader.load();
            Scene mainScene = new Scene(root);
            String cssPath = getClass().getResource("/com/aclsim/style.css").toExternalForm();
            mainScene.getStylesheets().add(cssPath);
            

            primaryStage.setTitle("ACL Simulator v5.0");
            primaryStage.setScene(mainScene);
            primaryStage.setMinWidth(1100);
            primaryStage.setMinHeight(750);

            // Window taskbar icon
            try {
                InputStream iconStream = getClass().getResourceAsStream("/com/aclsim/logo.png");
                if (iconStream != null) {
                    primaryStage.getIcons().add(new Image(iconStream));
                }
            } catch (Exception e) {
                System.err.println(">>> Failed to load window icon: " + e.getMessage());
            }

            // macOS Dock Icon
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                try {
                    InputStream macStream = getClass().getResourceAsStream("/com/aclsim/logo.png");
                    if (macStream != null) {
                        java.awt.image.BufferedImage awtImage = javax.imageio.ImageIO.read(macStream);
                        java.awt.Taskbar.getTaskbar().setIconImage(awtImage);
                    }
                } catch (Exception e) {
                    System.err.println(">>> macOS Dock bypass.");
                }
            }

            showSplashScreen(primaryStage);

        } catch (IOException e) {
            System.err.println("CRITICAL ERROR: Could not load main_view.fxml");
            e.printStackTrace();
        }
    }

    private void showSplashScreen(Stage mainStage) {
        Stage splashStage = new Stage(StageStyle.UNDECORATED);

        VBox splashLayout = new VBox(20);
        splashLayout.setAlignment(Pos.CENTER);
        splashLayout.setPadding(new Insets(25));
        splashLayout.setStyle("-fx-background-color: #0b0f19; -fx-border-color: #3b4b61; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Pulsing brand logo image
        ImageView logoView = new ImageView();
        try {
            InputStream imgStream = getClass().getResourceAsStream("/com/aclsim/logo.png");
            if (imgStream != null) {
                logoView.setImage(new Image(imgStream));
                logoView.setFitWidth(200);
                logoView.setFitHeight(200);
                logoView.setSmooth(true);
            }
        } catch (Exception e) {
            System.err.println("Could not load splash logo: " + e.getMessage());
        }

        // Expanded Height Terminal Label to fit ASCII Art safely
        Label lblTerminal = new Label("booting sentinel_os...\n");
        lblTerminal.setPrefHeight(300); 
        lblTerminal.setPrefWidth(840);
        lblTerminal.setWrapText(true);
        lblTerminal.setStyle("-fx-font-family: 'JetBrains Mono', 'Consolas', 'Monospaced', monospace; -fx-font-size: 11px; -fx-text-fill: #06b6d4; -fx-alignment: top-left;");

        splashLayout.getChildren().addAll(logoView, lblTerminal);
        Scene splashScene = new Scene(splashLayout, 720, 500, Color.TRANSPARENT); // Made taller for ASCII design
        splashStage.setScene(splashScene);

        // --- ANIMATIONS ---

        FadeTransition logoPulse = new FadeTransition(Duration.seconds(1.0), logoView);
        logoPulse.setFromValue(0.5);
        logoPulse.setToValue(1.0);
        logoPulse.setCycleCount(Animation.INDEFINITE);
        logoPulse.setAutoReverse(true);
        logoPulse.play();

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), splashLayout);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        StringBuilder telemetryLog = new StringBuilder();
        Timeline telemetryTimeline = new Timeline();

        // Typeset logs including final ASCII MOTD Shield Logo
        String[] logLines = {
            "[BOOT] Sentinel Secure Engine v5.0... [OK]\n",
            "[INIT] Connecting SQLite database... [OK]\n",
            "[RULES] Parsing extended rulesets... [OK]\n",
            "[ACADEMY] Mapping 8 scenario labs... [OK]\n",
            "[NETOPS] Initializing topology... [READY]\n",
            " \n",
            " \n",
            " \n",
            " \n",
            "READY TO START \n",
            " \n",
            " \n",
            " \n", 
            ">>> The ACL Simulator v5.0 - The Ultimate Access Control List Training Platform \n" +
            ">>> Developed by vibe coding.\n",
            " \n",
            " \n"
        };

        int delay = 400;
        for (int i = 0; i < logLines.length; i++) {
            final String line = logLines[i];
            KeyFrame frame = new KeyFrame(Duration.millis(delay), e -> {
                telemetryLog.append(line);
                lblTerminal.setText(telemetryLog.toString());
            });
            telemetryTimeline.getKeyFrames().add(frame);
            
            // Give the final ASCII art block a slightly longer "printing" delay for realism
            if (i == logLines.length - 1) {
                delay += 600;
            } else {
                delay += 300;
            }
        }

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.6), splashLayout);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.millis(delay + 200));

        fadeOut.setOnFinished(e -> {
            logoPulse.stop();
            splashStage.close();
            mainStage.show();
        });

        fadeIn.setOnFinished(e -> telemetryTimeline.play());
        telemetryTimeline.setOnFinished(e -> fadeOut.play());

        splashStage.show();
        fadeIn.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
