package com.tcmseek.gateway.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "tcmseek.gateway.auth")
public class GatewayAuthProperties {

    private boolean enabled = true;

    private String tokenHeader = "Authorization";

    private String validateUrl = "http://tcmseek-admin/getInfo";

    private Duration timeout = Duration.ofSeconds(5);

    private List<String> protectedPaths = new ArrayList<>(List.of(
            "/api/ai/**",
            "/ai/**",
            "/api/web/tcmseek/tools/target-prediction/**"));

    private List<String> excludedPaths = new ArrayList<>(List.of(
            "/api/web/tcmseek/login",
            "/api/web/tcmseek/regist",
            "/api/web/tcmseek/sendCode",
            "/api/web/tcmseek/password/sendCode",
            "/api/web/tcmseek/password/reset"));

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTokenHeader() {
        return tokenHeader;
    }

    public void setTokenHeader(String tokenHeader) {
        this.tokenHeader = tokenHeader;
    }

    public String getValidateUrl() {
        return validateUrl;
    }

    public void setValidateUrl(String validateUrl) {
        this.validateUrl = validateUrl;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public List<String> getProtectedPaths() {
        return protectedPaths;
    }

    public void setProtectedPaths(List<String> protectedPaths) {
        this.protectedPaths = protectedPaths == null ? new ArrayList<>() : protectedPaths;
    }

    public List<String> getExcludedPaths() {
        return excludedPaths;
    }

    public void setExcludedPaths(List<String> excludedPaths) {
        this.excludedPaths = excludedPaths == null ? new ArrayList<>() : excludedPaths;
    }
}
