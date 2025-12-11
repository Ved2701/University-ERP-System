package edu.univ.erp.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import javax.swing.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.*;
import java.util.Arrays;

public class GradeCSVUtil {
    public static boolean exportToCSV(int sectionId, String path) {
        try (Connection conn = edu.univ.erp.data.ERPDatabase.getConnection()) {
            String query = """
                SELECT e.enrollment_id, s.roll_no, g.component, g.score, g.final_score, g.final_grade
                FROM enrollments e
                JOIN students s ON e.student_id = s.student_id
                LEFT JOIN grades g ON e.enrollment_id = g.enrollment_id
                WHERE e.section_id = ?
            """;
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, sectionId);
            ResultSet rs = ps.executeQuery();
            CSVWriter w = new CSVWriter(new FileWriter(path));
            w.writeNext(new String[]{"Enrollment ID", "Roll No", "Component", "Score", "Final Score", "Final Grade"});
            while (rs.next()) {
                w.writeNext(new String[]{
                        String.valueOf(rs.getInt("enrollment_id")),
                        rs.getString("roll_no"),
                        rs.getString("component"),
                        String.valueOf(rs.getDouble("score")),
                        String.valueOf(rs.getDouble("final_score")),
                        rs.getString("final_grade")
                });
            }
            w.close();
            JOptionPane.showMessageDialog(null, "Grades exported successfully to:\n" + path);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to export grades: " + e.getMessage());
            return false;
        }
    }
    public static boolean importFromCSV(String path) {
        int imported = 0, skipped = 0;
        try (Connection conn = edu.univ.erp.data.ERPDatabase.getConnection()) {
            CSVReader reader = new CSVReader(new FileReader(path));
            String[] i;
            reader.readNext();
            while ((i = reader.readNext()) != null) {
                if (i.length < 4 || i[0].isBlank() || i[2].isBlank() || i[3].isBlank()) {
                    System.out.println("Skipping invalid or incomplete row: " + Arrays.toString(i));
                    skipped++;
                    continue;
                }
                try {
                    int enrollmentId = Integer.parseInt(i[0].trim());
                    String component = i[2].trim();
                    double score = Double.parseDouble(i[3].trim());
                    String sql = """
                        INSERT INTO grades (enrollment_id, component, score)
                        VALUES (?, ?, ?)
                        ON DUPLICATE KEY UPDATE score = VALUES(score)
                    """;
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, enrollmentId);
                    ps.setString(2, component);
                    ps.setDouble(3, score);
                    ps.executeUpdate();
                    imported++;
                } catch (NumberFormatException ex) {
                    System.out.println("Skipping the row with an invalid number format: " + Arrays.toString(i));
                    skipped++;
                }
            }
            reader.close();
            JOptionPane.showMessageDialog(null,
                    "Import is complete!\n"
                            + "Imported: " + imported + " rows\n"
                            + "Skipped: " + skipped + " invalid rows");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to import the grades: " + e.getMessage());
            return false;
        }
    }
}

