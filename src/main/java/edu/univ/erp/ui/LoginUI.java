package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    public LoginUI() {
        setTitle("University ERP Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 1, 10, 10));
        JLabel userLabel = new JLabel("Username:");
        usernameField = new JTextField();
        JLabel passLabel = new JLabel("Password:");
        passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(this::handleLogin);
        add(userLabel);
        add(usernameField);
        add(passLabel);
        add(passwordField);
        add(loginButton);
    }
    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        int a = AuthService.login(username, password);
        if (a == 1) {
            String role = edu.univ.erp.auth.Session.getRole();
            JOptionPane.showMessageDialog(this, "Login successful! Role: " + role);
            dispose();
            if ("ADMIN".equalsIgnoreCase(role)) {
                new AdminDashboardUI().setVisible(true);
            } else if ("STUDENT".equalsIgnoreCase(role)) {
                new StudentDashboardUI().setVisible(true);
            } else if ("INSTRUCTOR".equalsIgnoreCase(role)) {
                new InstructorDashboardUI().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Dashboard for this role not ready yet.");
                new LoginUI().setVisible(true);
            }

        } else if (a == 2) {
            return;
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.");
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}

