package com.tcmseek.ai.service;

import com.tcmseek.ai.dto.AiMessage;
import com.tcmseek.ai.dto.ToolCallResult;

import java.util.ArrayList;
import java.util.List;

public class AiConversationExchange {

    private String conversationId;

    private String userId;

    private String mode;

    private AiMessage userMessage;

    private String assistantMessageId;

    private String assistantContent;

    private String provider;

    private String model;

    private String finishReason;

    private Integer totalResults;

    private Integer displayedResults;

    private String csvExportId;

    private String requestId;

    private Integer latencyMs;

    private List<ToolCallResult> toolResults = new ArrayList<>();

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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public AiMessage getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(AiMessage userMessage) {
        this.userMessage = userMessage;
    }

    public String getAssistantMessageId() {
        return assistantMessageId;
    }

    public void setAssistantMessageId(String assistantMessageId) {
        this.assistantMessageId = assistantMessageId;
    }

    public String getAssistantContent() {
        return assistantContent;
    }

    public void setAssistantContent(String assistantContent) {
        this.assistantContent = assistantContent;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFinishReason() {
        return finishReason;
    }

    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }

    public Integer getDisplayedResults() {
        return displayedResults;
    }

    public void setDisplayedResults(Integer displayedResults) {
        this.displayedResults = displayedResults;
    }

    public String getCsvExportId() {
        return csvExportId;
    }

    public void setCsvExportId(String csvExportId) {
        this.csvExportId = csvExportId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Integer getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Integer latencyMs) {
        this.latencyMs = latencyMs;
    }

    public List<ToolCallResult> getToolResults() {
        return toolResults;
    }

    public void setToolResults(List<ToolCallResult> toolResults) {
        this.toolResults = toolResults;
    }
}
