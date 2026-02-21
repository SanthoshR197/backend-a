package com.odmanagement.backend_a.service;

import com.odmanagement.backend_a.entity.Faculty;
import com.odmanagement.backend_a.entity.FacultyLeave;
import com.odmanagement.backend_a.entity.Mentor;
import com.odmanagement.backend_a.entity.od.OdRequest;
import com.odmanagement.backend_a.entity.Student;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Async
    public void sendOdApplicationEmail(OdRequest odRequest, Student student, Mentor mentor) {
        if (mentor.getEmail() == null || mentor.getEmail().isEmpty()) {
            logger.warn("⚠️ Mentor {} has no email. Skipping notification.", mentor.getName());
            return;
        }

        String subject = "OD Application - " + student.getStudentName() + " (" + student.getRegNo() + ")";

        String approveLink = baseUrl + "/api/od/verify/approve?odId=" + odRequest.getOdId();
        String rejectLink = baseUrl + "/api/od/verify/reject?odId=" + odRequest.getOdId();

        String htmlContent = String.format(
                """
                        <html>
                        <body>
                            <h2>OD Application Received</h2>
                            <p><strong>Student:</strong> %s (%s)</p>
                            <p><strong>Department:</strong> %s | <strong>Section:</strong> %s</p>
                            <p><strong>Dates:</strong> %s to %s</p>
                            <p><strong>Subject:</strong> %s</p>
                            <p><strong>Reason:</strong> %s</p>
                            <p><strong>Attachment:</strong> %s</p>
                            <br>
                            <p>Please take action:</p>
                            <a href="%s" style="background-color: green; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">APPROVE</a>
                            &nbsp;&nbsp;
                            <a href="%s" style="background-color: red; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">REJECT</a>
                        </body>
                        </html>
                        """,
                student.getStudentName(), student.getRegNo(),
                student.getDepartment(), student.getSection(),
                odRequest.getFromDate(), odRequest.getToDate(),
                odRequest.getSubject(),
                odRequest.getReason(),
                odRequest.getAttachmentPath() != null ? odRequest.getAttachmentPath() : "None",
                approveLink, rejectLink);

        sendEmail(mentor.getEmail(), subject, htmlContent);
    }

    @Async
    public void sendStudentStatusEmail(OdRequest odRequest, Student student, String status, String reason) {
        if (student.getStudentEmail() == null || student.getStudentEmail().isEmpty()) {
            logger.warn("⚠️ Student {} has no email. Skipping status notification.", student.getRegNo());
            return;
        }

        String subject = "OD Application Update - " + status;
        String color = "APPROVED".equalsIgnoreCase(status) ? "green" : "red";

        String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Your OD Application has been <span style="color: %s;">%s</span></h2>
                    <p><strong>Dates:</strong> %s to %s</p>
                    <p><strong>Subject:</strong> %s</p>
                    %s
                </body>
                </html>
                """,
                color, status,
                odRequest.getFromDate(), odRequest.getToDate(),
                odRequest.getSubject(),
                (reason != null && !reason.isEmpty()) ? "<p><strong>Reason for Rejection:</strong> " + reason + "</p>"
                        : "");

        sendEmail(student.getStudentEmail(), subject, htmlContent);
    }

    @Async
    public void sendStudentApplicationCopy(OdRequest odRequest, Student student) {
        if (student.getStudentEmail() == null || student.getStudentEmail().isEmpty()) {
            logger.warn("⚠️ Student {} has no email. Cannot send application copy.", student.getRegNo());
            return;
        }

        String subject = "OD Application Submitted: " + odRequest.getSubject();

        String htmlContent = String.format("""
                <html>
                <body>
                    <h2>OD Application Submitted</h2>
                    <p>You have successfully applied for OD.</p>
                    <p><strong>Subject:</strong> %s</p>
                    <p><strong>Dates:</strong> %s to %s</p>
                    <p><strong>Reason:</strong> %s</p>
                    <p>You will be notified once the mentor reviews your application.</p>
                </body>
                </html>
                """,
                odRequest.getSubject(),
                odRequest.getFromDate(), odRequest.getToDate(),
                odRequest.getReason());

        sendEmail(student.getStudentEmail(), subject, htmlContent);
    }

    @Async
    public void sendHodApprovalEmail(OdRequest odRequest, Student student, com.odmanagement.backend_a.entity.Hod hod) {
        if (hod.getEmail() == null || hod.getEmail().isEmpty()) {
            logger.warn("⚠️ HOD {} has no email. Skipping notification.", hod.getName());
            return;
        }

        String subject = "HOD Approval Required - OD Application - " + student.getStudentName();

        String approveLink = baseUrl + "/api/od/verify/approve?odId=" + odRequest.getOdId();
        String rejectLink = baseUrl + "/api/od/verify/reject?odId=" + odRequest.getOdId();

        String htmlContent = String.format(
                """
                        <html>
                        <body>
                            <h2>OD Application - Mentor Approved</h2>
                            <p><strong>Student:</strong> %s (%s)</p>
                            <p><strong>Department:</strong> %s | <strong>Section:</strong> %s</p>
                            <p><strong>Dates:</strong> %s to %s</p>
                            <p><strong>Subject:</strong> %s</p>
                            <p><strong>Reason:</strong> %s</p>
                            <p><strong>Attachment:</strong> %s</p>
                            <p><strong>Status:</strong> Mentor Approved. Waiting for your approval.</p>
                            <br>
                            <p>Please take action:</p>
                            <a href="%s" style="background-color: green; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">APPROVE</a>
                            &nbsp;&nbsp;
                            <a href="%s" style="background-color: red; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">REJECT</a>
                        </body>
                        </html>
                        """,
                student.getStudentName(), student.getRegNo(),
                student.getDepartment(), student.getSection(),
                odRequest.getFromDate(), odRequest.getToDate(),
                odRequest.getSubject(),
                odRequest.getReason(),
                odRequest.getAttachmentPath() != null ? odRequest.getAttachmentPath() : "None",
                approveLink, rejectLink);

        sendEmail(hod.getEmail(), subject, htmlContent);
    }

    @Async
    public void sendFacultyLeaveApplicationEmail(FacultyLeave leave, Faculty hod) {
        if (hod.getEmail() == null || hod.getEmail().isEmpty()) {
            logger.warn("⚠️ HOD {} has no email. Skipping notification.", hod.getName());
            return;
        }

        String subject = "Leave Application - " + leave.getFaculty().getName();

        // Links for HOD to approve/reject
        String approveLink = baseUrl + "/api/faculty/leave/verify?id=" + leave.getId() + "&action=approve";
        String rejectLink = baseUrl + "/api/faculty/leave/verify?id=" + leave.getId() + "&action=reject";

        String htmlContent = String.format(
                """
                        <html>
                        <body>
                            <h2>Leave Application Received</h2>
                            <p><strong>Faculty:</strong> %s</p>
                            <p><strong>Department:</strong> %s</p>
                            <p><strong>Type:</strong> %s</p>
                            <p><strong>Dates:</strong> %s to %s</p>
                            <p><strong>Reason:</strong> %s</p>
                            <br>
                            <p>Please take action:</p>
                            <a href="%s" style="background-color: green; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">APPROVE</a>
                            &nbsp;&nbsp;
                            <a href="%s" style="background-color: red; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">REJECT</a>
                        </body>
                        </html>
                        """,
                leave.getFaculty().getName(),
                leave.getFaculty().getDepartment(),
                leave.getLeaveType(),
                leave.getFromDate(), leave.getToDate(),
                leave.getReason() != null ? leave.getReason()
                        : (leave.getSubject() != null ? leave.getSubject() : "CL"),
                approveLink, rejectLink);

        // Send to HOD (Actionable)
        sendEmail(hod.getEmail(), subject, htmlContent);

        // Send Copy to Faculty (Read-only)
        if (leave.getFaculty().getEmail() != null && !leave.getFaculty().getEmail().isEmpty()) {
            String facultySubject = "Leave Application Submitted: " + leave.getLeaveType();
            String facultyHtmlContent = String.format(
                    """
                            <html>
                            <body>
                                <h2>Leave Application Submitted</h2>
                                <p>You have successfully applied for leave.</p>
                                <p><strong>Type:</strong> %s</p>
                                <p><strong>Dates:</strong> %s to %s</p>
                                <p><strong>Reason:</strong> %s</p>
                                <p>You will be notified once the HOD reviews your application.</p>
                            </body>
                            </html>
                            """,
                    leave.getLeaveType(),
                    leave.getFromDate(), leave.getToDate(),
                    leave.getReason() != null ? leave.getReason()
                            : (leave.getSubject() != null ? leave.getSubject() : "CL"));

            sendEmail(leave.getFaculty().getEmail(), facultySubject, facultyHtmlContent);
        }
    }

    @Async
    public void sendFacultyLeaveStatusEmail(FacultyLeave leave, String status, String reason) {
        if (leave.getFaculty().getEmail() == null || leave.getFaculty().getEmail().isEmpty()) {
            logger.warn("⚠️ Faculty {} has no email. Skipping status notification.", leave.getFaculty().getName());
            return;
        }

        String subject = "Leave Application Update - " + status;
        String color = "APPROVED".equalsIgnoreCase(status) ? "green" : "red";

        String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Your Leave Application has been <span style="color: %s;">%s</span></h2>
                    <p><strong>Type:</strong> %s</p>
                    <p><strong>Dates:</strong> %s to %s</p>
                    %s
                </body>
                </html>
                """,
                color, status,
                leave.getLeaveType(),
                leave.getFromDate(), leave.getToDate(),
                (reason != null && !reason.isEmpty()) ? "<p><strong>Reason for Rejection:</strong> " + reason + "</p>"
                        : "");

        sendEmail(leave.getFaculty().getEmail(), subject, htmlContent);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        sendEmail(to, null, subject, htmlContent);
    }

    private void sendEmail(String to, String cc, String subject, String htmlContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            if (cc != null && !cc.isEmpty()) {
                helper.setCc(cc);
            }
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            logger.info("📧 Email sent to {} (CC: {})", to, cc);
        } catch (MessagingException e) {
            logger.error("❌ Failed to send email to {}: {}", to, e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Unexpected error sending email to {}: {}", to, e.getMessage());
        }
    }
}
