package com.odmanagement.backend_a.entity;

import jakarta.persistence.*;
import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @Column(name = "reg_no")
    private String regNo;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "dob")
    private String dob;

    @Column(name = "gender")
    private String gender;

    @Column(name = "student_mobile")
    private String studentMobile;

    @Column(name = "student_email")
    private String studentEmail;

    @Column(name = "student_whatsapp")
    private String studentWhatsapp;

    @Column(name = "father_name")
    private String fatherName;

    @Column(name = "father_mobile")
    private String fatherMobile;

    @Column(name = "mother_name")
    private String motherName;

    @Column(name = "mother_mobile")
    private String motherMobile;

    @Column(name = "district")
    private String district;

    @Column(name = "state")
    private String state;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "department")
    private String department = "CSE-AIML";

    @Column(name = "section")
    private String section = "A";

    @Column(name = "year")
    private String year = "3rd";

    @Column(name = "semester")
    private String semester = "6";

    // ===== Constructors =====
    public Student() {
    }

    // ===== Getters & Setters =====

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStudentMobile() {
        return studentMobile;
    }

    public void setStudentMobile(String studentMobile) {
        this.studentMobile = studentMobile;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getStudentWhatsapp() {
        return studentWhatsapp;
    }

    public void setStudentWhatsapp(String studentWhatsapp) {
        this.studentWhatsapp = studentWhatsapp;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getFatherMobile() {
        return fatherMobile;
    }

    public void setFatherMobile(String fatherMobile) {
        this.fatherMobile = fatherMobile;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getMotherMobile() {
        return motherMobile;
    }

    public void setMotherMobile(String motherMobile) {
        this.motherMobile = motherMobile;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }
}
