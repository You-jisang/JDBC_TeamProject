// Model/Employee.java
package org.example.model;


import java.sql.Timestamp;
import java.util.Date;

/**
 * 직원 정보를 담는 모델 클래스
 * EMPLOYEE 테이블의 각 컬럼에 대응되는 필드들을 포함
 */
public class Employee {
    private String firstName;    // 이름
    private char minit;         // 중간 이름 이니셜
    private String lastName;    // 성
    private String ssn;         // 사회보장번호 (PK)
    private Date birthDate;     // 생년월일
    private String address;     // 주소
    private char sex;          // 성별
    private double salary;     // 급여
    private String supervisorSsn; // 상사의 SSN (FK)
    private int departmentNumber; // 부서 번호 (FK)
    private String departmentName; // 부서명 (JOIN 결과 저장용)
    private Timestamp modified;

    // 기본 생성자
    public Employee() {
    }

    // 전체 필드 생성자
    public Employee(String firstName, char minit, String lastName, String ssn,
                    Date birthDate, String address, char sex, double salary,
                    String supervisorSsn, int departmentNumber) {
        this.firstName = firstName;
        this.minit = minit;
        this.lastName = lastName;
        this.ssn = ssn;
        this.birthDate = birthDate;
        this.address = address;
        this.sex = sex;
        this.salary = salary;
        this.supervisorSsn = supervisorSsn;
        this.departmentNumber = departmentNumber;
        this.modified = getModified();
    }

    // Getter/Setter 메서드들
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public char getMinit() {
        return minit;
    }

    public void setMinit(char minit) {
        this.minit = minit;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public char getSex() {
        return sex;
    }

    public void setSex(char sex) {
        this.sex = sex;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getSupervisorSsn() {
        return supervisorSsn;
    }

    public void setSupervisorSsn(String supervisorSsn) {
        this.supervisorSsn = supervisorSsn;
    }

    public int getDepartmentNumber() {
        return departmentNumber;
    }

    public void setDepartmentNumber(int departmentNumber) {
        this.departmentNumber = departmentNumber;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Timestamp getModified() {
        return modified;
    }

    public void setModified(Timestamp modified) {
        this.modified = modified;
    }
}