package com.tcmseek.ai.dto;

public class ConversationSummaryState {

    private String conversationId;

    private String userId;

    private String summary;

    private int coveredMessageCount;

    private String updatedAt;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public int getCoveredMessageCount() {
        return coveredMessageCount;
    }

    public void setCoveredMessageCount(int coveredMessageCount) {
        this.coveredMessageCount = coveredMessageCount;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
