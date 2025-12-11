package edu.univ.erp.ui;

import edu.univ.erp.auth.Session;
import edu.univ.erp.service.SettingsService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminDashboardUI extends JFrame {
    public AdminDashboardUI() {
        setTitle("Admin Dashboard - University ERP");
        setSize(750, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        String note = edu.univ.erp.service.NotificationService.getLatestNotification();
        JLabel notif = new JLabel("📢 " + note, SwingConstants.CENTER);
        notif.setForeground(Color.BLUE);
        mainPanel.add(notif, BorderLayout.SOUTH);
        JLabel welcomeLabel = new JLabel("Welcome, " + Session.getUsername() + " (Admin)", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JButton addUserButton = new JButton("Add User");
        JButton manageCoursesButton = new JButton("Manage Courses & Sections");
        JButton assignInstructorButton = new JButton("Assign Instructor to Section");
        JButton viewButton = new JButton("View Courses & Sections");
        JButton deleteCourseButton = new JButton("Delete Course");
        JButton deleteSectionButton = new JButton("Delete Section");
        JButton maintenanceButton = new JButton("Toggle Maintenance Mode");
        JButton backupButton = new JButton("Backup Database");
        JButton restoreButton = new JButton("Restore Database");
        JButton changePasswordButton = new JButton("Change Password");
        JButton unblockUserButton = new JButton("Unblock User");
        JButton logoutButton = new JButton("Logout");
        backupButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "⏳ Please wait... Feature under development.", "Working", JOptionPane.INFORMATION_MESSAGE);
        });
        restoreButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "⏳ Please wait... Feature under development.", "Working", JOptionPane.INFORMATION_MESSAGE);
        });
        addUserButton.addActionListener(this::handleAddUser);
        manageCoursesButton.addActionListener(this::handleManageCourses);
        assignInstructorButton.addActionListener(e -> new AssignInstructorDialog(this).setVisible(true));
        viewButton.addActionListener(e -> handleViewCoursesAndSections());
        maintenanceButton.addActionListener(this::handleMaintenanceToggle);
        changePasswordButton.addActionListener(e -> new ChangePasswordDialog(this).setVisible(true));
        logoutButton.addActionListener(e -> handleLogout());
        deleteCourseButton.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter the Course ID to delete:");
            if (input != null && !input.isBlank()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete the Course ID " + input + "?\nThis will remove all linked sections, enrollments, and grades.",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        JOptionPane.showMessageDialog(this, "Please wait... Deleting course.", "Working", JOptionPane.INFORMATION_MESSAGE);
                        int id = Integer.parseInt(input);
                        boolean ok = edu.univ.erp.service.AdminService.deleteCourse(id);
                        JOptionPane.showMessageDialog(this,
                                ok ? "Course has been deleted successfully!" : "Course was not found or could not be deleted.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Invalid Course ID format.");
                    }
                }
            }
        });
        deleteSectionButton.addActionListener(e -> {
            String in = JOptionPane.showInputDialog(this, "Enter the Section ID to delete:");
            if (in != null && !in.isBlank()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete Section ID " + in + "?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        JOptionPane.showMessageDialog(this, "Please wait... Deleting section.", "Working", JOptionPane.INFORMATION_MESSAGE);
                        int id = Integer.parseInt(in);
                        boolean ok = edu.univ.erp.service.AdminService.deleteSection(id);
                        JOptionPane.showMessageDialog(this,
                                ok ? "Section has been deleted successfully!" : "Section was not found or could not be deleted.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Invalid Section ID format.");
                    }
                }
            }
        });
        unblockUserButton.addActionListener(e -> {
            String un = JOptionPane.showInputDialog(this, "Enter username to unblock:");
            if (un != null && !un.isBlank()) {
                boolean ok = edu.univ.erp.auth.AuthService.unblockUser(un);
                JOptionPane.showMessageDialog(this,
                        ok ? "User '" + un + "' has been unblocked successfully!"
                                : "User not found or already active.");
            }
        });
        JPanel buttonPanel = new JPanel(new GridLayout(13, 1, 10, 10));
        buttonPanel.add(welcomeLabel);
        buttonPanel.add(addUserButton);
        buttonPanel.add(manageCoursesButton);
        buttonPanel.add(assignInstructorButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(deleteCourseButton);
        buttonPanel.add(deleteSectionButton);
        buttonPanel.add(maintenanceButton);
        buttonPanel.add(backupButton);
        buttonPanel.add(restoreButton);
        buttonPanel.add(changePasswordButton);
        buttonPanel.add(unblockUserButton);
        buttonPanel.add(logoutButton);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        add(mainPanel);
    }
    private void handleAddUser(ActionEvent e) {
        JOptionPane.showMessageDialog(this, "Please wait...Opening Add User form.", "Working", JOptionPane.INFORMATION_MESSAGE);
        new AddUserDialog(this).setVisible(true);
    }
    private void handleManageCourses(ActionEvent e) {
        Object[] options = {"Add Course", "Add Section", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this, "What do you want to do?",
                "Manage Courses & Sections", JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice == JOptionPane.YES_OPTION)
            new AddCourseDialog(this).setVisible(true);
        else if (choice == JOptionPane.NO_OPTION)
            new AddSectionDialog(this).setVisible(true);
    }
    private void handleMaintenanceToggle(ActionEvent e) {
        boolean current = SettingsService.isMaintenanceOn();
        SettingsService.toggleMaintenance(!current);
        JOptionPane.showMessageDialog(this,
                "Maintenance mode turned " + (!current ? "ON" : "OFF"),
                "Maintenance Status", JOptionPane.INFORMATION_MESSAGE);
    }
    private void handleViewCoursesAndSections() {
        try (Connection conn = edu.univ.erp.data.ERPDatabase.getConnection();
             Statement stmt = conn.createStatement()) {
            JOptionPane.showMessageDialog(this, "Please wait...Loading courses.", "Working", JOptionPane.INFORMATION_MESSAGE);
            String query = """
                SELECT c.course_id, c.code, c.title, s.section_id, s.day_time, s.room, s.capacity, s.semester, s.year,
                       (SELECT u.username FROM instructors i 
                        JOIN auth_db.users_auth u ON i.user_id = u.user_id
                        WHERE i.instructor_id = s.instructor_id) AS instructor
                FROM courses c
                LEFT JOIN sections s ON c.course_id = s.course_id
                ORDER BY c.course_id, s.section_id
                """;
            ResultSet rs = stmt.executeQuery(query);
            DefaultTableModel model = new DefaultTableModel(
                    new String[]{"Course ID", "Code", "Title", "Section ID", "Day/Time", "Room", "Cap", "Sem", "Year", "Instructor"}, 0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("course_id"),
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getInt("section_id"),
                        rs.getString("day_time"),
                        rs.getString("room"),
                        rs.getInt("capacity"),
                        rs.getString("semester"),
                        rs.getInt("year"),
                        rs.getString("instructor")
                });
            }
            JTable table = new JTable(model);
            table.setAutoCreateRowSorter(true);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(700, 400));
            JOptionPane.showMessageDialog(this, scroll, "All Courses & Sections", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load data.");
        }
    }
    private void handleLogout() {
        Session.endSession();
        dispose();
        new LoginUI().setVisible(true);
    }
}

