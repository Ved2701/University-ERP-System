package edu.univ.erp.service;

import edu.univ.erp.data.ERPDatabase;
import edu.univ.erp.auth.Session;
import java.sql.*;

public class TranscriptService {
    public static ResultSet getTranscriptData() throws SQLException {
        Connection conn = ERPDatabase.getConnection();
        String sql = """
            SELECT c.code, c.title, g.score AS final_score, g.final_grade
            FROM grades g
            JOIN enrollments e ON g.enrollment_id = e.enrollment_id
            JOIN sections s ON e.section_id = s.section_id
            JOIN courses c ON s.course_id = c.course_id
            JOIN students st ON e.student_id = st.student_id
            JOIN auth_db.users_auth u ON st.user_id = u.user_id
            WHERE u.username = ? AND g.component = 'FINAL'
            """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, Session.getUsername());
        return ps.executeQuery();
    }
}
