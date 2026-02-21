package com.odmanagement.backend_a.controller.od;

import com.odmanagement.backend_a.service.od.OdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/od/verify")
public class OdActionController {

    @Autowired
    private OdService odService;

    @GetMapping(value = "/approve", produces = MediaType.TEXT_HTML_VALUE)
    public String approveOd(@RequestParam("odId") Long odId) {
        try {
            com.odmanagement.backend_a.entity.od.OdRequest od = odService.approveOd(odId, "EMAIL_ACTION");

            if ("MENTOR_APPROVED".equals(od.getStatus())) {
                return """
                        <html>
                        <body style="font-family: Arial, sans-serif; text-align: center; padding: 50px;">
                            <h1 style="color: green;">Mentor Approval Successful!</h1>
                            <p>The request has been forwarded to the HOD.</p>
                        </body>
                        </html>
                        """;
            } else {
                return """
                        <html>
                        <body style="font-family: Arial, sans-serif; text-align: center; padding: 50px;">
                            <h1 style="color: green;">OD Approved Successfully!</h1>
                            <p>The student has been notified.</p>
                        </body>
                        </html>
                        """;
            }
        } catch (Exception e) {
            return "<html><body><h1 style='color: red;'>Error: " + e.getMessage() + "</h1></body></html>";
        }
    }

    @GetMapping(value = "/reject", produces = MediaType.TEXT_HTML_VALUE)
    public String rejectOdPage(@RequestParam("odId") Long odId) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; text-align: center; padding: 50px;">
                    <h1>Reject OD Application</h1>
                    <form action="/api/od/verify/reject-confirm" method="POST" style="max-width: 400px; margin: auto;">
                        <input type="hidden" name="odId" value="%d">
                        <textarea name="reason" placeholder="Enter reason for rejection..." required style="width: 100%%; height: 100px; padding: 10px; margin-bottom: 20px;"></textarea>
                        <br>
                        <button type="submit" style="background-color: red; color: white; padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer;">Submit Rejection</button>
                    </form>
                </body>
                </html>
                """
                .formatted(odId);
    }

    @PostMapping(value = "/reject-confirm", produces = MediaType.TEXT_HTML_VALUE)
    public String rejectOdConfirm(@RequestParam("odId") Long odId, @RequestParam("reason") String reason) {
        try {
            odService.rejectOd(odId, "EMAIL_ACTION", reason);
            return """
                    <html>
                    <body style="font-family: Arial, sans-serif; text-align: center; padding: 50px;">
                        <h1 style="color: red;">OD Rejected</h1>
                        <p>The rejection reason has been recorded and the student notified.</p>
                    </body>
                    </html>
                    """;
        } catch (Exception e) {
            return "<html><body><h1 style='color: red;'>Error: " + e.getMessage() + "</h1></body></html>";
        }
    }
}
