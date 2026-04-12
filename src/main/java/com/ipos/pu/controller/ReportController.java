package com.ipos.pu.controller;

import com.ipos.pu.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales")
    public ResponseEntity<Map<String, Object>> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateSalesReport(startDate, endDate));
    }

    @GetMapping("/campaigns")
    public ResponseEntity<Map<String, Object>> getCampaignReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateCampaignReport(startDate, endDate));
    }

    @GetMapping("/campaigns/{campaignId}/engagement")
    public ResponseEntity<Map<String, Object>> getCampaignEngagementReport(
            @PathVariable Long campaignId) {
        return ResponseEntity.ok(reportService.generateCampaignEngagementReport(campaignId));
    }
}
