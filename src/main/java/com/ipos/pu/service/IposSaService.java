package com.ipos.pu.service;

import com.ipos.pu.model.Member;
import org.springframework.stereotype.Service;

@Service
public class IposSaService {

    // UC3 - Submit commercial application to IPOS-SA for review
    public void submitCommercialApplication(Member member) {
        // MOCK: In Week 5, replace this with a real HTTP call to the IPOS-SA team's endpoint
        System.out.println("=== IPOS-SA MOCK ===");
        System.out.println("Submitting application to IPOS-SA:");
        System.out.println("  Email: " + member.getEmail());
        System.out.println("  Company Reg: " + member.getCompanyRegistrationNumber());
        System.out.println("  Business Type: " + member.getBusinessType());
        System.out.println("  Address: " + member.getAddress());
        System.out.println("===================");

        // TODO Week 5 — replace above with:
        // RestTemplate restTemplate = new RestTemplate();
        // restTemplate.postForObject("http://[IPOS-SA-IP]:8081/api/applications", member, String.class);
    }
}