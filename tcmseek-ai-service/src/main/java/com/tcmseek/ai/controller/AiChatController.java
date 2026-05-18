package com.tcmseek.ai.controller;

import com.tcmseek.ai.dto.AiChatRequest;
import com.tcmseek.ai.dto.AiChatResponse;
import com.tcmseek.ai.exception.AiServiceException;
import com.tcmseek.ai.service.AiRequestContext;
import com.tcmseek.ai.service.AiChatService;
import com.tcmseek.ai.service.CsvExportStore;
import com.tcmseek.ai.service.CsvRenderer;
import com.tcmseek.ai.service.TcmReasonChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/ai")
public class AiChatController {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private static final String USER_ID_HEADER = "X-User-Id";

    private static final String USERNAME_HEADER = "X-User-Name";

    private static final String ACCOUNT_HEADER = "X-User-Account";

    private final AiChatService aiChatService;

    private final TcmReasonChatService tcmReasonChatService;

    private final CsvExportStore csvExportStore;

    private final CsvRenderer csvRenderer;

    private final ExecutorService aiModelExecutor;

    private final ScheduledExecutorService aiStreamHeartbeatExecutor;

    public AiChatController(AiChatService aiChatService,
                            TcmReasonChatService tcmReasonChatService,
                            CsvExportStore csvExportStore,
                            CsvRenderer csvRenderer,
                            @Qualifier("aiModelExecutor") ExecutorService aiModelExecutor,
                            @Qualifier("aiStreamHeartbeatExecutor") ScheduledExecutorService aiStreamHeartbeatExecutor) {
        this.aiChatService = aiChatService;
        this.tcmReasonChatService = tcmReasonChatService;
        this.csvExportStore = csvExportStore;
        this.csvRenderer = csvRenderer;
        this.aiModelExecutor = aiModelExecutor;
        this.aiStreamHeartbeatExecutor = aiStreamHeartbeatExecutor;
    }

    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public AiChatResponse chat(@Valid @RequestBody AiChatRequest request,
                               @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
                               @RequestHeader(value = USER_ID_HEADER, required = false) String userId,
                               @RequestHeader(value = USERNAME_HEADER, required = false) String username,
                               @RequestHeader(value = ACCOUNT_HEADER, required = false) String account) {
        return tcmReasonChatService.chat(request, new AiRequestContext(requestId, userId, username, account));
    }

    @PostMapping(value = "/aichat", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public AiChatResponse aichat(@Valid @RequestBody AiChatRequest request,
                                 @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
                                 @RequestHeader(value = USER_ID_HEADER, required = false) String userId,
                                 @RequestHeader(value = USERNAME_HEADER, required = false) String username,
                                 @RequestHeader(value = ACCOUNT_HEADER, required = false) String account) {
        return aiChatService.chat(request, new AiRequestContext(requestId, userId, username, account));
    }

    @PostMapping(value = "/aichat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter aichatStream(@Valid @RequestBody AiChatRequest request,
                                   @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
                                   @RequestHeader(value = USER_ID_HEADER, required = false) String userId,
                                   @RequestHeader(value = USERNAME_HEADER, required = false) String username,
                                   @RequestHeader(value = ACCOUNT_HEADER, required = false) String account) {
        SseEmitter emitter = new SseEmitter(0L);
        Object sendLock = new Object();
        AtomicBoolean completed = new AtomicBoolean(false);
        AtomicReference<ScheduledFuture<?>> heartbeatRef = new AtomicReference<>();
        ScheduledFuture<?> heartbeat = aiStreamHeartbeatExecutor.scheduleAtFixedRate(() -> {
                    if (completed.get()) {
                        ScheduledFuture<?> future = heartbeatRef.get();
                        if (future != null) {
                            future.cancel(false);
                        }
                        return;
                    }
                    sendEvent(emitter, sendLock, completed, "heartbeat", Map.of("ts", System.currentTimeMillis()));
                },
                15,
                15,
                TimeUnit.SECONDS);
        heartbeatRef.set(heartbeat);

        Runnable cleanup = () -> {
            completed.set(true);
            heartbeat.cancel(true);
        };
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(error -> cleanup.run());

        sendEvent(emitter, sendLock, completed, "start", Map.of("provider", "deepseek"));
        aiModelExecutor.execute(() -> {
            try {
                aiChatService.stream(
                        request,
                        new AiRequestContext(requestId, userId, username, account),
                        (event, data) -> {
                            sendEvent(emitter, sendLock, completed, event, data);
                            if (completed.get()) {
                                throw new IllegalStateException("SSE emitter closed");
                            }
                        });
                sendEvent(emitter, sendLock, completed, "done", Map.of());
                if (completed.compareAndSet(false, true)) {
                    heartbeat.cancel(true);
                    emitter.complete();
                }
            } catch (RuntimeException ex) {
                sendEvent(emitter, sendLock, completed, "error", Map.of("message", streamErrorMessage(ex)));
                if (completed.compareAndSet(false, true)) {
                    heartbeat.cancel(true);
                    emitter.complete();
                }
            }
        });
        return emitter;
    }

    @GetMapping(value = "/exports/{exportId}", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> exportCsv(@PathVariable String exportId,
                                            @RequestHeader(value = USER_ID_HEADER, required = false) String userId) {
        CsvExportStore.ExportRecord record = csvExportStore.get(exportId, userId);
        if (record == null) {
            throw new ResponseStatusException(NOT_FOUND, "export not found or expired");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + record.getFilename() + "\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body("\uFEFF" + csvRenderer.render(record));
    }

    private void sendEvent(SseEmitter emitter,
                           Object sendLock,
                           AtomicBoolean completed,
                           String event,
                           Object data) {
        if (completed.get()) {
            return;
        }
        synchronized (sendLock) {
            if (completed.get()) {
                return;
            }
            try {
                emitter.send(SseEmitter.event()
                        .name(event)
                        .data(data == null ? Map.of() : data, MediaType.APPLICATION_JSON));
            } catch (IOException | IllegalStateException ex) {
                completed.set(true);
            }
        }
    }

    private String streamErrorMessage(RuntimeException ex) {
        if (ex instanceof AiServiceException aiServiceException) {
            return aiServiceException.getUserMessage();
        }
        return "AI model service is temporarily unavailable.";
    }
}
