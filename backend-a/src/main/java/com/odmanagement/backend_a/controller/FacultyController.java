package com.odmanagement.backend_a.controller;

import com.odmanagement.backend_a.entity.Faculty;
import com.odmanagement.backend_a.repository.FacultyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    @Autowired
    private FacultyRepository facultyRepository;

    @GetMapping("/search")
    public List<Faculty> searchFaculty(@RequestParam(value = "query", required = false) String query) {
        try {
            System.out.println("🔍 [DEBUG] Search Request Received. Query: '" + query + "'");

            if (query == null || query.trim().isEmpty()) {
                System.out.println("⚠️ [DEBUG] Query is empty/null. Returning all.");
                return facultyRepository.findAll(); // Fallback: Return ALL if empty
            }

            List<Faculty> results = facultyRepository.findByNameContainingIgnoreCaseOrRegNoContainingIgnoreCase(query,
                    query);
            System.out.println("✅ [DEBUG] Found " + results.size() + " results for: " + query);
            results.forEach(f -> System.out.println("   -> " + f.getName() + " (" + f.getRegNo() + ")"));

            return results;
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Search Failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
