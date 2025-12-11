package edu.univ.erp.domain;

import java.util.Map;
import java.util.HashMap;

public class SectionStats {
    private int totalStudents;
    private double avgFinalScore;
    private Double minFinalScore;
    private Double maxFinalScore;
    private Map<String, Integer> gradeCounts = new HashMap<>();
    public SectionStats() {}
    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
    public double getAvgFinalScore() { return avgFinalScore; }
    public void setAvgFinalScore(double avgFinalScore) { this.avgFinalScore = avgFinalScore; }
    public Double getMinFinalScore() { return minFinalScore; }
    public void setMinFinalScore(Double minFinalScore) { this.minFinalScore = minFinalScore; }
    public Double getMaxFinalScore() { return maxFinalScore; }
    public void setMaxFinalScore(Double maxFinalScore) { this.maxFinalScore = maxFinalScore; }
    public Map<String,Integer> getGradeCounts() { return gradeCounts; }
    public void addGradeCount(String grade, int count) { gradeCounts.put(grade, count); }
}
