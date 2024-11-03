package org.example;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class DBUpdate extends JFrame {
    private static void addNewDataSwing (){
        // 이 부분에 UI를 통해 데이터셋을 입력

        // Test Code
        Map<String, Object> sampleData = new HashMap<>();
        sampleData.put("Fname", "Sample");
        sampleData.put("Minit", "J");
        sampleData.put("Lname", "Data");
        sampleData.put("Ssn", 999999999);
        sampleData.put("Bdate", "1998-03-27");
        sampleData.put("Address", "221 Sunjin, Daechung, BI");
        sampleData.put("SEX", "M");
        sampleData.put("Salary", 99999.00);
        sampleData.put("Super_ssn", 888665555);
        sampleData.put("Dno", 5);
        sampleData.put("Created", "2024-11-03 04:00:00");
        sampleData.put("Modified", "2024-11-03 04:00:00");

        try{
            addNewData("Employee", sampleData);
        }
        catch (SQLException e){
            e.printStackTrace();
        }

    }

    private static void addNewData(String tableName, Map<String, Object> data) throws SQLException {
        try(Connection conn = JDBCConnection.getConnection();){
            // Data 결합을 위한 StringJoiner
            StringJoiner attributes = new StringJoiner(", ");
            StringJoiner values = new StringJoiner(", ");

            // Data 를 String 형태로 결합
            for (String key : data.keySet()) {
                attributes.add(key);
                values.add("?");
            }

            // SQL: INSERT INTO
            String sql = "INSERT INTO " + tableName + " (" + attributes + ") VALUES (" + values + ")";

            // Values 대입 및 Update
            try (PreparedStatement cmd = conn.prepareStatement(sql)) {
                int index = 1;
                for (Object value : data.values()) {
                    cmd.setObject(index++, value);
                }
                cmd.executeUpdate();
            }

        }
    }
}
