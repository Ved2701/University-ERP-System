package edu.univ.erp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionTest {
    public static void main(String[] args) {
        String urlAuth = "jdbc:mysql://localhost:3306/auth_db";
        String urlERP = "jdbc:mysql://localhost:3306/erp_db";
        String user = "root";
        String password = "root123";
        testConnection("Auth DB", urlAuth, user, password);
        testConnection("ERP DB", urlERP, user, password);
    }
    private static void testConnection(String name, String url, String user, String password) {
        try (Connection c = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected successfully to " + name);
        } catch (SQLException e) {
            System.err.println("Failed to connect to " + name);
            e.printStackTrace();
        }
    }
}