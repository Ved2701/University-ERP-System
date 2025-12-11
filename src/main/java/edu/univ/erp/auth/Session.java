package edu.univ.erp.auth;

public class Session {
    private static String username;
    private static String role;
    private static boolean readOnly = false;
    public static void startSession(String user, String userRole) {
        username = user;
        role = userRole;
        readOnly = false;
    }
    public static String getUsername() {
        return username;
    }
    public static String getRole() {
        return role;
    }
    public static void setReadOnly(boolean flag) {
        readOnly = flag;
    }
    public static boolean isReadOnly() {
        return readOnly;
    }
    public static void endSession() {
        username = null;
        role = null;
        readOnly = false;
    }
}
