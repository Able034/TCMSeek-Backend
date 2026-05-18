package com.tcmseek.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmseek.ai.config.AiCacheProperties;
import com.tcmseek.ai.dto.AiMemoryDto;
import com.tcmseek.ai.dto.AiMessage;
import com.tcmseek.ai.dto.ConversationSummaryState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class AiRedisCache {

    private static final Logger log = LoggerFactory.getLogger(AiRedisCache.class);

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final AiCacheProperties properties;

    public AiRedisCache(StringRedisTemplate redisTemplate,
                        ObjectMapper objectMapper,
                        AiCacheProperties properties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public List<AiMessage> getContext(String userId, String conversationId, int limit) {
        if (!enabled() || !StringUtils.hasText(conversationId) || limit <= 0) {
            return List.of();
        }
        String key = contextKey(userId, conversationId);
        try {
            List<String> values = redisTemplate.opsForList().range(key, -limit, -1);
            if (values == null || values.isEmpty()) {
                return List.of();
            }
            List<AiMessage> messages = new ArrayList<>();
            for (String value : values) {
                AiMessage message = fromJson(value, AiMessage.class);
                if (message != null && StringUtils.hasText(message.getContent())) {
                    messages.add(message);
                }
            }
            refreshTtl(key, properties.getContextTtl());
            return messages;
        } catch (RuntimeException ex) {
            log.warn("read ai context from redis failed key={} message={}", key, ex.getMessage());
            return List.of();
        }
    }

    public void putContext(String userId, String conversationId, List<AiMessage> messages, int limit) {
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
            log.warn("write ai context to redis failed key={} message={}", key, ex.getMessage());
        }
    }

    public ConversationSummaryState getSummary(String userId, String conversationId) {
        if (!enabled() || !StringUtils.hasText(conversationId)) {
            return null;
        }
        String key = summaryKey(userId, conversationId);
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(json)) {
                return null;
            }
            ConversationSummaryState summary = objectMapper.readValue(json, ConversationSummaryState.class);
            refreshTtl(key, properties.getSummaryTtl());
            return summary;
        } catch (Exception ex) {
            log.warn("read ai summary from redis failed key={} message={}", key, ex.getMessage());
            return null;
        }
    }

    public void putSummary(String userId, String conversationId, ConversationSummaryState summary) {
        if (!enabled() || !StringUtils.hasText(conversationId) || summary == null) {
            return;
        }
        String key = summaryKey(userId, conversationId);
        try {
            redisTemplate.opsForValue().set(
                    key,
                    objectMapper.writeValueAsString(summary),
                    positiveTtl(properties.getSummaryTtl()));
        } catch (RuntimeException | JsonProcessingException ex) {
            log.warn("write ai summary to redis failed key={} message={}", key, ex.getMessage());
        }
    }

    public List<AiMemoryDto> getMemories(String userId) {
        if (!enabled()) {
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
            log.warn("read ai memories from redis failed key={} message={}", key, ex.getMessage());
            return List.of();
        }
    }

    public void putMemories(String userId, List<AiMemoryDto> memories) {
        if (!enabled()) {
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
            log.warn("write ai memories to redis failed key={} message={}", key, ex.getMessage());
        }
    }

    public CsvExportStore.ExportRecord getExport(String userId, String exportId) {
        if (!enabled() || !StringUtils.hasText(exportId)) {
            return null;
        }
        String key = exportKey(userId, exportId);
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(json)) {
                return null;
            }
            ExportPayload payload = objectMapper.readValue(json, ExportPayload.class);
            refreshTtl(key, properties.getExportTtl());
            return payload.toRecord();
        } catch (Exception ex) {
            log.warn("read ai export from redis failed key={} message={}", key, ex.getMessage());
            return null;
        }
    }

    public void putExport(String userId, String exportId, CsvExportStore.ExportRecord record) {
        if (!enabled() || !StringUtils.hasText(exportId) || record == null) {
            return;
        }
        String key = exportKey(userId, exportId);
        try {
            String json = objectMapper.writeValueAsString(ExportPayload.from(record));
            redisTemplate.opsForValue().set(key, json, positiveTtl(properties.getExportTtl()));
        } catch (RuntimeException | JsonProcessingException ex) {
            log.warn("write ai export to redis failed key={} message={}", key, ex.getMessage());
        }
    }

    public String contextKey(String userId, String conversationId) {
        return prefix() + ":ctx:" + owner(userId) + ":" + conversationId;
    }

    public String exportKey(String userId, String exportId) {
        return prefix() + ":export:" + owner(userId) + ":" + exportId;
    }

    public String summaryKey(String userId, String conversationId) {
        return prefix() + ":summary:" + owner(userId) + ":" + conversationId;
    }

    public String memoryKey(String userId) {
        return prefix() + ":memory:" + owner(userId);
    }

    private List<String> messagesToJson(List<AiMessage> messages, int limit) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        int fromIndex = Math.max(0, messages.size() - Math.max(1, limit));
        List<String> values = new ArrayList<>();
        for (AiMessage message : messages.subList(fromIndex, messages.size())) {
            if (message == null || !StringUtils.hasText(message.getContent())) {
                continue;
            }
            try {
                values.add(objectMapper.writeValueAsString(message));
            } catch (JsonProcessingException ex) {
                log.warn("serialize ai context message failed message={}", ex.getMessage());
            }
        }
        return values;
    }

    private <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception ex) {
            log.warn("deserialize ai redis value failed message={}", ex.getMessage());
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
        return StringUtils.hasText(properties.getKeyPrefix()) ? properties.getKeyPrefix() : "ai";
    }

    private String owner(String userId) {
        return StringUtils.hasText(userId) ? userId : "anonymous";
    }

    private static class ExportPayload {

        private String filename;

        private List<String> headers = new ArrayList<>();

        private List<List<String>> rows = new ArrayList<>();

        public static ExportPayload from(CsvExportStore.ExportRecord record) {
            ExportPayload payload = new ExportPayload();
            payload.setFilename(record.getFilename());
            payload.setHeaders(record.getHeaders());
            payload.setRows(record.getRows());
            return payload;
        }

        public CsvExportStore.ExportRecord toRecord() {
            return new CsvExportStore.ExportRecord(filename, headers, rows);
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public List<String> getHeaders() {
            return headers;
        }

        public void setHeaders(List<String> headers) {
            this.headers = headers == null ? new ArrayList<>() : headers;
        }

        public List<List<String>> getRows() {
            return rows;
        }

        public void setRows(List<List<String>> rows) {
            this.rows = rows == null ? new ArrayList<>() : rows;
        }
    }

    private static class MemoryPayload {

        private List<AiMemoryDto> items = new ArrayList<>();

        public List<AiMemoryDto> getItems() {
            return items;
        }

        public void setItems(List<AiMemoryDto> items) {
            this.items = items == null ? new ArrayList<>() : items;
        }
    }
}
