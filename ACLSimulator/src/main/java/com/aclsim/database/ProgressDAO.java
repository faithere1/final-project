package com.aclsim.database;

import java.sql.*;

public class ProgressDAO{

    /**
     * Increments the attempt count for a spesific lab.
     */
    public void incrementAttempt(int labId){
        String sql = """
                INSERT INTO user_progress(lab_id, completed, attempts)
                VALUES(?,0,1)
                ON CONFLICT(lab_id) DO UPDATE SET attempts = attempts + 1;
                """;

        try(Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, labId);
            pstmt.executeUpdate();
        } catch (SQLException e){
            System.err.println("Database Error (incrementAttempt):" + e.getMessage());
        }
    }

    /**
     * Marks a lab as completed.
     */
    public void markAsCompleted(int labId){
        String sql = """
                INSERT INTO user_progress(lab_id, completed, attempts)
                VALUES(?,1,1)
                ON CONFLICT(lab_id) DO UPDATE SET completed = 1;
                """;

        try(Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, labId);
            pstmt.executeUpdate();
        } catch (SQLException e){
            System.err.println("Database Error (markAsCompleted):" + e.getMessage());
        }
    }
    /**
     * Retrieves the attempt count for a spesific lab.
     */
    public int getAttempts(int labId){
        String sql = "SELECT attempts FROM user_progress WHERE lab_id=?";
        try(Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, labId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                return rs.getInt("attempts");
            }
        } catch (SQLException e) {
            System.err.println("Database Error (getAttempts): " + e.getMessage());
        }
        return 0;
    }

    /**
     * Gets total completed labs count out of all active labs.
     */

    public int getCompletedCount(){
        String sql = "SELECT COUNT(*) AS total FROM user_progress WHERE completed = 1";
        try(Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            if(rs.next()){
                return rs.getInt("total");
            }
        } catch(SQLException e){
            System.err.println("Database Error (getCompletedCount): " + e.getMessage());
        }
        return 0;
    }

    public void clearAllProgress(){
        String sql="DELETE FROM user_progress";

        try(Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()){
            stmt.executeUpdate(sql);
            System.out.println("LOG: Academic progress database cleared successfully.");
        } catch(SQLException e){
            System.err.println("Database Error (clearAllProgress):" + e.getMessage());
        }
    }
}