package com.tcmseek.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmseek.ai.dto.AiMemoryDto;
import com.tcmseek.ai.dto.AiMessage;
import com.tcmseek.ai.dto.ConversationSummaryState;
import com.tcmseek.ai.dto.ConversationMessageDto;
import com.tcmseek.ai.dto.ConversationSummaryDto;
import com.tcmseek.ai.dto.GraphToolResult;
import com.tcmseek.ai.dto.ToolCallResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Repository
public class AiConversationRepository {

    private final JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper;

    public AiConversationRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public List<AiMessage> findRecentMessages(String conversationId, int limit) {
        return findRecentMessages("anonymous", conversationId, limit);
    }

    public List<AiMessage> findRecentMessages(String userId, String conversationId, int limit) {
        if (!StringUtils.hasText(conversationId) || limit <= 0) {
            return List.of();
        }
        return jdbcTemplate.query("""
                        select role, content
                        from (
                            select id, role, content, created_at
                            from ai_message
                            where user_id = ?
                              and conversation_id = ?
                            order by created_at desc, id desc
                            limit ?
                        ) recent
                        order by created_at asc, id asc
                        """,
                (rs, rowNum) -> {
                    AiMessage message = new AiMessage();
                    message.setRole(rs.getString("role"));
                    message.setContent(rs.getString("content"));
                    return message;
                },
                normalizeUserId(userId),
                conversationId,
                limit);
    }

    public int countMessages(String userId, String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return 0;
        }
        Integer total = jdbcTemplate.queryForObject("""
                        select count(*)
                        from ai_message
                        where user_id = ?
                          and conversation_id = ?
                        """,
                Integer.class,
                normalizeUserId(userId),
                conversationId);
        return total == null ? 0 : total;
    }

    public ConversationSummaryState findSummary(String userId, String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return null;
        }
        List<ConversationSummaryState> items = jdbcTemplate.query("""
                        select conversation_id, user_id, summary, covered_message_count,
                               updated_at::text as updated_at
                        from ai_conversation_summary
                        where user_id = ?
                          and conversation_id = ?
                        """,
                (rs, rowNum) -> {
                    ConversationSummaryState state = new ConversationSummaryState();
                    state.setConversationId(rs.getString("conversation_id"));
                    state.setUserId(rs.getString("user_id"));
                    state.setSummary(rs.getString("summary"));
                    state.setCoveredMessageCount(rs.getInt("covered_message_count"));
                    state.setUpdatedAt(rs.getString("updated_at"));
                    return state;
                },
                normalizeUserId(userId),
                conversationId);
        return items.isEmpty() ? null : items.get(0);
    }

    public void upsertSummary(String userId, String conversationId, String summary, int coveredMessageCount) {
        if (!StringUtils.hasText(userId)
                || !StringUtils.hasText(conversationId)
                || !StringUtils.hasText(summary)) {
            return;
        }
        jdbcTemplate.update("""
                        insert into ai_conversation_summary
                            (conversation_id, user_id, summary, covered_message_count)
                        values (?, ?, ?, ?)
                        on conflict (conversation_id) do update set
                            summary = excluded.summary,
                            covered_message_count = excluded.covered_message_count,
                            updated_at = now()
                        where ai_conversation_summary.user_id = excluded.user_id
                        """,
                conversationId,
                normalizeUserId(userId),
                summary.trim(),
                Math.max(0, coveredMessageCount));
    }

    public List<AiMemoryDto> findMemories(String userId, int limit) {
        int safeLimit = Math.max(1, Math.min(50, limit));
        return jdbcTemplate.query("""
                        select id, user_id, memory_type, content, confidence,
                               updated_at::text as updated_at
                        from ai_memory
                        where user_id = ?
                          and enabled = true
                        order by confidence desc, updated_at desc
                        limit ?
                        """,
                (rs, rowNum) -> {
                    AiMemoryDto memory = new AiMemoryDto();
                    memory.setId(rs.getString("id"));
                    memory.setUserId(rs.getString("user_id"));
                    memory.setMemoryType(rs.getString("memory_type"));
                    memory.setContent(rs.getString("content"));
                    BigDecimal confidence = rs.getBigDecimal("confidence");
                    memory.setConfidence(confidence == null ? 1.0 : confidence.doubleValue());
                    memory.setUpdatedAt(rs.getString("updated_at"));
                    return memory;
                },
                normalizeUserId(userId),
                safeLimit);
    }

    public void upsertMemory(String userId,
                             String memoryType,
                             String content,
                             String sourceConversationId,
                             double confidence) {
        if (!StringUtils.hasText(userId)
                || !StringUtils.hasText(memoryType)
                || !StringUtils.hasText(content)) {
            return;
        }
        String owner = normalizeUserId(userId);
        String safeType = memoryType.trim();
        String safeContent = content.replaceAll("\\s+", " ").trim();
        if (safeContent.length() > 1000) {
            safeContent = safeContent.substring(0, 1000);
        }
        BigDecimal safeConfidence = BigDecimal.valueOf(Math.max(0, Math.min(1, confidence)));
        List<String> existingIds = jdbcTemplate.queryForList("""
                        select id
                        from ai_memory
                        where user_id = ?
                          and memory_type = ?
                          and lower(content) = lower(?)
                        order by updated_at desc
                        limit 1
                        """,
                String.class,
                owner,
                safeType,
                safeContent);
        if (existingIds.isEmpty()) {
            jdbcTemplate.update("""
                            insert into ai_memory
                                (id, user_id, memory_type, content, source_conversation_id, confidence, enabled)
                            values (?, ?, ?, ?, ?, ?, true)
                            """,
                    compactUuid(),
                    owner,
                    safeType,
                    safeContent,
                    sourceConversationId,
                    safeConfidence);
        } else {
            jdbcTemplate.update("""
                            update ai_memory
                            set source_conversation_id = coalesce(?, source_conversation_id),
                                confidence = greatest(confidence, ?),
                                enabled = true,
                                updated_at = now()
                            where id = ?
                            """,
                    sourceConversationId,
                    safeConfidence,
                    existingIds.get(0));
        }
    }

    public ConversationSummaryDto createConversation(String userId, String mode, String title) {
        String normalizedMode = normalizeMode(mode);
        String conversationId = normalizedMode + "-" + compactUuid();
        String owner = normalizeUserId(userId);
        String safeTitle = StringUtils.hasText(title) ? title.trim() : "新对话";
        jdbcTemplate.update("""
                        insert into ai_conversation (id, user_id, title, mode, status, message_count, last_message_at)
                        values (?, ?, ?, ?, 'active', 0, null)
                        """,
                conversationId,
                owner,
                safeTitle.length() <= 200 ? safeTitle : safeTitle.substring(0, 200),
                normalizedMode);
        return findConversation(owner, conversationId);
    }

    public ConversationSummaryDto findConversation(String userId, String conversationId) {
        List<ConversationSummaryDto> items = jdbcTemplate.query("""
                        select id, title, mode, status, message_count,
                               last_message_at::text as last_message_at,
                               updated_at::text as updated_at
                        from ai_conversation
                        where user_id = ?
                          and id = ?
                          and status <> 'deleted'
                        """,
                (rs, rowNum) -> mapConversation(rs),
                normalizeUserId(userId),
                conversationId);
        return items.isEmpty() ? null : items.get(0);
    }

    public List<ConversationSummaryDto> listConversations(String userId, String mode, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safePageSize = Math.max(1, Math.min(100, pageSize));
        int offset = (safePage - 1) * safePageSize;
        String owner = normalizeUserId(userId);
        String normalizedMode = normalizeMode(mode);
        return jdbcTemplate.query("""
                        select id, title, mode, status, message_count,
                               last_message_at::text as last_message_at,
                               updated_at::text as updated_at
                        from ai_conversation
                        where user_id = ?
                          and mode = ?
                          and status = 'active'
                        order by updated_at desc, id desc
                        limit ? offset ?
                        """,
                (rs, rowNum) -> mapConversation(rs),
                owner,
                normalizedMode,
                safePageSize,
                offset);
    }

    public long countConversations(String userId, String mode) {
        Long total = jdbcTemplate.queryForObject("""
                        select count(*)
                        from ai_conversation
                        where user_id = ?
                          and mode = ?
                          and status = 'active'
                        """,
                Long.class,
                normalizeUserId(userId),
                normalizeMode(mode));
        return total == null ? 0 : total;
    }

    public List<ConversationMessageDto> findMessages(String userId, String conversationId, int limit) {
        if (!StringUtils.hasText(conversationId)) {
            return List.of();
        }
        int safeLimit = Math.max(1, Math.min(300, limit));
        return jdbcTemplate.query("""
                        select id, role, content, provider, model, finish_reason,
                               total_results, displayed_results, csv_export_id,
                               request_id, latency_ms, created_at::text as created_at
                        from (
                            select id, role, content, provider, model, finish_reason,
                                   total_results, displayed_results, csv_export_id,
                                   request_id, latency_ms, created_at
                            from ai_message
                            where user_id = ?
                              and conversation_id = ?
                            order by created_at desc, id desc
                            limit ?
                        ) recent
                        order by created_at asc, id asc
                        """,
                (rs, rowNum) -> {
                    ConversationMessageDto message = new ConversationMessageDto();
                    message.setId(rs.getString("id"));
                    message.setRole(rs.getString("role"));
                    message.setContent(rs.getString("content"));
                    message.setProvider(rs.getString("provider"));
                    message.setModel(rs.getString("model"));
                    message.setFinishReason(rs.getString("finish_reason"));
                    message.setTotalResults((Integer) rs.getObject("total_results"));
                    message.setDisplayedResults((Integer) rs.getObject("displayed_results"));
                    message.setCsvExportId(rs.getString("csv_export_id"));
                    message.setRequestId(rs.getString("request_id"));
                    message.setLatencyMs((Integer) rs.getObject("latency_ms"));
                    message.setCreatedAt(rs.getString("created_at"));
                    return message;
                },
                normalizeUserId(userId),
                conversationId,
                safeLimit);
    }

    public boolean updateConversationTitle(String userId, String conversationId, String title) {
        if (!StringUtils.hasText(title)) {
            return false;
        }
        String safeTitle = title.trim();
        int rows = jdbcTemplate.update("""
                        update ai_conversation
                        set title = ?, updated_at = now()
                        where user_id = ?
                          and id = ?
                          and status <> 'deleted'
                        """,
                safeTitle.length() <= 200 ? safeTitle : safeTitle.substring(0, 200),
                normalizeUserId(userId),
                conversationId);
        return rows > 0;
    }

    public boolean markConversationDeleted(String userId, String conversationId) {
        int rows = jdbcTemplate.update("""
                        update ai_conversation
                        set status = 'deleted', updated_at = now()
                        where user_id = ?
                          and id = ?
                          and status <> 'deleted'
                        """,
                normalizeUserId(userId),
                conversationId);
        return rows > 0;
    }

    @Transactional
    public void saveExchange(AiConversationExchange exchange) {
        if (exchange == null
                || !StringUtils.hasText(exchange.getConversationId())
                || exchange.getUserMessage() == null
                || !StringUtils.hasText(exchange.getUserMessage().getContent())
                || !StringUtils.hasText(exchange.getAssistantContent())) {
            return;
        }

        String conversationId = exchange.getConversationId();
        String userId = StringUtils.hasText(exchange.getUserId()) ? exchange.getUserId() : "anonymous";
        String mode = normalizeMode(exchange.getMode());
        String title = titleFrom(exchange.getUserMessage().getContent());
        String userMessageId = compactUuid();
        String assistantMessageId = StringUtils.hasText(exchange.getAssistantMessageId())
                ? exchange.getAssistantMessageId()
                : compactUuid();

        if (!upsertConversation(conversationId, userId, title, mode)) {
            return;
        }
        insertUserMessage(userMessageId, conversationId, userId, exchange);
        insertAssistantMessage(assistantMessageId, conversationId, userId, exchange);
        insertToolCalls(assistantMessageId, conversationId, userId, exchange.getToolResults(), exchange.getLatencyMs());
        touchConversation(conversationId, userId, title);
    }

    private boolean upsertConversation(String conversationId, String userId, String title, String mode) {
        int rows = jdbcTemplate.update("""
                        insert into ai_conversation (id, user_id, title, mode, status, message_count, last_message_at)
                        values (?, ?, ?, ?, 'active', 0, now())
                        on conflict (id) do update set
                            mode = excluded.mode,
                            title = coalesce(ai_conversation.title, excluded.title),
                            last_message_at = now(),
                            updated_at = now()
                        where ai_conversation.user_id = excluded.user_id
                          and ai_conversation.status <> 'deleted'
                        """,
                conversationId,
                userId,
                title,
                mode);
        return rows > 0;
    }

    private void insertUserMessage(String messageId,
                                   String conversationId,
                                   String userId,
                                   AiConversationExchange exchange) {
        jdbcTemplate.update("""
                        insert into ai_message
                            (id, conversation_id, user_id, role, content, request_id, latency_ms, created_at)
                        values (?, ?, ?, 'user', ?, ?, ?, clock_timestamp())
                        """,
                messageId,
                conversationId,
                userId,
                exchange.getUserMessage().getContent(),
                exchange.getRequestId(),
                null);
    }

    private void insertAssistantMessage(String messageId,
                                        String conversationId,
                                        String userId,
                                        AiConversationExchange exchange) {
        jdbcTemplate.update("""
                        insert into ai_message
                            (id, conversation_id, user_id, role, content, provider, model, finish_reason,
                             total_results, displayed_results, csv_export_id, request_id, latency_ms, created_at)
                        values (?, ?, ?, 'assistant', ?, ?, ?, ?, ?, ?, ?, ?, ?, clock_timestamp())
                        """,
                messageId,
                conversationId,
                userId,
                exchange.getAssistantContent(),
                exchange.getProvider(),
                exchange.getModel(),
                exchange.getFinishReason(),
                exchange.getTotalResults(),
                exchange.getDisplayedResults(),
                exchange.getCsvExportId(),
                exchange.getRequestId(),
                exchange.getLatencyMs());
    }

    private void insertToolCalls(String messageId,
                                 String conversationId,
                                 String userId,
                                 List<ToolCallResult> toolResults,
                                 Integer latencyMs) {
        if (toolResults == null || toolResults.isEmpty()) {
            return;
        }
        for (ToolCallResult toolResult : toolResults) {
            if (toolResult == null || !StringUtils.hasText(toolResult.getToolName())) {
                continue;
            }
            GraphToolResult result = toolResult.getResult();
            jdbcTemplate.update("""
                            insert into ai_tool_call
                                (id, message_id, conversation_id, user_id, tool_name, arguments,
                                 result_total, result_preview, success, latency_ms)
                            values (?, ?, ?, ?, ?, cast(? as jsonb), ?, cast(? as jsonb), true, ?)
                            """,
                    compactUuid(),
                    messageId,
                    conversationId,
                    userId,
                    toolResult.getToolName(),
                    toJson(toolResult.getArguments() == null ? Map.of() : toolResult.getArguments()),
                    result == null ? null : result.getTotal(),
                    toJson(resultPreview(result)),
                    latencyMs);
        }
    }

    private void touchConversation(String conversationId, String userId, String title) {
        jdbcTemplate.update("""
                        update ai_conversation
                        set message_count = (
                                select count(*)
                                from ai_message
                                where conversation_id = ?
                                  and user_id = ?
                            ),
                            title = case
                                when title is null or title = '' or title = '新对话' then ?
                                else title
                            end,
                            last_message_at = now(),
                            updated_at = now()
                        where id = ?
                          and user_id = ?
                        """,
                conversationId,
                userId,
                title,
                conversationId,
                userId);
    }

    private ConversationSummaryDto mapConversation(java.sql.ResultSet rs) throws java.sql.SQLException {
        ConversationSummaryDto item = new ConversationSummaryDto();
        item.setId(rs.getString("id"));
        item.setTitle(rs.getString("title"));
        item.setMode(rs.getString("mode"));
        item.setStatus(rs.getString("status"));
        item.setMessageCount(rs.getInt("message_count"));
        item.setLastMessageAt(rs.getString("last_message_at"));
        item.setUpdatedAt(rs.getString("updated_at"));
        return item;
    }

    private Map<String, Object> resultPreview(GraphToolResult result) {
        if (result == null) {
            return Map.of();
        }
        Map<String, Object> preview = new LinkedHashMap<>();
        preview.put("queryType", result.getQueryType());
        preview.put("total", result.getTotal());
        preview.put("items", result.getItems() == null ? List.of() : result.getItems());
        return preview;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private String normalizeMode(String mode) {
        if (!StringUtils.hasText(mode)) {
            return "academic";
        }
        String normalized = mode.toLowerCase(Locale.ROOT);
        return "general".equals(normalized) ? "general" : "academic";
    }

    private String normalizeUserId(String userId) {
        return StringUtils.hasText(userId) ? userId : "anonymous";
    }

    private String titleFrom(String content) {
        if (!StringUtils.hasText(content)) {
            return "新会话";
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 80 ? normalized : normalized.substring(0, 80);
    }

    private String compactUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
