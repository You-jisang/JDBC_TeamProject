package org.example.component;

import org.example.dao.EmployeeDAO;
import org.example.dao.JDBCConnection;
import org.example.model.Employee;
import org.example.view.EmployeeReportView;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

/**
 * 조건 검색 기능을 제공하는 클래스
 */

/**
 * 직원 정보 검색을 위한 클래스
 * 일반 검색과 그룹별 평균 급여 검색 기능을 제공
 */
public class DBConditionSearch {
    private final EmployeeDAO employeeDAO;           // DB 접근 객체
    private final EmployeeReportView parentFrame;    // 부모 프레임 참조
    // 주요 컴포넌트 필드
    private JComboBox<String> searchTypeComboBox;    // 검색 유형 선택 콤보박스
    private JComboBox<String> departmentComboBox;  // 부서 선택 콤보박스
    private JComboBox<String> sexComboBox;         // 성별 선택 콤보박스
    private JTextField salaryField;                // 급여 입력 필드
    private JPanel inputPanel;                     // 입력 컴포넌트를 담을 패널
    private JComboBox<String> groupByComboBox;       // 그룹화 기준 선택 콤보박스


    /**
     * 생성자: 부모 프레임 참조를 받아 초기화
     */
    public DBConditionSearch(EmployeeReportView parent) {
        this.parentFrame = parent;
        this.employeeDAO = new EmployeeDAO();
    }

    /**
     * 검색 조건 패널 생성
     * 검색 유형, 검색어 입력, 그룹화 옵션을 포함한 UI 구성
     */
    public JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 상단 패널 (검색 조건)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // 검색 유형 콤보박스
        topPanel.add(new JLabel("검색 범위:"));
        searchTypeComboBox = new JComboBox<>(new String[]{
                "전체", "부서", "성별", "급여"
        });
        searchTypeComboBox.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        topPanel.add(searchTypeComboBox);

        // 입력 컴포넌트를 담을 패널
        inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // 부서 선택 콤보박스 초기화
        departmentComboBox = new JComboBox<>(getDepartmentList());
        departmentComboBox.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

        // 성별 선택 콤보박스 초기화
        sexComboBox = new JComboBox<>(new String[]{"F", "M"});
        sexComboBox.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

        // 급여 입력 필드 초기화
        salaryField = new JTextField(10);
        salaryField.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

        // 검색 유형 변경 이벤트 처리
        searchTypeComboBox.addActionListener(e -> updateInputComponent());

        topPanel.add(inputPanel);

        // 그룹별 평균 급여 콤보박스
        groupByComboBox = new JComboBox<>(new String[]{
                "그룹 없음", "성별", "부서", "상급자"
        });
        groupByComboBox.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        topPanel.add(new JLabel("그룹별 평균급여:"));
        topPanel.add(groupByComboBox);

        // 검색 버튼
        JButton searchButton = new JButton("검색");
        searchButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        searchButton.addActionListener(e -> performSearch());
        topPanel.add(searchButton);

        searchPanel.add(topPanel, BorderLayout.NORTH);

        // 초기 입력 컴포넌트 설정
        updateInputComponent();

        return searchPanel;
    }

    // 데이터베이스에서 부서 목록을 가져오는 메서드
    private String[] getDepartmentList() {
        try (Connection conn = JDBCConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT Dname FROM DEPARTMENT")) {

            ArrayList<String> departments = new ArrayList<>();
            while (rs.next()) {
                departments.add(rs.getString("Dname"));
            }
            return departments.toArray(new String[0]);
        } catch (SQLException e) {
            e.printStackTrace();
            return new String[]{"Error loading departments"};
        }
    }

    // 선택된 검색 유형에 따라 입력 컴포넌트 업데이트
    private void updateInputComponent() {
        inputPanel.removeAll();
        String selectedType = (String) searchTypeComboBox.getSelectedItem();

        if (selectedType != null && !"전체".equals(selectedType)) {
            switch (selectedType) {
                case "부서" -> inputPanel.add(departmentComboBox);
                case "성별" -> inputPanel.add(sexComboBox);
                case "급여" -> {
                    inputPanel.add(salaryField);
                    inputPanel.add(new JLabel("이상"));
                }
            }
        }

        inputPanel.revalidate();
        inputPanel.repaint();
    }

    // 검색 수행 메서드는 기존과 동일하게 유지하되, 검색 조건 가져오는 부분 수정
    private Map<String, Object> getSearchCriteria() {
        Map<String, Object> criteria = new HashMap<>();
        String searchType = (String) searchTypeComboBox.getSelectedItem();

        if (!"전체".equals(searchType)) {
            criteria.put("type", searchType);
            switch (searchType) {
                case "부서" -> criteria.put("value", departmentComboBox.getSelectedItem());
                case "성별" -> criteria.put("value", sexComboBox.getSelectedItem());
                case "급여" -> criteria.put("value", salaryField.getText().trim());
            }
        }

        // 체크된 속성 목록 추가
        List<String> selectedAttributes = new ArrayList<>();
        for (JCheckBox checkBox : parentFrame.getCheckBoxes()) {
            if (checkBox.isSelected()) {
                selectedAttributes.add(checkBox.getText());
            }
        }
        criteria.put("attributes", selectedAttributes);

        return criteria;
    }

    /**
     * 검색 수행 메소드
     * 일반 검색 또는 그룹별 검색을 구분하여 처리
     */
    /**
     * 검색 수행 메소드
     * 일반 검색 또는 그룹별 검색을 구분하여 처리
     */
    public void performSearch() {
        // 검색 시작할 때 이전 선택 목록 초기화
        parentFrame.clearSelectedEmployees();

        String groupBy = (String) groupByComboBox.getSelectedItem();

        try {
            // 체크박스 선택 검증
            boolean anyCheckBoxSelected = false;
            for (JCheckBox checkBox : parentFrame.getCheckBoxes()) {
                if (checkBox.isSelected()) {
                    anyCheckBoxSelected = true;
                    break;
                }
            }

            // 검색 항목 미선택 시 경고
            if (!anyCheckBoxSelected) {
                JOptionPane.showMessageDialog(parentFrame,
                        "검색할 항목을 하나 이상 선택해주세요.",
                        "알림",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 그룹별 검색 또는 일반 검색 분기
            if (groupBy != null && !"그룹 없음".equals(groupBy)) {
                // 그룹별 평균 급여 검색
                Map<String, Double> avgSalaries = getAverageSalaryByGroup(groupBy);
                displayGroupResults(avgSalaries, groupBy);
            } else {
                // 일반 검색 수행
                Map<String, Object> searchCriteria = getSearchCriteria();  // 수정된 getSearchCriteria() 메서드 사용

                // 급여 검색 시 유효성 검사
                if ("급여".equals(searchCriteria.get("type"))) {
                    String salaryStr = (String) searchCriteria.get("value");
                    try {
                        Double.parseDouble(salaryStr);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(parentFrame,
                                "급여는 숫자로 입력해주세요.",
                                "입력 오류",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // 검색 실행 및 결과 표시
                List<Employee> results = employeeDAO.searchEmployees(searchCriteria);
                displayResults(results);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "검색 중 오류가 발생했습니다: " + e.getMessage(),
                    "검색 오류",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 일반 검색 결과를 테이블에 표시
     * 선택된 속성만 컬럼으로 표시
     */
    private void displayResults(List<Employee> employees) {
        // 테이블 모델 생성 (체크박스 포함)
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        // 컬럼 추가
        model.addColumn(""); // 체크박스 컬럼
        // 선택된 속성만 컬럼으로 추가
        for (JCheckBox checkBox : parentFrame.getCheckBoxes()) {
            if (checkBox.isSelected()) {
                model.addColumn(checkBox.getText().toUpperCase());
            }
        }

        // 데이터 행 추가
        for (Employee emp : employees) {
            Vector<Object> rowData = new Vector<>();
            rowData.add(false); // 체크박스 초기값

            // 선택된 속성 값만 추가
            for (JCheckBox checkBox : parentFrame.getCheckBoxes()) {
                if (checkBox.isSelected()) {
                    switch (checkBox.getText()) {
                        case "Name" ->
                                rowData.add(emp.getFirstName() + " " + emp.getMinit() + ". " + emp.getLastName());
                        case "Ssn" -> rowData.add(emp.getSsn());
                        case "Bdate" -> rowData.add(emp.getBirthDate() != null ?
                                new SimpleDateFormat("yyyy-MM-dd").format(emp.getBirthDate()) : "");
                        case "Address" -> rowData.add(emp.getAddress());
                        case "Sex" -> rowData.add(emp.getSex());
                        case "Salary" -> rowData.add(String.format("%.2f", emp.getSalary()));
                        case "Supervisor" -> rowData.add(emp.getSupervisorSsn());
                        case "Department" -> rowData.add(emp.getDepartmentName());
                        case "Modified" -> {
                            if (emp.getModified() != null) {
                                SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                timestampFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));  // 한국 시간대 설정
                                rowData.add(timestampFormat.format(emp.getModified()));
                            } else {
                                rowData.add("");
                            }
                        }
                    }
                }
            }
            model.addRow(rowData);
        }
        parentFrame.updateTableModel(model);

        JTable table = parentFrame.getResultTable();  // getResultTable() 메서드 필요
        if (table.getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JCheckBox()));
            table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
                private final JCheckBox checkBox = new JCheckBox();

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus, int row, int column) {
                    checkBox.setSelected(value != null && (Boolean) value);
                    checkBox.setHorizontalAlignment(JLabel.CENTER);
                    return checkBox;
                }
            });
        }

        parentFrame.addCheckboxListener(model);
    }

    /**
     * 그룹별 평균 급여 결과를 테이블에 표시
     */
    private void displayGroupResults(Map<String, Double> avgSalaries, String groupBy) {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // 그룹별 컬럼 설정
        String groupColumn = switch (groupBy) {
            case "성별" -> "SEX";
            case "부서" -> "Dname";
            case "상급자" -> "SUPERVISOR";
            default -> groupBy;
        };
        model.addColumn(groupColumn);
        model.addColumn("AVG_Salary");

        // 그룹별 평균 급여 데이터 추가
        for (Map.Entry<String, Double> entry : avgSalaries.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.trim().isEmpty()) {
                key = "No Supervisor";
            }
            model.addRow(new Object[]{
                    key,
                    String.format("%.2f", entry.getValue())
            });
        }

        // 테이블 모델 업데이트
        parentFrame.updateTableModel(model);
    }

    /**
     * 그룹별 평균 급여 조회
     * 성별, 부서, 상급자별 평균 급여 계산
     */
    public Map<String, Double> getAverageSalaryByGroup(String groupBy) throws SQLException {
        Map<String, Double> avgSalaries = new HashMap<>();

        // 그룹별 SQL 쿼리 설정
        String sql = switch (groupBy) {
            case "성별" -> "SELECT Sex, AVG(Salary) as avg_salary FROM EMPLOYEE GROUP BY Sex";
            case "부서" -> """
                    SELECT d.Dname, AVG(e.Salary) as avg_salary 
                    FROM EMPLOYEE e 
                    JOIN DEPARTMENT d ON e.Dno = d.Dnumber 
                    GROUP BY d.Dname
                    """;
            case "상급자" -> """
                    SELECT CONCAT(s.Fname, ' ', s.Lname) as supervisor, AVG(e.Salary) as avg_salary 
                    FROM EMPLOYEE e 
                    LEFT JOIN EMPLOYEE s ON e.Super_ssn = s.Ssn 
                    GROUP BY s.Ssn, s.Fname, s.Lname
                    """;
            default -> throw new SQLException("Invalid group by option");
        };

        // 쿼리 실행 및 결과 처리
        try (Connection conn = JDBCConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String key = rs.getString(1);
                Double avgSalary = rs.getDouble("avg_salary");
                avgSalaries.put(key, avgSalary);
            }
        }

        return avgSalaries;
    }
}