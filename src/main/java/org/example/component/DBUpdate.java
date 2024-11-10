/*package org.example;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class DBUpdate extends JFrame {
    private static void addNewDataSwing (){
        // 이 부분에 UI를 통해 데이터셋을 입력

        // Test Code
        Map<String, Object> sampleData = new HashMap<>();
        sampleData.put("Fname", "Sample");
        sampleData.put("Minit", "J");
        sampleData.put("Lname", "Data");
        sampleData.put("Ssn", 999999999);
        sampleData.put("Bdate", "1998-03-27");
        sampleData.put("Address", "221 Sunjin, Daechung, BI");
        sampleData.put("SEX", "M");
        sampleData.put("Salary", 99999.00);
        sampleData.put("Super_ssn", 888665555);
        sampleData.put("Dno", 5);
        sampleData.put("Created", "2024-11-03 04:00:00");
        sampleData.put("Modified", "2024-11-03 04:00:00");

        try{
            addNewData("Employee", sampleData);
        }
        catch (SQLException e){
            e.printStackTrace();
        }

    }

    private static void addNewData(String tableName, Map<String, Object> data) throws SQLException {
        try(Connection conn = JDBCConnection.getConnection();){
            // Data 결합을 위한 StringJoiner
            StringJoiner attributes = new StringJoiner(", ");
            StringJoiner values = new StringJoiner(", ");

            // Data 를 String 형태로 결합
            for (String key : data.keySet()) {
                attributes.add(key);
                values.add("?");
            }

            // SQL: INSERT INTO
            String sql = "INSERT INTO " + tableName + " (" + attributes + ") VALUES (" + values + ")";

            // Values 대입 및 Update
            try (PreparedStatement cmd = conn.prepareStatement(sql)) {
                int index = 1;
                for (Object value : data.values()) {
                    cmd.setObject(index++, value);
                }
                cmd.executeUpdate();
            }

        }
    }
}
*/

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

/**
 * 새로운 직원 정보를 입력받고 데이터베이스에 추가하는 다이얼로그 클래스
 */
public class DBUpdate extends JDialog {
    // === 클래스 필드 ===
    private final EmployeeReportView parentFrame;    // 부모 프레임 참조 (메인 화면)
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
    private final EmployeeDAO employeeDAO;           // 데이터베이스 접근 객체

    /**
     * 생성자: 부모 프레임을 받아 다이얼로그 초기화
     */
    public DBUpdate(EmployeeReportView parent) {
        // 모달 다이얼로그로 설정 (부모 창 비활성화)
        super(parent, "새로운 직원 정보 추가", true);
        this.parentFrame = parent;
        employeeDAO = new EmployeeDAO();
        initializeUI();
        pack();                           // 컴포넌트 크기에 맞게 다이얼로그 크기 조정
        setLocationRelativeTo(parent);    // 부모 창 중앙에 위치
    }

    /**
     * UI 초기화 메소드
     * GridBagLayout을 사용하여 입력 필드들을 배치
     */
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

    /**
     * 입력 필드와 라벨을 그리드에 추가하는 헬퍼 메소드
     */
    private void addFieldRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;    // 첫 번째 열 (라벨)
        gbc.gridy = row;  // 현재 행
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;    // 두 번째 열 (입력 필드)
        panel.add(field, gbc);
    }

    /**
     * 새로운 직원 정보를 추가하는 메소드
     * 입력값 검증 후 데이터베이스에 추가
     */
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

    /**
     * 입력값 검증 메소드
     * 필수 필드가 모두 입력되었는지 확인
     */
    private boolean validateInput(JTextField input, String regEx, String errMessage, boolean isRequired){
        String inputData = input.getText().trim(); // JTextField 입력값을 String으로 추출
        
        if(inputData.isEmpty()){ // 값이 없을 경우
            if(isRequired){ // 필수값일 경우 오류 메시지 출력
                JOptionPane.showMessageDialog(this,
                        "필수 필드는 반드시 입력되어야 합니다.",
                        "입력 오류",
                        JOptionPane.ERROR_MESSAGE);
                input.requestFocus();  // 해당 입력 필드에 포커스 이동
                return false;
            }else{ return true; }// 필수값이 아닐 경우 검증을 종료
        }

        if(!inputData.matches(regEx)){ // 유효성 검사를 통과하지 못할 경우
            JOptionPane.showMessageDialog(
                    this,
                    errMessage,
                    "입력 오류",
                    JOptionPane.ERROR_MESSAGE);
            input.requestFocus();  // 해당 입력 필드에 포커스 이동
            return false;
        }else{ return true; }
    }

    private boolean validateInputs(){

        if (!validateInput(firstNameField, "[a-zA-Z]+", "이름은 영어로 입력해야 합니다.",true)) { return false; }
        if (!validateInput(minitField, "[a-zA-Z]", "이니셜은 영어로 입력해야 합니다.",false)) { return false; }
        if (!validateInput(lastNameField, "[a-zA-Z]+", "성은 영어로 입력해야 합니다.",true)) { return false; }
        if (!validateInput(ssnField, "\\d{9}", "SSN은 9자리 숫자여야 합니다.",true)) { return false; }
        if (!validateInput(birthdateField, "\\d{4}-\\d{2}-\\d{2}", "출생일은 YYYY-MM-DD 형식입니다.",false)) { return false; }
        if (!validateInput(salaryField, "(\\d{1,8}(\\.\\d{2})?)", "Salary는 소숫점 둘째 자리까지 허용되는 숫자 형식입니다.",false)) { return false; }
        if (!validateInput(superSsnField, "\\d{9}", "Super_SSN은 9자리 숫자여야 합니다.",false)) { return false; }
        if (!validateInput(dnoField, "\\d{1,2}", "Dno는 1자리 또는 2자리 숫자여야 합니다.",true)) { return false; }

        return true;
    }

    /*private boolean validateInputs() {

        // 필수 필드 입력 확인
        if (firstNameField.getText().trim().isEmpty() ||
                lastNameField.getText().trim().isEmpty() ||
                ssnField.getText().trim().isEmpty() ||
                birthdateField.getText().trim().isEmpty() ||
                addressField.getText().trim().isEmpty() ||
                salaryField.getText().trim().isEmpty() ||
                dnoField.getText().trim().isEmpty()) {

            // 누락된 필드가 있을 경우 경고 메시지 표시
            JOptionPane.showMessageDialog(this,
                    "모든 필수 필드를 입력해주세요.",
                    "입력 오류",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Fname 유효성 검사
        String fname = firstNameField.getText().trim();
        if (!fname.matches("[a-zA-Z]+")) {  // 정규식을 사용하여 영문자인지 검사
            JOptionPane.showMessageDialog(this,
                    "이름은 영어로 입력해야 합니다.",
                    "입력 오류",
                    JOptionPane.ERROR_MESSAGE);
            firstNameField.requestFocus();  // Fname 입력 필드에 포커스 이동
            return false;
        }

        // Minit 유효성 검사
        String minit = minitField.getText().trim();
        if (!minit.matches("[a-zA-Z]")) {  // 정규식을 사용하여 영문자 하나인지 검사
            JOptionPane.showMessageDialog(this,
                    "이니셜은 영어로 입력해야 합니다.",
                    "입력 오류",
                    JOptionPane.ERROR_MESSAGE);
            minitField.requestFocus();  // Minit 입력 필드에 포커스 이동
            return false;
        }

        // Lname 유효성 검사
        String lname = lastNameField.getText().trim();
        if (!lname.matches("[a-zA-Z]+")) {  // 정규식을 사용하여 영문자인지 검사
            JOptionPane.showMessageDialog(this,
                    "성은 영어로 입력해야 합니다.",
                    "입력 오류",
                    JOptionPane.ERROR_MESSAGE);
            lastNameField.requestFocus();  // Lname 입력 필드에 포커스 이동
            return false;
        }

        // SSN 유효성 검사
        String ssn = ssnField.getText().trim();
        if (!ssn.matches("\\d{9}")) {  // 정규식을 사용하여 9자리 숫자인지 검사
            JOptionPane.showMessageDialog(this,
                    "SSN은 9자리 숫자여야 합니다.",
                    "입력 오류",
                    JOptionPane.ERROR_MESSAGE);
            ssnField.requestFocus();  // SSN 입력 필드에 포커스 이동
            return false;
        }

        // Bdate 유효성 검사
        String bDate = birthdateField.getText().trim();
        if (!ssn.matches("\\d{4}-\\d{2}-\\d{2}")) {  // 정규식을 사용하여 DATE 형식인지 검사
            JOptionPane.showMessageDialog(this,
                    "출생일은 YYYY-MM-DD 형식입니다.",
                    "입력 오류",
                    JOptionPane.ERROR_MESSAGE);
            birthdateField.requestFocus();  // Bdate 입력 필드에 포커스 이동
            return false;
        }
        // 주소 형식은 유효성 검사 없음
        // 성별 형식은 유효성 검사 없음

        // Salary 유효성 검사
        String salary = salaryField.getText().trim();
        if (!ssn.matches("(\\d{1,10}|\\d{1,7}\\.\\d{2})")) {  // 정규식을 사용하여 DECIMAL(10, 2) 형식인지 검사
            JOptionPane.showMessageDialog(this,
                    "Salary는 소숫점 둘째 자리까지 허용되는 숫자 형식입니다.",
                    "입력 오류",
                    JOptionPane.ERROR_MESSAGE);
            salaryField.requestFocus();  // Salary 입력 필드에 포커스 이동
            return false;
        }

        // Super_ssn 유효성 검사
        String super_ssn = superSsnField.getText().trim();
        if (!ssn.matches("\\d{9}")) {  // 정규식을 사용하여 9자리 숫자인지 검사
            JOptionPane.showMessageDialog(this,
                    "Super_SSN은 9자리 숫자여야 합니다.",
                    "입력 오류",
                    JOptionPane.ERROR_MESSAGE);
            superSsnField.requestFocus();  // Super_SSN 입력 필드에 포커스 이동
            return false;
        }

        // Dno 유효성 검사
        String dno = dnoField.getText().trim();
        if (!ssn.matches("\\d{1,2}")) {  // 정규식을 사용하여 1~2자리 숫자인지 검사
            JOptionPane.showMessageDialog(this,
                    "Dno는 1자리 또는 2자리 숫자여야 합니다.",
                    "입력 오류",
                    JOptionPane.ERROR_MESSAGE);
            dnoField.requestFocus();  // Dno 입력 필드에 포커스 이동
            return false;
        }


        return true;
    }*/
}