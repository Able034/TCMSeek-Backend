package com.tcmseek.ai.service;

import com.tcmseek.ai.dto.ConversationCreateRequest;
import com.tcmseek.ai.dto.ConversationListResponse;
import com.tcmseek.ai.dto.ConversationMessageDto;
import com.tcmseek.ai.dto.ConversationMessagesResponse;
import com.tcmseek.ai.dto.ConversationSummaryDto;
import com.tcmseek.ai.dto.ConversationUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AiConversationService {

    private final AiConversationRepository conversationRepository;

    public AiConversationService(AiConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    public ConversationListResponse list(String userId, String mode, int page, int pageSize) {
        String owner = owner(userId);
        List<ConversationSummaryDto> items = conversationRepository.listConversations(owner, mode, page, pageSize);
        long total = conversationRepository.countConversations(owner, mode);
        return new ConversationListResponse(items, total);
    }

    public ConversationSummaryDto create(String userId, ConversationCreateRequest request) {
        String owner = owner(userId);
        String mode = request == null ? "academic" : request.getMode();
        String title = request == null ? null : request.getTitle();
        return conversationRepository.createConversation(owner, mode, title);
    }

    public ConversationMessagesResponse messages(String userId, String conversationId, int limit) {
        String owner = owner(userId);
        ensureVisible(owner, conversationId);
        List<ConversationMessageDto> messages = conversationRepository.findMessages(owner, conversationId, limit);
        return new ConversationMessagesResponse(conversationId, messages);
    }

    public ConversationSummaryDto update(String userId,
                                         String conversationId,
                                         ConversationUpdateRequest request) {
        String owner = owner(userId);
        String title = request == null ? null : request.getTitle();
        boolean updated = conversationRepository.updateConversationTitle(owner, conversationId, title);
        if (!updated) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "conversation not found");
        }
        return conversationRepository.findConversation(owner, conversationId);
    }

    public void delete(String userId, String conversationId) {
        boolean deleted = conversationRepository.markConversationDeleted(owner(userId), conversationId);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "conversation not found");
        }
    }

    private void ensureVisible(String userId, String conversationId) {
        if (!StringUtils.hasText(conversationId)
                || conversationRepository.findConversation(userId, conversationId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "conversation not found");
        }
    }

    private String owner(String userId) {
        return StringUtils.hasText(userId) ? userId : "anonymous";
    }
}
