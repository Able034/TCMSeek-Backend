package com.tcmseek.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "tcmseek.ai.tcmreason")
public class TcmReasonProperties {

    private String baseUrl;

    private String apiKey;

    private String model = "TCMReason";

    private Double defaultTemperature = 0.3d;

    private Integer defaultMaxTokens = 1024;

    private Boolean stream = Boolean.FALSE;

    private boolean verify = false;

    private String systemPrompt =
            "你是一位资深中医专家，精通辨证论治、中药方剂和经典理论。"
                    + "请根据患者的症状、舌象、脉象等信息，进行严谨的中医辨证分析，"
                    + "并提供治法、方剂（含剂量）、加减建议和生活调护指导。"
                    + "回答需专业、清晰、有理有据，避免西医术语。";

    private Duration connectTimeout = Duration.ofSeconds(10);

    private Duration readTimeout = Duration.ofSeconds(120);

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Double getDefaultTemperature() {
        return defaultTemperature;
    }

    public void setDefaultTemperature(Double defaultTemperature) {
        this.defaultTemperature = defaultTemperature;
    }

    public Integer getDefaultMaxTokens() {
        return defaultMaxTokens;
    }

    public void setDefaultMaxTokens(Integer defaultMaxTokens) {
        this.defaultMaxTokens = defaultMaxTokens;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    public boolean isVerify() {
        return verify;
    }

    public void setVerify(boolean verify) {
        this.verify = verify;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }
}
