package com.aclsim.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:acl_simulator.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        // Updated rules table: dest_port is now TEXT to support ranges like "80-443"
        String createRulesTable = """
            CREATE TABLE IF NOT EXISTS rules (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                priority INTEGER NOT NULL,
                action TEXT NOT NULL,
                protocol TEXT NOT NULL,
                source_ip TEXT NOT NULL,
                dest_ip TEXT NOT NULL,
                dest_port TEXT 
            );
            """;

        // NEW FOR 2.3.1: Table to store simulation history
        String createLogsTable = """
            CREATE TABLE IF NOT EXISTS simulation_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                src_ip TEXT,
                dest_ip TEXT,
                protocol TEXT,
                port TEXT,
                result TEXT,
                matched_rule_id INTEGER
            );
            """;

        // NEW FOR 3.4.2: Progress Tracking Table
        String createProgressTable ="""
                CREATE TABLE IF NOT EXISTS user_progress(
                    lab_id INTEGER PRIMARY KEY,
                    completed INTEGER DEFAULT 0,
                    attempts INTEGER DEFAULT 0
                );
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Execute both table creations
            stmt.execute(createRulesTable);
            stmt.execute(createLogsTable);
            stmt.execute(createProgressTable); // Initialize the progress table
            
            System.out.println(">>> Database initialized with Rules, Logs, and Progress tracking.");
        } catch (SQLException e) {
            System.err.println(">>> Database initialization error: " + e.getMessage());
        }
    }
}