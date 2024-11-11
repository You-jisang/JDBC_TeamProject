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
        setSize(500, 200);  // 크기를 더 크게 조정
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);

        employeeDAO = new EmployeeDAO();

        // 메인 패널
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));  // 세로 방향 레이아웃
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 30, 10, 30));  // 여백 증가


        // 입력 패널
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel ssnLabel = new JLabel("새 관리자 SSN:");
        ssnLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));  // 폰트 크기 증가
        inputPanel.add(ssnLabel);

        ssnField = new JTextField(20);  // 텍스트 필드 크기 증가
        ssnField.setFont(new Font("맑은 고딕", Font.PLAIN, 12));  // 폰트 크기 증가
        inputPanel.add(ssnField);

        inputPanel.setMaximumSize(new Dimension(450, 50));  // 패널 크기 제한
        inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(inputPanel);
        mainPanel.add(Box.createVerticalStrut(30));  // 간격 추가

        // 버튼
        JButton addButton = new JButton("추가");
        addButton.setFont(new Font("맑은 고딕", Font.PLAIN, 16));  // 폰트 크기 증가
        addButton.setPreferredSize(new Dimension(100, 40));  // 버튼 크기 증가
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.addActionListener(new AddAdminActionListener());
        mainPanel.add(addButton);

        add(mainPanel);

        // 엔터 키로 추가 버튼 클릭 효과
        ssnField.addActionListener(e -> addButton.doClick());
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
