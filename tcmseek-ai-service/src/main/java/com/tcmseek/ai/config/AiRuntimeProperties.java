package com.tcmseek.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "tcmseek.ai.runtime")
public class AiRuntimeProperties {

    private Duration requestTimeout = Duration.ofSeconds(60);

    private int maxRetries = 1;

    private Duration retryBackoff = Duration.ofMillis(500);

    private int modelExecutorPoolSize = 8;

    private int questionLogMaxLength = 120;

    private int maxReplyChars = 4000;

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Duration getRetryBackoff() {
        return retryBackoff;
    }

    public void setRetryBackoff(Duration retryBackoff) {
        this.retryBackoff = retryBackoff;
    }

    public int getModelExecutorPoolSize() {
        return modelExecutorPoolSize;
    }

    public void setModelExecutorPoolSize(int modelExecutorPoolSize) {
        this.modelExecutorPoolSize = modelExecutorPoolSize;
    }

    public int getQuestionLogMaxLength() {
        return questionLogMaxLength;
    }

    public void setQuestionLogMaxLength(int questionLogMaxLength) {
        this.questionLogMaxLength = questionLogMaxLength;
    }

    public int getMaxReplyChars() {
        return maxReplyChars;
    }

    public void setMaxReplyChars(int maxReplyChars) {
        this.maxReplyChars = maxReplyChars;
    }
}
