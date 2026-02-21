package com.odmanagement.backend_a.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "faculty_leaves")
@Data
public class FacultyLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @Column(nullable = false)
    private String leaveType; // OD, CL

    @Column(nullable = false)
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate fromDate;

    // For CL, toDate can be same as fromDate or handled via logic.
    // Prompt says "for cl just the date is enough". We can store fromDate=toDate
    // for 1 day CL.
    @Column(nullable = false)
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate toDate;

    // Subject/Reason for OD
    private String subject;
    private String reason;

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED

    private String rejectionReason;

    private String documentUrl;

    @Column(nullable = false)
    private LocalDateTime appliedAt;

    private LocalDateTime decisionAt;
}
