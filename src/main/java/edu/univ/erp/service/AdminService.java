package edu.univ.erp.service;

import edu.univ.erp.data.AuthDatabase;
import edu.univ.erp.data.ERPDatabase;
import org.mindrot.jbcrypt.BCrypt;
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminService {
    public static boolean addUser(String username, String password, String role,
                                  String rollNo, String program, int year, String department) {
        if (edu.univ.erp.service.SettingsService.checkMaintenanceBlock("Admin operation")) return false;
        Connection authConn = null;
        Connection erpConn = null;
        try {
            authConn = AuthDatabase.getConnection();
            String csql = "SELECT COUNT(*) FROM users_auth WHERE username = ?";
            PreparedStatement checkStmt = authConn.prepareStatement(csql);
            checkStmt.setString(1, username);
            ResultSet rsCheck = checkStmt.executeQuery();
            rsCheck.next();
            if (rsCheck.getInt(1) > 0) {
                JOptionPane.showMessageDialog(null,
                        "Username '" + username + "' already exists.\nPlease choose another username.",
                        "Duplicate Username", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            String insertAuth = "INSERT INTO users_auth (username, role, password_hash, status) VALUES (?, ?, ?, 'ACTIVE')";
            PreparedStatement psAuth = authConn.prepareStatement(insertAuth, PreparedStatement.RETURN_GENERATED_KEYS);
            psAuth.setString(1, username);
            psAuth.setString(2, role);
            psAuth.setString(3, BCrypt.hashpw(password, BCrypt.gensalt()));
            psAuth.executeUpdate();
            int userId = 0;
            var rs = psAuth.getGeneratedKeys();
            if (rs.next()) userId = rs.getInt(1);
            erpConn = ERPDatabase.getConnection();
            if ("STUDENT".equalsIgnoreCase(role)) {
                String sql = "INSERT INTO students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = erpConn.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setString(2, rollNo);
                ps.setString(3, program);
                ps.setInt(4, year);
                ps.executeUpdate();
            } else if ("INSTRUCTOR".equalsIgnoreCase(role)) {
                String sql = "INSERT INTO instructors (user_id, department) VALUES (?, ?)";
                PreparedStatement ps = erpConn.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setString(2, department);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (authConn != null) authConn.close();
                if (erpConn != null) erpConn.close();
            } catch (SQLException ignored) {}
        }
    }
    public static boolean addCourse(String code, String title, int credits) {
        if (edu.univ.erp.service.SettingsService.checkMaintenanceBlock("Admin operation")) return false;
        try (Connection conn = ERPDatabase.getConnection()) {
            String sql = "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, code);
            ps.setString(2, title);
            ps.setInt(3, credits);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean addSection(int courseId, int instructorId, String dayTime,
                                     String room, int capacity, String semester, int year) {
        if (edu.univ.erp.service.SettingsService.checkMaintenanceBlock("Admin operation")) return false;
        try (Connection conn = ERPDatabase.getConnection()) {
            String sql = "INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, courseId);
            ps.setInt(2, instructorId);
            ps.setString(3, dayTime);
            ps.setString(4, room);
            ps.setInt(5, capacity);
            ps.setString(6, semester);
            ps.setInt(7, year);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean deleteSection(int sectionId) {
        if (edu.univ.erp.service.SettingsService.checkMaintenanceBlock("Admin operation")) return false;
        try (Connection conn = ERPDatabase.getConnection()) {
            String delGrades = """
            DELETE g FROM grades g
            JOIN enrollments e ON g.enrollment_id = e.enrollment_id
            WHERE e.section_id = ?
            """;
            PreparedStatement ps1 = conn.prepareStatement(delGrades);
            ps1.setInt(1, sectionId);
            ps1.executeUpdate();
            String delEnroll = "DELETE FROM enrollments WHERE section_id = ?";
            PreparedStatement ps2 = conn.prepareStatement(delEnroll);
            ps2.setInt(1, sectionId);
            ps2.executeUpdate();
            String delSection = "DELETE FROM sections WHERE section_id = ?";
            PreparedStatement ps3 = conn.prepareStatement(delSection);
            ps3.setInt(1, sectionId);
            int aff = ps3.executeUpdate();
            return aff > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean deleteCourse(int courseId) {
        if (edu.univ.erp.service.SettingsService.checkMaintenanceBlock("Admin operation")) return false;
        try (Connection conn = ERPDatabase.getConnection()) {
            String selectSections = "SELECT section_id FROM sections WHERE course_id = ?";
            PreparedStatement ps = conn.prepareStatement(selectSections);
            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int sectionId = rs.getInt("section_id");
                deleteSection(sectionId); // reuse safe deletion logic
            }
            String delCourse = "DELETE FROM courses WHERE course_id = ?";
            PreparedStatement ps2 = conn.prepareStatement(delCourse);
            ps2.setInt(1, courseId);
            int aff = ps2.executeUpdate();
            return aff > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean assignInstructorToSectionByUsername(int sectionId, String instructorUsername) {
        if (edu.univ.erp.service.SettingsService.checkMaintenanceBlock("Admin operation")) return false;
        try (Connection conn = ERPDatabase.getConnection()) {
            String q = "SELECT i.instructor_id FROM instructors i " +
                    "JOIN auth_db.users_auth u ON i.user_id = u.user_id WHERE u.username = ?";
            try (PreparedStatement ps = conn.prepareStatement(q)) {
                ps.setString(1, instructorUsername);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return false;
                    int instructorId = rs.getInt("instructor_id");
                    String upd = "UPDATE sections SET instructor_id = ? WHERE section_id = ?";
                    try (PreparedStatement updPs = conn.prepareStatement(upd)) {
                        updPs.setInt(1, instructorId);
                        updPs.setInt(2, sectionId);
                        return updPs.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
