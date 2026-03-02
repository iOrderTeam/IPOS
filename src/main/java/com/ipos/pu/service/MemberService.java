package com.ipos.pu.service;

import com.ipos.pu.model.Member;
import com.ipos.pu.model.MemberStatus;
import com.ipos.pu.model.MemberType;
import com.ipos.pu.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // UC4 - Register a non-commercial member
    public Member registerNonCommercial(String email, String firstName, String lastName) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        String temporaryPassword = UUID.randomUUID().toString().substring(0, 8);

        Member member = new Member();
        member.setEmail(email);
        member.setPassword(passwordEncoder.encode(temporaryPassword));
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setMemberType(MemberType.NON_COMMERCIAL);
        member.setStatus(MemberStatus.ACTIVE);
        member.setPasswordChangeRequired(true);
        member.setOrderCounter(0);

        Member saved = memberRepository.save(member);

        emailService.sendEmail(
                email,
                "Welcome to IPOS-PU - Your Login Credentials",
                "Hello " + firstName + ",\n\n" +
                "Your account has been created.\n" +
                "Email: " + email + "\n" +
                "Temporary password: " + temporaryPassword + "\n\n" +
                "Please log in and change your password immediately."
        );

        return saved;
    }

    // UC2 - Register a commercial member (pending approval)
    public Member registerCommercial(String email, String companyRegistrationNumber,
                                     String directorDetails, String businessType, String address) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        Member member = new Member();
        member.setEmail(email);
        member.setPassword("");
        member.setMemberType(MemberType.COMMERCIAL);
        member.setStatus(MemberStatus.PENDING);
        member.setPasswordChangeRequired(false);
        member.setOrderCounter(0);
        member.setCompanyRegistrationNumber(companyRegistrationNumber);
        member.setDirectorDetails(directorDetails);
        member.setBusinessType(businessType);
        member.setAddress(address);

        return memberRepository.save(member);
    }

    // UC6 - Login
    public Member login(String email, String rawPassword) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (member.getStatus() == MemberStatus.SUSPENDED || member.getStatus() == MemberStatus.INACTIVE) {
            throw new IllegalStateException("This account is suspended or inactive.");
        }

        if (member.getStatus() == MemberStatus.PENDING) {
            throw new IllegalStateException("This account is pending approval.");
        }

        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return member;
    }

    // UC7 - Change password
    public void changePassword(Long memberId, String currentPassword, String newPassword) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));

        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        member.setPassword(passwordEncoder.encode(newPassword));
        member.setPasswordChangeRequired(false);
        memberRepository.save(member);
    }

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }
}
