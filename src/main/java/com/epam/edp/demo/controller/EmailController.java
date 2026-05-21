package com.epam.edp.demo.controller;

import com.epam.edp.demo.dto.ManualEmailRequest;
import com.epam.edp.demo.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

import java.util.Map;

/**
 * REST controller that exposes a manual email-send endpoint backed by AWS SES.
 *
 * <h3>Send a custom email</h3>
 * <pre>
 * POST /api/emails/send
 * Content-Type: application/json
 *
 * {
 *   "to":      "someone@example.com",
 *   "subject": "Test from Travel Agency",
 *   "body":    "Hello! This is a manual test.",
 *   "html":    false
 * }
 * </pre>
 *
 * <h3>Responses</h3>
 * <ul>
 *   <li>200 OK  – email queued by SES</li>
 *   <li>400 Bad Request – missing required fields</li>
 *   <li>502 Bad Gateway – SES rejected the request</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private static final Logger log = LoggerFactory.getLogger(EmailController.class);

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Manually send an email via AWS SES.
     *
     * @param request JSON body containing {@code to}, {@code subject}, {@code body}, and optional {@code html} flag
     * @return 200 with confirmation, or an error response
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendEmail(@RequestBody ManualEmailRequest request) {
        if (request.getTo() == null || request.getTo().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Field 'to' is required"));
        }
        if (request.getSubject() == null || request.getSubject().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Field 'subject' is required"));
        }
        if (request.getBody() == null || request.getBody().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Field 'body' is required"));
        }

        try {
            emailService.sendCustomEmail(request.getTo(), request.getSubject(),
                    request.getBody(), request.isHtml());

            return ResponseEntity.ok(Map.of(
                    "status",  "sent",
                    "to",      request.getTo(),
                    "subject", request.getSubject()
            ));
        } catch (SesV2Exception ex) {
            log.error("AWS SES rejected email to '{}': {} (requestId={})",
                    request.getTo(), ex.getMessage(), ex.requestId(), ex);
            String reqId = ex.requestId() != null ? ex.requestId() : "n/a";
            return ResponseEntity.status(502)
                    .body(Map.of(
                            "error",     "AWS SES rejected the request",
                            "detail",    ex.getMessage() != null ? ex.getMessage() : "unknown",
                            "requestId", reqId
                    ));
        }
    }
}

