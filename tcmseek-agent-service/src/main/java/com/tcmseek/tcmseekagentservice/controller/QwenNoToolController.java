package com.tcmseek.tcmseekagentservice.controller;

import com.tcmseek.tcmseekagentservice.service.AgentRequestContext;
import com.tcmseek.tcmseekagentservice.service.QwenNoToolChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/agentqwen")
public class QwenNoToolController {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private static final String USER_ID_HEADER = "X-User-Id";

    private static final String USERNAME_HEADER = "X-User-Name";

    private static final String ACCOUNT_HEADER = "X-User-Account";

    private final QwenNoToolChatService qwenNoToolChatService;

    public QwenNoToolController(QwenNoToolChatService qwenNoToolChatService) {
        this.qwenNoToolChatService = qwenNoToolChatService;
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody QwenNoToolChatRequest request,
                                 @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
                                 @RequestHeader(value = USER_ID_HEADER, required = false) String userId,
                                 @RequestHeader(value = USERNAME_HEADER, required = false) String username,
                                 @RequestHeader(value = ACCOUNT_HEADER, required = false) String account) {
        AgentRequestContext context = new AgentRequestContext(requestId, userId, username, account);
        return qwenNoToolChatService.stream(request.query(), request.sessionId(), context);
    }

    public record QwenNoToolChatRequest(String query, String sessionId) {
    }
}
