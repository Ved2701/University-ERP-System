package edu.univ.erp.domain;

public class Student {
    private int studentId;
    private int userId;
    private String rollNo;
    private String program;
    private int year;
    public Student() {}
    public Student(int studentId, int userId, String rollNo, String program, int year) {
        this.studentId = studentId;
        this.userId = userId;
        this.rollNo = rollNo;
        this.program = program;
        this.year = year;
    }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    @Override
    public String toString() {
        return rollNo + " (" + program + " Y" + year + ")";
    }
}
