package com.tcmseek.tcmseekagentservice.memory;

import com.tcmseek.tcmseekagentservice.config.AgentConversationStorageProperties;
import com.tcmseek.tcmseekagentservice.service.AgentRequestContext;
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
public class AgentSessionManager {

    private static final Logger log = LoggerFactory.getLogger(AgentSessionManager.class);

    private static final int MAX_SESSIONS = 5000;

    private static final Duration SESSION_TTL = Duration.ofMinutes(60);

    private final AgentConversationRepository conversationRepository;

    private final AgentConversationStorageProperties storageProperties;

    private final AgentRedisCache redisCache;

    private final AgentConversationMemoryService memoryService;

    private final ConcurrentMap<String, SessionState> sessions = new ConcurrentHashMap<>();

    public AgentSessionManager(AgentConversationRepository conversationRepository,
                               AgentConversationStorageProperties storageProperties,
                               AgentRedisCache redisCache,
                               AgentConversationMemoryService memoryService) {
        this.conversationRepository = conversationRepository;
        this.storageProperties = storageProperties;
        this.redisCache = redisCache;
        this.memoryService = memoryService;
    }

    public AgentConversationContext buildContext(String sessionId,
                                                 String query,
                                                 AgentRequestContext requestContext) {
        return buildContext(sessionId, query, requestContext, true);
    }

    public AgentConversationContext buildContext(String sessionId,
                                                 String query,
                                                 AgentRequestContext requestContext,
                                                 boolean includeLongTermMemory) {
        String userId = requestContext == null ? null : requestContext.getUserId();
        String conversationId = conversationKey(sessionId, userId);
        String stateKey = localStateKey(userId, conversationId);
        List<AgentMessage> incomingMessages = StringUtils.hasText(query)
                ? List.of(AgentMessage.user(query.trim()))
                : List.of();

        List<AgentMessage> storedMessages = loadCachedMessages(userId, conversationId);
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

        List<AgentMessage> context = mergeContext(storedMessages, incomingMessages);
        List<AgentMessage> enriched = memoryService.enrichContext(
                userId,
                conversationId,
                trimToLimit(context),
                includeLongTermMemory);
        return new AgentConversationContext(conversationId, buildPrompt(enriched));
    }

    public void appendExchange(String sessionId,
                               String query,
                               String reply,
                               AgentRequestContext requestContext,
                               long latencyMs) {
        appendExchange(sessionId, query, reply, requestContext, latencyMs, "academic", true);
    }

    public void appendExchange(String sessionId,
                               String query,
                               String reply,
                               AgentRequestContext requestContext,
                               long latencyMs,
                               String mode,
                               boolean includeLongTermMemory) {
        if (!StringUtils.hasText(query) || !StringUtils.hasText(reply)) {
            return;
        }

        String userId = requestContext == null ? null : requestContext.getUserId();
        String conversationId = conversationKey(sessionId, userId);
        String stateKey = localStateKey(userId, conversationId);
        updateLocalState(stateKey, query.trim(), reply);
        cacheContext(userId, conversationId, localMessages(stateKey));
        persistExchange(conversationId, query.trim(), reply, requestContext, latencyMs, mode);
        memoryService.refreshAfterExchangeAsync(conversationId, userId, includeLongTermMemory);
        cleanupIfNeeded();
    }

    private List<AgentMessage> loadCachedMessages(String userId, String conversationId) {
        List<AgentMessage> messages = redisCache.getContext(userId, conversationId, contextLimit());
        if (!messages.isEmpty()) {
            replaceLocalState(localStateKey(userId, conversationId), messages);
        }
        return messages;
    }

    private List<AgentMessage> loadStoredMessages(String userId, String conversationId) {
        if (!storageProperties.isPersistenceEnabled()) {
            return List.of();
        }
        try {
            return conversationRepository.findRecentMessages(userId, conversationId, contextLimit());
        } catch (RuntimeException ex) {
            log.warn("load agent conversation context from postgres failed conversationId={} message={}",
                    conversationId, ex.getMessage());
            return List.of();
        }
    }

    private void persistExchange(String conversationId,
                                 String query,
                                 String reply,
                                 AgentRequestContext requestContext,
                                 long latencyMs,
                                 String mode) {
        if (!storageProperties.isPersistenceEnabled()) {
            return;
        }
        try {
            conversationRepository.saveExchange(
                    conversationId,
                    requestContext == null ? null : requestContext.getUserId(),
                    query,
                    reply,
                    requestContext == null ? null : requestContext.getRequestId(),
                    latencyMs,
                    mode);
        } catch (RuntimeException ex) {
            log.warn("persist agent conversation to postgres failed conversationId={} message={}",
                    conversationId, ex.getMessage(), ex);
        }
    }

    private List<AgentMessage> mergeContext(List<AgentMessage> storedMessages, List<AgentMessage> incomingMessages) {
        if (storedMessages.isEmpty()) {
            return new ArrayList<>(incomingMessages);
        }
        if (incomingMessages.isEmpty()) {
            return new ArrayList<>(storedMessages);
        }
        int matchedIndex = lastIncomingIndexOf(incomingMessages, storedMessages.get(storedMessages.size() - 1));
        List<AgentMessage> context = new ArrayList<>(storedMessages);
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

    private int lastIncomingIndexOf(List<AgentMessage> incomingMessages, AgentMessage target) {
        String fingerprint = fingerprint(target);
        for (int i = incomingMessages.size() - 1; i >= 0; i--) {
            if (fingerprint.equals(fingerprint(incomingMessages.get(i)))) {
                return i;
            }
        }
        return -1;
    }

    private List<AgentMessage> localMessages(String stateKey) {
        SessionState state = getState(stateKey);
        synchronized (state) {
            return new ArrayList<>(state.getMessages());
        }
    }

    private void updateLocalState(String stateKey, String query, String reply) {
        SessionState state = getState(stateKey);
        synchronized (state) {
            state.getMessages().add(AgentMessage.user(query));
            state.getMessages().add(AgentMessage.assistant(reply));
            trimMessages(state);
            touch(state);
        }
    }

    private void replaceLocalState(String stateKey, List<AgentMessage> messages) {
        SessionState state = getState(stateKey);
        synchronized (state) {
            state.getMessages().clear();
            state.getMessages().addAll(trimToLimit(sanitizeMessages(messages)));
            touch(state);
        }
    }

    private void cacheContext(String userId, String conversationId, List<AgentMessage> messages) {
        redisCache.putContext(userId, conversationId, trimToLimit(sanitizeMessages(messages)), contextLimit());
    }

    private SessionState getState(String stateKey) {
        SessionState state = sessions.computeIfAbsent(stateKey, ignored -> new SessionState());
        touch(state);
        return state;
    }

    private List<AgentMessage> sanitizeMessages(List<AgentMessage> messages) {
        if (messages == null) {
            return new ArrayList<>();
        }
        return messages.stream()
                .filter(message -> message != null && StringUtils.hasText(message.getContent()))
                .map(this::copyMessage)
                .collect(Collectors.toList());
    }

    private AgentMessage copyMessage(AgentMessage source) {
        AgentMessage copy = new AgentMessage();
        copy.setRole(StringUtils.hasText(source.getRole()) ? source.getRole() : "user");
        copy.setContent(source.getContent());
        return copy;
    }

    private void trimMessages(SessionState state) {
        List<AgentMessage> messages = state.getMessages();
        int limit = contextLimit();
        if (messages.size() <= limit) {
            return;
        }
        messages.subList(0, messages.size() - limit).clear();
    }

    private List<AgentMessage> trimToLimit(List<AgentMessage> messages) {
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

    private String buildPrompt(List<AgentMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        return messages.stream()
                .filter(message -> message != null && StringUtils.hasText(message.getContent()))
                .map(this::formatMessage)
                .collect(Collectors.joining("\n"));
    }

    private String formatMessage(AgentMessage message) {
        String role = StringUtils.hasText(message.getRole())
                ? message.getRole().toLowerCase(Locale.ROOT)
                : "user";
        return role + ": " + message.getContent();
    }

    private String fingerprint(AgentMessage message) {
        if (message == null) {
            return "";
        }
        String role = StringUtils.hasText(message.getRole())
                ? message.getRole().toLowerCase(Locale.ROOT)
                : "user";
        return role + "\n" + (message.getContent() == null ? "" : message.getContent());
    }

    private static class SessionState {

        private final List<AgentMessage> messages = new ArrayList<>();

        private long lastAccess = Instant.now().toEpochMilli();

        public List<AgentMessage> getMessages() {
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
