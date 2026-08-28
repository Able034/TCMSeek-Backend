package com.tcmseek.tcmseekagentservice.controller;

import com.tcmseek.tcmseekagentservice.service.AgentRequestContext;
import com.tcmseek.tcmseekagentservice.service.QwenNoToolChatService;
import com.tcmseek.tcmseekagentservice.service.ShowModelConversationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping({"/agentqwen", "/showmodel"})
public class QwenNoToolController {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final QwenNoToolChatService qwenNoToolChatService;

    private final ShowModelConversationService conversationService;

    public QwenNoToolController(QwenNoToolChatService qwenNoToolChatService,
                                ShowModelConversationService conversationService) {
        this.qwenNoToolChatService = qwenNoToolChatService;
        this.conversationService = conversationService;
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody QwenNoToolChatRequest request,
                                 @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId) {
        conversationService.ensureConversation(request.sessionId());
        AgentRequestContext context = new AgentRequestContext(
                requestId,
                ShowModelConversationService.USER_ID,
                "showmodel",
                "showmodel");
        return qwenNoToolChatService.stream(request.query(), request.sessionId(), context);
    }

    public record QwenNoToolChatRequest(String query, String sessionId) {
    }
}
