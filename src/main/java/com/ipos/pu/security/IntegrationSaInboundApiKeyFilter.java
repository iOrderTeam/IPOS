package com.ipos.pu.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class IntegrationSaInboundApiKeyFilter extends OncePerRequestFilter {

    public static final String INTEGRATION_KEY_HEADER = "X-IPOS-Integration-Key";

    private static final String RELAY_PATH = "/api/integration-sa/relay-email";

    private final String expectedKey;

    public IntegrationSaInboundApiKeyFilter(String expectedKey) {
        this.expectedKey = expectedKey != null ? expectedKey : "";
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (!request.getMethod().equalsIgnoreCase("POST")
                || uri == null
                || !uri.endsWith(RELAY_PATH)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (expectedKey.isBlank()) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"IPOS-SA relay is not configured (set ipos.pu.integration.sa-api-key).\"}");
            return;
        }

        String provided = request.getHeader(INTEGRATION_KEY_HEADER);
        if (provided == null || !expectedKey.equals(provided)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid or missing integration API key.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
