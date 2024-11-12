package org.example.component;

import org.example.dao.EmployeeDAO;
import org.example.model.Employee;
import org.example.view.EmployeeReportView;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DBUpdate extends JDialog {
    // === 클래스 필드 ===
    private final EmployeeReportView parentFrame;    // 부모 프레임 참조 (메인 화면)
    private final EmployeeDAO employeeDAO;           // 데이터베이스 접근 객체
    private JTextField firstNameField;               // 이름 입력 필드
    private JTextField minitField;                   // 중간 이니셜 입력 필드
    private JTextField lastNameField;                // 성 입력 필드
    private JTextField ssnField;                     // SSN 입력 필드
    private JTextField birthdateField;               // 생년월일 입력 필드
    private JTextField addressField;                 // 주소 입력 필드
    private JComboBox<String> sexComboBox;          // 성별 선택 콤보박스
    private JTextField salaryField;                  // 급여 입력 필드
    private JTextField superSsnField;                // 상사 SSN 입력 필드
    private JTextField dnoField;                     // 부서번호 입력 필드

    public DBUpdate(EmployeeReportView parent) {
        // 모달 다이얼로그로 설정 (부모 창 비활성화)
        super(parent, "새로운 직원 정보 추가", true);
        this.parentFrame = parent;
        employeeDAO = new EmployeeDAO();
        initializeUI();
        pack();                           // 컴포넌트 크기에 맞게 다이얼로그 크기 조정
        setLocationRelativeTo(parent);    // 부모 창 중앙에 위치
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));

        // 입력 필드 패널 생성
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 입력 필드 초기화 - 숫자는 너비
        firstNameField = new JTextField(20);
        minitField = new JTextField(1);
        lastNameField = new JTextField(20);
        ssnField = new JTextField(9);
        birthdateField = new JTextField(10);
        addressField = new JTextField(30);
        sexComboBox = new JComboBox<>(new String[]{"F", "M"});
        salaryField = new JTextField(10);
        superSsnField = new JTextField(9);
        dnoField = new JTextField(2);

        // 입력 필드 배치
        int row = 0;  // GridBagLayout의 행 위치
        // 각 입력 필드와 라벨을 순서대로 배치
        addFieldRow(inputPanel, gbc, row++, "First Name:", firstNameField);
        addFieldRow(inputPanel, gbc, row++, "Middle Init.:", minitField);
        addFieldRow(inputPanel, gbc, row++, "Last Name:", lastNameField);
        addFieldRow(inputPanel, gbc, row++, "Ssn:", ssnField);
        addFieldRow(inputPanel, gbc, row++, "Birthdate:", birthdateField);
        addFieldRow(inputPanel, gbc, row++, "Address:", addressField);
        addFieldRow(inputPanel, gbc, row++, "Sex:", sexComboBox);
        addFieldRow(inputPanel, gbc, row++, "Salary:", salaryField);
        addFieldRow(inputPanel, gbc, row++, "Super_ssn:", superSsnField);
        addFieldRow(inputPanel, gbc, row++, "Dno:", dnoField);

        // 버튼 패널 생성
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addButton = new JButton("정보 추가하기");
        addButton.addActionListener(e -> addEmployee());  // 버튼 클릭 시 직원 추가 메소드 호출
        buttonPanel.add(addButton);

        // 메인 패널에 컴포넌트 추가
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFieldRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;    // 첫 번째 열 (라벨)
        gbc.gridy = row;  // 현재 행
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;    // 두 번째 열 (입력 필드)
        panel.add(field, gbc);
    }

    private void addEmployee() {
        try {
            // 필수 입력값 검증
            if (!validateInputs()) {
                return;
            }

            // Employee 객체 생성 및 데이터 설정
            Employee employee = new Employee();
            employee.setFirstName(firstNameField.getText().trim());
            employee.setMinit(minitField.getText().trim().charAt(0));
            employee.setLastName(lastNameField.getText().trim());
            employee.setSsn(ssnField.getText().trim());

            // 날짜 형식 변환
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date birthDate = dateFormat.parse(birthdateField.getText().trim());
            employee.setBirthDate(birthDate);

            // 나머지 필드 설정
            employee.setAddress(addressField.getText().trim());
            employee.setSex(((String) sexComboBox.getSelectedItem()).charAt(0));
            employee.setSalary(Double.parseDouble(salaryField.getText().trim()));
            employee.setSupervisorSsn(superSsnField.getText().trim());
            employee.setDepartmentNumber(Integer.parseInt(dnoField.getText().trim()));

            // 데이터베이스에 추가
            if (employeeDAO.addEmployee(employee)) {
                // 성공 메시지 표시
                JOptionPane.showMessageDialog(this,
                        "새로운 직원이 성공적으로 추가되었습니다.",
                        "성공",
                        JOptionPane.INFORMATION_MESSAGE);

                // 부모 프레임의 테이블 갱신 및 다이얼로그 종료
                parentFrame.refreshTable();
                dispose();
            } else {
                // 실패 메시지 표시
                JOptionPane.showMessageDialog(this,
                        "직원 추가에 실패했습니다.",
                        "오류",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (ParseException e) {
            // 날짜 형식 오류 처리
            JOptionPane.showMessageDialog(this,
                    "날짜 형식이 잘못되었습니다. YYYY-MM-DD 형식으로 입력해주세요.",
                    "입력 오류",
                    JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            // 숫자 형식 오류 처리
            JOptionPane.showMessageDialog(this,
                    "숫자 형식이 잘못되었습니다. 급여와 부서번호를 확인해주세요.",
                    "입력 오류",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            // 데이터베이스 오류 처리
            JOptionPane.showMessageDialog(this,
                    "데이터베이스 오류: " + e.getMessage(),
                    "오류",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateInput(JTextField input, String regEx, String errMessage, boolean isRequired) {
        String inputData = input.getText().trim(); // JTextField 입력값을 String으로 추출

        if (inputData.isEmpty()) { // 값이 없을 경우
            if (isRequired) { // 필수값일 경우 오류 메시지 출력
                JOptionPane.showMessageDialog(this,
                        "필수 필드는 반드시 입력되어야 합니다.",
                        "입력 오류",
                        JOptionPane.ERROR_MESSAGE);
                input.requestFocus();  // 해당 입력 필드에 포커스 이동
                return false;
            } else {
                return true;
            }// 필수값이 아닐 경우 검증을 종료
        }

        if (!inputData.matches(regEx)) { // 유효성 검사를 통과하지 못할 경우
            JOptionPane.showMessageDialog(
                    this,
                    errMessage,
                    "입력 오류",
                    JOptionPane.ERROR_MESSAGE);
            input.requestFocus();  // 해당 입력 필드에 포커스 이동
            return false;
        } else {
            return true;
        }
    }

    private boolean validateInputs() {

        if (!validateInput(firstNameField, "[a-zA-Z]+", "이름은 영어로 입력해야 합니다.", true)) {
            return false;
        }
        if (!validateInput(minitField, "[a-zA-Z]", "이니셜은 영어로 입력해야 합니다.", false)) {
            return false;
        }
        if (!validateInput(lastNameField, "[a-zA-Z]+", "성은 영어로 입력해야 합니다.", true)) {
            return false;
        }
        if (!validateInput(ssnField, "\\d{9}", "SSN은 9자리 숫자여야 합니다.", true)) {
            return false;
        }
        if (!validateInput(birthdateField, "\\d{4}-\\d{2}-\\d{2}", "출생일은 YYYY-MM-DD 형식입니다.", false)) {
            return false;
        }
        if (!validateInput(salaryField, "(\\d{1,8}(\\.\\d{2})?)", "Salary는 소숫점 둘째 자리까지 허용되는 숫자 형식입니다.", false)) {
            return false;
        }
        if (!validateInput(superSsnField, "\\d{9}", "Super_SSN은 9자리 숫자여야 합니다.", false)) {
            return false;
        }
        return validateInput(dnoField, "\\d{1,2}", "Dno는 1자리 또는 2자리 숫자여야 합니다.", true);
    }

}