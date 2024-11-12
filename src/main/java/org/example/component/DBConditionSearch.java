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

    private List<JPanel> conditionPanels = new ArrayList<>();  // 추가
    private JPanel conditionsPanel;  // 추가


    public DBConditionSearch(EmployeeReportView parent) {
        this.parentFrame = parent;
        this.employeeDAO = new EmployeeDAO();
        this.conditionPanels = new ArrayList<>();
    }

    public JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 전체 검색 조건을 담을 패널
        conditionsPanel = new JPanel();
        conditionsPanel.setLayout(new BoxLayout(conditionsPanel, BoxLayout.Y_AXIS));

        // 기본 검색 조건 패널 (첫 줄)
        JPanel baseCondition = createConditionRow(true);
        conditionPanels.add(baseCondition);
        conditionsPanel.add(baseCondition);

        // 조건 추가 버튼과 검색 버튼을 담을 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton addButton = new JButton("+ 조건 추가");
        addButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        addButton.setPreferredSize(new Dimension(100, 25));
        addButton.addActionListener(e -> addConditionRow());
        buttonPanel.add(addButton);

        JButton searchButton = new JButton("검색");
        searchButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        searchButton.setPreferredSize(new Dimension(80, 25));
        searchButton.addActionListener(e -> performSearch());
        buttonPanel.add(searchButton);

        // 그룹별 평균 급여 패널 (기존 코드 유지)
        JPanel groupPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        groupPanel.add(new JLabel("그룹별 평균급여:"));
        groupByComboBox = new JComboBox<>(new String[]{
                "그룹 없음", "성별", "부서", "상급자"
        });
        groupByComboBox.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        groupByComboBox.setPreferredSize(new Dimension(120, 25));
        groupPanel.add(groupByComboBox);

        // 전체 패널 구성
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(conditionsPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        topPanel.add(groupPanel, BorderLayout.NORTH);

        searchPanel.add(topPanel, BorderLayout.NORTH);
        return searchPanel;
    }

    private JPanel createConditionRow(boolean isFirst) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        // 검색 유형 콤보박스 생성
        String[] options = isFirst && conditionPanels.isEmpty() ?
                new String[]{"전체", "Name", "Ssn", "Bdate", "Address", "부서", "성별", "Salary", "Supervisor"} :
                new String[]{"Name", "Ssn", "Bdate", "Address", "부서", "성별", "Salary", "Supervisor"};

        JComboBox<String> typeBox = new JComboBox<>(options);
        typeBox.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        typeBox.setPreferredSize(new Dimension(100, 25));
        panel.add(typeBox);

        // 입력 컴포넌트를 담을 패널
        JPanel inputPanel = new JPanel(new CardLayout());
        inputPanel.setPreferredSize(new Dimension(200, 25));

        // 각 타입별 입력 컴포넌트 생성
        JTextField nameField = new JTextField(20);
        JTextField ssnField = new JTextField(20);
        JTextField bdateField = new JTextField(20);
        JTextField addressField = new JTextField(20);
        JTextField supervisorField = new JTextField(20);

        // 기존 컴포넌트
        JComboBox<String> deptBox = new JComboBox<>(getDepartmentList());
        JComboBox<String> sexBox = new JComboBox<>(new String[]{"F", "M"});

        JPanel salaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JTextField salaryField = new JTextField(10);
        salaryPanel.add(salaryField);
        salaryPanel.add(new JLabel("이상"));

        // 각 필드에 툴팁 추가
        nameField.setToolTipText("예: John B. Smith");
        ssnField.setToolTipText("예: 123456789");
        bdateField.setToolTipText("예: 1965-01-09");
        addressField.setToolTipText("예: 731 Fondren, Houston, TX");
        supervisorField.setToolTipText("관리자 SSN (예: 333445555)");

        // 컴포넌트 추가
        inputPanel.add(new JPanel(), "empty");
        inputPanel.add(nameField, "Name");
        inputPanel.add(ssnField, "Ssn");
        inputPanel.add(bdateField, "Bdate");
        inputPanel.add(addressField, "Address");
        inputPanel.add(deptBox, "부서");
        inputPanel.add(sexBox, "성별");
        inputPanel.add(salaryPanel, "Salary");
        inputPanel.add(supervisorField, "Supervisor");

        panel.add(inputPanel);

        // 삭제 버튼 (첫 번째 줄이 아닌 경우에만)
        if (!isFirst) {
            JButton removeButton = new JButton("X");
            removeButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            removeButton.setPreferredSize(new Dimension(25, 25));
            removeButton.addActionListener(e -> removeConditionRow(panel));
            panel.add(removeButton);
        }

        // 검색 유형 변경 리스너
        typeBox.addActionListener(e -> {
            CardLayout cl = (CardLayout) inputPanel.getLayout();
            String selected = (String) typeBox.getSelectedItem();
            cl.show(inputPanel, selected != null && !"전체".equals(selected) ? selected : "empty");
        });

        return panel;
    }

    private void addConditionRow() {
        JPanel newRow = createConditionRow(false);
        conditionPanels.add(newRow);
        conditionsPanel.add(newRow);
        conditionsPanel.revalidate();
        conditionsPanel.repaint();

        updateTypeBoxStates();  // 상태 업데이트
    }

    private void removeConditionRow(JPanel panel) {
        conditionPanels.remove(panel);
        conditionsPanel.remove(panel);
        conditionsPanel.revalidate();
        conditionsPanel.repaint();

        updateTypeBoxStates();  // 상태 업데이트
    }

    private void updateTypeBoxStates() {
        boolean hasMultipleRows = conditionPanels.size() > 1;

        for (JPanel panel : conditionPanels) {
            @SuppressWarnings("unchecked")
            JComboBox<String> typeBox = (JComboBox<String>) panel.getComponent(0);
            DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) typeBox.getModel();

            if (hasMultipleRows) {
                if ("전체".equals(typeBox.getSelectedItem())) {
                    typeBox.setSelectedIndex(1);
                }
                model.removeElement("전체");
            } else {
                if (model.getIndexOf("전체") == -1) {
                    model.insertElementAt("전체", 0);
                }
            }
        }
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

    public void performSearch() {
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

            if (!anyCheckBoxSelected) {
                JOptionPane.showMessageDialog(parentFrame,
                        "검색할 항목을 하나 이상 선택해주세요.",
                        "알림",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (groupBy != null && !"그룹 없음".equals(groupBy)) {
                Map<String, Double> avgSalaries = getAverageSalaryByGroup(groupBy);
                displayGroupResults(avgSalaries, groupBy);
            } else {
                Map<String, List<Object>> searchCriteria = getAllConditions();
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

    private void displayResults(List<Employee> employees) {
        // 체크박스 열을 포함한 커스텀 테이블 모델 생성
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int column) {
                // 첫 번째 열은 체크박스로 사용
                return column == 0 ? Boolean.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // 체크박스 열만 편집 가능하도록 설정
                return column == 0;
            }
        };

        // 체크박스 열 추가 (첫 번째 열)
        model.addColumn("");

        // 선택된 컬럼들의 순서를 보존하기 위한 리스트
        List<String> selectedColumns = new ArrayList<>();

        // 체크된 속성들을 사용자가 선택한 순서대로 컬럼 추가
        for (JCheckBox checkBox : parentFrame.getCheckBoxes()) {
            if (checkBox.isSelected()) {
                String columnName = checkBox.getText().toUpperCase();
                selectedColumns.add(columnName);  // 선택된 컬럼 순서 저장
                model.addColumn(columnName);      // 테이블에 컬럼 추가
            }
        }

        // 각 직원의 데이터를 행으로 추가
        for (Employee emp : employees) {
            Vector<Object> rowData = new Vector<>();
            rowData.add(false);  // 체크박스 초기값 (false = 미선택)

            // 선택된 컬럼 순서대로 데이터 추가
            for (String column : selectedColumns) {
                switch (column) {
                    case "NAME" ->
                        // 이름은 "FirstName M. LastName" 형식으로 표시
                            rowData.add(emp.getFirstName() + " " +
                                    emp.getMinit() + ". " +
                                    emp.getLastName());

                    case "SSN" -> rowData.add(emp.getSsn());

                    case "BDATE" ->
                        // 생년월일이 있는 경우에만 날짜 형식으로 변환하여 표시
                            rowData.add(emp.getBirthDate() != null ?
                                    new SimpleDateFormat("yyyy-MM-dd").format(emp.getBirthDate()) :
                                    "");

                    case "ADDRESS" -> rowData.add(emp.getAddress());

                    case "SEX" -> rowData.add(emp.getSex());

                    case "SALARY" ->
                        // 급여는 소수점 둘째자리까지 표시
                            rowData.add(String.format("%.2f", emp.getSalary()));

                    case "SUPERVISOR" -> rowData.add(emp.getSupervisorSsn());

                    case "DEPARTMENT" -> rowData.add(emp.getDepartmentName());

                    case "MODIFIED" ->
                        // 수정일시가 있는 경우에만 날짜시간 형식으로 변환하여 표시
                            rowData.add(emp.getModified() != null ?
                                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(emp.getModified()) :
                                    "");
                }
            }
            // 완성된 행 데이터를 테이블 모델에 추가
            model.addRow(rowData);
        }

        // 부모 프레임의 테이블 모델 업데이트
        parentFrame.updateTableModel(model);

        // 결과 테이블 가져오기
        JTable table = parentFrame.getResultTable();

        // 체크박스 열에 대한 설정 (첫 번째 열)
        if (table.getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setCellEditor(
                    new DefaultCellEditor(new JCheckBox()));

            // 체크박스 렌더러 설정
            table.getColumnModel().getColumn(0).setCellRenderer(
                    new DefaultTableCellRenderer() {
                        private final JCheckBox checkBox = new JCheckBox();

                        @Override
                        public Component getTableCellRendererComponent(
                                JTable table, Object value,
                                boolean isSelected, boolean hasFocus,
                                int row, int column) {
                            // 체크박스 상태 설정
                            checkBox.setSelected(value != null && (Boolean) value);
                            checkBox.setHorizontalAlignment(JLabel.CENTER);
                            return checkBox;
                        }
                    });
        }

        // 체크박스 선택 이벤트 리스너 추가
        parentFrame.addCheckboxListener(model);
    }

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

    private Map<String, List<Object>> getAllConditions() {
        Map<String, List<Object>> conditions = new HashMap<>();

        // 검색 조건들 수집
        for (JPanel panel : conditionPanels) {
            JComboBox<?> typeBox = (JComboBox<?>) panel.getComponent(0);
            JPanel inputPanel = (JPanel) panel.getComponent(1);

            String type = (String) typeBox.getSelectedItem();
            if (!"전체".equals(type)) {
                Object value = null;
                Component[] components = inputPanel.getComponents();
                for (Component comp : components) {
                    if (comp.isVisible()) {
                        if (comp instanceof JComboBox) {
                            value = ((JComboBox<?>) comp).getSelectedItem();
                        } else if (comp instanceof JTextField) {
                            value = ((JTextField) comp).getText().trim();
                        } else if (comp instanceof JPanel) {
                            // 급여 패널의 경우
                            JTextField salaryField = (JTextField) ((JPanel) comp).getComponent(0);
                            value = salaryField.getText().trim();
                        }
                    }
                }
                if (value != null && !value.toString().isEmpty()) {
                    conditions.computeIfAbsent(type, k -> new ArrayList<>()).add(value);
                }
            }
        }

        // 체크된 속성 목록 추가
        List<Object> selectedAttributes = new ArrayList<>();
        for (JCheckBox checkBox : parentFrame.getCheckBoxes()) {
            if (checkBox.isSelected()) {
                selectedAttributes.add(checkBox.getText());
            }
        }
        conditions.put("attributes", selectedAttributes);

        return conditions;
    }
}