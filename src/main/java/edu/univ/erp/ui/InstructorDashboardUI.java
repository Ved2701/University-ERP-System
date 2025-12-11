package edu.univ.erp.ui;

import edu.univ.erp.auth.Session;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;

public class InstructorDashboardUI extends JFrame {
    private JTable sectionTable, studentTable;
    public InstructorDashboardUI() {
        setTitle("Instructor Dashboard - " + Session.getUsername());
        setSize(850, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        String note = edu.univ.erp.service.NotificationService.getLatestNotification();
        JLabel notif = new JLabel("📢 " + note, SwingConstants.CENTER);
        notif.setForeground(Color.BLUE);
        panel.add(notif, BorderLayout.SOUTH);
        JLabel label = new JLabel("Welcome, " + Session.getUsername() + " (Instructor)", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(label, BorderLayout.NORTH);
        JButton changePassBtn = new JButton("Change Password");
        changePassBtn.addActionListener(e -> new ChangePasswordDialog(this).setVisible(true));
        add(changePassBtn, BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sectionTable = new JTable();
        studentTable = new JTable();
        loadSections();
        split.setTopComponent(new JScrollPane(sectionTable));
        split.setBottomComponent(new JScrollPane(studentTable));
        split.setDividerLocation(250);
        panel.add(split, BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        JButton loadBtn = new JButton("View Students");
        JButton addGradeBtn = new JButton("Add Score");
        JButton computeBtn = new JButton("Compute Final");
        JButton statsBtn = new JButton("View Stats");
        JButton exportGradesBtn = new JButton("Export Grades (CSV)");
        JButton importGradesBtn = new JButton("Import Grades (CSV)");
        loadBtn.addActionListener(this::handleViewStudents);
        addGradeBtn.addActionListener(this::handleAddScore);
        computeBtn.addActionListener(this::handleCompute);
        statsBtn.addActionListener(this::handleViewStats);
        exportGradesBtn.addActionListener(e -> {
            int r = sectionTable.getSelectedRow();
            if (r == -1) {
                JOptionPane.showMessageDialog(this, "Select a section first.");
                return;
            }
            int sectionId = (int) sectionTable.getValueAt(r, 0);
            JOptionPane.showMessageDialog(this, "Please wait... Exporting grades.", "Working", JOptionPane.INFORMATION_MESSAGE);
            String path = System.getProperty("user.dir") + "\\" +
                    Session.getUsername() + "_grades_section_" + sectionId + ".csv";
            edu.univ.erp.util.GradeCSVUtil.exportToCSV(sectionId, path);
        });
        importGradesBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(this, "Please wait... Importing grades.", "Working", JOptionPane.INFORMATION_MESSAGE);
                String path = chooser.getSelectedFile().getAbsolutePath();
                edu.univ.erp.util.GradeCSVUtil.importFromCSV(path);
            }
        });
        buttons.add(loadBtn);
        buttons.add(addGradeBtn);
        buttons.add(computeBtn);
        buttons.add(statsBtn);
        buttons.add(exportGradesBtn);
        buttons.add(importGradesBtn);
        panel.add(buttons, BorderLayout.SOUTH);
        add(panel);
    }
    private void loadSections() {
        try {
            ResultSet rs = InstructorService.getMySections();
            DefaultTableModel model = new DefaultTableModel(
                    new String[]{"ID", "Code", "Title", "Day/Time", "Room", "Sem", "Year"}, 0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("section_id"),
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getString("day_time"),
                        rs.getString("room"),
                        rs.getString("semester"),
                        rs.getInt("year")
                });
            }
            sectionTable.setModel(model);
            sectionTable.setAutoCreateRowSorter(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void handleViewStudents(ActionEvent e) {
        int row = sectionTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a section first.");
            return;
        }
        JOptionPane.showMessageDialog(this, "Please wait... Loading students.", "Working", JOptionPane.INFORMATION_MESSAGE);
        int sectionId = (int) sectionTable.getValueAt(row, 0);
        try {
            ResultSet rs = InstructorService.getStudentsInSection(sectionId);
            DefaultTableModel model = new DefaultTableModel(
                    new String[]{"EnrollID", "Roll No", "Program", "Year"}, 0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("enrollment_id"),
                        rs.getString("roll_no"),
                        rs.getString("program"),
                        rs.getInt("year")
                });
            }
            studentTable.setModel(model);
            studentTable.setAutoCreateRowSorter(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void handleAddScore(ActionEvent e) {
        if (Session.isReadOnly() || edu.univ.erp.service.SettingsService.isMaintenanceOn()) {
            JOptionPane.showMessageDialog(this,
                    "The system is in Maintenance Mode (Read-only). You cannot add or modify grades.",
                    "Action Blocked", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int r = studentTable.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Select a student first.");
            return;
        }
        int enrollId = (int) studentTable.getValueAt(r, 0);
        String component = JOptionPane.showInputDialog(this, "Enter component (quiz/midterm/endsem):");
        if (component == null || component.isBlank()) return;
        String scoreStr = JOptionPane.showInputDialog(this, "Enter score:");
        if (scoreStr == null || scoreStr.isBlank()) return;
        try {
            JOptionPane.showMessageDialog(this, "Please wait... Saving score.", "Working", JOptionPane.INFORMATION_MESSAGE);
            double score = Double.parseDouble(scoreStr);
            boolean ok = InstructorService.addScore(enrollId, component, score);
            JOptionPane.showMessageDialog(this, ok ? "Score added!" : "Failed.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number entered.");
        }
    }
    private void handleCompute(ActionEvent e) {
        if (Session.isReadOnly() || edu.univ.erp.service.SettingsService.isMaintenanceOn()) {
            JOptionPane.showMessageDialog(this,
                    "The system is in Maintenance Mode (Read-only). You cannot compute grades.",
                    "Action Blocked", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int r = studentTable.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Select a student first.");
            return;
        }
        int enrollId = (int) studentTable.getValueAt(r, 0);
        int conf = JOptionPane.showConfirmDialog(this,
                "Compute final grade for the selected student?",
                "Confirm Compute", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "Please wait... Computing final grade.", "Working", JOptionPane.INFORMATION_MESSAGE);
            boolean ok = InstructorService.computeFinalGrade(enrollId);
            JOptionPane.showMessageDialog(this, ok ? "Final grade computed!" : "Failed to compute grade.");
        }
    }
    private void handleViewStats(ActionEvent e) {
        int r = sectionTable.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Select a section first.");
            return;
        }
        int sectionId = (int) sectionTable.getValueAt(r, 0);
        JOptionPane.showMessageDialog(this, "Please wait... Loading stats.", "Working", JOptionPane.INFORMATION_MESSAGE);
        try {
            edu.univ.erp.domain.SectionStats stats = edu.univ.erp.service.InstructorService.getSectionStats(sectionId);
            ResultSet rs = edu.univ.erp.service.InstructorService.getStudentsResultsForSection(sectionId);
            JPanel top = new JPanel(new GridLayout(3, 2, 8, 8));
            top.add(new JLabel("Total students: " + stats.getTotalStudents()));
            top.add(new JLabel("Avg final score: " +
                    (stats.getAvgFinalScore() > 0 ? String.format("%.2f", stats.getAvgFinalScore()) : "N/A")));
            top.add(new JLabel("Min final score: " +
                    (stats.getMinFinalScore() == null ? "N/A" : stats.getMinFinalScore())));
            top.add(new JLabel("Max final score: " +
                    (stats.getMaxFinalScore() == null ? "N/A" : stats.getMaxFinalScore())));
            top.add(new JLabel(""));
            top.add(new JLabel("Section ID: " + sectionId));
            StringBuilder dist = new StringBuilder("<html><b> Grade distribution:</b><br/>");
            if (stats.getGradeCounts().isEmpty()) dist.append("No grades yet");
            else stats.getGradeCounts().forEach((g, c) -> dist.append(g).append(": ").append(c).append("<br/>"));
            dist.append("</html>");
            JLabel distLabel = new JLabel(dist.toString());
            distLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            DefaultTableModel model = new DefaultTableModel(
                    new String[]{"EnrollID", "Username", "Roll No", "Final Score", "Final Grade"}, 0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("enrollment_id"),
                        rs.getString("username"),
                        rs.getString("roll_no"),
                        rs.getObject("final_score"),
                        rs.getObject("final_grade")
                });
            }
            JTable table = new JTable(model);
            table.setAutoCreateRowSorter(true);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(700, 300));
            JPanel cont = new JPanel(new BorderLayout(10, 10));
            cont.add(top, BorderLayout.NORTH);
            cont.add(distLabel, BorderLayout.WEST);
            cont.add(scroll, BorderLayout.CENTER);
            JOptionPane.showMessageDialog(this, cont, "Section Stats & Results", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load stats: " + ex.getMessage());
        }
    }
}

