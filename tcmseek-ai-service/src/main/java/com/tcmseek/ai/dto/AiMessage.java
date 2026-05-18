package com.tcmseek.ai.dto;

import jakarta.validation.constraints.NotBlank;

public class AiMessage {

    private String role = "user";

    @NotBlank(message = "message content cannot be blank")
    private String content;

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
