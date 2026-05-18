package com.tcmseek.ai.service;

import com.tcmseek.ai.config.AiConversationStorageProperties;
import com.tcmseek.ai.dto.AiChatResponse;
import com.tcmseek.ai.dto.AiMessage;
import com.tcmseek.ai.dto.ToolCallResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
public class AiSessionManager {

    private static final Logger log = LoggerFactory.getLogger(AiSessionManager.class);

    private static final int MAX_SESSIONS = 5000;
    private static final Duration SESSION_TTL = Duration.ofMinutes(60);

    private final AiConversationRepository conversationRepository;

    private final AiConversationStorageProperties storageProperties;

    private final AiRedisCache redisCache;

    private final AiConversationMemoryService memoryService;

    private final ConcurrentMap<String, SessionState> sessions = new ConcurrentHashMap<>();

    public AiSessionManager(AiConversationRepository conversationRepository,
                            AiConversationStorageProperties storageProperties,
                            AiRedisCache redisCache,
                            AiConversationMemoryService memoryService) {
        this.conversationRepository = conversationRepository;
        this.storageProperties = storageProperties;
        this.redisCache = redisCache;
        this.memoryService = memoryService;
    }

    public List<AiMessage> buildContext(String sessionId, List<AiMessage> incoming, AiRequestContext requestContext) {
        String userId = requestContext == null ? null : requestContext.getUserId();
        String conversationId = conversationKey(sessionId, userId);
        String stateKey = localStateKey(userId, conversationId);
        List<AiMessage> incomingMessages = sanitizeMessages(incoming);
        List<AiMessage> storedMessages = loadCachedMessages(userId, conversationId);
        if (storedMessages.isEmpty()) {
            storedMessages = loadStoredMessages(userId, conversationId);
            if (!storedMessages.isEmpty()) {
                replaceLocalState(stateKey, storedMessages);
                cacheContext(userId, conversationId, storedMessages);
            }
        }
        if (storedMessages.isEmpty()) {
            storedMessages = localMessages(stateKey);
        }
        List<AiMessage> context = mergeContext(storedMessages, incomingMessages);
        return memoryService.enrichContext(userId, conversationId, trimToLimit(context));
    }

    public void appendExchange(String sessionId,
                               List<AiMessage> incoming,
                               String reply,
                               AiRequestContext requestContext,
                               AiChatResponse response,
                               List<ToolCallResult> toolResults,
                               long latencyMs) {
        AiMessage latestUserMessage = latestUserMessage(incoming);
        if (latestUserMessage == null || !StringUtils.hasText(reply)) {
            return;
        }

        String userId = requestContext == null ? null : requestContext.getUserId();
        String conversationId = conversationKey(sessionId, userId);
        String stateKey = localStateKey(userId, conversationId);
        updateLocalState(stateKey, latestUserMessage, reply);
        cacheContext(userId, conversationId, localMessages(stateKey));
        persistExchange(conversationId, latestUserMessage, reply, requestContext, response, toolResults, latencyMs);
        memoryService.refreshAfterExchangeAsync(conversationId, userId);
        cleanupIfNeeded();
    }

    private List<AiMessage> loadCachedMessages(String userId, String conversationId) {
        List<AiMessage> messages = redisCache.getContext(userId, conversationId, contextLimit());
        if (!messages.isEmpty()) {
            replaceLocalState(localStateKey(userId, conversationId), messages);
        }
        return messages;
    }

    private List<AiMessage> loadStoredMessages(String userId, String conversationId) {
        if (!storageProperties.isPersistenceEnabled()) {
            return List.of();
        }
        try {
            return conversationRepository.findRecentMessages(userId, conversationId, contextLimit());
        } catch (RuntimeException ex) {
            log.warn("load ai conversation context from postgres failed conversationId={} message={}",
                    conversationId, ex.getMessage());
            return List.of();
        }
    }

    private void persistExchange(String conversationId,
                                 AiMessage latestUserMessage,
                                 String reply,
                                 AiRequestContext requestContext,
                                 AiChatResponse response,
                                 List<ToolCallResult> toolResults,
                                 long latencyMs) {
        if (!storageProperties.isPersistenceEnabled()) {
            return;
        }
        try {
            AiConversationExchange exchange = new AiConversationExchange();
            exchange.setConversationId(conversationId);
            exchange.setUserId(requestContext == null ? null : requestContext.getUserId());
            exchange.setMode(modeFromConversationId(conversationId));
            exchange.setUserMessage(latestUserMessage);
            exchange.setAssistantMessageId(response == null ? null : response.getId());
            exchange.setAssistantContent(reply);
            exchange.setProvider(response == null ? null : response.getProvider());
            exchange.setModel(response == null ? null : response.getModel());
            exchange.setFinishReason(response == null ? null : response.getFinishReason());
            exchange.setTotalResults(response == null ? null : response.getTotalResults());
            exchange.setDisplayedResults(response == null ? null : response.getDisplayedResults());
            exchange.setCsvExportId(response == null ? null : exportIdFromUrl(response.getCsvDownloadUrl()));
            exchange.setRequestId(requestContext == null ? null : requestContext.getRequestId());
            exchange.setLatencyMs(Math.toIntExact(Math.min(Integer.MAX_VALUE, Math.max(0, latencyMs))));
            exchange.setToolResults(toolResults == null ? List.of() : toolResults);
            conversationRepository.saveExchange(exchange);
        } catch (RuntimeException ex) {
            log.warn("persist ai conversation to postgres failed conversationId={} message={}",
                    conversationId, ex.getMessage(), ex);
        }
    }

    private List<AiMessage> mergeContext(List<AiMessage> storedMessages, List<AiMessage> incomingMessages) {
        if (storedMessages.isEmpty()) {
            return new ArrayList<>(incomingMessages);
        }
        if (incomingMessages.isEmpty()) {
            return new ArrayList<>(storedMessages);
        }
        int matchedIndex = lastIncomingIndexOf(incomingMessages, storedMessages.get(storedMessages.size() - 1));
        List<AiMessage> context = new ArrayList<>(storedMessages);
        if (matchedIndex >= 0) {
            context.addAll(incomingMessages.subList(matchedIndex + 1, incomingMessages.size()));
            return context;
        }
        if (incomingMessages.size() == 1) {
            context.addAll(incomingMessages);
            return context;
        }
        return new ArrayList<>(incomingMessages);
    }

    private int lastIncomingIndexOf(List<AiMessage> incomingMessages, AiMessage target) {
        String fingerprint = fingerprint(target);
        for (int i = incomingMessages.size() - 1; i >= 0; i--) {
            if (fingerprint.equals(fingerprint(incomingMessages.get(i)))) {
                return i;
            }
        }
        return -1;
    }

    private List<AiMessage> localMessages(String conversationId) {
        SessionState state = getState(conversationId);
        synchronized (state) {
            return new ArrayList<>(state.getMessages());
        }
    }

    private void updateLocalState(String conversationId, AiMessage latestUserMessage, String reply) {
        SessionState state = getState(conversationId);
        synchronized (state) {
            state.getMessages().add(copyMessage(latestUserMessage));
            AiMessage assistant = new AiMessage();
            assistant.setRole("assistant");
            assistant.setContent(reply);
            state.getMessages().add(assistant);
            trimMessages(state);
            touch(state);
        }
    }

    private void replaceLocalState(String conversationId, List<AiMessage> messages) {
        SessionState state = getState(conversationId);
        synchronized (state) {
            state.getMessages().clear();
            state.getMessages().addAll(trimToLimit(sanitizeMessages(messages)));
            touch(state);
        }
    }

    private void cacheContext(String userId, String conversationId, List<AiMessage> messages) {
        redisCache.putContext(userId, conversationId, trimToLimit(sanitizeMessages(messages)), contextLimit());
    }

    private SessionState getState(String conversationId) {
        SessionState state = sessions.computeIfAbsent(conversationId, ignored -> new SessionState());
        touch(state);
        return state;
    }

    private List<AiMessage> sanitizeMessages(List<AiMessage> messages) {
        if (messages == null) {
            return new ArrayList<>();
        }
        return messages.stream()
                .filter(message -> message != null && StringUtils.hasText(message.getContent()))
                .map(this::copyMessage)
                .collect(Collectors.toList());
    }

    private AiMessage latestUserMessage(List<AiMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        for (int i = messages.size() - 1; i >= 0; i--) {
            AiMessage message = messages.get(i);
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
        return null;
    }

    private AiMessage copyMessage(AiMessage source) {
        AiMessage copy = new AiMessage();
        copy.setRole(StringUtils.hasText(source.getRole()) ? source.getRole() : "user");
        copy.setContent(source.getContent());
        return copy;
    }

    private void trimMessages(SessionState state) {
        List<AiMessage> messages = state.getMessages();
        int limit = contextLimit();
        if (messages.size() <= limit) {
            return;
        }
        messages.subList(0, messages.size() - limit).clear();
    }

    private List<AiMessage> trimToLimit(List<AiMessage> messages) {
        int limit = contextLimit();
        if (messages.size() <= limit) {
            return messages;
        }
        return new ArrayList<>(messages.subList(messages.size() - limit, messages.size()));
    }

    private int contextLimit() {
        return Math.max(1, storageProperties.getContextMessageLimit());
    }

    private void cleanupIfNeeded() {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry ->
                now.toEpochMilli() - entry.getValue().getLastAccess() > SESSION_TTL.toMillis());
        if (sessions.size() <= MAX_SESSIONS) {
            return;
        }
        int removeCount = sessions.size() - MAX_SESSIONS;
        List<String> victims = sessions.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getValue().getLastAccess()))
                .limit(removeCount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        victims.forEach(sessions::remove);
    }

    private void touch(SessionState state) {
        state.setLastAccess(Instant.now().toEpochMilli());
    }

    private String conversationKey(String sessionId, String userId) {
        if (StringUtils.hasText(sessionId)) {
            return sessionId;
        }
        return StringUtils.hasText(userId) ? "default-" + userId : "default";
    }

    private String localStateKey(String userId, String conversationId) {
        String owner = StringUtils.hasText(userId) ? userId : "anonymous";
        return owner + ":" + conversationId;
    }

    private String modeFromConversationId(String conversationId) {
        if (StringUtils.hasText(conversationId)
                && conversationId.toLowerCase(Locale.ROOT).startsWith("general")) {
            return "general";
        }
        return "academic";
    }

    private String exportIdFromUrl(String csvDownloadUrl) {
        if (!StringUtils.hasText(csvDownloadUrl)) {
            return null;
        }
        int index = csvDownloadUrl.lastIndexOf('/');
        return index >= 0 ? csvDownloadUrl.substring(index + 1) : csvDownloadUrl;
    }

    private String fingerprint(AiMessage message) {
        if (message == null) {
            return "";
        }
        String role = StringUtils.hasText(message.getRole())
                ? message.getRole().toLowerCase(Locale.ROOT)
                : "user";
        return role + "\n" + (message.getContent() == null ? "" : message.getContent());
    }

    private static class SessionState {

        private final List<AiMessage> messages = new ArrayList<>();

        private long lastAccess = Instant.now().toEpochMilli();

        public List<AiMessage> getMessages() {
            return messages;
        }

        public long getLastAccess() {
            return lastAccess;
        }

        public void setLastAccess(long lastAccess) {
            this.lastAccess = lastAccess;
        }
    }
}
