package com.odmanagement.backend_a.controller;

import com.odmanagement.backend_a.repository.od.OdRepository;
import com.odmanagement.backend_a.repository.FacultyLeaveRepository;
import com.odmanagement.backend_a.repository.FacultyRepository;
import com.odmanagement.backend_a.entity.Faculty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/hod")
@CrossOrigin(origins = "*")
public class HodController {

        @Autowired
        private OdRepository odRepository;

        @Autowired
        private FacultyLeaveRepository leaveRepository;

        @Autowired
        private FacultyRepository facultyRepository;

        @GetMapping("/stats")
        public ResponseEntity<Map<String, Object>> getDashboardStats(@RequestParam("regNo") String regNo) {
                System.out.println("🔍 Debug: HOD Stats Requested for: " + regNo);
                System.out.println("🔍 Debug: HOD Stats Requested for: " + regNo);

                String identifier = regNo;
                String department = "AIML"; // Default fallback

                try {
                        Faculty hod = facultyRepository.findByRegNo(regNo)
                                        .or(() -> facultyRepository.findByName(regNo))
                                        .orElse(null);
                        if (hod != null) {
                                identifier = hod.getName();
                                department = hod.getDepartment();
                                System.out.println("✅ HOD Found: " + identifier + " | Dept: " + department);
                        } else {
                                System.out.println(
                                                "⚠️ HOD Not Found in Faculty Table, using provided string: " + regNo);
                                // Try to guess department if known user
                                if (regNo.contains("Balaji"))
                                        department = "AIML";
                        }
                } catch (Exception e) {
                        System.out.println("❌ Error during HOD lookup: " + e.getMessage());
                }

                // 1. Student ODs
                long pendingStudentODs = odRepository.countByStatus("MENTOR_APPROVED");
                long approvedStudentODs = odRepository.countByHodRegNoAndStatus(identifier, "APPROVED");
                long rejectedStudentODs = odRepository.countByHodRegNoAndStatus(identifier, "REJECTED");

                // 2. Faculty Leaves
                long pendingFacultyLeaves = leaveRepository.countByStatusAndFaculty_Department("PENDING", department);
                long approvedFacultyLeaves = leaveRepository.countByStatusAndFaculty_Department("APPROVED", department);
                long rejectedFacultyLeaves = leaveRepository.countByStatusAndFaculty_Department("REJECTED", department);

                return ResponseEntity.ok(Map.of(
                                "pendingStudentODs", pendingStudentODs,
                                "approvedStudentODs", approvedStudentODs,
                                "rejectedStudentODs", rejectedStudentODs,
                                "pendingFacultyLeaves", pendingFacultyLeaves,
                                "approvedFacultyLeaves", approvedFacultyLeaves,
                                "rejectedFacultyLeaves", rejectedFacultyLeaves));
        }
}
