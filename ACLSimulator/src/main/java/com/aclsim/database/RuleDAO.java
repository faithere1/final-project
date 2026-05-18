package com.aclsim.database;

import com.aclsim.model.Rule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RuleDAO {

    /**
     * 1. Get all rules from the database.
     * Ordered by priority so the simulator processes them in the correct sequence.
     */
    public List<Rule> getAllRules() {
        List<Rule> rules = new ArrayList<>();
        String sql = "SELECT * FROM rules ORDER BY priority ASC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rules.add(new Rule(
                    rs.getInt("id"),
                    rs.getInt("priority"),
                    rs.getString("action"),
                    rs.getString("protocol"),
                    rs.getString("source_ip"),
                    rs.getString("dest_ip"),
                    rs.getString("dest_port") 
                ));
            }
        } catch (SQLException e) {
            System.err.println("Database Error (getAllRules): " + e.getMessage());
        }
        return rules;
    }

    /**
     * 2. Add a new rule to the database.
     */
    public void addRule(Rule rule) {
        String sql = "INSERT INTO rules (priority, action, protocol, source_ip, dest_ip, dest_port) VALUES (?,?,?,?,?,?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, rule.getPriority());
            pstmt.setString(2, rule.getAction());
            pstmt.setString(3, rule.getProtocol());
            pstmt.setString(4, rule.getSourceIp());
            pstmt.setString(5, rule.getDestIp());
            pstmt.setString(6, rule.getDestPort());
            
            pstmt.executeUpdate();
            System.out.println("LOG: Rule saved to database.");
        } catch (SQLException e) {
            System.err.println("Database Error (addRule): " + e.getMessage());
        }
    }

    /**
     * 3. Update an existing rule (Supports 2.1.3 Editing).
     */
    public void updateRule(Rule rule) {
        String sql = "UPDATE rules SET priority=?, action=?, protocol=?, source_ip=?, dest_ip=?, dest_port=? WHERE id=?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, rule.getPriority());
            pstmt.setString(2, rule.getAction());
            pstmt.setString(3, rule.getProtocol());
            pstmt.setString(4, rule.getSourceIp());
            pstmt.setString(5, rule.getDestIp());
            pstmt.setString(6, rule.getDestPort());
            pstmt.setInt(7, rule.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database Error (updateRule): " + e.getMessage());
        }
    }

    /**
     * 4. Optimized Batch Update for 2.4.1 (Drag-and-Drop).
     * Uses a Transaction to update all priorities in one go.
     */
    public void syncPriorities(List<Rule> rules) {
        String sql = "UPDATE rules SET priority = ? WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Start Transaction
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Rule rule : rules) {
                    pstmt.setInt(1, rule.getPriority());
                    pstmt.setInt(2, rule.getId());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                conn.commit(); // Commit Transaction
                System.out.println("LOG: Batch priority sync complete.");
            } catch (SQLException e) {
                conn.rollback(); // Rollback if any update fails
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Database Error (syncPriorities): " + e.getMessage());
        }
    }

    /**
     * 5. Delete a rule by ID.
     */
    public void deleteRule(int id) {
        String sql = "DELETE FROM rules WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("LOG: Rule deleted.");
        } catch (SQLException e) {
            System.err.println("Database Error (deleteRule): " + e.getMessage());
        }
    }
}