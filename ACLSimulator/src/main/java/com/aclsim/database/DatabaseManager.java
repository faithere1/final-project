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
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS rules (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                priority INTEGER NOT NULL,
                action TEXT NOT NULL,
                protocol TEXT NOT NULL,
                source_ip TEXT NOT NULL,
                dest_ip TEXT NOT NULL,
                dest_port INTEGER
            );
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println(">>> Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println(">>> Database error: " + e.getMessage());
        }
    }
}