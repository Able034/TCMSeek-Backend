package com.tcmseek.tcmseekagentservice.service;

import com.tcmseek.tcmseekagentservice.ai.model.workflow.WorkflowStreamEvent;
import com.tcmseek.tcmseekagentservice.memory.AgentConversationContext;
import com.tcmseek.tcmseekagentservice.memory.AgentSessionManager;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class QwenNoToolChatService {

    private static final long SSE_TIMEOUT_MS = 10 * 60 * 1000L;

    private static final long MODEL_TIMEOUT_SECONDS = 180;

    private final StreamingChatModel qwenStreamingChatModel;

    private final AgentSessionManager sessionManager;

    public QwenNoToolChatService(@Qualifier("streamingChatModelQwen") StreamingChatModel qwenStreamingChatModel,
                                 AgentSessionManager sessionManager) {
        this.qwenStreamingChatModel = qwenStreamingChatModel;
        this.sessionManager = sessionManager;
    }

    public SseEmitter stream(String query, String sessionId, AgentRequestContext requestContext) {
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("query cannot be empty");
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        AgentRequestContext safeContext = requestContext == null ? AgentRequestContext.empty() : requestContext;
        String trimmedQuery = query.trim();
        String traceId = StringUtils.hasText(safeContext.getRequestId())
                ? safeContext.getRequestId()
                : UUID.randomUUID().toString();
        long startedAt = System.currentTimeMillis();

        CompletableFuture.runAsync(() -> execute(
                emitter,
                traceId,
                sessionId,
                trimmedQuery,
                safeContext,
                startedAt));

        return emitter;
    }

    private void execute(SseEmitter emitter,
                         String traceId,
                         String sessionId,
                         String query,
                         AgentRequestContext requestContext,
                         long startedAt) {
        String conversationId = null;
        try {
            AgentConversationContext conversationContext =
                    sessionManager.buildContext(sessionId, query, requestContext, false);
            conversationId = conversationContext.getConversationId();
            send(emitter, traceId, "chat_started", Map.of(
                    "query", query,
                    "conversationId", conversationId
            ));

            String finalAnswer = callModel(emitter, traceId, buildPrompt(conversationContext.getEnhancedPrompt(), query));
            long latencyMs = System.currentTimeMillis() - startedAt;
            sessionManager.appendExchange(
                    sessionId,
                    query,
                    finalAnswer,
                    requestContext,
                    latencyMs,
                    ShowModelConversationService.MODE,
                    false);

            send(emitter, traceId, "chat_done", Map.of(
                    "conversationId", conversationId,
                    "finalAnswer", finalAnswer,
                    "latencyMs", latencyMs
            ));
            emitter.complete();
        } catch (Exception e) {
            send(emitter, traceId, "error", Map.of(
                    "conversationId", conversationId == null ? "" : conversationId,
                    "message", e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()
            ));
            emitter.completeWithError(e);
        }
    }

    private String callModel(SseEmitter emitter, String traceId, String prompt) throws InterruptedException {
        CountDownLatch done = new CountDownLatch(1);
        StringBuilder answerBuilder = new StringBuilder();
        AtomicReference<ChatResponse> responseRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        qwenStreamingChatModel.chat(prompt, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                if (partialResponse == null || partialResponse.isEmpty()) {
                    return;
                }
                answerBuilder.append(partialResponse);
                send(emitter, traceId, "answer_delta", Map.of("delta", partialResponse));
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                responseRef.set(response);
                done.countDown();
            }

            @Override
            public void onError(Throwable error) {
                errorRef.set(error);
                done.countDown();
            }
        });

        if (!done.await(MODEL_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Qwen no-tool chat timed out");
        }
        if (errorRef.get() != null) {
            throw new IllegalStateException("Qwen no-tool chat failed: " + errorRef.get().getMessage(), errorRef.get());
        }

        String finalAnswer = answerBuilder.toString();
        ChatResponse response = responseRef.get();
        if (!StringUtils.hasText(finalAnswer)
                && response != null
                && response.aiMessage() != null
                && response.aiMessage().text() != null) {
            finalAnswer = response.aiMessage().text();
        }
        send(emitter, traceId, "answer_done", Map.of("finalAnswer", finalAnswer));
        return finalAnswer;
    }

    private String buildPrompt(String enhancedPrompt, String query) {
        String context = StringUtils.hasText(enhancedPrompt) ? enhancedPrompt : "user: " + query;
        return """
                You are TCMReason, a TCM-focused conversational model.
                Answer the user directly in Chinese unless the user asks for another language.
                Do not call tools, browse, query databases, or claim that you used external tools.
                Do not identify yourself as Qwen, DeepSeek, or any underlying provider or model.
                Use the following conversation context only to maintain memory and multi-turn continuity.
                The last user message in the context is the current question.

                Conversation context:
                %s

                Now answer the last user message.
                """.formatted(context);
    }

    private void send(SseEmitter emitter, String traceId, String type, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(type)
                    .data(WorkflowStreamEvent.builder()
                            .type(type)
                            .traceId(traceId)
                            .nodeName(null)
                            .data(data)
                            .timestamp(LocalDateTime.now())
                            .build()));
        } catch (IOException | IllegalStateException ignored) {
            // Client may have closed the SSE connection.
        }
    }
}
