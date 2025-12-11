package edu.univ.erp.ui;

import edu.univ.erp.auth.Session;
import edu.univ.erp.auth.AuthService;
import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {
    public ChangePasswordDialog(JFrame parent) {
        super(parent, "Change Password", true);
        setSize(350, 220);
        setLayout(new GridLayout(4, 2, 10, 10));
        setLocationRelativeTo(parent);
        JLabel oldLabel = new JLabel("Old Password:");
        JLabel newLabel = new JLabel("New Password:");
        JLabel confirmLabel = new JLabel("Confirm New:");
        JPasswordField oldField = new JPasswordField();
        JPasswordField newField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();
        JButton changeButton = new JButton("Change Password");
        changeButton.addActionListener(e -> {
            String oldPass = new String(oldField.getPassword());
            String newPass = new String(newField.getPassword());
            String confirm = new String(confirmField.getPassword());
            if (!newPass.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match!");
                return;
            }
            boolean success = changePassword(Session.getUsername(), oldPass, newPass);
            JOptionPane.showMessageDialog(this,
                    success ? "Password updated successfully!" : "Old password incorrect!");
            if (success) dispose();
        });
        add(oldLabel);
        add(oldField);
        add(newLabel);
        add(newField);
        add(confirmLabel);
        add(confirmField);
        add(new JLabel());
        add(changeButton);
    }
    private boolean changePassword(String username, String oldPass, String newPass) {
        try {
            return AuthService.changePassword(username, oldPass, newPass);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
