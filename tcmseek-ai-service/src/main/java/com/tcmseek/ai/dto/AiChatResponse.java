package com.tcmseek.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class AiChatResponse {

    private String id;

    private String reply;

    private String provider;

    private String model;

    private String finishReason;

    private Usage usage;

    private AiGraphData graph;

    private String csvDownloadUrl;

    private Integer totalResults;

    private Integer displayedResults;

    private List<ToolCallResult> toolResults = new ArrayList<>();

    public AiChatResponse() {
    }

    public AiChatResponse(String reply, String provider, String model) {
        this.reply = reply;
        this.provider = provider;
        this.model = model;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
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

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    public AiGraphData getGraph() {
        return graph;
    }

    public void setGraph(AiGraphData graph) {
        this.graph = graph;
    }

    public String getCsvDownloadUrl() {
        return csvDownloadUrl;
    }

    public void setCsvDownloadUrl(String csvDownloadUrl) {
        this.csvDownloadUrl = csvDownloadUrl;
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

    public List<ToolCallResult> getToolResults() {
        return toolResults;
    }

    public void setToolResults(List<ToolCallResult> toolResults) {
        this.toolResults = toolResults;
    }

    public static class Usage {

        private Integer promptTokens;

        private Integer completionTokens;

        private Integer totalTokens;

        public Integer getPromptTokens() {
            return promptTokens;
        }

        public void setPromptTokens(Integer promptTokens) {
            this.promptTokens = promptTokens;
        }

        public Integer getCompletionTokens() {
            return completionTokens;
        }

        public void setCompletionTokens(Integer completionTokens) {
            this.completionTokens = completionTokens;
        }

        public Integer getTotalTokens() {
            return totalTokens;
        }

        public void setTotalTokens(Integer totalTokens) {
            this.totalTokens = totalTokens;
        }
    }
}
