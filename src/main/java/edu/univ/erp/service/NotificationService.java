package edu.univ.erp.service;

import edu.univ.erp.data.ERPDatabase;
import java.sql.*;

public class NotificationService {
    public static String getLatestNotification() {
        try (Connection conn = ERPDatabase.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT message FROM notifications ORDER BY created_at DESC LIMIT 1")) {
            if (rs.next()) return rs.getString("message");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "No new notifications.";
    }
}
