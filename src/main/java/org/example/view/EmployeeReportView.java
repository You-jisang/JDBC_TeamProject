package org.example.view;

import org.example.component.DBConditionSearch;
import org.example.component.DBDelete;
import org.example.component.DBModify;
import org.example.component.DBUpdate;
import org.example.dao.EmployeeDAO;
import org.example.model.Employee;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;


public class EmployeeReportView extends JFrame {
    private final SimpleDateFormat dateFormat;
    private final SimpleDateFormat timestampFormat;
    private final String[] attributes = {
            "Name", "Ssn", "Bdate", "Address", "Sex",
            "Salary", "Supervisor", "Department", "Modified"
    };
    private final EmployeeDAO employeeDAO;
    private final List<JCheckBox> checkBoxes = new ArrayList<>();  // 필드에서 초기화
    private final List<String> selectedEmployeeNames = new ArrayList<>();  // 필드에서 초기화
    private final List<String> selectedEmployeeSsns = new ArrayList<>();  // 필드에서 초기화
    private final DBDelete deletePanel;
    private final DBConditionSearch dbConditionSearch;
    private final DBModify dbModify;
    private boolean isAdmin = false;  // 로그인한 사용자가 관리자라면 true
    private JButton addAdminButton;
    private JTable resultTable;
    private DefaultTableModel tableModel;

    public EmployeeReportView() {
        // SimpleDateFormat 초기화
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        timestampFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        // 나머지 필드들 초기화
        employeeDAO = new EmployeeDAO();
        deletePanel = new DBDelete();
        dbConditionSearch = new DBConditionSearch(this);
        dbModify = new DBModify(this);

        showLoginDialog();

        initializeUI();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            EmployeeReportView system = new EmployeeReportView();
            system.setVisible(true);
        });
    }

    private void showLoginDialog() {
        LoginView loginView = new LoginView(this);
        loginView.setVisible(true);
        isAdmin = loginView.isAdmin();  // 로그인한 사용자가 관리자라면 true
    }

    private void initializeUI() {
        setTitle("Information Retrieval System");
        setSize(1200, 600);  // 너비 증가
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        // 빈 테이블 모델로 초기화
        String[] columnNames = {
                "", "NAME", "SSN", "BDATE", "ADDRESS", "SEX",
                "SALARY", "SUPERVISOR", "DEPARTMENT", "MODIFIED"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };
        resultTable.setModel(tableModel);
    }

    /**
     * 전체 직원 데이터를 로드하고 테이블에 표시하는 메소드
     * 체크된 컬럼만 선택된 순서대로 표시하며, 선택 상태를 초기화함
     */
    private void loadEmployeeData() {
        // 선택된 직원 정보 초기화 (테이블 새로고침 시 선택 상태 리셋)
        selectedEmployeeNames.clear();
        selectedEmployeeSsns.clear();
        deletePanel.updateSelectedEmployees(selectedEmployeeNames, selectedEmployeeSsns);
        dbModify.setSelectedSsns(selectedEmployeeSsns);

        // 현재 선택된 컬럼들의 순서를 보존하기 위한 리스트
        List<String> selectedColumns = new ArrayList<>();
        for (JCheckBox checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                selectedColumns.add(checkBox.getText().toUpperCase());
            }
        }

        // 체크박스 기능을 포함한 커스텀 테이블 모델 생성
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int column) {
                // 첫 번째 열은 체크박스로 표시
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

        // 선택된 컬럼들을 순서대로 추가
        for (String columnName : selectedColumns) {
            model.addColumn(columnName);
        }

        try {
            // 데이터베이스에서 전체 직원 정보 조회
            List<Employee> employees = employeeDAO.getAllEmployees();

            // 각 직원의 데이터를 행으로 추가
            for (Employee emp : employees) {
                Vector<Object> rowData = new Vector<>();
                rowData.add(false);  // 체크박스 초기값 (미선택)

                // 선택된 컬럼 순서대로 데이터 추가
                for (String column : selectedColumns) {
                    switch (column) {
                        case "NAME" -> rowData.add(emp.getFirstName() + " " +
                                emp.getMinit() + ". " +
                                emp.getLastName());
                        case "SSN" -> rowData.add(emp.getSsn());
                        case "BDATE" -> rowData.add(emp.getBirthDate() != null ?
                                dateFormat.format(emp.getBirthDate()) :
                                "");
                        case "ADDRESS" -> rowData.add(emp.getAddress());
                        case "SEX" -> rowData.add(emp.getSex());
                        case "SALARY" -> rowData.add(String.format("%.2f", emp.getSalary()));
                        case "SUPERVISOR" -> rowData.add(emp.getSupervisorSsn());
                        case "DEPARTMENT" -> rowData.add(emp.getDepartmentName());
                        case "MODIFIED" -> rowData.add(emp.getModified() != null ?
                                timestampFormat.format(emp.getModified()) :
                                "");
                    }
                }
                model.addRow(rowData);
            }

            // 테이블 모델 업데이트
            tableModel = model;
            resultTable.setModel(model);
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            resultTable.setRowSorter(sorter);

            // 테이블 컬럼 설정
            if (resultTable.getColumnCount() > 0) {
                // 체크박스 열 설정
                resultTable.getColumnModel().getColumn(0).setMaxWidth(30);  // 체크박스 열 너비를 최소화
                resultTable.getColumnModel().getColumn(0).setCellEditor(
                        new DefaultCellEditor(new JCheckBox()));

                // 체크박스 렌더러 설정 (체크박스 중앙 정렬)
                resultTable.getColumnModel().getColumn(0).setCellRenderer(
                        new DefaultTableCellRenderer() {
                            private final JCheckBox checkBox = new JCheckBox();

                            @Override
                            public Component getTableCellRendererComponent(
                                    JTable table, Object value,
                                    boolean isSelected, boolean hasFocus,
                                    int row, int column) {
                                checkBox.setSelected(value != null && (Boolean) value);
                                checkBox.setHorizontalAlignment(JLabel.CENTER);
                                return checkBox;
                            }
                        });
            }

            // 체크박스 선택 이벤트 리스너 추가
            addCheckboxListener(model);

        } catch (SQLException e) {
            // 데이터 로드 중 오류 발생 시 사용자에게 알림
            JOptionPane.showMessageDialog(this,
                    "데이터를 불러오는 중 오류가 발생했습니다: " + e.getMessage(),
                    "에러",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JPanel searchPanel = dbConditionSearch.createSearchPanel();

        JPanel attributePanel = new JPanel(new BorderLayout());
        attributePanel.setBorder(BorderFactory.createTitledBorder("검색 항목"));

        JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 5));  // 체크박스 간격 15로 설정
        for (String attr : attributes) {
            JCheckBox checkBox = new JCheckBox(attr);
            checkBox.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            checkBoxes.add(checkBox);
            checkBoxPanel.add(checkBox);
        }

        attributePanel.add(checkBoxPanel, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(attributePanel, BorderLayout.CENTER);

        panel.add(mainPanel, BorderLayout.CENTER);

        return panel;
    }

    private void showAddAdminDialog() {
        AddAdminView addAdminView = new AddAdminView(this);
        addAdminView.setVisible(true);
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        String[] columnNames = {
                "", "NAME", "SSN", "BDATE", "ADDRESS", "SEX",
                "SALARY", "SUPERVISOR", "DEPARTMENT", "MODIFIED"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        resultTable = new JTable(tableModel);


        resultTable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        resultTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 12));

        // 체크박스 렌더러 설정
        resultTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JCheckBox()));
        resultTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            private final JCheckBox checkBox = new JCheckBox();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                checkBox.setSelected(value != null && (Boolean) value);
                checkBox.setHorizontalAlignment(JLabel.CENTER);
                return checkBox;
            }
        });

        // 컬럼 너비 설정
        resultTable.getColumnModel().getColumn(0).setMaxWidth(30);
        resultTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        resultTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        resultTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        resultTable.getColumnModel().getColumn(4).setPreferredWidth(200);
        resultTable.getColumnModel().getColumn(5).setPreferredWidth(50);
        resultTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        resultTable.getColumnModel().getColumn(7).setPreferredWidth(150);
        resultTable.getColumnModel().getColumn(8).setPreferredWidth(150);
        resultTable.getColumnModel().getColumn(9).setPreferredWidth(150);

        tableModel.addTableModelListener(e -> {
            if (e.getColumn() == 0) {
                updateSelectedEmployees();
            }
        });

        JScrollPane scrollPane = new JScrollPane(resultTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        // 상단 패널 - 수정 기능과 버튼들을 포함
        JPanel topPanel = new JPanel(new BorderLayout());

        // 수정 패널은 왼쪽에
        JPanel modifyPanel = dbModify.createModifyPanel();
        // 관리자가 아닐 경우 모든 컴포넌트 비활성화
        if (!isAdmin) {
            for (Component comp : modifyPanel.getComponents()) {
                comp.setEnabled(false);
                if (comp instanceof JButton) {
                    ((JButton) comp).setToolTipText("관리자 권한이 필요합니다");
                }
            }
        }
        topPanel.add(modifyPanel, BorderLayout.CENTER);

        // 버튼들은 오른쪽에
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        // 직원 추가 버튼
        JButton addButton = new JButton("직원 추가");
        addButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        if (isAdmin) {
            addButton.addActionListener(e -> showAddEmployeeDialog());
        } else {
            addButton.setEnabled(false);
            addButton.setToolTipText("관리자 권한이 필요합니다");
        }
        buttonPanel.add(addButton);

        // 관리자 추가 버튼
        addAdminButton = new JButton("관리자 추가");
        addAdminButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        if (isAdmin) {
            addAdminButton.addActionListener(e -> showAddAdminDialog());
        } else {
            addAdminButton.setEnabled(false);
            addAdminButton.setToolTipText("관리자 권한이 필요합니다");
        }
        buttonPanel.add(addAdminButton);

        topPanel.add(buttonPanel, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);

        // 하단 패널 - 삭제 기능
        // 삭제 패널 컴포넌트들 비활성화 (관리자가 아닐 경우)
        if (!isAdmin) {
            for (Component comp : deletePanel.getComponents()) {
                comp.setEnabled(false);
                if (comp instanceof JButton) {
                    ((JButton) comp).setToolTipText("관리자 권한이 필요합니다");
                }
            }
        }
        panel.add(deletePanel, BorderLayout.SOUTH);

        return panel;
    }

    private void updateSelectedEmployees() {
        selectedEmployeeNames.clear();
        selectedEmployeeSsns.clear();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean isSelected = (Boolean) tableModel.getValueAt(i, 0);
            if (Boolean.TRUE.equals(isSelected)) {
                String name = (String) tableModel.getValueAt(i, 1);
                String ssn = (String) tableModel.getValueAt(i, 2);
                selectedEmployeeNames.add(name);
                selectedEmployeeSsns.add(ssn);
            }
        }

        deletePanel.updateSelectedEmployees(selectedEmployeeNames, selectedEmployeeSsns);
        dbModify.setSelectedSsns(selectedEmployeeSsns);
    }

    private void showAddEmployeeDialog() {
        DBUpdate dialog = new DBUpdate(this);
        dialog.setVisible(true);
    }

    public void updateTableModel(DefaultTableModel newModel) {
        // 검색으로 테이블 초기화 되었을 때 튜플 체크할 수 있게 업데이트
        if (tableModel != null) {
            for (var listener : tableModel.getTableModelListeners()) {
                tableModel.removeTableModelListener(listener);
            }
        }


        tableModel = newModel;  // 클래스의 tableModel 필드 업데이트
        resultTable.setModel(newModel);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(newModel);
        resultTable.setRowSorter(sorter);

        if (newModel.getColumnCount() > 2) {
            resultTable.getColumnModel().getColumn(0).setMaxWidth(30);
            resultTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JCheckBox()));
            resultTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
                private final JCheckBox checkBox = new JCheckBox();

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus, int row, int column) {
                    checkBox.setSelected(value != null && (Boolean) value);
                    checkBox.setHorizontalAlignment(JLabel.CENTER);
                    return checkBox;
                }
            });

            newModel.addTableModelListener(e -> {
                if (e.getColumn() == 0) {
                    updateSelectedEmployees();
                }
            });

            for (int i = 1; i < newModel.getColumnCount(); i++) {
                String columnName = newModel.getColumnName(i);
                switch (columnName) {
                    case "NAME" -> resultTable.getColumnModel().getColumn(i).setPreferredWidth(150);
                    case "SSN" -> resultTable.getColumnModel().getColumn(i).setPreferredWidth(100);
                    case "BDATE" -> resultTable.getColumnModel().getColumn(i).setPreferredWidth(100);
                    case "ADDRESS" -> resultTable.getColumnModel().getColumn(i).setPreferredWidth(200);
                    case "SEX" -> resultTable.getColumnModel().getColumn(i).setPreferredWidth(50);
                    case "SALARY" -> resultTable.getColumnModel().getColumn(i).setPreferredWidth(100);
                    case "SUPERVISOR" -> resultTable.getColumnModel().getColumn(i).setPreferredWidth(150);
                    case "DEPARTMENT" -> resultTable.getColumnModel().getColumn(i).setPreferredWidth(150);
                    case "MODIFIED" -> resultTable.getColumnModel().getColumn(i).setPreferredWidth(150);
                }
            }
        }
    }

    public List<JCheckBox> getCheckBoxes() {
        return checkBoxes;
    }

    // 튜플 체크 박스 초기화 후 선택 가능하게 함
    public void addCheckboxListener(DefaultTableModel model) {
        model.addTableModelListener(e -> {
            if (e.getColumn() == 0) {
                updateSelectedEmployees();
            }
        });
    }

    // 검색 등으로 테이플 초기화 될 때 튜플 체크박스도 같이 초기화
    public void clearSelectedEmployees() {
        selectedEmployeeNames.clear();
        selectedEmployeeSsns.clear();
        deletePanel.updateSelectedEmployees(selectedEmployeeNames, selectedEmployeeSsns);
        dbModify.setSelectedSsns(selectedEmployeeSsns);
    }

    public void refreshTable() {
        loadEmployeeData();
    }

    public JTable getResultTable() {
        return resultTable;
    }
}