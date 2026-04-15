package com.ipos.pu.config;

import com.ipos.pu.security.IntegrationSaInboundApiKeyFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class IntegrationSaRelayFilterConfig {

    @Bean
    public FilterRegistrationBean<IntegrationSaInboundApiKeyFilter> integrationSaInboundApiKeyFilterRegistration(
            @Value("${ipos.pu.integration.sa-api-key:}") String saApiKey) {
        FilterRegistrationBean<IntegrationSaInboundApiKeyFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new IntegrationSaInboundApiKeyFilter(saApiKey));
        bean.addUrlPatterns("/api/integration-sa/relay-email");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
