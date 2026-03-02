package com.ipos.pu.controller;

import com.ipos.pu.dto.*;
import com.ipos.pu.model.Member;
import com.ipos.pu.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // UC4 - Non-commercial registration
    @PostMapping("/register/non-commercial")
    public ResponseEntity<?> registerNonCommercial(@RequestBody NonCommercialRegistrationRequest request) {
        try {
            Member member = memberService.registerNonCommercial(
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName()
            );
            return ResponseEntity.ok(MemberResponse.from(member));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // UC2 - Commercial registration
    @PostMapping("/register/commercial")
    public ResponseEntity<?> registerCommercial(@RequestBody CommercialRegistrationRequest request) {
        try {
            Member member = memberService.registerCommercial(
                    request.getEmail(),
                    request.getCompanyRegistrationNumber(),
                    request.getDirectorDetails(),
                    request.getBusinessType(),
                    request.getAddress()
            );
            return ResponseEntity.ok(MemberResponse.from(member));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // UC6 - Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Member member = memberService.login(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(MemberResponse.from(member));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // UC7 - Change password
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            memberService.changePassword(
                    request.getMemberId(),
                    request.getCurrentPassword(),
                    request.getNewPassword()
            );
            return ResponseEntity.ok("Password changed successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
