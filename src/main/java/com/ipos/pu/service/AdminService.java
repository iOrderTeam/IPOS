package com.ipos.pu.service;

import com.ipos.pu.model.*;
import com.ipos.pu.repository.*;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final CampaignRepository campaignRepository;
    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public AdminService(MemberRepository memberRepository,
                        CampaignRepository campaignRepository,
                        EmailService emailService,
                        PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.campaignRepository = campaignRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }
    // UC3 - Get all pending commercial applications
    public List<Member> getPendingApplications() {
        return memberRepository.findAll().stream()
                .filter(m -> m.getStatus() == MemberStatus.PENDING)
                .collect(Collectors.toList());
    }
    // UC3 - Approve a commercial member
    public void approveMember(Long memberId, String temporaryPassword) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));

        member.setStatus(MemberStatus.ACTIVE);
        member.setPassword(passwordEncoder.encode(temporaryPassword));
        member.setPasswordChangeRequired(true);
        memberRepository.save(member);

        emailService.sendEmail(
                member.getEmail(),
                "Your IPOS-PU Application Has Been Approved",
                "Your commercial membership has been approved.\n" +
                        "Temporary password: " + temporaryPassword + "\n" +
                        "Please log in and change your password immediately."
        );
    }
    // UC3 - Reject a commercial member
    public void rejectMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));

        member.setStatus(MemberStatus.INACTIVE);
        memberRepository.save(member);

        emailService.sendEmail(
                member.getEmail(),
                "Your IPOS-PU Application Was Not Approved",
                "Unfortunately your commercial membership application was not approved.\n" +
                        "Please contact us if you have any questions."
        );
    }
    // UC18 - Create a campaign
    public Campaign createCampaign(String name, String description,
                                   double discountPercentage,
                                   LocalDate startDate, LocalDate endDate) {
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }

        Campaign campaign = new Campaign();
        campaign.setName(name);
        campaign.setDescription(description);
        campaign.setDiscountPercentage(discountPercentage);
        campaign.setStartDate(startDate);
        campaign.setEndDate(endDate);
        campaign.setHits(0);
        return campaignRepository.save(campaign);
    }

    // UC18 - Delete a campaign
    public void deleteCampaign(Long campaignId) {
        campaignRepository.deleteById(campaignId);
    }

    // UC10 - Get currently active campaigns
    public List<Campaign> getActiveCampaigns() {
        LocalDate today = LocalDate.now();
        return campaignRepository.findByStartDateBeforeAndEndDateAfter(today, today);
    }

    // UC12 - Increment campaign hits counter
    public void incrementCampaignHits(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));
        campaign.setHits(campaign.getHits() + 1);
        campaignRepository.save(campaign);
    }
}
