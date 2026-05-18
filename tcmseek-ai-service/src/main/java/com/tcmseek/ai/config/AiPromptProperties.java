package com.tcmseek.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tcmseek.ai")
public class AiPromptProperties {

    private String systemPrompt;

    private String summaryPromptTemplate;

    private String noToolResultMessage = "知识图谱未查询到相关数据。";

    private String replyTruncatedMessage = "回答已压缩，完整明细请通过 CSV 下载。";

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getSummaryPromptTemplate() {
        return summaryPromptTemplate;
    }

    public void setSummaryPromptTemplate(String summaryPromptTemplate) {
        this.summaryPromptTemplate = summaryPromptTemplate;
    }

    public String getNoToolResultMessage() {
        return noToolResultMessage;
    }

    public void setNoToolResultMessage(String noToolResultMessage) {
        this.noToolResultMessage = noToolResultMessage;
    }

    public String getReplyTruncatedMessage() {
        return replyTruncatedMessage;
    }

    public void setReplyTruncatedMessage(String replyTruncatedMessage) {
        this.replyTruncatedMessage = replyTruncatedMessage;
    }
}
