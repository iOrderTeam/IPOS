package com.ipos.pu.ui.controller;

import com.ipos.pu.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/pending")
    public ResponseEntity<?>  getPending() {
        return ResponseEntity.ok(adminService.getPendingApplications());
    }

    @PostMapping("/approve/{memberId}")
    public ResponseEntity<?>  approve(@PathVariable Long memberId, @RequestParam String temporaryPassword) {
        try {
            adminService.approveMember(memberId, temporaryPassword);
            return ResponseEntity.ok("Member approved");
        }  catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reject/{memberId}")
    public ResponseEntity<?> reject(@PathVariable Long memberId) {
        try {
            adminService.rejectMember(memberId);
            return ResponseEntity.ok("Member rejected.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/campaigns")
    public ResponseEntity<?> createCampaign(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(adminService.createCampaign(
                    body.get("name"),
                    body.get("description"),
                    Double.parseDouble(body.get("discountPercentage")),
                    LocalDate.parse(body.get("startDate")),
                    LocalDate.parse(body.get("endDate"))
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/campaigns/{id}")
    public ResponseEntity<?> deleteCampaign(@PathVariable Long id) {
        adminService.deleteCampaign(id);
        return ResponseEntity.ok("Campaign deleted.");
    }

    @GetMapping("/campaigns/active")
    public ResponseEntity<?> getActiveCampaigns() {
        return ResponseEntity.ok(adminService.getActiveCampaigns());
    }

    @PostMapping("/campaigns/{id}/hit")
    public ResponseEntity<?> incrementHit(@PathVariable Long id) {
        try {
            adminService.incrementCampaignHits(id);
            return ResponseEntity.ok("Hit recorded.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
