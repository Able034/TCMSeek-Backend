package com.tcmseek.tcmseekagentservice.memory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmseek.tcmseekagentservice.config.AgentCacheProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class AgentRedisCache {

    private static final Logger log = LoggerFactory.getLogger(AgentRedisCache.class);

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final AgentCacheProperties properties;

    public AgentRedisCache(StringRedisTemplate redisTemplate,
                           ObjectMapper objectMapper,
                           AgentCacheProperties properties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public List<AgentMessage> getContext(String userId, String conversationId, int limit) {
        if (!enabled() || !StringUtils.hasText(conversationId) || limit <= 0) {
            return List.of();
        }
        String key = contextKey(userId, conversationId);
        try {
            List<String> values = redisTemplate.opsForList().range(key, -limit, -1);
            if (values == null || values.isEmpty()) {
                return List.of();
            }
            List<AgentMessage> messages = new ArrayList<>();
            for (String value : values) {
                AgentMessage message = fromJson(value, AgentMessage.class);
                if (message != null && StringUtils.hasText(message.getContent())) {
                    messages.add(message);
                }
            }
            refreshTtl(key, properties.getContextTtl());
            return messages;
        } catch (RuntimeException ex) {
            log.warn("read agent context from redis failed key={} message={}", key, ex.getMessage());
            return List.of();
        }
    }

    public void putContext(String userId, String conversationId, List<AgentMessage> messages, int limit) {
        if (!enabled() || !StringUtils.hasText(conversationId)) {
            return;
        }
        String key = contextKey(userId, conversationId);
        try {
            redisTemplate.delete(key);
            List<String> values = messagesToJson(messages, limit);
            if (!values.isEmpty()) {
                redisTemplate.opsForList().rightPushAll(key, values);
            }
            refreshTtl(key, properties.getContextTtl());
        } catch (RuntimeException ex) {
            log.warn("write agent context to redis failed key={} message={}", key, ex.getMessage());
        }
    }

    public AgentConversationSummaryState getSummary(String userId, String conversationId) {
        if (!enabled() || !StringUtils.hasText(userId) || !StringUtils.hasText(conversationId)) {
            return null;
        }
        String key = summaryKey(userId, conversationId);
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(json)) {
                return null;
            }
            AgentConversationSummaryState summary =
                    objectMapper.readValue(json, AgentConversationSummaryState.class);
            refreshTtl(key, properties.getSummaryTtl());
            return summary;
        } catch (Exception ex) {
            log.warn("read agent summary from redis failed key={} message={}", key, ex.getMessage());
            return null;
        }
    }

    public void putSummary(String userId, String conversationId, AgentConversationSummaryState summary) {
        if (!enabled() || !StringUtils.hasText(userId) || !StringUtils.hasText(conversationId) || summary == null) {
            return;
        }
        String key = summaryKey(userId, conversationId);
        try {
            redisTemplate.opsForValue().set(
                    key,
                    objectMapper.writeValueAsString(summary),
                    positiveTtl(properties.getSummaryTtl()));
        } catch (RuntimeException | JsonProcessingException ex) {
            log.warn("write agent summary to redis failed key={} message={}", key, ex.getMessage());
        }
    }

    public List<AgentMemoryDto> getMemories(String userId) {
        if (!enabled() || !StringUtils.hasText(userId)) {
            return List.of();
        }
        String key = memoryKey(userId);
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(json)) {
                return List.of();
            }
            MemoryPayload payload = objectMapper.readValue(json, MemoryPayload.class);
            refreshTtl(key, properties.getMemoryTtl());
            return payload.getItems();
        } catch (Exception ex) {
            log.warn("read agent memories from redis failed key={} message={}", key, ex.getMessage());
            return List.of();
        }
    }

    public void putMemories(String userId, List<AgentMemoryDto> memories) {
        if (!enabled() || !StringUtils.hasText(userId)) {
            return;
        }
        String key = memoryKey(userId);
        try {
            MemoryPayload payload = new MemoryPayload();
            payload.setItems(memories == null ? List.of() : memories);
            redisTemplate.opsForValue().set(
                    key,
                    objectMapper.writeValueAsString(payload),
                    positiveTtl(properties.getMemoryTtl()));
        } catch (RuntimeException | JsonProcessingException ex) {
            log.warn("write agent memories to redis failed key={} message={}", key, ex.getMessage());
        }
    }

    public String contextKey(String userId, String conversationId) {
        return prefix() + ":ctx:" + owner(userId) + ":" + conversationId;
    }

    public String summaryKey(String userId, String conversationId) {
        return prefix() + ":summary:" + owner(userId) + ":" + conversationId;
    }

    public String memoryKey(String userId) {
        return prefix() + ":memory:" + owner(userId);
    }

    private List<String> messagesToJson(List<AgentMessage> messages, int limit) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        int fromIndex = Math.max(0, messages.size() - Math.max(1, limit));
        List<String> values = new ArrayList<>();
        for (AgentMessage message : messages.subList(fromIndex, messages.size())) {
            if (message == null || !StringUtils.hasText(message.getContent())) {
                continue;
            }
            try {
                values.add(objectMapper.writeValueAsString(message));
            } catch (JsonProcessingException ex) {
                log.warn("serialize agent context message failed message={}", ex.getMessage());
            }
        }
        return values;
    }

    private <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception ex) {
            log.warn("deserialize agent redis value failed message={}", ex.getMessage());
            return null;
        }
    }

    private void refreshTtl(String key, Duration ttl) {
        redisTemplate.expire(key, positiveTtl(ttl));
    }

    private Duration positiveTtl(Duration ttl) {
        return ttl == null || ttl.isZero() || ttl.isNegative() ? Duration.ofMinutes(30) : ttl;
    }

    private boolean enabled() {
        return properties.isEnabled();
    }

    private String prefix() {
        return StringUtils.hasText(properties.getKeyPrefix()) ? properties.getKeyPrefix() : "agent";
    }

    private String owner(String userId) {
        return StringUtils.hasText(userId) ? userId : "anonymous";
    }

    private static class MemoryPayload {

        private List<AgentMemoryDto> items = new ArrayList<>();

        public List<AgentMemoryDto> getItems() {
            return items;
        }

        public void setItems(List<AgentMemoryDto> items) {
            this.items = items == null ? new ArrayList<>() : items;
        }
    }
}
