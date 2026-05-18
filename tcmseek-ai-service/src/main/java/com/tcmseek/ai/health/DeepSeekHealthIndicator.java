package com.tcmseek.ai.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DeepSeekHealthIndicator implements HealthIndicator {

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model:}")
    private String model;

    @Override
    public Health health() {
        Health.Builder builder = StringUtils.hasText(apiKey)
                ? Health.up()
                : Health.status("DEGRADED");
        return builder
                .withDetail("provider", "deepseek")
                .withDetail("baseUrl", baseUrl)
                .withDetail("model", model)
                .withDetail("apiKeyConfigured", StringUtils.hasText(apiKey))
                .build();
    }
}
