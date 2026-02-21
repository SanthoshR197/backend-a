package com.odmanagement.backend_a.controller;

import com.odmanagement.backend_a.entity.FacultyLeave;
import com.odmanagement.backend_a.service.FacultyLeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faculty/leave")
public class FacultyLeaveController {

    @Autowired
    private FacultyLeaveService leaveService;

    @Autowired
    private com.odmanagement.backend_a.service.CloudStorageService cloudStorageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAttachment(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            String url = cloudStorageService.uploadFile(file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (java.io.IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "File upload failed: " + e.getMessage()));
        }
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyLeave(@RequestParam("regNo") String regNo, @RequestBody FacultyLeave request) {
        try {
            FacultyLeave saved = leaveService.applyLeave(regNo, request);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<FacultyLeave>> getHistory(@RequestParam("regNo") String regNo) {
        return ResponseEntity.ok(leaveService.getHistory(regNo));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<FacultyLeave>> getPendingLeaves() {
        String hodRegNo = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return ResponseEntity.ok(leaveService.getPendingLeavesForHod(hodRegNo));
    }

    @PostMapping("/approve/{leaveId}")
    public ResponseEntity<?> approveLeave(@PathVariable("leaveId") Long leaveId) {
        String hodRegNo = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        try {
            FacultyLeave approved = leaveService.approveLeave(leaveId, hodRegNo);
            return ResponseEntity.ok(approved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reject/{leaveId}")
    public ResponseEntity<?> rejectLeave(@PathVariable("leaveId") Long leaveId,
            @RequestParam("reason") String reason) {
        String hodRegNo = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        try {
            FacultyLeave rejected = leaveService.rejectLeave(leaveId, hodRegNo, reason);
            return ResponseEntity.ok(rejected);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Endpoint for email links to verify (GET or POST)
    @GetMapping(value = "/verify", produces = MediaType.TEXT_HTML_VALUE)
    public String verifyFromEmail(@RequestParam("id") Long id, @RequestParam("action") String action) {
        try {
            if ("approve".equalsIgnoreCase(action)) {
                leaveService.approveLeave(id, "EMAIL_ACTION");
                return """
                        <html>
                        <body style="font-family: Arial, sans-serif; text-align: center; padding: 50px;">
                            <h1 style="color: green;">Leave Approved Successfully!</h1>
                            <p>The faculty has been notified.</p>
                        </body>
                        </html>
                        """;
            } else if ("reject".equalsIgnoreCase(action)) {
                return """
                        <html>
                        <body style="font-family: Arial, sans-serif; text-align: center; padding: 50px;">
                            <h1>Reject Leave Application</h1>
                            <form action="/api/faculty/leave/verify/reject-confirm" method="POST" style="max-width: 400px; margin: auto;">
                                <input type="hidden" name="id" value="%d">
                                <textarea name="reason" placeholder="Enter reason for rejection..." required style="width: 100%%; height: 100px; padding: 10px; margin-bottom: 20px;"></textarea>
                                <br>
                                <button type="submit" style="background-color: red; color: white; padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer;">Submit Rejection</button>
                            </form>
                        </body>
                        </html>
                        """
                        .formatted(id);
            } else {
                return "<html><body><h1 style='color: red;'>Invalid Action</h1></body></html>";
            }
        } catch (Exception e) {
            return "<html><body><h1 style='color: red;'>Error: " + e.getMessage() + "</h1></body></html>";
        }
    }

    @PostMapping(value = "/verify/reject-confirm", produces = MediaType.TEXT_HTML_VALUE)
    public String rejectLeaveConfirm(@RequestParam("id") Long id, @RequestParam("reason") String reason) {
        try {
            leaveService.rejectLeave(id, "EMAIL_ACTION", reason);
            return """
                    <html>
                    <body style="font-family: Arial, sans-serif; text-align: center; padding: 50px;">
                        <h1 style="color: red;">Leave Rejected</h1>
                        <p>The rejection reason has been recorded and the faculty notified.</p>
                    </body>
                    </html>
                    """;
        } catch (Exception e) {
            return "<html><body><h1 style='color: red;'>Error: " + e.getMessage() + "</h1></body></html>";
        }
    }
}
