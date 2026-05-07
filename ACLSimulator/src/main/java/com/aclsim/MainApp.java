package com.aclsim;

import java.util.List;
import java.util.Arrays; // Useful for the Arrays.asList method

import com.aclsim.database.DatabaseManager;
import com.aclsim.model.Rule;     // Corrected: Removed main.java
import com.aclsim.model.Packet;   // Added: Missing import
import com.aclsim.service.SimulatorService; // Corrected: Removed main.java

import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize the database
        DatabaseManager.initializeDatabase();

        // --- PHASE 2 TEST SCENARIO ---
        // Rule 1: Priority 10, DENY TCP from 1.1.1.1 to 2.2.2.2
        Rule r1 = new Rule(1, 10, "DENY", "TCP", "1.1.1.1", "2.2.2.2", 80);
        
        // Rule 2: Priority 20, PERMIT ANY from 1.1.1.1 to anywhere
        Rule r2 = new Rule(2, 20, "PERMIT", "ANY", "1.1.1.1", "0.0.0.0", -1);

        // Store rules in a list
        List<Rule> myRules = Arrays.asList(r1, r2);
        SimulatorService engine = new SimulatorService();

        // Test Case: A TCP packet that should hit Rule 1 (DENY)
        Packet testPacket = new Packet("1.1.1.1", "2.2.2.2", "TCP", 80);
        Rule result = engine.simulate(testPacket, myRules);

        System.out.println("--- SIMULATION TEST ---");
        if (result != null) {
            System.out.println("Packet Result: " + result.getAction() + " (Matched Rule #" + result.getPriority() + ")");
        } else {
            System.out.println("Packet Result: DENY (Implicit Deny)");
        }
        // ----------------------------

        primaryStage.setTitle("ACL Simulator");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}