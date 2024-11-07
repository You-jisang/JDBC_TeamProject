package org.example.dao;


import org.example.model.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 직원 데이터의 데이터베이스 접근을 담당하는 DAO(Data Access Object) 클래스
 * CRUD(Create, Read, Update, Delete) 작업을 처리
 */
public class EmployeeDAO {

    /**
     * 기본 직원 조회 SQL 쿼리
     * Employee 테이블과 Department 테이블을 조인하여 전체 직원 정보 조회
     */
    private static final String SELECT_ALL_EMPLOYEES = """
            SELECT e.Fname, e.Minit, e.Lname, e.Ssn, e.Bdate, e.Address, 
                   e.Sex, e.Salary, e.Super_ssn, e.Dno, d.Dname, e.modified
            FROM EMPLOYEE e
            LEFT JOIN DEPARTMENT d ON e.Dno = d.Dnumber
            ORDER BY e.Fname, e.Lname
            """;

    /**
     * 전체 직원 정보 조회 메소드
     *
     * @return 전체 직원 목록
     * @throws SQLException SQL 실행 중 발생할 수 있는 예외
     */
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

    /**
     * 새로운 직원 정보 추가 메소드
     *
     * @param employee 추가할 직원 정보
     * @return 추가 성공 여부
     * @throws SQLException SQL 실행 중 발생할 수 있는 예외
     */
    public boolean addEmployee(Employee employee) throws SQLException {
        // PreparedStatement를 사용하여 SQL 인젝션 방지
        String sql = """
                INSERT INTO EMPLOYEE (Fname, Minit, Lname, Ssn, Bdate, Address, 
                                    Sex, Salary, Super_ssn, Dno)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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

    /**
     * 직원 정보 삭제 메소드
     *
     * @param ssn 삭제할 직원의 SSN
     * @return 삭제 성공 여부
     * @throws SQLException SQL 실행 중 발생할 수 있는 예외
     */
    public boolean deleteEmployee(String ssn) throws SQLException {
        String sql = "DELETE FROM EMPLOYEE WHERE Ssn = ?";

        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ssn);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * 조건에 맞는 직원 삭제 메소드
     *
     * @param condition WHERE 절에 들어갈 조건문
     * @return 삭제된 직원 수
     * @throws SQLException SQL 실행 중 발생할 수 있는 예외
     */
    public int deleteEmployeesByCondition(String condition) throws SQLException {
        String sql = "DELETE FROM EMPLOYEE WHERE " + condition;

        try (Connection conn = JDBCConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            return stmt.executeUpdate(sql);
        }
    }

    /**
     * ResultSet에서 Employee 객체 생성하는 헬퍼 메소드
     *
     * @param rs 데이터베이스 조회 결과
     * @return Employee 객체
     * @throws SQLException SQL 실행 중 발생할 수 있는 예외
     */
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
        employee.setModified(rs.getTimestamp("modified"));

        return employee;
    }

    /**
     * 검색 조건에 따른 직원 검색 메소드
     *
     * @param criteria 검색 조건 Map
     * @return 검색된 직원 목록
     * @throws SQLException SQL 실행 중 발생할 수 있는 예외
     */
    public List<Employee> searchEmployees(Map<String, Object> criteria) throws SQLException {
        List<Employee> employees = new ArrayList<>();

        // 선택된 속성들 가져오기
        @SuppressWarnings("unchecked")
        List<String> selectedAttributes = (List<String>) criteria.getOrDefault("attributes",
                new ArrayList<String>());

        // 동적 SQL 쿼리 생성
        StringBuilder sql = new StringBuilder("SELECT e.Ssn"); // SSN은 항상 필요 (기본키)

        // 선택된 속성에 따라 SELECT 절 구성
        if (selectedAttributes.contains("Name")) {
            sql.append(", e.Fname, e.Minit, e.Lname");
        }
        if (selectedAttributes.contains("Bdate")) {
            sql.append(", e.Bdate");
        }
        if (selectedAttributes.contains("Address")) {
            sql.append(", e.Address");
        }
        if (selectedAttributes.contains("Sex")) {
            sql.append(", e.Sex");
        }
        if (selectedAttributes.contains("Salary")) {
            sql.append(", e.Salary");
        }
        if (selectedAttributes.contains("Supervisor")) {
            sql.append(", e.Super_ssn");
        }
        if (selectedAttributes.contains("Department")) {
            sql.append(", d.Dname");
        }
        if (selectedAttributes.contains("Modified")) {  // Modified 컬럼 추가
            sql.append(", e.modified");
        }

        // FROM 절과 기본 JOIN 추가
        sql.append(" FROM EMPLOYEE e LEFT JOIN DEPARTMENT d ON e.Dno = d.Dnumber WHERE 1=1");

        // 검색 조건 추가
        if (criteria.containsKey("type") && criteria.containsKey("value")) {
            String type = (String) criteria.get("type");
            String value = (String) criteria.get("value");

            // 검색 타입에 따른 WHERE 조건 추가
            switch (type) {
                case "부서" -> sql.append(" AND d.Dname = ?");
                case "성별" -> sql.append(" AND e.Sex = ?");
                case "급여" -> sql.append(" AND e.Salary >= ?");
            }
        }

        // PreparedStatement 생성 및 실행
        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // 검색 조건 파라미터 설정
            if (criteria.containsKey("type") && criteria.containsKey("value")) {
                String type = (String) criteria.get("type");
                String value = (String) criteria.get("value");

                if ("급여".equals(type)) {
                    pstmt.setDouble(1, Double.parseDouble(value));
                } else {
                    pstmt.setString(1, value);
                }
            }

            // 쿼리 실행 및 결과 처리
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Employee employee = new Employee();
                    employee.setSsn(rs.getString("Ssn")); // 기본키는 항상 설정

                    // 선택된 속성만 설정
                    if (selectedAttributes.contains("Name")) {
                        employee.setFirstName(rs.getString("Fname"));
                        employee.setMinit(rs.getString("Minit").charAt(0));
                        employee.setLastName(rs.getString("Lname"));
                    }
                    if (selectedAttributes.contains("Bdate")) {
                        employee.setBirthDate(rs.getDate("Bdate"));
                    }
                    if (selectedAttributes.contains("Address")) {
                        employee.setAddress(rs.getString("Address"));
                    }
                    if (selectedAttributes.contains("Sex")) {
                        employee.setSex(rs.getString("Sex").charAt(0));
                    }
                    if (selectedAttributes.contains("Salary")) {
                        employee.setSalary(rs.getDouble("Salary"));
                    }
                    if (selectedAttributes.contains("Supervisor")) {
                        employee.setSupervisorSsn(rs.getString("Super_ssn"));
                    }
                    if (selectedAttributes.contains("Department")) {
                        employee.setDepartmentName(rs.getString("Dname"));
                    }
                    if (selectedAttributes.contains("Modified")) {
                        employee.setModified(rs.getTimestamp("modified"));
                    }

                    employees.add(employee);
                }
            }
        }

        return employees;
    }
}