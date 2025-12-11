package edu.univ.erp.ui;

import edu.univ.erp.auth.Session;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;

public class StudentDashboardUI extends JFrame {
    private JTable sectionTable;
    private JTable myTable;
    public StudentDashboardUI() {
        setTitle("Student Dashboard - " + Session.getUsername());
        setSize(850, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        String n = edu.univ.erp.service.NotificationService.getLatestNotification();
        JLabel notifLabel = new JLabel("📢 " + n, SwingConstants.CENTER);
        notifLabel.setForeground(Color.BLUE);
        JLabel welcome = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "Welcome, " + Session.getUsername() + " (Student)"
                        + "<br><span style='color:red; font-size:11pt;'>Drop Deadline: 30 Dec 2025</span>"
                        + "</div></html>",
                SwingConstants.CENTER
        );
        welcome.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(welcome, BorderLayout.NORTH);
        JButton btn = new JButton("Change Password");
        btn.addActionListener(e -> new ChangePasswordDialog(this).setVisible(true));
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(btn, BorderLayout.CENTER);
        bottomPanel.add(notifLabel, BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        JTabbedPane tabs = new JTabbedPane();
        JPanel browsePanel = new JPanel(new BorderLayout());
        sectionTable = new JTable();
        sectionTable.setAutoCreateRowSorter(true);
        loadAvailableSections();
        browsePanel.add(new JScrollPane(sectionTable), BorderLayout.CENTER);
        JButton rbtn = new JButton("Register for Selected Section");
        rbtn.addActionListener(this::handleRegister);
        browsePanel.add(rbtn, BorderLayout.SOUTH);
        tabs.add("Browse Sections", browsePanel);
        JPanel myPanel = new JPanel(new BorderLayout());
        myTable = new JTable();
        myTable.setAutoCreateRowSorter(true);
        loadMyRegistrations();
        myPanel.add(new JScrollPane(myTable), BorderLayout.CENTER);
        JPanel dropPanel = new JPanel();
        JButton dropBtn = new JButton("Drop Selected Section");
        dropBtn.addActionListener(this::handleDrop);
        dropPanel.add(dropBtn);
        myPanel.add(dropPanel, BorderLayout.SOUTH);
        tabs.add("My Timetable", myPanel);
        JPanel transcriptPanel = new JPanel(new BorderLayout());
        JTable transcriptTable = new JTable();
        transcriptTable.setAutoCreateRowSorter(true);
        try {
            ResultSet rs = edu.univ.erp.service.TranscriptService.getTranscriptData();
            DefaultTableModel model = new DefaultTableModel(
                    new String[]{"Course Code", "Course Title", "Final Score", "Final Grade"}, 0
            );
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getString("final_score"),
                        rs.getString("final_grade")
                });
            }
            transcriptTable.setModel(model);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        transcriptPanel.add(new JScrollPane(transcriptTable), BorderLayout.CENTER);
        JPanel exportPanel = new JPanel();
        JButton csvBtn = new JButton("Export as CSV");
        JButton pdfBtn = new JButton("Export as PDF");
        csvBtn.addActionListener(ev -> {
            JOptionPane.showMessageDialog(this, "Please wait... Generating CSV.", "Working", JOptionPane.INFORMATION_MESSAGE);
            try {
                ResultSet rs = edu.univ.erp.service.TranscriptService.getTranscriptData();
                String path = System.getProperty("user.dir") + "\\" +
                        edu.univ.erp.auth.Session.getUsername() + "_transcript.csv";
                boolean ok = edu.univ.erp.util.CSVUtil.exportToCSV(rs, path);
                JOptionPane.showMessageDialog(this,
                        ok ? "Transcript saved as CSV in project folder!" : "Export failed.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        pdfBtn.addActionListener(ev -> {
            JOptionPane.showMessageDialog(this, "Please wait... Generating PDF.", "Working", JOptionPane.INFORMATION_MESSAGE);
            try {
                ResultSet rs = edu.univ.erp.service.TranscriptService.getTranscriptData();
                String path = System.getProperty("user.dir") + "\\" +
                        edu.univ.erp.auth.Session.getUsername() + "_transcript.pdf";
                boolean ok = edu.univ.erp.util.PDFUtil.exportToPDF(
                        rs, edu.univ.erp.auth.Session.getUsername(), path);
                JOptionPane.showMessageDialog(this,
                        ok ? "Transcript saved as PDF in project folder!" : "PDF export failed.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        exportPanel.add(csvBtn);
        exportPanel.add(pdfBtn);
        transcriptPanel.add(exportPanel, BorderLayout.SOUTH);
        tabs.add("Transcript", transcriptPanel);
        mainPanel.add(tabs, BorderLayout.CENTER);
        add(mainPanel);
    }
    private void loadAvailableSections() {
        try {
            ResultSet rs = StudentService.listAvailableSections();
            DefaultTableModel model = new DefaultTableModel(
                    new String[]{"ID", "Code", "Title", "Day/Time", "Room", "Cap", "Dept"}, 0
            );
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("section_id"),
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getString("day_time"),
                        rs.getString("room"),
                        rs.getInt("capacity"),
                        rs.getString("department")
                });
            }
            sectionTable.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void loadMyRegistrations() {
        try {
            ResultSet rs = StudentService.getRegisteredSections();
            DefaultTableModel model = new DefaultTableModel(
                    new String[]{"EnrollID", "Code", "Title", "Day/Time", "Room"}, 0
            );
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("enrollment_id"),
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getString("day_time"),
                        rs.getString("room")
                });
            }
            myTable.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void handleRegister(ActionEvent e) {
        int chose = sectionTable.getSelectedRow();
        if (chose == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section to register.");
            return;
        }
        JOptionPane.showMessageDialog(this, "Please wait... Registering.", "Working", JOptionPane.INFORMATION_MESSAGE);
        int sectionId = (int) sectionTable.getValueAt(chose, 0);
        boolean success = StudentService.registerSection(sectionId);
        if (success) {
            JOptionPane.showMessageDialog(this, "Registered successfully!");
            loadMyRegistrations();
        } else {
            JOptionPane.showMessageDialog(this, "Already registered or failed.");
        }
    }
    private void handleDrop(ActionEvent e) {
        int chosen = myTable.getSelectedRow();
        if (chosen == -1) {
            JOptionPane.showMessageDialog(this, "Select a section to drop.");
            return;
        }
        JOptionPane.showMessageDialog(this, "Please wait... Dropping section.", "Working", JOptionPane.INFORMATION_MESSAGE);
        int enrollId = (int) myTable.getValueAt(chosen, 0);
        boolean passed = StudentService.dropSection(enrollId);
        if (passed) {
            JOptionPane.showMessageDialog(this, "Dropped successfully!");
            loadMyRegistrations();
        } else {
            JOptionPane.showMessageDialog(this, "Drop failed.");
        }
    }
}
