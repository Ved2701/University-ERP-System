package edu.univ.erp.ui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssignInstructorDialog extends JDialog {
    public AssignInstructorDialog(JFrame parent) {
        super(parent, "Assign Instructor to Section", true);
        setSize(600, 220);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(4,1,8,8));
        List<ComboItem> sections = new ArrayList<>();
        try (Connection conn = edu.univ.erp.data.ERPDatabase.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT s.section_id, c.code, c.title, s.room FROM sections s JOIN courses c ON s.course_id = c.course_id ORDER BY s.section_id")) {
            while (rs.next()) {
                int id = rs.getInt("section_id");
                String label = id + " - " + rs.getString("code") + " - " + rs.getString("title") + " (" + rs.getString("room") + ")";
                sections.add(new ComboItem(id, label));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        List<ComboItem> insts = new ArrayList<>();
        try (Connection conn = edu.univ.erp.data.ERPDatabase.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT i.instructor_id, u.username FROM instructors i JOIN auth_db.users_auth u ON i.user_id = u.user_id ORDER BY u.username")) {
            while (rs.next()) {
                int id = rs.getInt("instructor_id");
                String label = rs.getString("username");
                insts.add(new ComboItem(id, label));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        JComboBox<ComboItem> sectionBox = new JComboBox<>(sections.toArray(new ComboItem[0]));
        JComboBox<ComboItem> instructorBox = new JComboBox<>(insts.toArray(new ComboItem[0]));
        add(new JLabel("Choose Section:"));
        add(sectionBox);
        add(new JLabel("Choose Instructor (username):"));
        add(instructorBox);
        JPanel bottom = new JPanel();
        JButton assignBtn = new JButton("Assign");
        assignBtn.addActionListener(ae -> {
            ComboItem sec = (ComboItem) sectionBox.getSelectedItem();
            ComboItem ins = (ComboItem) instructorBox.getSelectedItem();
            if (sec == null || ins == null) {
                JOptionPane.showMessageDialog(this, "Select both section and instructor.");
                return;
            }
            boolean ok = edu.univ.erp.service.AdminService.assignInstructorToSectionByUsername(sec.getId(), ins.getLabel());
            JOptionPane.showMessageDialog(this, ok ? "Assigned!" : "Could not assign.");
            if (ok) dispose();
        });
        bottom.add(assignBtn);
        add(bottom);
    }
    private static class ComboItem {
        private final int id;
        private final String label;
        public ComboItem(int id, String label) { this.id = id; this.label = label; }
        public int getId() { return id; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }
}
