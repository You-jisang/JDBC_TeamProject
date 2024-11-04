package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBDelete extends JPanel {
    private JTextField conditionField;
    private JButton deleteButton;

    public DBDelete() {
        setLayout(new GridLayout(1, 3));

        // 삭제 조건 입력 필드와 레이블 설정
        JLabel conditionLabel = new JLabel("삭제 조건:");
        conditionField = new JTextField();

        // 삭제 버튼 설정
        deleteButton = new JButton("조건 삭제 실행");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteEmployeeByCondition();
            }
        });

        // 패널에 컴포넌트 추가
        add(conditionLabel);
        add(conditionField);
        add(deleteButton);
    }

    private void deleteEmployeeByCondition() {
        String condition = conditionField.getText();
        if (condition.isEmpty()) {
            JOptionPane.showMessageDialog(this, "조건을 입력하세요.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "DELETE FROM EMPLOYEE WHERE " + condition;

        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "조건에 맞는 직원이 삭제되었습니다.");
            } else {
                JOptionPane.showMessageDialog(this, "조건에 맞는 직원이 없습니다.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "삭제 중 오류가 발생했습니다: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
