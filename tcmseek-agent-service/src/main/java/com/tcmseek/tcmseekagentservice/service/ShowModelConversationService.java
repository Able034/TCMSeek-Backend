package com.tcmseek.tcmseekagentservice.service;

import com.tcmseek.tcmseekagentservice.memory.AgentConversationRepository;
import com.tcmseek.tcmseekagentservice.memory.AgentConversationSummary;
import com.tcmseek.tcmseekagentservice.memory.AgentStoredMessage;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ShowModelConversationService {

    public static final String USER_ID = "showmodel-default-user";

    public static final String MODE = "general";

    private static final int HISTORY_LIMIT = 50;

    private final AgentConversationRepository conversationRepository;

    public ShowModelConversationService(AgentConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    public ConversationListResponse list() {
        List<AgentConversationSummary> items = conversationRepository.findActiveConversations(
                USER_ID,
                MODE,
                HISTORY_LIMIT);
        return new ConversationListResponse(items);
    }

    public AgentConversationSummary create() {
        return conversationRepository.createConversation(USER_ID, MODE, "新对话");
    }

    public ConversationMessagesResponse messages(String conversationId) {
        requireConversation(conversationId);
        return new ConversationMessagesResponse(
                conversationId,
                conversationRepository.findConversationMessages(USER_ID, conversationId, 100));
    }

    public void requireConversation(String conversationId) {
        if (!StringUtils.hasText(conversationId)
                || conversationRepository.findActiveConversation(USER_ID, MODE, conversationId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "conversation not found");
        }
    }

    public void ensureConversation(String conversationId) {
        if (!StringUtils.hasText(conversationId) || conversationId.length() > 64) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid conversation id");
        }
        if (conversationRepository.findActiveConversation(USER_ID, MODE, conversationId) == null) {
            conversationRepository.createConversation(conversationId, USER_ID, MODE, "新对话");
        }
    }

    public record ConversationListResponse(List<AgentConversationSummary> items) {
    }

    public record ConversationMessagesResponse(String conversationId, List<AgentStoredMessage> messages) {
    }
}
