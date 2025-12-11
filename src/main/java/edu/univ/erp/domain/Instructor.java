package edu.univ.erp.domain;

public class Instructor {
    private int instructorId;
    private int userId;
    private String department;
    public Instructor() {}
    public Instructor(int instructorId, int userId, String department) {
        this.instructorId = instructorId;
        this.userId = userId;
        this.department = department;
    }
    public int getInstructorId() { return instructorId; }
    public void setInstructorId(int instructorId) { this.instructorId = instructorId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    @Override
    public String toString() {
        return "Instructor[id=" + instructorId + ", dept=" + department + "]";
    }
}
