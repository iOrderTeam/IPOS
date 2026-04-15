package com.ipos.pu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ipos.pu.config.IposPuIntegrationProperties;
import com.ipos.pu.model.Member;
import com.ipos.pu.repository.MemberRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class IposSaService {

    private final IposPuIntegrationProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;

    public IposSaService(IposPuIntegrationProperties properties,
                         RestClient restClient,
                         ObjectMapper objectMapper,
                         MemberRepository memberRepository) {
        this.properties = properties;
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.memberRepository = memberRepository;
    }

    public void submitCommercialApplication(Member member) {
        if (member.getId() == null) {
            throw new IllegalStateException("Member must be persisted before IPOS-SA submit");
        }
        if (member.getSaApplicationId() != null) {
            return;
        }
        if (properties.getSaApiKey() == null || properties.getSaApiKey().isBlank()) {
            throw new IllegalStateException(
                    "IPOS-SA integration is not configured (set ipos.pu.integration.sa-api-key)");
        }
        String base = properties.getSaBaseUrl().replaceAll("/+$", "");
        String url = base + "/api/integration-pu/inbound/applications";

        ObjectNode root = objectMapper.createObjectNode();
        root.put("externalReferenceId", "PU-MEMBER-" + member.getId());
        root.set("payload", buildPayload(member));
        String callback = buildCallbackUrl();
        if (callback != null && !callback.isBlank()) {
            root.put("callbackUrl", callback);
        }

        try {
            String jsonBody = objectMapper.writeValueAsString(root);
            String responseBody = restClient.post()
                    .uri(url)
                    .header("X-IPOS-Integration-Key", properties.getSaApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBody)
                    .retrieve()
                    .body(String.class);

            JsonNode resp = objectMapper.readTree(responseBody);
            if (resp != null && resp.has("id") && !resp.get("id").isNull()) {
                member.setSaApplicationId(resp.get("id").asLong());
                memberRepository.save(member);
            }
        } catch (RestClientResponseException e) {
            int code = e.getStatusCode().value();
            if (code == 409) {
                return;
            }
            if (code == 401) {
                throw new IllegalStateException("IPOS-SA rejected the integration API key (HTTP 401).", e);
            }
            if (code == 503) {
                throw new IllegalStateException("IPOS-SA inbound integration is unavailable (HTTP 503).", e);
            }
            throw new IllegalStateException("IPOS-SA submission failed (HTTP " + code + ").", e);
        } catch (RestClientException | com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("IPOS-SA submission failed: " + e.getMessage(), e);
        }
    }

    private ObjectNode buildPayload(Member member) {
        ObjectNode p = objectMapper.createObjectNode();
        String reg = nullToEmpty(member.getCompanyRegistrationNumber());
        String biz = nullToEmpty(member.getBusinessType());
        String cName = nullToEmpty(member.getCompanyName());
        p.put("companyName", cName.isBlank() ? "Commercial applicant (" + reg + ") \u2014 " + biz : cName);
        p.put("contactName", contactNameFromDirector(member.getDirectorDetails()));
        p.put("contactEmail", member.getEmail());
        p.put("email", member.getEmail());
        p.put("contactPhone", "");
        String summary = "Business type: " + biz + "\n"
                + "Companies House no.: " + reg + "\n"
                + "Address:\n" + nullToEmpty(member.getAddress());
        p.put("summary", summary);
        p.put("address", nullToEmpty(member.getAddress()));
        return p;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String contactNameFromDirector(String directorDetails) {
        if (directorDetails == null || directorDetails.isBlank()) {
            return "Applicant";
        }
        String firstLine = directorDetails.trim().split("\\R", 2)[0].trim();
        return firstLine.isEmpty() ? "Applicant" : firstLine;
    }

    private String buildCallbackUrl() {
        String pub = properties.getPublicBaseUrl();
        if (pub == null || pub.isBlank()) {
            return null;
        }
        String base = pub.replaceAll("/+$", "");
        String path = properties.getWebhookPath();
        if (path == null || path.isBlank()) {
            return base;
        }
        String p = path.startsWith("/") ? path : "/" + path;
        return base + p;
    }
}
