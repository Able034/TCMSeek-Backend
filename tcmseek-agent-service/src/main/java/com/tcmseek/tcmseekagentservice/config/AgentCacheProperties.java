package com.tcmseek.tcmseekagentservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "tcmseek.agent.cache")
public class AgentCacheProperties {

    private boolean enabled = true;

    private String keyPrefix = "agent";

    private Duration contextTtl = Duration.ofHours(2);

    private Duration summaryTtl = Duration.ofHours(2);

    private Duration memoryTtl = Duration.ofMinutes(30);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public Duration getContextTtl() {
        return contextTtl;
    }

    public void setContextTtl(Duration contextTtl) {
        this.contextTtl = contextTtl;
    }

    public Duration getSummaryTtl() {
        return summaryTtl;
    }

    public void setSummaryTtl(Duration summaryTtl) {
        this.summaryTtl = summaryTtl;
    }

    public Duration getMemoryTtl() {
        return memoryTtl;
    }

    public void setMemoryTtl(Duration memoryTtl) {
        this.memoryTtl = memoryTtl;
    }
}
