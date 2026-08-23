package com.tcmseek.tcmseekagentservice.memory;

public class AgentMessage {

    private String role;

    private String content;

    public static AgentMessage of(String role, String content) {
        AgentMessage message = new AgentMessage();
        message.setRole(role);
        message.setContent(content);
        return message;
    }

    public static AgentMessage user(String content) {
        return of("user", content);
    }

    public static AgentMessage assistant(String content) {
        return of("assistant", content);
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
