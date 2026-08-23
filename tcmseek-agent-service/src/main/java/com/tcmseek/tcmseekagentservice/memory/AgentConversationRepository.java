package com.tcmseek.tcmseekagentservice.memory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Repository
public class AgentConversationRepository {

    private final JdbcTemplate jdbcTemplate;

    public AgentConversationRepository(
            @Qualifier("agentConversationJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AgentMessage> findRecentMessages(String userId, String conversationId, int limit) {
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
                (rs, rowNum) -> AgentMessage.of(rs.getString("role"), rs.getString("content")),
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

    public AgentConversationSummaryState findSummary(String userId, String conversationId) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(conversationId)) {
            return null;
        }
        List<AgentConversationSummaryState> items = jdbcTemplate.query("""
                        select conversation_id, user_id, summary, covered_message_count,
                               updated_at::text as updated_at
                        from ai_conversation_summary
                        where user_id = ?
                          and conversation_id = ?
                        """,
                (rs, rowNum) -> {
                    AgentConversationSummaryState state = new AgentConversationSummaryState();
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

    public List<AgentMemoryDto> findMemories(String userId, int limit) {
        if (!StringUtils.hasText(userId)) {
            return List.of();
        }
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
                    AgentMemoryDto memory = new AgentMemoryDto();
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

    @Transactional(transactionManager = "agentPostgresTransactionManager")
    public void saveExchange(String conversationId,
                             String userId,
                             String userContent,
                             String assistantContent,
                             String requestId,
                             long latencyMs) {
        if (!StringUtils.hasText(conversationId)
                || !StringUtils.hasText(userContent)
                || !StringUtils.hasText(assistantContent)) {
            return;
        }

        String owner = normalizeUserId(userId);
        String title = titleFrom(userContent);
        String userMessageId = compactUuid();
        String assistantMessageId = compactUuid();

        if (!upsertConversation(conversationId, owner, title)) {
            return;
        }
        insertUserMessage(userMessageId, conversationId, owner, userContent, requestId);
        insertAssistantMessage(assistantMessageId, conversationId, owner, assistantContent, requestId, latencyMs);
        touchConversation(conversationId, owner, title);
    }

    private boolean upsertConversation(String conversationId, String userId, String title) {
        int rows = jdbcTemplate.update("""
                        insert into ai_conversation (id, user_id, title, mode, status, message_count, last_message_at)
                        values (?, ?, ?, 'academic', 'active', 0, now())
                        on conflict (id) do update set
                            title = coalesce(ai_conversation.title, excluded.title),
                            last_message_at = now(),
                            updated_at = now()
                        where ai_conversation.user_id = excluded.user_id
                          and ai_conversation.status <> 'deleted'
                        """,
                conversationId,
                userId,
                title);
        return rows > 0;
    }

    private void insertUserMessage(String messageId,
                                   String conversationId,
                                   String userId,
                                   String content,
                                   String requestId) {
        jdbcTemplate.update("""
                        insert into ai_message
                            (id, conversation_id, user_id, role, content, request_id, created_at)
                        values (?, ?, ?, 'user', ?, ?, clock_timestamp())
                        """,
                messageId,
                conversationId,
                userId,
                content,
                requestId);
    }

    private void insertAssistantMessage(String messageId,
                                        String conversationId,
                                        String userId,
                                        String content,
                                        String requestId,
                                        long latencyMs) {
        jdbcTemplate.update("""
                        insert into ai_message
                            (id, conversation_id, user_id, role, content, provider, model,
                             finish_reason, request_id, latency_ms, created_at)
                        values (?, ?, ?, 'assistant', ?, 'agent', 'tcmseek-agent-service',
                                'agent_answer', ?, ?, clock_timestamp())
                        """,
                messageId,
                conversationId,
                userId,
                content,
                requestId,
                Math.toIntExact(Math.min(Integer.MAX_VALUE, Math.max(0, latencyMs))));
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

    private String normalizeUserId(String userId) {
        return StringUtils.hasText(userId) ? userId : "anonymous";
    }

    private String titleFrom(String content) {
        if (!StringUtils.hasText(content)) {
            return "新对话";
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 80 ? normalized : normalized.substring(0, 80);
    }

    private String compactUuid() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase(Locale.ROOT);
    }
}
