package com.odmanagement.backend_a.controller.od;

import com.odmanagement.backend_a.entity.Student;
import com.odmanagement.backend_a.entity.od.OdRequest;
import com.odmanagement.backend_a.repository.od.StudentRepository;
import com.odmanagement.backend_a.service.od.OdService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/od")
@CrossOrigin(origins = "http://localhost:5173")
public class OdController {

    @Autowired
    private OdService odService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private com.odmanagement.backend_a.repository.od.OdRepository odRepository;

    // 1️⃣ Get all students
    @GetMapping("/students")
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Autowired
    private com.odmanagement.backend_a.service.CloudStorageService cloudStorageService;

    // 2️⃣ Student applies OD
    @PostMapping("/apply")
    public OdRequest applyOd(@RequestBody OdRequest request) {
        return odService.applyOd(request);
    }

    // 2️⃣b Upload Attachment
    @PostMapping("/upload")
    public String uploadAttachment(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            return cloudStorageService.uploadFile(file);
        } catch (java.io.IOException e) {
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }

    // 3️⃣ Get OD history of the logged-in student
    @GetMapping("/history")
    public List<OdRequest> getMyHistory() {
        String regNo = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getName();
        return odService.getStudentHistory(regNo);
    }

    // 3️⃣b Get OD history of a specific student (Admin/Mentor use)
    @GetMapping("/history/{regNo}")
    public List<OdRequest> getStudentHistory(@PathVariable String regNo) {
        return odService.getStudentHistory(regNo);
    }

    // 4️⃣ Mentor view pending ODs
    @GetMapping("/pending")
    public List<OdRequest> getPendingOds() {
        return odService.getPendingOds();
    }

    // 4️⃣a Mentor view approval history
    @GetMapping("/history/mentor")
    public List<OdRequest> getMentorHistory() {
        String regNo = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getName();
        if (regNo != null)
            regNo = regNo.trim();
        return odService.getMentorHistory(regNo);
    }

    // 4️⃣b HOD view pending ODs (Approved by Mentor)
    @GetMapping("/pending-hod")
    public List<OdRequest> getHodPendingOds() {
        return odService.getHodPendingOds();
    }

    // 5️⃣ Mentor approves OD
    @PutMapping("/approve/{odId}")
    public OdRequest approveOd(@PathVariable("odId") Long odId) {
        String regNo = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getName();
        if (regNo != null)
            regNo = regNo.trim();
        return odService.approveOd(odId, regNo);
    }

    // 6️⃣ Mentor rejects OD
    @PutMapping("/reject/{odId}")
    public OdRequest rejectOd(
            @PathVariable("odId") Long odId,
            @RequestParam("reason") String reason) {
        String regNo = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getName();
        if (regNo != null)
            regNo = regNo.trim();
        return odService.rejectOd(odId, regNo, reason);
    }

    // 77️⃣ Mentor Stats (Corrected)
    @GetMapping("/mentor/stats")
    public java.util.Map<String, Long> getMentorStats() {
        String regNo = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getName();
        if (regNo != null)
            regNo = regNo.trim();

        // Delegate to Service
        return odService.getMentorStats(regNo);
    }

    @GetMapping("/faculty/check")
    public java.util.List<com.odmanagement.backend_a.entity.Student> checkStudentODs(
            @RequestParam(name = "date", required = false) String dateStr,
            @RequestParam("dept") String dept,
            @RequestParam("year") String year,
            @RequestParam("sem") String sem,
            @RequestParam("sec") String sec) {

        java.time.LocalDate date = (dateStr != null && !dateStr.isEmpty())
                ? java.time.LocalDate.parse(dateStr)
                : java.time.LocalDate.now();

        return odRepository.findStudentsWithOdOnDate(date, dept, year, sem, sec);
    }
}
