package com.aclsim.database;

import com.aclsim.model.Packet;
import com.aclsim.model.Rule;
import java.sql.*;

public class LogDAO {

    public void saveLog(Packet packet, String result, Integer ruleId) {
        String sql = "INSERT INTO simulation_logs (src_ip, dest_ip, protocol, port, result, matched_rule_id) VALUES (?,?,?,?,?,?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, packet.getSourceIp());
            pstmt.setString(2, packet.getDestIp());
            pstmt.setString(3, packet.getProtocol());
            pstmt.setString(4, String.valueOf(packet.getDestPort()));
            pstmt.setString(5, result);
            
            if (ruleId != null) pstmt.setInt(6, ruleId);
            else pstmt.setNull(6, java.sql.Types.INTEGER);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}