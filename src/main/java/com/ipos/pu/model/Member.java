package com.ipos.pu.model;

import jakarta.persistence.*;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberType memberType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    private boolean passwordChangeRequired;

    private int orderCounter;

    // Commercial members only - null for non-commercial
    private String companyRegistrationNumber;
    private String directorDetails;
    private String businessType;
    private String address;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public MemberType getMemberType() { return memberType; }
    public void setMemberType(MemberType memberType) { this.memberType = memberType; }

    public MemberStatus getStatus() { return status; }
    public void setStatus(MemberStatus status) { this.status = status; }

    public boolean isPasswordChangeRequired() { return passwordChangeRequired; }
    public void setPasswordChangeRequired(boolean passwordChangeRequired) { this.passwordChangeRequired = passwordChangeRequired; }

    public int getOrderCounter() { return orderCounter; }
    public void setOrderCounter(int orderCounter) { this.orderCounter = orderCounter; }

    public String getCompanyRegistrationNumber() { return companyRegistrationNumber; }
    public void setCompanyRegistrationNumber(String companyRegistrationNumber) { this.companyRegistrationNumber = companyRegistrationNumber; }

    public String getDirectorDetails() { return directorDetails; }
    public void setDirectorDetails(String directorDetails) { this.directorDetails = directorDetails; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
