package com.tcmseek.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmseek.ai.config.AiConversationStorageProperties;
import com.tcmseek.ai.config.AiPromptProperties;
import com.tcmseek.ai.config.AiRuntimeProperties;
import com.tcmseek.ai.dto.AiMemoryDto;
import com.tcmseek.ai.dto.AiMessage;
import com.tcmseek.ai.dto.ConversationSummaryState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
public class AiConversationMemoryService {

    private static final Logger log = LoggerFactory.getLogger(AiConversationMemoryService.class);

    private static final Set<String> MEMORY_TYPES = Set.of(
            "preference", "research_topic", "entity_focus", "constraint");

    private final AiConversationRepository conversationRepository;

    private final AiConversationStorageProperties storageProperties;

    private final AiRuntimeProperties runtimeProperties;

    private final AiPromptProperties promptProperties;

    private final AiRedisCache redisCache;

    private final ChatClient.Builder chatClientBuilder;

    private final ObjectMapper objectMapper;

    private final ExecutorService aiModelExecutor;

    public AiConversationMemoryService(AiConversationRepository conversationRepository,
                                       AiConversationStorageProperties storageProperties,
                                       AiRuntimeProperties runtimeProperties,
                                       AiPromptProperties promptProperties,
                                       AiRedisCache redisCache,
                                       ChatClient.Builder chatClientBuilder,
                                       ObjectMapper objectMapper,
                                       ExecutorService aiModelExecutor) {
        this.conversationRepository = conversationRepository;
        this.storageProperties = storageProperties;
        this.runtimeProperties = runtimeProperties;
        this.promptProperties = promptProperties;
        this.redisCache = redisCache;
        this.chatClientBuilder = chatClientBuilder;
        this.objectMapper = objectMapper;
        this.aiModelExecutor = aiModelExecutor;
    }

    public List<AiMessage> enrichContext(String userId, String conversationId, List<AiMessage> recentMessages) {
        if (!storageProperties.isPersistenceEnabled()) {
            return recentMessages;
        }
        try {
            String context = buildPersistentContext(userId, conversationId);
            if (!StringUtils.hasText(context)) {
                return recentMessages;
            }
            List<AiMessage> enriched = new ArrayList<>();
            AiMessage contextMessage = new AiMessage();
            contextMessage.setRole("system");
            contextMessage.setContent(context);
            enriched.add(contextMessage);
            if (recentMessages != null) {
                enriched.addAll(recentMessages);
            }
            return enriched;
        } catch (RuntimeException ex) {
            log.warn("build persistent ai context failed userId={} conversationId={} message={}",
                    userId, conversationId, ex.getMessage());
            return recentMessages;
        }
    }

    public void refreshAfterExchangeAsync(String conversationId, String userId) {
        if (!storageProperties.isPersistenceEnabled()
                || (!storageProperties.isSummaryEnabled() && !storageProperties.isMemoryEnabled())
                || !StringUtils.hasText(conversationId)) {
            return;
        }
        CompletableFuture.runAsync(() -> refreshAfterExchange(conversationId, userId))
                .exceptionally(error -> {
                    log.warn("refresh ai conversation summary/memory failed userId={} conversationId={} message={}",
                            userId, conversationId, error.getMessage());
                    return null;
                });
    }

    private String buildPersistentContext(String userId, String conversationId) {
        List<String> parts = new ArrayList<>();
        if (storageProperties.isMemoryEnabled()) {
            List<AiMemoryDto> memories = loadMemories(userId);
            if (!memories.isEmpty()) {
                parts.add("用户长期记忆：\n" + memories.stream()
                        .map(memory -> "- " + memoryLabel(memory.getMemoryType()) + "：" + memory.getContent())
                        .collect(Collectors.joining("\n")));
            }
        }
        if (storageProperties.isSummaryEnabled()) {
            ConversationSummaryState summary = loadSummary(userId, conversationId);
            if (summary != null && StringUtils.hasText(summary.getSummary())) {
                parts.add("当前会话摘要：\n" + summary.getSummary());
            }
        }
        if (parts.isEmpty()) {
            return "";
        }
        return "以下上下文仅用于理解用户意图，不要向用户直接复述，也不要编造其中没有的信息。\n\n"
                + String.join("\n\n", parts);
    }

    private ConversationSummaryState loadSummary(String userId, String conversationId) {
        ConversationSummaryState cached = redisCache.getSummary(userId, conversationId);
        if (cached != null) {
            return cached;
        }
        ConversationSummaryState summary = conversationRepository.findSummary(userId, conversationId);
        if (summary != null) {
            redisCache.putSummary(userId, conversationId, summary);
        }
        return summary;
    }

    private List<AiMemoryDto> loadMemories(String userId) {
        List<AiMemoryDto> cached = redisCache.getMemories(userId);
        if (!cached.isEmpty()) {
            return cached;
        }
        List<AiMemoryDto> memories = conversationRepository.findMemories(userId, memoryLimit());
        redisCache.putMemories(userId, memories);
        return memories;
    }

    private void refreshAfterExchange(String conversationId, String userId) {
        int messageCount = conversationRepository.countMessages(userId, conversationId);
        ConversationSummaryState currentSummary = conversationRepository.findSummary(userId, conversationId);
        if (!shouldRefresh(messageCount, currentSummary)) {
            return;
        }
        List<AiMessage> sourceMessages = conversationRepository.findRecentMessages(
                userId,
                conversationId,
                Math.max(6, storageProperties.getSummarySourceMessageLimit()));
        if (sourceMessages.isEmpty()) {
            return;
        }
        if (storageProperties.isSummaryEnabled()) {
            String summary = generateSummary(currentSummary, sourceMessages, messageCount);
            if (StringUtils.hasText(summary)) {
                conversationRepository.upsertSummary(userId, conversationId, summary, messageCount);
                ConversationSummaryState state = new ConversationSummaryState();
                state.setConversationId(conversationId);
                state.setUserId(userId);
                state.setSummary(summary);
                state.setCoveredMessageCount(messageCount);
                redisCache.putSummary(userId, conversationId, state);
            }
        }
        if (storageProperties.isMemoryEnabled()) {
            List<AiMemoryDto> extracted = extractMemories(
                    sourceMessages.subList(
                            Math.max(0, sourceMessages.size() - memorySourceLimit()),
                            sourceMessages.size()));
            for (AiMemoryDto memory : extracted) {
                conversationRepository.upsertMemory(
                        userId,
                        memory.getMemoryType(),
                        memory.getContent(),
                        conversationId,
                        memory.getConfidence());
            }
            redisCache.putMemories(userId, conversationRepository.findMemories(userId, memoryLimit()));
        }
    }

    private boolean shouldRefresh(int messageCount, ConversationSummaryState currentSummary) {
        int trigger = Math.max(2, storageProperties.getSummaryTriggerMessageCount());
        int refreshEvery = Math.max(1, storageProperties.getSummaryRefreshMessageCount());
        return messageCount >= trigger
                && (currentSummary == null
                || messageCount - currentSummary.getCoveredMessageCount() >= refreshEvery);
    }

    private String generateSummary(ConversationSummaryState currentSummary,
                                   List<AiMessage> sourceMessages,
                                   int messageCount) {
        String prompt = """
                你要为 TCMSeek 的单个 AI 会话生成可复用摘要。

                要求：
                - 只总结用户目标、已确认实体、已得出的关键结论、仍待解决的问题。
                - 不保留完整表格、完整 CSV、长 ID 列表和工具原始结果。
                - 不要写函数名、工具名、接口名。
                - 不要把一次性闲聊当作长期事实。
                - 输出中文，控制在 800 字以内。

                已有摘要：
                {previousSummary}

                最近消息：
                {messages}

                当前会话总消息数：{messageCount}
                """;
        String content = prompt
                .replace("{previousSummary}", currentSummary == null ? "无" : safeText(currentSummary.getSummary()))
                .replace("{messages}", formatMessages(sourceMessages))
                .replace("{messageCount}", String.valueOf(messageCount));
        return limitText(callModel("conversation-summary", content), 1200);
    }

    private List<AiMemoryDto> extractMemories(List<AiMessage> sourceMessages) {
        String prompt = """
                你要从 TCMSeek 用户与 AI 的最近对话中提取“长期用户记忆”。

                只允许提取这些类型：
                - preference：用户明确表达的回答偏好
                - research_topic：用户持续关注的研究主题
                - entity_focus：用户反复关注的中药、方剂、疾病、靶点、化合物
                - constraint：用户明确提出的长期约束

                保守规则：
                - 一次性问题不要写入记忆。
                - 医疗结论、治疗建议、工具查询结果不要写成用户长期记忆。
                - 不要保存敏感隐私。
                - 不要输出解释文字，只输出 JSON 数组。
                - 最多输出 {limit} 条。

                JSON 格式：
                [
                  {"memoryType":"preference","content":"用户偏好中文简洁回答","confidence":0.8}
                ]

                最近消息：
                {messages}
                """;
        String content = prompt
                .replace("{limit}", String.valueOf(memoryLimit()))
                .replace("{messages}", formatMessages(sourceMessages));
        String raw = callModel("memory-extract", content);
        return parseMemories(raw).stream()
                .filter(memory -> MEMORY_TYPES.contains(memory.getMemoryType()))
                .filter(memory -> StringUtils.hasText(memory.getContent()))
                .limit(memoryLimit())
                .collect(Collectors.toList());
    }

    private List<AiMemoryDto> parseMemories(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        String json = raw.trim();
        int start = json.indexOf('[');
        int end = json.lastIndexOf(']');
        if (start >= 0 && end >= start) {
            json = json.substring(start, end + 1);
        }
        try {
            List<ExtractedMemory> extracted = objectMapper.readValue(
                    json,
                    new TypeReference<List<ExtractedMemory>>() {
                    });
            List<AiMemoryDto> memories = new ArrayList<>();
            for (ExtractedMemory item : extracted) {
                if (item == null) {
                    continue;
                }
                AiMemoryDto memory = new AiMemoryDto();
                memory.setMemoryType(normalizeMemoryType(item.getMemoryType()));
                memory.setContent(limitText(item.getContent(), 300));
                memory.setConfidence(item.getConfidence() == null ? 0.65 : item.getConfidence());
                memories.add(memory);
            }
            return memories;
        } catch (JsonProcessingException ex) {
            log.warn("parse extracted ai memories failed raw={} message={}", limitText(raw, 300), ex.getMessage());
            return List.of();
        }
    }

    private String callModel(String operation, String prompt) {
        Duration timeout = runtimeProperties.getRequestTimeout();
        CompletableFuture<String> future = CompletableFuture.supplyAsync(
                () -> chatClientBuilder.build()
                        .prompt()
                        .system(promptProperties.getSystemPrompt())
                        .user(prompt)
                        .call()
                        .content(),
                aiModelExecutor);
        try {
            if (timeout == null || timeout.isZero() || timeout.isNegative()) {
                return future.get();
            }
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            future.cancel(true);
            throw new IllegalStateException(operation + " timed out", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(operation + " interrupted", ex);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException(cause);
        }
    }

    private String formatMessages(List<AiMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "无";
        }
        return messages.stream()
                .filter(message -> message != null && StringUtils.hasText(message.getContent()))
                .map(message -> {
                    String role = StringUtils.hasText(message.getRole())
                            ? message.getRole().toLowerCase(Locale.ROOT)
                            : "user";
                    return role + ": " + safeText(message.getContent());
                })
                .collect(Collectors.joining("\n"));
    }

    private String normalizeMemoryType(String memoryType) {
        if (!StringUtils.hasText(memoryType)) {
            return "";
        }
        return memoryType.trim().toLowerCase(Locale.ROOT);
    }

    private String memoryLabel(String memoryType) {
        return switch (normalizeMemoryType(memoryType)) {
            case "preference" -> "偏好";
            case "research_topic" -> "研究主题";
            case "entity_focus" -> "关注实体";
            case "constraint" -> "约束";
            default -> "记忆";
        };
    }

    private int memoryLimit() {
        return Math.max(1, storageProperties.getMemoryMaxItems());
    }

    private int memorySourceLimit() {
        return Math.max(4, storageProperties.getMemorySourceMessageLimit());
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String limitText(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String text = value.trim();
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    private static class ExtractedMemory {

        private String memoryType;

        private String content;

        private Double confidence;

        public String getMemoryType() {
            return memoryType;
        }

        public void setMemoryType(String memoryType) {
            this.memoryType = memoryType;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Double getConfidence() {
            return confidence;
        }

        public void setConfidence(Double confidence) {
            this.confidence = confidence;
        }
    }
}
