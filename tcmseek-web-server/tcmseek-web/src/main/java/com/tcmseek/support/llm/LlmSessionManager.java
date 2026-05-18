package com.tcmseek.support.llm;

import com.tcmseek.dto.llm.LlmMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;

/**
 * 简单的内存会话管理与摘要压缩，避免请求过长。
 */
@Component
public class LlmSessionManager {

    private final ConcurrentMap<String, LlmSessionState> sessions = new ConcurrentHashMap<>();
    private final LlmProperties properties;
    /** 会话过期时间：不活跃多久自动清理 */
    private final Duration sessionTtl = Duration.ofMinutes(60);
    /** 会话总量上限，超出后按最久未访问淘汰 */
    private final int maxSessions = 5000;

    public LlmSessionManager(LlmProperties properties) {
        this.properties = properties;
    }

    public LlmSessionState getState(String sessionId) {
        String sid = StringUtils.hasText(sessionId) ? sessionId : "default";
        LlmSessionState state = sessions.computeIfAbsent(sid, key -> new LlmSessionState());
        touch(state);
        return state;
    }

    public void appendUserMessages(String sessionId, List<LlmMessage> incoming) {
        if (incoming == null || incoming.isEmpty()) {
            return;
        }
        LlmSessionState state = getState(sessionId);
        synchronized (state) {
            for (LlmMessage raw : incoming) {
                if (raw == null || !StringUtils.hasText(raw.getContent())) {
                    continue;
                }
                LlmMessage cleaned = sanitize(raw, "user");
                state.getMessages().add(cleaned);
            }
            touch(state);
            maybeSummarize(state);
        }
    }

    public void appendAssistantReply(String sessionId, String reply) {
        if (!StringUtils.hasText(reply)) {
            return;
        }
        LlmSessionState state = getState(sessionId);
        synchronized (state) {
            LlmMessage message = new LlmMessage();
            message.setRole("assistant");
            message.setContent(sanitizeContent(reply));
            state.getMessages().add(message);
            touch(state);
            maybeSummarize(state);
        }
    }

    /**
     * 构造上下文：system（附带摘要） + 已存历史。
     */
    public List<LlmMessage> buildContext(String sessionId) {
        LlmSessionState state = getState(sessionId);
        String summary;
        List<LlmMessage> history;
        synchronized (state) {
            summary = state.getSummary();
            history = new ArrayList<>(state.getMessages());
            touch(state);
        }
        LlmMessage system = new LlmMessage();
        system.setRole("system");
        if (StringUtils.hasText(summary)) {
            system.setContent(properties.getSystemPrompt()
                    + "\n\n【记忆摘要】以下为此前多轮对话的关键信息，请注意保持前后回答一致：" + summary);
        } else {
            system.setContent(properties.getSystemPrompt());
        }
        List<LlmMessage> context = new ArrayList<>(history.size() + 1);
        context.add(system);
        context.addAll(history);
        return context;
    }

    private LlmMessage sanitize(LlmMessage raw, String defaultRole) {
        LlmMessage cleaned = new LlmMessage();
        String role = StringUtils.hasText(raw.getRole()) ? raw.getRole() : defaultRole;
        cleaned.setRole(role);
        cleaned.setContent(sanitizeContent(raw.getContent()));
        return cleaned;
    }

    private String sanitizeContent(String content) {
        if (content == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            if (ch == '\t' || ch == '\n' || ch == '\r') {
                sb.append(ch);
                continue;
            }
            // 跳过控制字符
            if (Character.getType(ch) == Character.CONTROL) {
                continue;
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    private void maybeSummarize(LlmSessionState state) {
        List<LlmMessage> messages = state.getMessages();
        if (messages.size() <= properties.getSummaryTriggerMessages()) {
            return;
        }
        int keep = Math.max(properties.getSummaryKeepRecent(), 2);
        int cutoff = Math.max(messages.size() - keep, 0);
        if (cutoff <= 0) {
            return;
        }
        List<LlmMessage> historySlice = messages.subList(0, cutoff);
        String existing = state.getSummary();
        String newSummary = summarize(existing, historySlice);
        state.setSummary(newSummary);
        List<LlmMessage> tail = new ArrayList<>(messages.subList(messages.size() - keep, messages.size()));
        state.getMessages().clear();
        state.getMessages().addAll(tail);
    }

    private String summarize(String existing, List<LlmMessage> history) {
        List<String> fragments = new ArrayList<>();
        if (StringUtils.hasText(existing)) {
            fragments.add(existing);
        }
        for (LlmMessage msg : history) {
            if (msg == null || !StringUtils.hasText(msg.getContent())) {
                continue;
            }
            String role = msg.getRole() == null ? "" : msg.getRole().toLowerCase();
            String label = "";
            if (role.startsWith("user")) {
                label = "【患者】";
            } else if (role.startsWith("assistant")) {
                label = "【医师】";
            }
            fragments.add(label + msg.getContent());
        }
        if (fragments.isEmpty()) {
            return StringUtils.hasText(existing) ? existing : "";
        }
        String joined = String.join("；", fragments);
        int maxLen = Math.max(properties.getSummaryMaxLength(), 50);
        if (joined.length() > maxLen) {
            return joined.substring(0, maxLen).trim() + "…";
        }
        return joined;
    }

    public List<String> listSessions() {
        return new ArrayList<>(sessions.keySet());
    }

    public void clear(String sessionId) {
        if (StringUtils.hasText(sessionId)) {
            sessions.remove(sessionId);
        } else {
            sessions.clear();
        }
    }

    public int snapshotSize(String sessionId) {
        return getState(sessionId).getMessages().size();
    }

    public void rollbackToSize(String sessionId, int size) {
        LlmSessionState state = getState(sessionId);
        synchronized (state) {
            if (size < 0) {
                size = 0;
            }
            if (size < state.getMessages().size()) {
                state.getMessages().subList(size, state.getMessages().size()).clear();
            }
        }
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000L, initialDelay = 5 * 60 * 1000L)
    public void scheduledCleanup() {
        Instant now = Instant.now();
        // 1) 清理过期
        sessions.entrySet().removeIf(entry -> isExpired(entry.getValue(), now));

        // 2) 如果超量，按最久未访问淘汰
        int currentSize = sessions.size();
        if (currentSize <= maxSessions) {
            return;
        }
        int toRemove = currentSize - maxSessions;
        List<String> victims = sessions.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getValue().getLastAccess()))
                .limit(toRemove)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        victims.forEach(sessions::remove);
    }

    private boolean isExpired(LlmSessionState state, Instant now) {
        if (state == null) {
            return true;
        }
        long last = state.getLastAccess();
        if (last <= 0) {
            return false;
        }
        return now.toEpochMilli() - last > sessionTtl.toMillis();
    }

    private void touch(LlmSessionState state) {
        state.setLastAccess(Instant.now().toEpochMilli());
    }

    /**
     * 内部会话状态。
     */
    public static class LlmSessionState {
        private final List<LlmMessage> messages = new ArrayList<>();
        private String summary = "";
        private long lastAccess = Instant.now().toEpochMilli();

        public List<LlmMessage> getMessages() {
            return messages;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public long getLastAccess() {
            return lastAccess;
        }

        public void setLastAccess(long lastAccess) {
            this.lastAccess = lastAccess;
        }
    }
}
