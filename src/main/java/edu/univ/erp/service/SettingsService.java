package edu.univ.erp.service;

import edu.univ.erp.data.ERPDatabase;
import java.sql.*;
import java.time.LocalDate;

public class SettingsService {
    public static boolean isMaintenanceOn() {
        try (Connection conn = ERPDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT maintenance FROM system_settings LIMIT 1")) {
            if (rs.next()) {
                return "ON".equalsIgnoreCase(rs.getString("maintenance"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static void toggleMaintenance(boolean on) {
        try (Connection conn = ERPDatabase.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE system_settings SET maintenance = ? WHERE id = 1");
            ps.setString(1, on ? "ON" : "OFF");
            ps.executeUpdate();
            String msg = "Maintenance mode turned " + (on ? "ON" : "OFF") + " by Admin.";
            PreparedStatement ps2 = conn.prepareStatement(
                    "INSERT INTO notifications (message) VALUES (?)");
            ps2.setString(1, msg);
            ps2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void ensureSettingsTableExists() {
        try (Connection conn = ERPDatabase.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS system_settings (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    maintenance ENUM('ON','OFF') DEFAULT 'OFF'
                )
            """);
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM system_settings");
            if (rs.next() && rs.getInt("cnt") == 0) {
                stmt.executeUpdate("INSERT INTO system_settings (maintenance) VALUES ('OFF')");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static boolean checkMaintenanceBlock(String actionName) {
        if (isMaintenanceOn()) {
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "System is currently under maintenance.\n" +
                            "Action blocked: " + actionName,
                    "Maintenance Mode Active",
                    javax.swing.JOptionPane.WARNING_MESSAGE
            );
            return true;
        }
        return false;
    }
    public static boolean isDropDeadlineOver() {
        LocalDate dl = LocalDate.of(2025, 12, 30);
        LocalDate tdy = LocalDate.now();
        return tdy.isAfter(dl);
    }
}
