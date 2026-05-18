package com.tcmseek.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "tcmseek.ai.cache")
public class AiCacheProperties {

    private boolean enabled = true;

    private String keyPrefix = "ai";

    private Duration contextTtl = Duration.ofHours(2);

    private Duration exportTtl = Duration.ofMinutes(30);

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

    public Duration getExportTtl() {
        return exportTtl;
    }

    public void setExportTtl(Duration exportTtl) {
        this.exportTtl = exportTtl;
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
