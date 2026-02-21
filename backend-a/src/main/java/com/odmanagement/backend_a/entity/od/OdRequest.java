package com.odmanagement.backend_a.entity.od;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "od_requests")
public class OdRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long odId;

    @Column(nullable = false)
    private String regNo;

    @Column(nullable = false)
    private LocalDate fromDate;

    @Column(nullable = false)
    private LocalDate toDate;

    private String subject;
    private String reason;
    private String attachmentPath;

    @Column(nullable = false)
    private String status;

    private String rejectionReason;

    @Column(nullable = false)
    private LocalDateTime appliedAt;

    private LocalDateTime decisionAt;

    private String approverRegNo; // Legacy/General approver field
    private String mentorRegNo; // Specifically for Mentor tracking
    private String hodRegNo; // Specifically for HOD tracking

    // ---------- GETTERS & SETTERS ----------

    public Long getOdId() {
        return odId;
    }

    public void setOdId(Long odId) {
        this.odId = odId;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) { // 🔴 THIS FIXES setStatus ERROR
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public LocalDateTime getDecisionAt() {
        return decisionAt;
    }

    public void setDecisionAt(LocalDateTime decisionAt) {
        this.decisionAt = decisionAt;
    }

    public String getApproverRegNo() {
        return approverRegNo;
    }

    public void setApproverRegNo(String approverRegNo) {
        this.approverRegNo = approverRegNo;
    }

    public String getMentorRegNo() {
        return mentorRegNo;
    }

    public void setMentorRegNo(String mentorRegNo) {
        this.mentorRegNo = mentorRegNo;
    }

    public String getHodRegNo() {
        return hodRegNo;
    }

    public void setHodRegNo(String hodRegNo) {
        this.hodRegNo = hodRegNo;
    }
}
