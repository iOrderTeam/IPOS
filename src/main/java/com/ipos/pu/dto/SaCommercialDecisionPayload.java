package com.ipos.pu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SaCommercialDecisionPayload {

    private long internalId;
    private String externalReferenceId;
    private String status;
    private String emailBody;
    private String rejectionReason;

    public long getInternalId() { return internalId; }
    public void setInternalId(long internalId) { this.internalId = internalId; }

    public String getExternalReferenceId() { return externalReferenceId; }
    public void setExternalReferenceId(String externalReferenceId) { this.externalReferenceId = externalReferenceId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEmailBody() { return emailBody; }
    public void setEmailBody(String emailBody) { this.emailBody = emailBody; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
