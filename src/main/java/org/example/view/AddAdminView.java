package org.example.view;

import org.example.dao.EmployeeDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class AddAdminView extends JDialog {
    private final JTextField ssnField;
    private final EmployeeDAO employeeDAO;

    public AddAdminView(Frame parent) {
        super(parent, "관리자 추가", true);
        setSize(300, 180);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent); // 다이얼로그 중앙에 위치

        employeeDAO = new EmployeeDAO();

        // 패널 설정
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(240, 240, 240)); // 배경색 설정

        JLabel titleLabel = new JLabel("관리자 추가", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16)); // 제목 폰트 설정
        panel.add(titleLabel);

        panel.add(new JLabel("새 관리자 SSN:"));
        ssnField = new JTextField();
        panel.add(ssnField);

        JButton addButton = new JButton("추가");
        addButton.setBackground(new Color(60, 120, 240)); // 버튼 배경색
        addButton.setForeground(Color.WHITE); // 버튼 글자색
        addButton.setFocusPainted(false); // 포커스 시 테두리 제거
        addButton.setPreferredSize(new Dimension(100, 30)); // 버튼 크기 설정
        addButton.addActionListener(new AddAdminActionListener());
        panel.add(addButton);

        add(panel, BorderLayout.CENTER);

        // 다이얼로그 배경색
        getContentPane().setBackground(new Color(240, 240, 240));
    }

    private class AddAdminActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String ssn = ssnField.getText().trim();

            try {
                if (employeeDAO.isEmployeeSsnExists(ssn)) {
                    employeeDAO.addAdminSsn(ssn);
                    JOptionPane.showMessageDialog(AddAdminView.this, "관리자 추가 성공!", "정보", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(AddAdminView.this, "유효하지 않은 SSN입니다. 직원으로 등록된 SSN만 추가 가능합니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(AddAdminView.this, "데이터베이스 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
