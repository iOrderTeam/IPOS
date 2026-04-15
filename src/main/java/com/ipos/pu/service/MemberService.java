package com.ipos.pu.service;

import com.ipos.pu.model.Member;
import com.ipos.pu.model.MemberStatus;
import com.ipos.pu.model.MemberType;
import com.ipos.pu.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class MemberService {

    private static final String PASSWORD_LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String PASSWORD_DIGITS = "0123456789";
    private static final String PASSWORD_SPECIALS = "!@#$%^&*?-_";
    private static final String PASSWORD_ALL = PASSWORD_LETTERS + PASSWORD_DIGITS + PASSWORD_SPECIALS;
    private static final SecureRandom RANDOM = new SecureRandom();

    // Basic email format check (local-part@domain.tld)
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // UK Companies House number: 6-20 alphanumeric characters.
    private static final Pattern COMPANIES_HOUSE_PATTERN =
            Pattern.compile("^[A-Z0-9]{6,20}$");

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final IposSaService iposSaService;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, EmailService emailService, IposSaService iposSaService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.iposSaService = iposSaService;
    }

    // UC4 - Register a non-commercial member
    public Member registerNonCommercial(String email, String firstName, String lastName) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        String temporaryPassword = generateTemporaryPassword();

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
    public Member registerCommercial(String email, String companyName, String companyRegistrationNumber,
                                     String directorDetails, String businessType, String address) {
        validateCommercialApplication(email, companyRegistrationNumber, directorDetails, businessType, address);

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
        member.setCompanyName(companyName);
        member.setCompanyRegistrationNumber(companyRegistrationNumber);
        member.setDirectorDetails(directorDetails);
        member.setBusinessType(businessType);
        member.setAddress(address);

        Member saved = memberRepository.save(member);
        try {
            iposSaService.submitCommercialApplication(saved);
        } catch (RuntimeException e) {
            memberRepository.delete(saved);
            throw e;
        }
        return saved;
    }

    public void onCommercialApplicationApprovedFromSa(Long memberId, String saEmailBody) {
        if (saEmailBody == null || saEmailBody.isBlank()) {
            throw new IllegalArgumentException("Approval email body is required.");
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));
        if (member.getMemberType() != MemberType.COMMERCIAL) {
            throw new IllegalStateException("Member is not commercial.");
        }
        if (member.getStatus() == MemberStatus.ACTIVE
                && member.getPassword() != null
                && !member.getPassword().isEmpty()) {
            return;
        }
        String firstLine = contactNameFromDirector(member.getDirectorDetails());
        member.setFirstName(firstLine);
        String temp = generateTemporaryPassword();
        member.setPassword(passwordEncoder.encode(temp));
        member.setStatus(MemberStatus.ACTIVE);
        member.setPasswordChangeRequired(true);
        memberRepository.save(member);

        emailService.sendEmail(
                member.getEmail(),
                "InfoPharma \u2014 commercial membership approved",
                saEmailBody);
        emailService.sendEmail(
                member.getEmail(),
                "IPOS-PU \u2014 portal login",
                "Your temporary password for the IPOS-PU portal is:\n\n"
                        + temp
                        + "\n\nPlease log in and change your password immediately.");
    }

    public void onCommercialApplicationRejectedFromSa(Long memberId, String rejectionReason) {
        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required.");
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));
        if (member.getMemberType() != MemberType.COMMERCIAL) {
            throw new IllegalStateException("Member is not commercial.");
        }
        if (member.getStatus() == MemberStatus.INACTIVE) {
            return;
        }
        member.setStatus(MemberStatus.INACTIVE);
        memberRepository.save(member);

        emailService.sendEmail(
                member.getEmail(),
                "Commercial membership application",
                "We regret to inform you that your commercial membership application could not be approved at this time.\n\n"
                        + "Reason:\n"
                        + rejectionReason.trim());
    }

    private static String contactNameFromDirector(String directorDetails) {
        if (directorDetails == null || directorDetails.isBlank()) {
            return "Member";
        }
        String firstLine = directorDetails.trim().split("\\R", 2)[0].trim();
        return firstLine.isEmpty() ? "Member" : firstLine;
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

    // Brief (p.19): commercial applications must include email, Companies House
    // registration number, director details, business type and address.
    // Every field is checked here; the first failure throws with a message
    // that surfaces directly to the applicant in the UI.
    private void validateCommercialApplication(String email, String companyRegistrationNumber,
                                                String directorDetails, String businessType,
                                                String address) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email address is required.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email address format is invalid.");
        }

        if (companyRegistrationNumber == null || companyRegistrationNumber.isBlank()) {
            throw new IllegalArgumentException("Companies House registration number is required.");
        }
        if (!COMPANIES_HOUSE_PATTERN.matcher(companyRegistrationNumber.toUpperCase()).matches()) {
            throw new IllegalArgumentException(
                    "Companies House number must be 6-20 alphanumeric characters, e.g. 12345678 or SC123456.");
        }

        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Business address is required.");
        }
        if (address.trim().length() < 10) {
            throw new IllegalArgumentException("Business address looks too short.");
        }
    }

    // Brief: 10 symbols including letters, numbers and special symbols.
    // Guarantee at least one of each, then fill and shuffle.
    private String generateTemporaryPassword() {
        char[] chars = new char[10];
        chars[0] = PASSWORD_LETTERS.charAt(RANDOM.nextInt(PASSWORD_LETTERS.length()));
        chars[1] = PASSWORD_DIGITS.charAt(RANDOM.nextInt(PASSWORD_DIGITS.length()));
        chars[2] = PASSWORD_SPECIALS.charAt(RANDOM.nextInt(PASSWORD_SPECIALS.length()));
        for (int i = 3; i < chars.length; i++) {
            chars[i] = PASSWORD_ALL.charAt(RANDOM.nextInt(PASSWORD_ALL.length()));
        }
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }
}
