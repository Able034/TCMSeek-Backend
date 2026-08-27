package com.tcmseek.tcmseekagentservice.memory;

public record AgentStoredMessage(
        String role,
        String content,
        String createdAt) {
}
