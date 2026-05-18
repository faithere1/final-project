package com.aclsim.database;

import com.aclsim.model.Packet;
import java.sql.*;

public class LogDAO {

    /**
     * 2.3.1: Saves a simulation result to the SQLite database.
     */
    public void saveLog(Packet packet, String result, Integer ruleId) {
        String sql = "INSERT INTO simulation_logs (src_ip, dest_ip, protocol, port, result, matched_rule_id) VALUES (?,?,?,?,?,?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, packet.getSourceIp());
            pstmt.setString(2, packet.getDestIp());
            pstmt.setString(3, packet.getProtocol());
            
            // If it's ICMP, log the type, otherwise log the port
            String portOrType = packet.getProtocol().equalsIgnoreCase("ICMP") 
                                ? String.valueOf(packet.getIcmpType()) 
                                : String.valueOf(packet.getDestPort());
            pstmt.setString(4, portOrType);
            
            pstmt.setString(5, result);
            
            if (ruleId != null) {
                pstmt.setInt(6, ruleId);
            } else {
                pstmt.setNull(6, java.sql.Types.INTEGER);
            }
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving log: " + e.getMessage());
        }
    }

    /**
     * 2.3.1: Retrieves the most recent logs formatted as a plain-text table.
     */
    public String getFormattedLogs(int limit) {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT * FROM simulation_logs ORDER BY id DESC LIMIT ?";

        // Table Header
        sb.append(String.format("%-20s | %-15s | %-5s | %-8s | %-25s%n", 
                  "Timestamp", "Source IP", "Prot", "Port", "Result"));
        sb.append("-".repeat(85)).append("\n");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                sb.append(String.format("%-20s | %-15s | %-5s | %-8s | %-25s%n",
                    rs.getString("timestamp"),
                    rs.getString("src_ip"),
                    rs.getString("protocol"),
                    rs.getString("port"),
                    rs.getString("result")
                ));
            }
        } catch (SQLException e) {
            return "Error retrieving history: " + e.getMessage();
        }

        return sb.length() > 110 ? sb.toString() : "No simulation history found.";
    }
}