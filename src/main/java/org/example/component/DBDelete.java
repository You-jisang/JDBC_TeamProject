package org.example.component;

import org.example.dao.EmployeeDAO;
import org.example.view.EmployeeReportView;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * 직원 정보 삭제를 담당하는 패널 클래스
 * 선택된 직원들의 삭제 처리를 수행
 */
public class DBDelete extends JPanel {
    private final EmployeeDAO employeeDAO;          // 데이터베이스 접근 객체
    // === 클래스 필드 ===
    private JLabel selectedEmployeesLabel;          // 선택된 직원 이름을 표시하는 레이블
    private List<String> selectedSsns;              // 선택된 직원들의 SSN 목록 저장

    /**
     * 생성자
     * 패널 초기화 및 EmployeeDAO 인스턴스 생성
     */
    public DBDelete() {
        employeeDAO = new EmployeeDAO();
        initializeUI();
    }

    /**
     * UI 초기화 메소드
     * 플로우 레이아웃을 사용하여 컴포넌트 배치
     */
    private void initializeUI() {
        // 패널 레이아웃 설정
        setLayout(new FlowLayout(FlowLayout.LEFT));
        // 패널 여백 설정
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // === UI 컴포넌트 초기화 및 배치 ===

        // 1. 선택한 직원 레이블 영역
        add(new JLabel("선택한 직원:"));  // 레이블 추가
        selectedEmployeesLabel = new JLabel("");    // 선택된 직원 이름 표시 레이블
        add(selectedEmployeesLabel);

        // 2. 선택된 인원수 표시 영역
        add(new JLabel("인원수:"));       // 레이블 추가
        JLabel countLabel = new JLabel("0");       // 선택된 직원 수 표시 레이블
        add(countLabel);

        // 3. 삭제 버튼 영역
        JButton deleteButton = new JButton("선택한 데이터 삭제");
        // 삭제 버튼 클릭 이벤트 처리
        deleteButton.addActionListener(e -> deleteSelectedEmployees());
        add(deleteButton);
    }

    /**
     * 선택된 직원 정보 업데이트 메소드
     * 테이블에서 체크박스 선택 시 호출됨
     *
     * @param selectedEmployees 선택된 직원 이름 목록
     * @param selectedSsns      선택된 직원 SSN 목록
     */
    public void updateSelectedEmployees(List<String> selectedEmployees, List<String> selectedSsns) {
        // 선택된 SSN 목록 업데이트
        this.selectedSsns = selectedSsns;
        // 선택된 직원 이름들을 쉼표로 구분하여 레이블에 표시
        selectedEmployeesLabel.setText(String.join(", ", selectedEmployees));
        // 선택된 직원 수 업데이트 (getComponent(3)은 인원수 레이블)
        ((JLabel) getComponent(3)).setText(String.valueOf(selectedEmployees.size()));
    }

    /**
     * 선택된 직원 삭제 메소드
     * 삭제 버튼 클릭 시 호출됨
     */
    private void deleteSelectedEmployees() {
        // 선택된 직원이 없는 경우 체크
        if (selectedSsns == null || selectedSsns.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "삭제할 직원을 선택해주세요.",
                    "알림",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 삭제 확인 대화상자 표시
        int confirm = JOptionPane.showConfirmDialog(this,
                "선택한 직원을 정말 삭제하시겠습니까?",
                "직원 삭제 확인",
                JOptionPane.YES_NO_OPTION);

        // 사용자가 '예'를 선택한 경우 삭제 진행
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int successCount = 0;  // 삭제 성공 카운트

                // 선택된 각 직원에 대해 삭제 수행
                for (String ssn : selectedSsns) {
                    if (employeeDAO.deleteEmployee(ssn)) {
                        successCount++;
                    }
                }

                // 삭제 결과 메시지 생성 및 표시
                String message = String.format(
                        "총 %d명의 직원 중 %d명이 삭제되었습니다.",
                        selectedSsns.size(), successCount);
                JOptionPane.showMessageDialog(this, message);

                // === 삭제 후 UI 업데이트 ===
                // 선택 정보 초기화
                selectedSsns.clear();
                selectedEmployeesLabel.setText("");
                ((JLabel) getComponent(3)).setText("0");

                // 부모 프레임의 테이블 갱신
                if (getParent() != null && getParent().getParent() instanceof EmployeeReportView) {
                    ((EmployeeReportView) getParent().getParent()).refreshTable();
                }

            } catch (SQLException ex) {
                // 데이터베이스 오류 처리
                JOptionPane.showMessageDialog(this,
                        "직원 삭제 중 오류가 발생했습니다: " + ex.getMessage(),
                        "오류",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}