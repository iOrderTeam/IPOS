package com.ipos.pu.controller;

import com.ipos.pu.dto.RelayEmailRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/integration-sa")
public class IntegrationSaRelayEmailController {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public IntegrationSaRelayEmailController(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String mailUsername) {
        this.mailSender = mailSender;
        this.fromAddress = mailUsername != null ? mailUsername.trim() : "";
    }

    @PostMapping("/relay-email")
    public ResponseEntity<Void> relayEmail(@Valid @RequestBody RelayEmailRequest body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (!fromAddress.isEmpty()) {
                message.setFrom(fromAddress);
            }
            message.setTo(body.getTo());
            message.setSubject(body.getSubject());
            message.setText(body.getBody());
            mailSender.send(message);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}
