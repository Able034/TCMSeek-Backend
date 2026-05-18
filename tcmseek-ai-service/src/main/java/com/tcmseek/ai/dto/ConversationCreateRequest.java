package com.tcmseek.ai.dto;

public class ConversationCreateRequest {

    private String mode = "academic";

    private String title;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
