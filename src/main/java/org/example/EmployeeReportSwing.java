package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class EmployeeReportSwing extends JFrame {
    private JTable resultTable;
    private JButton searchButton;
    private JScrollPane scrollPane;

    public EmployeeReportSwing() {
        setTitle("Employee Report");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // JTable 설정
        resultTable = new JTable();
        scrollPane = new JScrollPane(resultTable);
        add(scrollPane, BorderLayout.CENTER);

        // 검색 버튼 설정
        searchButton = new JButton("EMPLOYEE 테이블의 모든 직원 정보 출력");
        add(searchButton, BorderLayout.SOUTH);

        // 버튼 클릭 시 데이터 가져오기
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchEmployeeReport();
            }
        });
    }

    private void fetchEmployeeReport() {
        String sql = """
            SELECT e.Fname, e.Minit, e.Lname, e.Ssn, e.Bdate, e.Address, e.Sex, e.Salary, e.Super_ssn, d.Dname
            FROM EMPLOYEE e
            LEFT OUTER JOIN DEPARTMENT d ON e.dno = d.dnumber;
        """;

        try (Connection conn = JDBCConnection.getConnection();
             Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = stmt.executeQuery(sql)) {

            // ResultSet을 JTable에 설정
            resultTable.setModel(buildTableModel(rs));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "데이터를 가져오는 동안 오류가 발생했습니다: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ResultSet을 JTable 모델로 변환하는 메서드
    public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // 컬럼 이름 배열 생성
        String[] columnNames = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
            columnNames[i] = metaData.getColumnName(i + 1);
        }

        // 데이터 배열 생성
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        while (rs.next()) {
            Object[] rowData = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                rowData[i] = rs.getObject(i + 1);
            }
            model.addRow(rowData);
        }
        return model;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EmployeeReportSwing ui = new EmployeeReportSwing();
            ui.setVisible(true);
        });
    }
}
