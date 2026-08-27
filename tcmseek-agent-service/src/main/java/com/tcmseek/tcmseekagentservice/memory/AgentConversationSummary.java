package com.tcmseek.tcmseekagentservice.memory;

public record AgentConversationSummary(
        String id,
        String title,
        int messageCount,
        String lastMessageAt,
        String updatedAt) {
}
