package com.tcmseek.tcmseekagentservice.memory;

public class AgentConversationContext {

    private final String conversationId;

    private final String enhancedPrompt;

    public AgentConversationContext(String conversationId, String enhancedPrompt) {
        this.conversationId = conversationId;
        this.enhancedPrompt = enhancedPrompt;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getEnhancedPrompt() {
        return enhancedPrompt;
    }
}
