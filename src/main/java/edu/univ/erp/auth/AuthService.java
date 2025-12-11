package edu.univ.erp.auth;

import edu.univ.erp.data.AuthDatabase;
import edu.univ.erp.service.SettingsService;
import org.mindrot.jbcrypt.BCrypt;
import javax.swing.*;
import java.sql.*;

public class AuthService {
    public static int login(String username, String password) {
        try {
            ResultSet rs = AuthDatabase.getUserByUsername(username);
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String role = rs.getString("role");
                String status = rs.getString("status");
                int fails = rs.getInt("failed_attempts");
                if ("BLOCKED".equalsIgnoreCase(status)) {
                    JOptionPane.showMessageDialog(null,
                            "This account is locked due to multiple failed logins.\nContact admin to unblock.",
                            "Account Blocked",
                            JOptionPane.ERROR_MESSAGE);
                    return 3;
                }
                if (BCrypt.checkpw(password, storedHash)) {
                    resetFailedAttempts(username);
                    if (!"ADMIN".equalsIgnoreCase(role) && SettingsService.isMaintenanceOn()) {
                        JOptionPane.showMessageDialog(null,
                                "Maintenance Mode is Active.\nYou can view your data, but changes are disabled for now.",
                                "Maintenance Mode",
                                JOptionPane.INFORMATION_MESSAGE);
                        Session.startSession(username, role);
                        Session.setReadOnly(true);
                        System.out.println("Login (READ-ONLY) is successful! Role: " + role);
                        return 1;
                    }
                    Session.startSession(username, role);
                    Session.setReadOnly(false);
                    System.out.println("Login is successful! Role: " + role);
                    return 1;
                } else {
                    incrementFailedAttempt(username, fails);
                    return 0;
                }
            } else {
                JOptionPane.showMessageDialog(null, "User was not found!");
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Login has failed due to a system error.");
            return 0;
        }
    }
    private static void incrementFailedAttempt(String username, int fails) {
        try (Connection conn = AuthDatabase.getConnection()) {
            fails++;
            if (fails >= 5) {
                String lsql = "UPDATE users_auth SET status = 'BLOCKED', failed_attempts = ?, last_failed = NOW() WHERE username = ?";
                PreparedStatement ps = conn.prepareStatement(lsql);
                ps.setInt(1, fails);
                ps.setString(2, username);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(null,
                        "Too many failed attempts (5/5).\nYour account has now been locked.",
                        "Account Locked",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                String fs = "UPDATE users_auth SET failed_attempts = ?, last_failed = NOW() WHERE username = ?";
                PreparedStatement ps = conn.prepareStatement(fs);
                ps.setInt(1, fails);
                ps.setString(2, username);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(null,
                        "Incorrect password (" + fails + "/5 attempts).",
                        "Login Failed",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void resetFailedAttempts(String username) {
        try (Connection conn = AuthDatabase.getConnection()) {
            String sql = "UPDATE users_auth SET failed_attempts = 0, last_failed = NULL WHERE username = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean changePassword(String username, String oldPass, String newPass) {
        try (Connection conn = AuthDatabase.getConnection()) {
            var stmt = conn.prepareStatement("SELECT password_hash FROM users_auth WHERE username = ?");
            stmt.setString(1, username);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                String stored = rs.getString("password_hash");
                if (BCrypt.checkpw(oldPass, stored)) {
                    var update = conn.prepareStatement(
                            "UPDATE users_auth SET password_hash = ? WHERE username = ?");
                    update.setString(1, BCrypt.hashpw(newPass, BCrypt.gensalt()));
                    update.setString(2, username);
                    update.executeUpdate();
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }
    public static boolean unblockUser(String username) {
        try (Connection conn = AuthDatabase.getConnection()) {
            String sql = "UPDATE users_auth SET status = 'ACTIVE', failed_attempts = 0, last_failed = NULL WHERE username = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            int now = ps.executeUpdate();
            return now > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
