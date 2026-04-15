package com.ipos.pu.controller;

import com.ipos.pu.config.IposPuIntegrationProperties;
import com.ipos.pu.dto.SaCommercialDecisionPayload;
import com.ipos.pu.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/integration-pu")
public class SaCommercialWebhookController {

    private static final Pattern PU_MEMBER_REF = Pattern.compile("^PU-MEMBER-(\\d+)$", Pattern.CASE_INSENSITIVE);

    private final IposPuIntegrationProperties properties;
    private final MemberService memberService;

    public SaCommercialWebhookController(IposPuIntegrationProperties properties, MemberService memberService) {
        this.properties = properties;
        this.memberService = memberService;
    }

    @PostMapping("/sa-decision")
    public ResponseEntity<Void> handleDecision(
            @RequestBody SaCommercialDecisionPayload body,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (!validateBearer(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<Long> memberId = parseMemberId(body.getExternalReferenceId());
        if (memberId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        String status = body.getStatus() != null ? body.getStatus().trim().toUpperCase() : "";
        try {
            if ("APPROVED".equals(status)) {
                memberService.onCommercialApplicationApprovedFromSa(memberId.get(), body.getEmailBody());
            } else if ("REJECTED".equals(status)) {
                memberService.onCommercialApplicationRejectedFromSa(memberId.get(), body.getRejectionReason());
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    private boolean validateBearer(String authorizationHeader) {
        String secret = properties.getWebhookBearerSecret();
        if (secret == null || secret.isBlank()) {
            return true;
        }
        if (authorizationHeader == null || authorizationHeader.length() < 7) {
            return false;
        }
        if (!authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return false;
        }
        String token = authorizationHeader.substring(7).trim();
        return secret.equals(token);
    }

    private static Optional<Long> parseMemberId(String externalReferenceId) {
        if (externalReferenceId == null || externalReferenceId.isBlank()) {
            return Optional.empty();
        }
        Matcher m = PU_MEMBER_REF.matcher(externalReferenceId.trim());
        if (!m.matches()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(m.group(1)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
