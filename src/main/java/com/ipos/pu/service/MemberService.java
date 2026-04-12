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

    // UK Companies House number: exactly 8 characters, digits or uppercase letters.
    // Covers plain numeric (e.g. 12345678), Scotland (SC123456), NI (NI123456), LLPs (OC123456), etc.
    private static final Pattern COMPANIES_HOUSE_PATTERN =
            Pattern.compile("^[A-Z0-9]{8}$");

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
    public Member registerCommercial(String email, String companyRegistrationNumber,
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
        member.setCompanyRegistrationNumber(companyRegistrationNumber);
        member.setDirectorDetails(directorDetails);
        member.setBusinessType(businessType);
        member.setAddress(address);

        Member saved = memberRepository.save(member);
        iposSaService.submitCommercialApplication(saved);
        return saved;
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
                    "Companies House number must be 8 characters (digits or letters), e.g. 12345678 or SC123456.");
        }

        if (directorDetails == null || directorDetails.isBlank()) {
            throw new IllegalArgumentException("Company director details are required.");
        }
        if (directorDetails.trim().length() < 3) {
            throw new IllegalArgumentException("Company director details look too short.");
        }

        if (businessType == null || businessType.isBlank()) {
            throw new IllegalArgumentException("Type of business is required.");
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
