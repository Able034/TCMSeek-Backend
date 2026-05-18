package com.tcmseek.ai.controller;

import com.tcmseek.ai.dto.ConversationCreateRequest;
import com.tcmseek.ai.dto.ConversationListResponse;
import com.tcmseek.ai.dto.ConversationMessagesResponse;
import com.tcmseek.ai.dto.ConversationSummaryDto;
import com.tcmseek.ai.dto.ConversationUpdateRequest;
import com.tcmseek.ai.service.AiConversationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping(value = "/ai/conversations", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class AiConversationController {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final AiConversationService conversationService;

    public AiConversationController(AiConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public ConversationListResponse list(@RequestHeader(value = USER_ID_HEADER, required = false) String userId,
                                         @RequestParam(defaultValue = "academic") String mode,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int pageSize) {
        return conversationService.list(userId, mode, page, pageSize);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ConversationSummaryDto create(@RequestHeader(value = USER_ID_HEADER, required = false) String userId,
                                         @RequestBody(required = false) ConversationCreateRequest request) {
        return conversationService.create(userId, request);
    }

    @GetMapping("/{conversationId}/messages")
    public ConversationMessagesResponse messages(@RequestHeader(value = USER_ID_HEADER, required = false) String userId,
                                                 @PathVariable String conversationId,
                                                 @RequestParam(defaultValue = "100") int limit) {
        return conversationService.messages(userId, conversationId, limit);
    }

    @PatchMapping(value = "/{conversationId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ConversationSummaryDto update(@RequestHeader(value = USER_ID_HEADER, required = false) String userId,
                                         @PathVariable String conversationId,
                                         @RequestBody ConversationUpdateRequest request) {
        return conversationService.update(userId, conversationId, request);
    }

    @DeleteMapping("/{conversationId}")
    @ResponseStatus(NO_CONTENT)
    public void delete(@RequestHeader(value = USER_ID_HEADER, required = false) String userId,
                       @PathVariable String conversationId) {
        conversationService.delete(userId, conversationId);
    }
}
