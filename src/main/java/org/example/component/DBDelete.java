package org.example.component;

import org.example.dao.EmployeeDAO;
import org.example.dao.JDBCConnection;
import org.example.view.EmployeeReportView;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        // 부모 프레임에서 Name과 SSN 체크박스 상태 확인
        Container parent = getParent();
        while (parent != null && !(parent instanceof EmployeeReportView)) {
            parent = parent.getParent();
        }

        if (parent instanceof EmployeeReportView view) {
            boolean isNameAndSsnSelected = view.getCheckBoxes().stream()
                    .filter(cb -> cb.getText().equals("Name") || cb.getText().equals("Ssn"))
                    .allMatch(JCheckBox::isSelected);

            if (!isNameAndSsnSelected) {
                JOptionPane.showMessageDialog(this,
                        "직원 정보 삭제를 위해서는 검색 항목에서 Name과 SSN을 선택해주세요.",
                        "알림",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        // 선택된 직원이 없는 경우 체크
        if (selectedSsns == null || selectedSsns.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "삭제할 직원을 선택해주세요.",
                    "알림",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 다중 선택 경고
        if (selectedSsns.size() > 1) {
            int multipleConfirm = JOptionPane.showConfirmDialog(this,
                    selectedSsns.size() + "명의 직원이 선택되었습니다.\n계속하시겠습니까?",
                    "다중 선택 확인",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (multipleConfirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            // 선택된 직원들의 외래키 종속성 검사
            for (String ssn : selectedSsns) {
                if (hasDependencies(ssn)) {
                    int dependencyConfirm = JOptionPane.showConfirmDialog(this,
                            "선택된 직원(들)은 다른 데이터와 연결되어 있습니다.\n" +
                                    "삭제 시 관련된 모든 데이터가 함께 삭제될 수 있습니다.\n" +
                                    "계속하시겠습니까?",
                            "종속성 경고",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (dependencyConfirm != JOptionPane.YES_OPTION) {
                        return;
                    }
                    break;  // 하나라도 종속성이 있으면 더 이상 검사하지 않음
                }
            }

            // 최종 삭제 확인
            int confirm = JOptionPane.showConfirmDialog(this,
                    "선택한 직원을 정말 삭제하시겠습니까?",
                    "직원 삭제 확인",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                int successCount = 0;
                for (String ssn : selectedSsns) {
                    if (employeeDAO.deleteEmployee(ssn)) {
                        successCount++;
                    }
                }

                String message = String.format(
                        "총 %d명의 직원 중 %d명이 삭제되었습니다.",
                        selectedSsns.size(), successCount);
                JOptionPane.showMessageDialog(this, message);

                // 선택 정보 초기화 및 테이블 갱신
                selectedSsns.clear();
                selectedEmployeesLabel.setText("");
                ((JLabel) getComponent(3)).setText("0");

                if (getParent() != null && getParent().getParent() instanceof EmployeeReportView) {
                    ((EmployeeReportView) getParent().getParent()).refreshTable();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "직원 삭제 중 오류가 발생했습니다: " + ex.getMessage(),
                    "오류",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 주어진 SSN을 가진 직원의 외래키 종속성을 확인하는 메소드
     */
    private boolean hasDependencies(String ssn) throws SQLException {
        try (Connection conn = JDBCConnection.getConnection()) {
            // 각 테이블별 종속성 확인 쿼리
            String[] queries = {
                    "SELECT COUNT(*) FROM EMPLOYEE WHERE Super_ssn = ?",      // 다른 직원의 상사인 경우
                    "SELECT COUNT(*) FROM DEPARTMENT WHERE Mgr_ssn = ?",      // 부서 관리자인 경우
                    "SELECT COUNT(*) FROM WORKS_ON WHERE Essn = ?",           // 프로젝트 참여 중인 경우
                    "SELECT COUNT(*) FROM DEPENDENT WHERE Essn = ?"           // 부양가족이 있는 경우
            };

            for (String query : queries) {
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, ssn);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }
}