package edu.univ.erp.service;

import edu.univ.erp.data.ERPDatabase;
import edu.univ.erp.auth.Session;
import edu.univ.erp.domain.SectionStats;
import javax.swing.*;
import java.sql.*;

public class InstructorService {
    public static ResultSet getMySections() throws SQLException {
        Connection conn = ERPDatabase.getConnection();
        String sql = """
            SELECT s.section_id, c.code, c.title, s.day_time, s.room, s.semester, s.year
            FROM sections s
            JOIN courses c ON s.course_id = c.course_id
            JOIN instructors i ON s.instructor_id = i.instructor_id
            JOIN auth_db.users_auth u ON i.user_id = u.user_id
            WHERE u.username = ?
            """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, Session.getUsername());
        return ps.executeQuery();
    }
    public static ResultSet getStudentsInSection(int sectionId) throws SQLException {
        Connection conn = ERPDatabase.getConnection();
        String sql = """
            SELECT e.enrollment_id, s.roll_no, s.program, s.year
            FROM enrollments e
            JOIN students s ON e.student_id = s.student_id
            WHERE e.section_id = ?
            """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, sectionId);
        return ps.executeQuery();
    }
    public static boolean addScore(int enrollmentId, String component, double score) {
        if (Session.isReadOnly() || SettingsService.isMaintenanceOn()) {
            JOptionPane.showMessageDialog(null,
                    "System is in Maintenance Mode (Read-only).\nYou cannot add or modify grades now.",
                    "Action Blocked", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try (Connection conn = ERPDatabase.getConnection()) {
            String sql = "INSERT INTO grades (enrollment_id, component, score) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, enrollmentId);
            ps.setString(2, component);
            ps.setDouble(3, score);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Score has been added successfully!");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to add the score due to a system error.");
            return false;
        }
    }
    public static boolean computeFinalGrade(int enrollmentId) {
        if (Session.isReadOnly() || SettingsService.isMaintenanceOn()) {
            JOptionPane.showMessageDialog(null,
                    "System is in Maintenance Mode (Read-only).\nYou cannot compute grades now.",
                    "Action Blocked", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try (Connection conn = ERPDatabase.getConnection()) {
            String sql = "SELECT component, score FROM grades WHERE enrollment_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, enrollmentId);
            ResultSet rs = ps.executeQuery();
            double quiz = 0, mid = 0, end = 0;
            while (rs.next()) {
                switch (rs.getString("component").toLowerCase()) {
                    case "quiz" -> quiz = rs.getDouble("score");
                    case "midterm" -> mid = rs.getDouble("score");
                    case "endsem" -> end = rs.getDouble("score");
                }
            }
            double finalScore = (0.2 * quiz) + (0.3 * mid) + (0.5 * end);
            String grade;
            if (finalScore >= 85) grade = "A";
            else if (finalScore >= 70) grade = "B";
            else if (finalScore >= 55) grade = "C";
            else if (finalScore >= 40) grade = "D";
            else grade = "F";
            String insertFinal = """
                INSERT INTO grades (enrollment_id, component, score, final_score, final_grade)
                VALUES (?, 'FINAL', ?, ?, ?)
                ON DUPLICATE KEY UPDATE 
                    score = VALUES(score), 
                    final_score = VALUES(final_score), 
                    final_grade = VALUES(final_grade)
            """;
            PreparedStatement ps2 = conn.prepareStatement(insertFinal);
            ps2.setInt(1, enrollmentId);
            ps2.setDouble(2, finalScore);
            ps2.setDouble(3, finalScore);
            ps2.setString(4, grade);
            ps2.executeUpdate();
            JOptionPane.showMessageDialog(null, "Final grade has been computed successfully!");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to compute the final grade.");
            return false;
        }
    }
    public static ResultSet getStudentsResultsForSection(int sectionId) throws SQLException {
        Connection conn = ERPDatabase.getConnection();
        String sql = """
            SELECT e.enrollment_id, u.username, s.roll_no, MAX(g.final_score) AS final_score, MAX(g.final_grade) AS final_grade
            FROM enrollments e
            JOIN students s ON e.student_id = s.student_id
            JOIN auth_db.users_auth u ON s.user_id = u.user_id
            LEFT JOIN grades g ON e.enrollment_id = g.enrollment_id
            WHERE e.section_id = ?
            GROUP BY e.enrollment_id, u.username, s.roll_no
            ORDER BY u.username
        """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, sectionId);
        return ps.executeQuery();
    }
    public static SectionStats getSectionStats(int sectionId) {
        SectionStats stats = new SectionStats();
        try (Connection conn = ERPDatabase.getConnection()) {
            String q = "SELECT COUNT(*) AS total FROM enrollments WHERE section_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(q)) {
                ps.setInt(1, sectionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) stats.setTotalStudents(rs.getInt("total"));
                }
            }
            String qAgg = """
                SELECT AVG(final_score) AS avg_s, MIN(final_score) AS min_s, MAX(final_score) AS max_s
                FROM grades g JOIN enrollments e ON g.enrollment_id = e.enrollment_id
                WHERE e.section_id = ? AND g.final_score IS NOT NULL
            """;
            try (PreparedStatement ps = conn.prepareStatement(qAgg)) {
                ps.setInt(1, sectionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stats.setAvgFinalScore(rs.getDouble("avg_s"));
                        stats.setMinFinalScore(rs.getDouble("min_s"));
                        stats.setMaxFinalScore(rs.getDouble("max_s"));
                    }
                }
            }
            String qd = """
                SELECT g.final_grade, COUNT(*) AS cnt
                FROM grades g JOIN enrollments e ON g.enrollment_id = e.enrollment_id
                WHERE e.section_id = ? AND g.final_grade IS NOT NULL
                GROUP BY g.final_grade
            """;
            try (PreparedStatement ps = conn.prepareStatement(qd)) {
                ps.setInt(1, sectionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        stats.addGradeCount(rs.getString("final_grade"), rs.getInt("cnt"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
}

