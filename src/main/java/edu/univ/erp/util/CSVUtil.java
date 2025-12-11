package edu.univ.erp.util;

import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class CSVUtil {
    public static boolean exportToCSV(ResultSet rs, String filePath) {
        try (FileWriter csv = new FileWriter(filePath)) {
            ResultSetMetaData m = rs.getMetaData();
            int cols = m.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                csv.append(m.getColumnName(i));
                if (i < cols) csv.append(",");
            }
            csv.append("\n");
            while (rs.next()) {
                for (int i = 1; i <= cols; i++) {
                    csv.append(rs.getString(i) != null ? rs.getString(i) : "");
                    if (i < cols) csv.append(",");
                }
                csv.append("\n");
            }
            csv.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
