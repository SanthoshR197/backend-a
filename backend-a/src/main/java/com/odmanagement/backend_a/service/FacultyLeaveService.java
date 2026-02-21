package com.odmanagement.backend_a.service;

import com.odmanagement.backend_a.entity.Faculty;
import com.odmanagement.backend_a.entity.FacultyLeave;
import com.odmanagement.backend_a.repository.FacultyLeaveRepository;
import com.odmanagement.backend_a.repository.FacultyRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FacultyLeaveService {

    @Autowired
    private FacultyLeaveRepository leaveRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public FacultyLeave applyLeave(String regNo, FacultyLeave request) {
        Faculty faculty = facultyRepository.findByRegNo(regNo)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));

        request.setFaculty(faculty);
        request.setAppliedAt(LocalDateTime.now());
        request.setStatus("PENDING");

        // CL Logic: 1 per month
        if ("CL".equalsIgnoreCase(request.getLeaveType())) {
            // Check if already taken CL in this month
            LocalDate date = request.getFromDate();
            long clsCount = leaveRepository.countClsInMonth(regNo, date.getMonthValue(), date.getYear());
            if (clsCount >= 1) {
                throw new RuntimeException("Casual Leave limit exceeded (Max 1 per month).");
            }
            // For CL, just date is enough. If toDate is missing, set it to fromDate.
            if (request.getToDate() == null) {
                request.setToDate(request.getFromDate());
            }
        }

        FacultyLeave savedLeave = leaveRepository.save(request);

        // Notify HOD
        // Find HOD of the same department
        // If the applicant is HOD, who approves? Auto-approve or Principal?
        // Prompt says "The faculties can also apply leave... application should go to
        // the HOD."
        // We will assume if Role != HOD, send to HOD.
        // If Role == HOD, maybe auto-approve or just save (no approver defined yet).
        if (!"HOD".equalsIgnoreCase(faculty.getRole())) {
            Faculty hod = facultyRepository.findByRoleAndDepartment("HOD", faculty.getDepartment())
                    .orElse(null);

            if (hod != null) {
                // Send Email to HOD
                emailService.sendFacultyLeaveApplicationEmail(savedLeave, hod);
            }
        }

        return savedLeave;
    }

    public List<FacultyLeave> getHistory(String regNo) {
        return leaveRepository.findByFaculty_RegNo(regNo);
    }

    public List<FacultyLeave> getPendingLeavesForHod(String hodIdentifier) {
        // Find HOD department (Try RegNo first, then Name)
        Faculty hod = facultyRepository.findByRegNo(hodIdentifier)
                .or(() -> facultyRepository.findByName(hodIdentifier))
                .orElseThrow(() -> new RuntimeException("HOD not found: " + hodIdentifier));

        // Return all pending leaves where faculty.department == hod.department
        // We can do this in DB or filter in code.
        // Better to add method in Repo: findByStatusAndFaculty_Department
        // For now, let's filter in code or just return all pending if global HOD usage?
        // Let's iterate.
        List<FacultyLeave> pending = leaveRepository.findByStatus("PENDING");
        return pending.stream()
                .filter(l -> l.getFaculty().getDepartment().equals(hod.getDepartment()))
                .filter(l -> !l.getFaculty().getRegNo().equals(hod.getRegNo())) // Don't show own leaves?
                .toList();
    }

    @Transactional
    public FacultyLeave approveLeave(Long leaveId, String hodRegNo) {
        FacultyLeave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        leave.setStatus("APPROVED");
        leave.setDecisionAt(LocalDateTime.now());
        FacultyLeave saved = leaveRepository.save(leave);

        // Notify Faculty
        emailService.sendFacultyLeaveStatusEmail(saved, "APPROVED", null);

        return saved;
    }

    @Transactional
    public FacultyLeave rejectLeave(Long leaveId, String hodRegNo, String reason) {
        FacultyLeave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        leave.setStatus("REJECTED");
        leave.setRejectionReason(reason);
        leave.setDecisionAt(LocalDateTime.now());
        FacultyLeave saved = leaveRepository.save(leave);

        // Notify Faculty
        emailService.sendFacultyLeaveStatusEmail(saved, "REJECTED", reason);

        return saved;
    }
}
