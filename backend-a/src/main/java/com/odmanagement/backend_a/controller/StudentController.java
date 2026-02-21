package com.odmanagement.backend_a.controller;

import com.odmanagement.backend_a.entity.Student;
import com.odmanagement.backend_a.repository.od.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/search")
    public List<Student> searchStudents(@RequestParam(name = "query", required = false) String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return studentRepository.findByStudentNameContainingIgnoreCaseOrRegNoContainingIgnoreCase(query, query);
    }

    @GetMapping("/demographics")
    public java.util.Map<String, List<String>> getDemographics() {
        return java.util.Map.of(
                "departments", studentRepository.findDistinctDepartments(),
                "years", studentRepository.findDistinctYears(),
                "semesters", studentRepository.findDistinctSemesters(),
                "sections", studentRepository.findDistinctSections());
    }
}
