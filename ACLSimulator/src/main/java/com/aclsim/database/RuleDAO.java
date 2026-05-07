package com.aclsim.database;

import com.aclsim.model.Rule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RuleDAO {

    // 1. Get all rules from the database
    public List<Rule> getAllRules() {
        List<Rule> rules = new ArrayList<>();
        // We order by priority so the ACL logic engine processes them in the right sequence
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
                    rs.getInt("dest_port")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Database Error (getAllRules): " + e.getMessage());
        }
        return rules;
    }

    // 2. Add a new rule
    public void addRule(Rule rule) {
        // FIXED: Using standard INSERT syntax
        String sql = "INSERT INTO rules (priority, action, protocol, source_ip, dest_ip, dest_port) VALUES (?,?,?,?,?,?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // FIXED: Changed prepareCall to prepareStatement
            
            pstmt.setInt(1, rule.getPriority());
            pstmt.setString(2, rule.getAction());
            pstmt.setString(3, rule.getProtocol());
            pstmt.setString(4, rule.getSourceIp());
            pstmt.setString(5, rule.getDestIp());
            pstmt.setInt(6, rule.getDestPort());
            
            pstmt.executeUpdate();
            System.out.println("LOG: Rule with priority " + rule.getPriority() + " saved to SQLite.");
        } catch (SQLException e) {
            System.err.println("Database Error (addRule): " + e.getMessage());
        }
    }

    // 3. Delete a rule by ID
    public void deleteRule(int id) {
        String sql = "DELETE FROM rules WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("LOG: Rule ID " + id + " deleted from database.");
            }
        } catch (SQLException e) {
            System.err.println("Database Error (deleteRule): " + e.getMessage());
        }
    }
}