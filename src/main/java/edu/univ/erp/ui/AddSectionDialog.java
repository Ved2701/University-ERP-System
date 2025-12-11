package edu.univ.erp.ui;

import edu.univ.erp.service.AdminService;
import edu.univ.erp.data.ERPDatabase;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class AddSectionDialog extends JDialog {
    private JComboBox<String> courseBox;
    private JComboBox<String> instructorBox;
    private JTextField dayTimeField, roomField, capacityField, semesterField, yearField;
    public AddSectionDialog(JFrame parent) {
        super(parent, "Add New Section", true);
        setSize(450, 400);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(9, 2, 10, 10));
        add(new JLabel("Course:"));
        courseBox = new JComboBox<>();
        populateCourses();
        add(courseBox);
        add(new JLabel("Instructor:"));
        instructorBox = new JComboBox<>();
        populateInstructors();
        add(instructorBox);
        add(new JLabel("Day & Time (e.g., Mon 10–12):"));
        dayTimeField = new JTextField();
        add(dayTimeField);
        add(new JLabel("Room:"));
        roomField = new JTextField();
        add(roomField);
        add(new JLabel("Capacity:"));
        capacityField = new JTextField();
        add(capacityField);
        add(new JLabel("Semester:"));
        semesterField = new JTextField("Fall");
        add(semesterField);
        add(new JLabel("Year:"));
        yearField = new JTextField("2025");
        add(yearField);
        JButton saveButton = new JButton("Add Section");
        saveButton.addActionListener(this::handleAdd);
        add(saveButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);
    }
    private void populateCourses() {
        try (Connection conn = ERPDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT course_id, code, title FROM courses")) {
            while (rs.next()) {
                int id = rs.getInt("course_id");
                String label = id + " - " + rs.getString("code") + " (" + rs.getString("title") + ")";
                courseBox.addItem(label);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void populateInstructors() {
        try (Connection conn = ERPDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT instructor_id, department FROM instructors")) {
            while (rs.next()) {
                int id = rs.getInt("instructor_id");
                String label = id + " - " + rs.getString("department");
                instructorBox.addItem(label);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void handleAdd(ActionEvent e) {
        try {
            int courseId = Integer.parseInt(courseBox.getSelectedItem().toString().split(" - ")[0]);
            int instructorId = Integer.parseInt(instructorBox.getSelectedItem().toString().split(" - ")[0]);
            String dayTime = dayTimeField.getText().trim();
            String room = roomField.getText().trim();
            int capacity = Integer.parseInt(capacityField.getText().trim());
            String semester = semesterField.getText().trim();
            int year = Integer.parseInt(yearField.getText().trim());
            boolean success = AdminService.addSection(courseId, instructorId, dayTime, room, capacity, semester, year);
            if (success) {
                JOptionPane.showMessageDialog(this, "Section added successfully");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add section.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please check your entries.");
        }
    }
}
