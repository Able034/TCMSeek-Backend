package com.tcmseek.ai.service;

import com.tcmseek.ai.dto.AiChatRequest;
import com.tcmseek.ai.dto.AiChatResponse;
import com.tcmseek.ai.dto.AiMessage;
import com.tcmseek.ai.config.ShowModelChatProperties;
import com.tcmseek.ai.exception.AiServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

@Service
public class ShowModelChatService {

    private static final String DEFAULT_USER_ID = "showmodel-default-user";

    private static final String DEFAULT_USERNAME = "showmodel";

    private static final String DEFAULT_ACCOUNT = "showmodel";

    private static final String DEFAULT_SESSION_ID = "showmodel-default-session";

    private static final int MAX_ROUNDS = 20;

    private static final int MAX_MESSAGES = MAX_ROUNDS * 2;

    private final AiChatService aiChatService;

    private final ShowModelChatProperties properties;

    private final Deque<AiMessage> memory = new ArrayDeque<>();

    public ShowModelChatService(AiChatService aiChatService,
                                ShowModelChatProperties properties) {
        this.aiChatService = aiChatService;
        this.properties = properties;
    }

    public void stream(AiChatRequest request, String requestId, AiChatService.StreamEventSink sink) {
        AiMessage latestUserMessage = latestUserMessage(request);
        List<AiMessage> context = buildContext(latestUserMessage);
        AiChatRequest modelRequest = new AiChatRequest();
        modelRequest.setSessionId(DEFAULT_SESSION_ID);
        modelRequest.setMessages(context);
        AiChatResponse response = aiChatService.streamWithContext(
                modelRequest,
                new AiRequestContext(requestId, DEFAULT_USER_ID, DEFAULT_USERNAME, DEFAULT_ACCOUNT),
                context,
                AiChatService.ChatRunOptions.showModel(
                        properties.getSystemPrompt(),
                        properties.getSummaryPromptTemplate(),
                        properties.getNoToolResultMessage()),
                sink);
        appendMemory(latestUserMessage, response.getReply());
    }

    private List<AiMessage> buildContext(AiMessage latestUserMessage) {
        synchronized (memory) {
            List<AiMessage> context = new ArrayList<>(memory.size() + 1);
            memory.forEach(message -> context.add(copyMessage(message)));
            context.add(copyMessage(latestUserMessage));
            return context;
        }
    }

    private void appendMemory(AiMessage latestUserMessage, String reply) {
        if (!StringUtils.hasText(reply)) {
            return;
        }
        synchronized (memory) {
            memory.addLast(copyMessage(latestUserMessage));
            AiMessage assistant = new AiMessage();
            assistant.setRole("assistant");
            assistant.setContent(reply);
            memory.addLast(assistant);
            while (memory.size() > MAX_MESSAGES) {
                memory.removeFirst();
            }
        }
    }

    private AiMessage latestUserMessage(AiChatRequest request) {
        if (request == null || request.getMessages() == null || request.getMessages().isEmpty()) {
            throw new AiServiceException(HttpStatus.BAD_REQUEST, "SHOWMODEL_EMPTY_MESSAGE",
                    "messages cannot be empty", null);
        }
        for (int i = request.getMessages().size() - 1; i >= 0; i--) {
            AiMessage message = request.getMessages().get(i);
            if (message == null || !StringUtils.hasText(message.getContent())) {
                continue;
            }
            String role = StringUtils.hasText(message.getRole())
                    ? message.getRole().toLowerCase(Locale.ROOT)
                    : "user";
            if ("user".equals(role)) {
                return copyMessage(message);
            }
        }
        throw new AiServiceException(HttpStatus.BAD_REQUEST, "SHOWMODEL_EMPTY_MESSAGE",
                "user message cannot be empty", null);
    }

    private AiMessage copyMessage(AiMessage source) {
        AiMessage copy = new AiMessage();
        copy.setRole(StringUtils.hasText(source.getRole()) ? source.getRole() : "user");
        copy.setContent(source.getContent());
        return copy;
    }
}
