package com.ipos.pu.dto;

public class CommercialRegistrationRequest {
    private String email;
    private String companyRegistrationNumber;
    private String directorDetails;
    private String businessType;
    private String address;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCompanyRegistrationNumber() { return companyRegistrationNumber; }
    public void setCompanyRegistrationNumber(String companyRegistrationNumber) { this.companyRegistrationNumber = companyRegistrationNumber; }

    public String getDirectorDetails() { return directorDetails; }
    public void setDirectorDetails(String directorDetails) { this.directorDetails = directorDetails; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
