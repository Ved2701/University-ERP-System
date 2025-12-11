package edu.univ.erp.data;

import java.sql.*;

public class AuthDatabase {
    private static final String URL = "jdbc:mysql://localhost:3306/auth_db";
    private static final String USER = "root";
    private static final String PASSWORD = "root123";
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    public static ResultSet getUserByUsername(String username) throws SQLException {
        Connection c = getConnection();
        String q = "SELECT * FROM users_auth WHERE username = ?";
        PreparedStatement s = c.prepareStatement(q);
        s.setString(1, username);
        return s.executeQuery();
    }
}
