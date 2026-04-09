package com.ipos.pu.dto;

import com.ipos.pu.model.Member;
import com.ipos.pu.model.MemberStatus;
import com.ipos.pu.model.MemberType;

public class MemberResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private MemberType memberType;
    private MemberStatus status;
    private boolean passwordChangeRequired;

    public static MemberResponse from(Member member) {
        MemberResponse response = new MemberResponse();
        response.id = member.getId();
        response.email = member.getEmail();
        response.firstName = member.getFirstName();
        response.lastName = member.getLastName();
        response.memberType = member.getMemberType();
        response.status = member.getStatus();
        response.passwordChangeRequired = member.isPasswordChangeRequired();
        return response;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public MemberType getMemberType() { return memberType; }
    public MemberStatus getStatus() { return status; }
    public boolean isPasswordChangeRequired() { return passwordChangeRequired; }
}
