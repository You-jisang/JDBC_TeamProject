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
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class EmployeeReportView extends JFrame {
    private boolean isAdmin = false;  // 로그인한 사용자가 관리자라면 true
    private JButton addAdminButton;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final String[] attributes = {
            "Name", "Ssn", "Bdate", "Address", "Sex",
            "Salary", "Supervisor", "Department", "Modified"
    };
    private final EmployeeDAO employeeDAO;
    private final List<JCheckBox> checkBoxes;
    private final List<String> selectedEmployeeNames = new ArrayList<>();
    private final List<String> selectedEmployeeSsns = new ArrayList<>();
    private final DBDelete deletePanel;
    private final DBConditionSearch dbConditionSearch;
    private final DBModify dbModify;
    private JTable resultTable;
    private DefaultTableModel tableModel;

    public EmployeeReportView() {
        employeeDAO = new EmployeeDAO();
        checkBoxes = new ArrayList<>();
        deletePanel = new DBDelete();
        dbConditionSearch = new DBConditionSearch(this);
        dbModify = new DBModify(this);

        showLoginDialog();

        initializeUI();
        loadEmployeeData();
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
    }

    private void loadEmployeeData() {
        tableModel.setRowCount(0);

        try {
            List<Employee> employees = employeeDAO.getAllEmployees();

            for (Employee emp : employees) {
                Object[] rowData = new Object[10];
                rowData[0] = false;
                rowData[1] = emp.getFirstName() + " " + emp.getMinit() + ". " + emp.getLastName();
                rowData[2] = emp.getSsn();
                rowData[3] = emp.getBirthDate() != null ? dateFormat.format(emp.getBirthDate()) : "";
                rowData[4] = emp.getAddress();
                rowData[5] = String.valueOf(emp.getSex());
                rowData[6] = String.format("%.2f", emp.getSalary());
                rowData[7] = emp.getSupervisorSsn();
                rowData[8] = emp.getDepartmentName();
                rowData[9] = emp.getModified() != null ? timestampFormat.format(emp.getModified()) : "";

                tableModel.addRow(rowData);
            }
        } catch (SQLException e) {
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

        JButton addButton = new JButton("직원 추가");
        addButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        addButton.addActionListener(e -> showAddEmployeeDialog());
        searchPanel.add(Box.createHorizontalStrut(300));
        searchPanel.add(addButton);

        // 관리자 추가 버튼
        addAdminButton = new JButton("관리자 추가");
        addAdminButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        addAdminButton.setEnabled(isAdmin);  // 관리자인 경우에만 활성화
        addAdminButton.addActionListener(e -> showAddAdminDialog());
        searchPanel.add(addAdminButton);

        JPanel attributePanel = new JPanel(new BorderLayout());
        attributePanel.setBorder(BorderFactory.createTitledBorder("검색 항목"));

        JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
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

        if (isAdmin) {
            panel.add(dbModify.createModifyPanel(), BorderLayout.NORTH);
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
}