package com.ipos.pu.controller;

import com.ipos.pu.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailApiController {
    private final EmailService emailService;

    public EmailApiController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<?> sendEmail(@RequestBody Map<String, String> body) {
        try {
            emailService.sendEmail(body.get("to"), body.get("subject"), body.get("body"));
            return ResponseEntity.ok("Email sent.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
