package org.example.view;

import org.example.dao.EmployeeDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class LoginView extends JDialog {
    private final JTextField ssnField;  // SSN 입력 필드
    private boolean authenticated = false;
    private boolean isAdmin = false;  // 사용자가 관리자 여부를 확인하는 필드
    private final EmployeeDAO employeeDAO;

    public LoginView(Frame parent) {
        super(parent, "로그인", true);
        setSize(300, 180);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent); // 다이얼로그 중앙에 위치

        employeeDAO = new EmployeeDAO();

        // 패널 설정
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(240, 240, 240)); // 배경색
        panel.add(new JLabel("SSN을 입력해주세요."));
        ssnField = new JTextField();  // SSN 입력 필드
        panel.add(ssnField);

        JButton loginButton = new JButton("로그인");
        loginButton.setBackground(new Color(60, 120, 240)); // 버튼 배경색
        loginButton.setForeground(Color.BLACK); // 버튼 글자색
        loginButton.setFocusPainted(false); // 포커스 시 테두리 제거
        loginButton.setBorder(BorderFactory.createLineBorder(Color.black)); // 둥근 테두리

        // 버튼 크기 조정
        loginButton.setPreferredSize(new Dimension(100, 30));

        loginButton.addActionListener(new LoginActionListener());
        panel.add(loginButton);

        add(panel, BorderLayout.CENTER);

        // 라벨 추가
        JLabel titleLabel = new JLabel("환영합니다!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16)); // 폰트 설정
        add(titleLabel, BorderLayout.NORTH);

        // 다이얼로그 배경색 설정
        getContentPane().setBackground(new Color(240, 240, 240));
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    private class LoginActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String ssn = ssnField.getText().trim();

            try {
                if (employeeDAO.isAdminSsn(ssn)) {  // 관리자 SSN 확인
                    isAdmin = true;
                    authenticated = true;
                    JOptionPane.showMessageDialog(LoginView.this, "관리자로 로그인 성공!", "정보", JOptionPane.INFORMATION_MESSAGE);
                } else if (employeeDAO.isEmployeeSsnExists(ssn)) {  // 일반 직원 SSN 확인
                    authenticated = true;
                    JOptionPane.showMessageDialog(LoginView.this, "로그인 성공!", "정보", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(LoginView.this, "유효하지 않은 SSN입니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }

                if (authenticated) {
                    dispose();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(LoginView.this, "데이터베이스 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
