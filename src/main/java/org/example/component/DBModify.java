package org.example.component;

import org.example.dao.JDBCConnection;
import org.example.view.EmployeeReportView;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 직원 정보 수정을 담당하는 클래스
 * 선택된 직원들의 정보를 일괄 수정하는 기능 제공
 */
public class DBModify {
    private final EmployeeReportView parentFrame;      // 부모 프레임 참조
    private JTextField modifyValueField;               // 수정할 값 입력 필드
    private String selectedColumn;                     // 선택된 수정 컬럼
    private List<String> selectedSsns;                // 선택된 직원들의 SSN 목록

    /**
     * 생성자
     *
     * @param parent 부모 프레임 (EmployeeReportView)
     */
    public DBModify(EmployeeReportView parent) {
        this.parentFrame = parent;
    }

    /**
     * 수정 패널 생성
     * 수정할 속성 선택 콤보박스, 값 입력 필드, 수정 버튼 포함
     *
     * @return 수정 기능이 포함된 패널
     */
    public JPanel createModifyPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("수정:"));

        // 수정 가능한 컬럼 선택 콤보박스 (이름과 SSN 제외)
        String[] modifyOptions = {
                "Address", "Sex", "Salary", "Bdate",
                "Supervisor", "Department"
        };
        JComboBox<String> modifyComboBox = new JComboBox<>(modifyOptions);
        panel.add(modifyComboBox);

        // 수정할 값 입력 필드
        modifyValueField = new JTextField(20);
        panel.add(modifyValueField);

        // 수정 버튼
        JButton updateButton = new JButton("Modify");
        updateButton.addActionListener(e -> {
            selectedColumn = getColumnNameForDatabase((String) modifyComboBox.getSelectedItem());
            updateSelectedEmployees();
        });
        panel.add(updateButton);

        return panel;
    }

    /**
     * 화면 표시용 컬럼명을 데이터베이스 컬럼명으로 변환
     *
     * @param displayName 화면에 표시되는 컬럼명
     * @return 데이터베이스의 실제 컬럼명
     */
    private String getColumnNameForDatabase(String displayName) {
        return switch (displayName) {
            case "Salary" -> "Salary";
            case "Address" -> "Address";
            case "Sex" -> "Sex";
            case "Supervisor" -> "Super_ssn";
            case "Department" -> "Dno";
            case "Bdate" -> "Bdate";
            default -> displayName;
        };
    }

    /**
     * 선택된 직원들의 정보 수정
     * 선택된 컬럼의 값을 입력된 새 값으로 업데이트
     */
    private void updateSelectedEmployees() {
        // 직원 선택 여부 검증
        if (selectedSsns == null || selectedSsns.isEmpty()) {
            JOptionPane.showMessageDialog(parentFrame,
                    "수정할 직원을 선택해주세요.",
                    "알림",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 입력값 존재 여부 검증
        String newValue = modifyValueField.getText().trim();
        if (newValue.isEmpty()) {
            JOptionPane.showMessageDialog(parentFrame,
                    "수정할 값을 입력해주세요.",
                    "알림",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = JDBCConnection.getConnection()) {
            // Modified 컬럼도 함께 업데이트하는 SQL 준비
            String sql = "UPDATE EMPLOYEE SET " + selectedColumn + " = ?, modified = CURRENT_TIMESTAMP WHERE Ssn = ?";

            int successCount = 0;
            for (String ssn : selectedSsns) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    // 컬럼 타입에 따른 값 설정 및 유효성 검사
                    switch (selectedColumn) {
                        case "Salary" -> pstmt.setDouble(1, Double.parseDouble(newValue));
                        case "Dno" -> pstmt.setInt(1, Integer.parseInt(newValue));
                        case "Bdate" -> {
                            try {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                java.util.Date parsed = dateFormat.parse(newValue);
                                pstmt.setDate(1, new java.sql.Date(parsed.getTime()));
                            } catch (ParseException ex) {
                                throw new SQLException("날짜 형식이 잘못되었습니다. YYYY-MM-DD 형식으로 입력해주세요.");
                            }
                        }
                        case "Sex" -> {
                            if (!newValue.matches("[FM]")) {
                                throw new SQLException("성별은 F 또는 M만 입력 가능합니다.");
                            }
                            pstmt.setString(1, newValue);
                        }
                        default -> pstmt.setString(1, newValue);
                    }
                    pstmt.setString(2, ssn);

                    if (pstmt.executeUpdate() > 0) {
                        successCount++;
                    }
                }
            }

            // 수정 성공 시 메시지 표시 및 테이블 갱신
            if (successCount > 0) {
                JOptionPane.showMessageDialog(parentFrame,
                        successCount + "명의 직원 정보가 수정되었습니다.",
                        "수정 완료",
                        JOptionPane.INFORMATION_MESSAGE);
                parentFrame.refreshTable();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(parentFrame,
                    "데이터 수정 중 오류가 발생했습니다: " + ex.getMessage(),
                    "오류",
                    JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(parentFrame,
                    "잘못된 숫자 형식입니다.",
                    "입력 오류",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 선택된 직원들의 SSN 목록 설정
     * 체크박스 선택 변경 시 호출됨
     *
     * @param ssns 선택된 직원들의 SSN 목록
     */
    public void setSelectedSsns(List<String> ssns) {
        this.selectedSsns = ssns;
    }
}