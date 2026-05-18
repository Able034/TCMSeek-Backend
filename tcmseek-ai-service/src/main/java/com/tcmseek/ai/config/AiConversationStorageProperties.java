package com.tcmseek.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tcmseek.ai.conversation")
public class AiConversationStorageProperties {

    private boolean persistenceEnabled = true;

    private int contextMessageLimit = 20;

    private boolean summaryEnabled = true;

    private int summaryTriggerMessageCount = 12;

    private int summaryRefreshMessageCount = 6;

    private int summarySourceMessageLimit = 40;

    private boolean memoryEnabled = true;

    private int memoryMaxItems = 5;

    private int memorySourceMessageLimit = 20;

    public boolean isPersistenceEnabled() {
        return persistenceEnabled;
    }

    public void setPersistenceEnabled(boolean persistenceEnabled) {
        this.persistenceEnabled = persistenceEnabled;
    }

    public int getContextMessageLimit() {
        return contextMessageLimit;
    }

    public void setContextMessageLimit(int contextMessageLimit) {
        this.contextMessageLimit = contextMessageLimit;
    }

    public boolean isSummaryEnabled() {
        return summaryEnabled;
    }

    public void setSummaryEnabled(boolean summaryEnabled) {
        this.summaryEnabled = summaryEnabled;
    }

    public int getSummaryTriggerMessageCount() {
        return summaryTriggerMessageCount;
    }

    public void setSummaryTriggerMessageCount(int summaryTriggerMessageCount) {
        this.summaryTriggerMessageCount = summaryTriggerMessageCount;
    }

    public int getSummaryRefreshMessageCount() {
        return summaryRefreshMessageCount;
    }

    public void setSummaryRefreshMessageCount(int summaryRefreshMessageCount) {
        this.summaryRefreshMessageCount = summaryRefreshMessageCount;
    }

    public int getSummarySourceMessageLimit() {
        return summarySourceMessageLimit;
    }

    public void setSummarySourceMessageLimit(int summarySourceMessageLimit) {
        this.summarySourceMessageLimit = summarySourceMessageLimit;
    }

    public boolean isMemoryEnabled() {
        return memoryEnabled;
    }

    public void setMemoryEnabled(boolean memoryEnabled) {
        this.memoryEnabled = memoryEnabled;
    }

    public int getMemoryMaxItems() {
        return memoryMaxItems;
    }

    public void setMemoryMaxItems(int memoryMaxItems) {
        this.memoryMaxItems = memoryMaxItems;
    }

    public int getMemorySourceMessageLimit() {
        return memorySourceMessageLimit;
    }

    public void setMemorySourceMessageLimit(int memorySourceMessageLimit) {
        this.memorySourceMessageLimit = memorySourceMessageLimit;
    }
}
