package edu.univ.erp.ui;

import edu.univ.erp.service.AdminService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AddCourseDialog extends JDialog {
    private JTextField codeField;
    private JTextField titleField;
    private JTextField creditsField;
    public AddCourseDialog(JFrame parent) {
        super(parent, "Add New Course", true);
        setSize(400, 250);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(5, 2, 10, 10));
        add(new JLabel("Course Code:"));
        codeField = new JTextField();
        add(codeField);
        add(new JLabel("Course Title:"));
        titleField = new JTextField();
        add(titleField);
        add(new JLabel("Credits:"));
        creditsField = new JTextField();
        add(creditsField);
        JButton saveButton = new JButton("Add Course");
        saveButton.addActionListener(this::handleAdd);
        add(saveButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);
    }
    private void handleAdd(ActionEvent e) {
        String code = codeField.getText().trim();
        String title = titleField.getText().trim();
        int credits = creditsField.getText().isEmpty() ? 0 : Integer.parseInt(creditsField.getText().trim());
        boolean success = AdminService.addCourse(code, title, credits);
        if (success) {
            JOptionPane.showMessageDialog(this, "Course has been added successfully!");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add the course.");
        }
    }
}
