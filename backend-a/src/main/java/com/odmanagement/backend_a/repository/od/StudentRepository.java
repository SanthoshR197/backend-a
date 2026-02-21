package com.odmanagement.backend_a.repository.od;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.odmanagement.backend_a.entity.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    @Query("SELECT DISTINCT s.department FROM Student s")
    List<String> findDistinctDepartments();

    @Query("SELECT DISTINCT s.year FROM Student s")
    List<String> findDistinctYears();

    @Query("SELECT DISTINCT s.semester FROM Student s")
    List<String> findDistinctSemesters();

    @Query("SELECT DISTINCT s.section FROM Student s")
    List<String> findDistinctSections();

    // This allows the Controller to find your 69 students
    List<Student> findByStudentNameContainingIgnoreCaseOrRegNoContainingIgnoreCase(String name, String regNo);
}