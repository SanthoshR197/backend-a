package com.odmanagement.backend_a.service.od;

import com.odmanagement.backend_a.entity.od.OdRequest;
import com.odmanagement.backend_a.entity.Student;
import com.odmanagement.backend_a.entity.Mentor;
import com.odmanagement.backend_a.repository.od.OdRepository;
import com.odmanagement.backend_a.repository.od.StudentRepository;
import com.odmanagement.backend_a.repository.MentorRepository;
import com.odmanagement.backend_a.entity.Hod;
import com.odmanagement.backend_a.repository.HodRepository;
import com.odmanagement.backend_a.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OdService {

    @Autowired
    private OdRepository odRepo;

    @Autowired
    private StudentRepository studentRepo;

    @Autowired
    private MentorRepository mentorRepo;

    @Autowired
    private HodRepository hodRepo;

    @Autowired
    private EmailService emailService;

    // Student applies OD
    public OdRequest applyOd(OdRequest request) {
        request.setStatus("PENDING");
        request.setAppliedAt(LocalDateTime.now());
        request.setDecisionAt(null);
        request.setRejectionReason(null);

        OdRequest savedOd = odRepo.save(request);

        // Fetch Student & Mentor details for Email
        Student student = studentRepo.findById(request.getRegNo()).orElse(null);
        // Use findFirstByPriority to handle potential duplicates
        Mentor mentor = mentorRepo.findFirstByPriority(1).orElse(null);

        if (student != null) {
            // Send copy to student
            emailService.sendStudentApplicationCopy(savedOd, student);

            if (mentor != null) {
                // Send email to 1st Mentor
                emailService.sendOdApplicationEmail(savedOd, student, mentor);
            }
        }

        return savedOd;
    }

    // Student OD history
    public List<OdRequest> getStudentHistory(String regNo) {
        return odRepo.findByRegNoOrderByOdIdDesc(regNo);
    }

    // Mentor views pending ODs
    public List<OdRequest> getPendingOds() {
        return odRepo.findByStatusOrderByOdIdDesc("PENDING");
    }

    // Mentor views approval history
    public List<OdRequest> getMentorHistory(String regNo) {
        return odRepo.findByMentorRegNoOrderByOdIdDesc(regNo);
    }

    // HOD views pending ODs (Mentor Approved)
    public List<OdRequest> getHodPendingOds() {
        return odRepo.findByStatusOrderByOdIdDesc("MENTOR_APPROVED");
    }

    // Mentor/HOD approves OD
    public OdRequest approveOd(Long odId, String approverRegNo) {
        OdRequest od = odRepo.findById(odId)
                .orElseThrow(() -> new RuntimeException("OD not found"));

        if ("PENDING".equals(od.getStatus())) {
            // Mentor Approval -> Move to HOD
            od.setStatus("MENTOR_APPROVED");
            od.setDecisionAt(LocalDateTime.now());
            od.setApproverRegNo(approverRegNo); // Legacy
            od.setMentorRegNo(approverRegNo); // Specifically for Mentor tracking
            odRepo.save(od);

            // Notify HOD
            Student student = studentRepo.findById(od.getRegNo()).orElse(null);
            // Assuming 1 HOD for now, or fetch by department, handling duplicates
            Hod hod = hodRepo.findFirstByDepartment("AIML").orElse(
                    hodRepo.findAll().stream().findFirst().orElse(null));

            if (student != null && hod != null) {
                emailService.sendHodApprovalEmail(od, student, hod);
            }

        } else if ("MENTOR_APPROVED".equals(od.getStatus())) {
            // HOD Approval -> Final Approve
            od.setStatus("APPROVED");
            od.setDecisionAt(LocalDateTime.now());
            od.setHodRegNo(approverRegNo); // Specifically for HOD tracking
            od.setApproverRegNo(approverRegNo); // Legacy field
            odRepo.save(od);

            // Notify Student
            Student student = studentRepo.findById(od.getRegNo()).orElse(null);
            if (student != null) {
                emailService.sendStudentStatusEmail(od, student, "APPROVED", null);
            }
        }

        return od;
    }

    // Mentor rejects OD
    public OdRequest rejectOd(Long odId, String approverRegNo, String reason) {
        OdRequest od = odRepo.findById(odId)
                .orElseThrow(() -> new RuntimeException("OD not found"));

        od.setStatus("REJECTED");
        od.setRejectionReason(reason);
        od.setDecisionAt(LocalDateTime.now());
        od.setApproverRegNo(approverRegNo); // Legacy

        // Check if rejecter is HOD or Mentor (simple check for now)
        if (approverRegNo.contains("HOD") || approverRegNo.equals("Balaji Ganesh R")) {
            od.setHodRegNo(approverRegNo);
        } else {
            od.setMentorRegNo(approverRegNo);
        }

        OdRequest savedOd = odRepo.save(od);
        // ... notify

        // Notify Student
        Student student = studentRepo.findById(od.getRegNo()).orElse(null);
        if (student != null) {
            emailService.sendStudentStatusEmail(savedOd, student, "REJECTED", reason);
        }

        return savedOd;
    }

    // Mentor Stats
    public java.util.Map<String, Long> getMentorStats(String mentorRegNo) {
        // Pending: All PENDING requests (assuming 1 global mentor for now as per app
        // logic)
        // OR filtering by mentorRegNo if we had assigned mentors.
        // Current getPendingOds() returns all PENDING.
        long pending = odRepo.countByStatus("PENDING");

        // Approved by this Mentor: MENTOR_APPROVED or APPROVED (since approval chain
        // includes mentor)
        // REJECTED by this Mentor: countByMentorRegNoAndStatus("REJECTED") - but this
        // might include HOD rejections
        // Simplified: Approved = MENTOR_APPROVED + APPROVED where mentorRegNo matches.

        long approved = odRepo.countByMentorRegNoAndStatusIn(mentorRegNo,
                java.util.Arrays.asList("MENTOR_APPROVED", "APPROVED"));

        // We can also count specific mentor rejections if needed, but "Approved" was
        // the user's specific ask.
        // Let's just return these for now.

        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("pending", pending);
        stats.put("approved", approved);

        return stats;
    }
}
