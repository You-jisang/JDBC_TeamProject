package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class EmployeeReportSwing extends JFrame {
    private JTable resultTable;
    private JButton executeQueryButton;
    private JTextField selectField;
    private JTextField fromField;
    private JTextField whereField;
    private JScrollPane scrollPane;
    private JButton fetchAllButton;

    public EmployeeReportSwing() {
        setTitle("Employee Report");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // JTable 설정
        resultTable = new JTable();
        scrollPane = new JScrollPane(resultTable);
        add(scrollPane, BorderLayout.CENTER);

        // 입력 필드와 버튼 패널 설정
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(2, 4));

        JLabel selectLabel = new JLabel("SELECT");
        selectField = new JTextField(""); // 기본값 설정 시 ("e.Fname, e.Lname");

        JLabel fromLabel = new JLabel("FROM");
        fromField = new JTextField("");

        JLabel whereLabel = new JLabel("WHERE");
        whereField = new JTextField("");

        // 실행 버튼 설정
        executeQueryButton = new JButton("쿼리 실행");
        executeQueryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 검색 실행
            }
        });

        // 입력 패널에 레이블과 입력 필드 추가
        inputPanel.add(selectLabel);
        inputPanel.add(fromLabel);
        inputPanel.add(whereLabel);
        inputPanel.add(executeQueryButton);

        inputPanel.add(selectField);
        inputPanel.add(fromField);
        inputPanel.add(whereField);
        inputPanel.add(new JLabel(""));

        add(inputPanel, BorderLayout.NORTH);

        // 모든 직원 정보 출력 버튼 설정
        fetchAllButton = new JButton("EMPLOYEE 테이블의 모든 직원 정보 출력");
        fetchAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchAllEmployeeReport();
            }
        });
        // 하단에 출력 버튼 및 조건 삭제 패널을 추가하기 위한 새로운 패널 설정
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout());


        southPanel.add(fetchAllButton, BorderLayout.NORTH);

        // 조건 삭제 패널 추가
        DBDelete deletePanel = new DBDelete();
        southPanel.add(deletePanel, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);
    }

    private void fetchAllEmployeeReport() {
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
