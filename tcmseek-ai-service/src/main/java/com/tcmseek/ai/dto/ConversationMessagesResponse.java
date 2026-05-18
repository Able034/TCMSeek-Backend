package com.tcmseek.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class ConversationMessagesResponse {

    private String conversationId;

    private List<ConversationMessageDto> messages = new ArrayList<>();

    public ConversationMessagesResponse() {
    }

    public ConversationMessagesResponse(String conversationId, List<ConversationMessageDto> messages) {
        this.conversationId = conversationId;
        this.messages = messages;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public List<ConversationMessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<ConversationMessageDto> messages) {
        this.messages = messages;
    }
}
