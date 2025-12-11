package edu.univ.erp.service;

import edu.univ.erp.data.ERPDatabase;
import edu.univ.erp.auth.Session;
import javax.swing.*;
import java.sql.*;

public class StudentService {
    public static ResultSet listAvailableSections() throws SQLException {
        Connection conn = ERPDatabase.getConnection();
        String q = """
            SELECT s.section_id, c.code, c.title, s.day_time, s.room, s.capacity, i.department
            FROM sections s
            JOIN courses c ON s.course_id = c.course_id
            JOIN instructors i ON s.instructor_id = i.instructor_id
            """;
        PreparedStatement ps = conn.prepareStatement(q);
        return ps.executeQuery();
    }
    public static boolean registerSection(int sectionId) {
        if (Session.isReadOnly() || SettingsService.isMaintenanceOn()) {
            JOptionPane.showMessageDialog(null,
                    "System is in Maintenance Mode (Read-only).\nYou cannot register right now.",
                    "Action Blocked", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try (Connection conn = ERPDatabase.getConnection()) {
            int studentId = getStudentIdByUsername(Session.getUsername());
            if (studentId == -1) {
                JOptionPane.showMessageDialog(null, "Student record not found.");
                return false;
            }
            String checkDup = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND section_id = ?";
            PreparedStatement chk = conn.prepareStatement(checkDup);
            chk.setInt(1, studentId);
            chk.setInt(2, sectionId);
            ResultSet rs = chk.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(null, "Already registered for this section!");
                return false;
            }
            String cap = """
                SELECT s.capacity, COUNT(e.enrollment_id) AS enrolled
                FROM sections s
                LEFT JOIN enrollments e ON s.section_id = e.section_id
                WHERE s.section_id = ?
                GROUP BY s.capacity
                """;
            PreparedStatement pcap = conn.prepareStatement(cap);
            pcap.setInt(1, sectionId);
            ResultSet rcap = pcap.executeQuery();
            if (rcap.next()) {
                int capacity = rcap.getInt("capacity");
                int added = rcap.getInt("enrolled");
                if (added >= capacity) {
                    JOptionPane.showMessageDialog(null, "Section is full! Can't register.");
                    return false;
                }
            }
            String sql = "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'ENROLLED')";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Registered successfully!");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Registration failed due to a system error.");
            return false;
        }
    }
    public static boolean dropSection(int enrollmentId) {
        if (Session.isReadOnly() || SettingsService.isMaintenanceOn()) {
            JOptionPane.showMessageDialog(null,
                    "System is in Maintenance Mode (Read-only).\nYou cannot drop sections right now.",
                    "Action Blocked", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (SettingsService.isDropDeadlineOver()) {
            JOptionPane.showMessageDialog(null,
                    "Drop deadline has passed. You cannot drop this section now.",
                    "Deadline Passed", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try (Connection conn = ERPDatabase.getConnection()) {
            String delGrades = "DELETE FROM grades WHERE enrollment_id = ?";
            PreparedStatement ps1 = conn.prepareStatement(delGrades);
            ps1.setInt(1, enrollmentId);
            ps1.executeUpdate();
            String delEnroll = "DELETE FROM enrollments WHERE enrollment_id = ?";
            PreparedStatement ps2 = conn.prepareStatement(delEnroll);
            ps2.setInt(1, enrollmentId);
            int aff = ps2.executeUpdate();
            if (aff > 0) {
                JOptionPane.showMessageDialog(null, "Section has been dropped successfully!");
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Section was not found, drop failed.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Drop failed due to a system error.");
            return false;
        }
    }
    public static ResultSet getRegisteredSections() throws SQLException {
        Connection conn = ERPDatabase.getConnection();
        int studentId = getStudentIdByUsername(Session.getUsername());
        if (studentId == -1) return null;
        String query = """
            SELECT e.enrollment_id, c.code, c.title, s.day_time, s.room
            FROM enrollments e
            JOIN sections s ON e.section_id = s.section_id
            JOIN courses c ON s.course_id = c.course_id
            WHERE e.student_id = ?
            """;
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, studentId);
        return ps.executeQuery();
    }
    private static int getStudentIdByUsername(String username) {
        try (Connection conn = ERPDatabase.getConnection()) {
            String sql = """
                SELECT s.student_id
                FROM students s
                JOIN auth_db.users_auth u ON s.user_id = u.user_id
                WHERE u.username = ?
                """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("student_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
