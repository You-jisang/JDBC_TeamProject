package org.example;

import java.sql.*;

public class DBTest {
    public static void main(String[] args) {
        try (Connection conn = JDBCConnection.getConnection()) {
            System.out.println("DB 연결 성공!");
            testQuery(conn);
        } catch (SQLException e) {
            System.err.println("DB 연결 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testQuery(Connection conn) throws SQLException {
        String sql = "SELECT Fname, Salary FROM EMPLOYEE WHERE sex = 'M'";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String fname = rs.getString("Fname");
                double salary = rs.getDouble("Salary");
                System.out.printf("이름: %s, 급여: %.2f%n", fname, salary);
            }
        }
    }
}
