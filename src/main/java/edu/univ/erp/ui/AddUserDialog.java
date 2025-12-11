package edu.univ.erp.ui;

import edu.univ.erp.service.AdminService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AddUserDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;
    private JTextField rollField, programField, yearField, deptField;
    public AddUserDialog(JFrame parent) {
        super(parent, "Add New User", true);
        setSize(400, 400);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(9, 2, 5, 5));
        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);
        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);
        add(new JLabel("Role:"));
        roleBox = new JComboBox<>(new String[]{"STUDENT", "INSTRUCTOR", "ADMIN"});
        add(roleBox);
        add(new JLabel("Roll No (student only):"));
        rollField = new JTextField();
        add(rollField);
        add(new JLabel("Program (student only):"));
        programField = new JTextField();
        add(programField);
        add(new JLabel("Year (student only):"));
        yearField = new JTextField();
        add(yearField);
        add(new JLabel("Department (instructor only):"));
        deptField = new JTextField();
        add(deptField);
        JButton saveButton = new JButton("Add User");
        saveButton.addActionListener(this::handleAdd);
        add(saveButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);
    }
    private void handleAdd(ActionEvent e) {
        String un = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        String role = (String) roleBox.getSelectedItem();
        String roll = rollField.getText().trim();
        String program = programField.getText().trim();
        int year = yearField.getText().isEmpty() ? 0 : Integer.parseInt(yearField.getText().trim());
        String dept = deptField.getText().trim();
        boolean success = AdminService.addUser(un, pass, role, roll, program, year, dept);
        if (success) {
            JOptionPane.showMessageDialog(this, "User added successfully");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add user.");
        }
    }
}
