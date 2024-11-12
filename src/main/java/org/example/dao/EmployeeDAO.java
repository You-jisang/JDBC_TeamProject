package org.example.dao;


import org.example.model.Employee;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class EmployeeDAO {

    private static final String SELECT_ALL_EMPLOYEES = """
            SELECT e.Fname, e.Minit, e.Lname, e.Ssn, e.Bdate, e.Address, 
                   e.Sex, e.Salary, e.Super_ssn, e.Dno, d.Dname, e.modified
            FROM EMPLOYEE e
            LEFT JOIN DEPARTMENT d ON e.Dno = d.Dnumber
            ORDER BY e.Fname, e.Lname
            """;

    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> employees = new ArrayList<>();

        // try-with-resources를 사용하여 자원 자동 해제
        try (Connection conn = JDBCConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_EMPLOYEES)) {

            // 결과셋을 순회하며 Employee 객체 생성
            while (rs.next()) {
                employees.add(createEmployeeFromResultSet(rs));
            }
        }

        return employees;
    }

    public boolean isEmployeeSsnExists(String ssn) throws SQLException {
        String sql = "SELECT 1 FROM EMPLOYEE WHERE Ssn = ?";
        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ssn);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();  // SSN이 존재하면 true 반환
            }
        }
    }

    public boolean isAdminSsn(String ssn) throws SQLException {
        String sql = "SELECT 1 FROM ADMIN WHERE ssn = ?";
        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ssn);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();  // SSN이 존재하면 true 반환
            }
        }
    }

    public void addAdminSsn(String ssn) throws SQLException {
        String sql = "INSERT INTO ADMIN (ssn) VALUES (?)";
        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ssn);
            pstmt.executeUpdate();
        }
    }

    public boolean addEmployee(Employee employee) throws SQLException {
        // PreparedStatement를 사용하여 SQL 인젝션 방지
        String sql = """
            INSERT INTO EMPLOYEE (Fname, Minit, Lname, Ssn, Bdate, Address, 
                                Sex, Salary, Super_ssn, Dno, created, modified)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;

        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // PreparedStatement 파라미터 설정
            pstmt.setString(1, employee.getFirstName());
            pstmt.setString(2, String.valueOf(employee.getMinit()));
            pstmt.setString(3, employee.getLastName());
            pstmt.setString(4, employee.getSsn());
            pstmt.setDate(5, new java.sql.Date(employee.getBirthDate().getTime()));
            pstmt.setString(6, employee.getAddress());
            pstmt.setString(7, String.valueOf(employee.getSex()));
            pstmt.setDouble(8, employee.getSalary());
            pstmt.setString(9, employee.getSupervisorSsn());
            pstmt.setInt(10, employee.getDepartmentNumber());

            // 실행 결과가 1이면 성공
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteEmployee(String ssn) throws SQLException {
        String sql = "DELETE FROM EMPLOYEE WHERE Ssn = ?";

        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ssn);
            return pstmt.executeUpdate() > 0;
        }
    }

    private Employee createEmployeeFromResultSet(ResultSet rs) throws SQLException {
        Employee employee = new Employee();

        // ResultSet에서 각 컬럼 값을 가져와 Employee 객체에 설정
        employee.setFirstName(rs.getString("Fname"));
        employee.setMinit(rs.getString("Minit").charAt(0));
        employee.setLastName(rs.getString("Lname"));
        employee.setSsn(rs.getString("Ssn"));
        employee.setBirthDate(rs.getDate("Bdate"));
        employee.setAddress(rs.getString("Address"));
        employee.setSex(rs.getString("Sex").charAt(0));
        employee.setSalary(rs.getDouble("Salary"));
        employee.setSupervisorSsn(rs.getString("Super_ssn"));
        employee.setDepartmentNumber(rs.getInt("Dno"));
        employee.setDepartmentName(rs.getString("Dname"));

        // modified 타임스탬프 처리 수정
        Timestamp timestamp = rs.getTimestamp("modified");
        if (timestamp != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(timestamp);
            cal.add(Calendar.HOUR, 9);  // UTC to KST (Korea Standard Time)
            employee.setModified(new Timestamp(cal.getTimeInMillis()));
        }

        return employee;
    }

    public List<Employee> searchEmployees(Map<String, List<Object>> criteria) throws SQLException {
        List<Employee> employees = new ArrayList<>();

        // criteria에서 attributes 가져오기 (타입 안전하게 처리)
        @SuppressWarnings("unchecked")
        List<Object> attrObjects = criteria.getOrDefault("attributes", new ArrayList<>());
        List<String> selectedAttributes = attrObjects.stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        // 모든 컬럼을 항상 조회
        StringBuilder sql = new StringBuilder("""
                SELECT e.Fname, e.Minit, e.Lname, e.Ssn, e.Bdate, e.Address, 
                       e.Sex, e.Salary, e.Super_ssn, e.Dno, d.Dname, e.modified
                FROM EMPLOYEE e 
                LEFT JOIN DEPARTMENT d ON e.Dno = d.Dnumber 
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        // 검색 조건 추가
        if (criteria.containsKey("Name") && !criteria.get("Name").isEmpty()) {
            sql.append(" AND (CONCAT(e.Fname, ' ', e.Minit, '. ', e.Lname) LIKE ?)");
            params.add("%" + criteria.get("Name").get(0) + "%");
        }

        if (criteria.containsKey("Ssn") && !criteria.get("Ssn").isEmpty()) {
            sql.append(" AND e.Ssn = ?");
            params.add(criteria.get("Ssn").get(0));
        }

        if (criteria.containsKey("Bdate") && !criteria.get("Bdate").isEmpty()) {
            sql.append(" AND e.Bdate = ?");
            params.add(criteria.get("Bdate").get(0));
        }

        if (criteria.containsKey("Address") && !criteria.get("Address").isEmpty()) {
            sql.append(" AND e.Address LIKE ?");
            params.add("%" + criteria.get("Address").get(0) + "%");
        }

        if (criteria.containsKey("Supervisor") && !criteria.get("Supervisor").isEmpty()) {
            sql.append(" AND e.Super_ssn = ?");
            params.add(criteria.get("Supervisor").get(0));
        }

        if (criteria.containsKey("부서") && !criteria.get("부서").isEmpty()) {
            sql.append(" AND d.Dname IN (");
            sql.append(String.join(",", Collections.nCopies(criteria.get("부서").size(), "?")));
            sql.append(")");
            params.addAll(criteria.get("부서"));
        }

        if (criteria.containsKey("성별") && !criteria.get("성별").isEmpty()) {
            sql.append(" AND e.Sex IN (");
            sql.append(String.join(",", Collections.nCopies(criteria.get("성별").size(), "?")));
            sql.append(")");
            params.addAll(criteria.get("성별"));
        }

        if (criteria.containsKey("Salary") && !criteria.get("Salary").isEmpty()) {  // "급여" -> "Salary"로 변경
            for (Object salary : criteria.get("Salary")) {
                sql.append(" AND e.Salary >= ?");
                try {
                    params.add(Double.parseDouble(salary.toString()));
                } catch (NumberFormatException e) {
                    throw new SQLException("Invalid salary format: " + salary);
                }
            }
        }

        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // 파라미터 설정
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // createEmployeeFromResultSet 사용하여 모든 필드 설정
                    Employee employee = createEmployeeFromResultSet(rs);
                    employees.add(employee);
                }
            }
        }

        return employees;
    }
}