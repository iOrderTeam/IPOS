package com.ipos.pu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ipos.pu.integration")
public class IposPuIntegrationProperties {

    private String saBaseUrl = "http://localhost:8080";
    private String saApiKey = "";
    private String publicBaseUrl = "http://localhost:8082";
    private String webhookPath = "/api/integration-pu/sa-decision";
    private String webhookBearerSecret = "";

    public String getSaBaseUrl() { return saBaseUrl; }
    public void setSaBaseUrl(String saBaseUrl) { this.saBaseUrl = saBaseUrl != null ? saBaseUrl : ""; }

    public String getSaApiKey() { return saApiKey; }
    public void setSaApiKey(String saApiKey) { this.saApiKey = saApiKey != null ? saApiKey : ""; }

    public String getPublicBaseUrl() { return publicBaseUrl; }
    public void setPublicBaseUrl(String publicBaseUrl) { this.publicBaseUrl = publicBaseUrl != null ? publicBaseUrl : ""; }

    public String getWebhookPath() { return webhookPath; }
    public void setWebhookPath(String webhookPath) { this.webhookPath = webhookPath != null ? webhookPath : ""; }

    public String getWebhookBearerSecret() { return webhookBearerSecret; }
    public void setWebhookBearerSecret(String webhookBearerSecret) { this.webhookBearerSecret = webhookBearerSecret != null ? webhookBearerSecret : ""; }
}
