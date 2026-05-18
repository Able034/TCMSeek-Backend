package com.tcmseek.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmseek.ai.config.AiPromptProperties;
import com.tcmseek.ai.config.AiRuntimeProperties;
import com.tcmseek.ai.dto.AiGraphData;
import com.tcmseek.ai.dto.AiChatRequest;
import com.tcmseek.ai.dto.AiChatResponse;
import com.tcmseek.ai.dto.AiMessage;
import com.tcmseek.ai.dto.ToolCallResult;
import com.tcmseek.ai.exception.AiServiceException;
import com.tcmseek.ai.tools.TcmGraphTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);

    private static final List<String> FALLBACK_FIELD_PRIORITY = List.of(
            "herb", "herbName", "prescription", "prescriptionName",
            "disease", "compound", "target", "formula",
            "symptom", "syndrome", "pathway", "evidenceType", "inchikey");

    private final ChatClient.Builder chatClientBuilder;
    private final AiPromptProperties promptProperties;
    private final AiRuntimeProperties runtimeProperties;
    private final TcmGraphTools tcmGraphTools;
    private final ToolExecutionRecorder toolExecutionRecorder;
    private final AiSessionManager sessionManager;
    private final AiGraphBuilder graphBuilder;
    private final CsvExportStore csvExportStore;
    private final ToolFallbackService toolFallbackService;
    private final ToolResultCompressor toolResultCompressor;
    private final ObjectMapper objectMapper;
    private final ExecutorService aiModelExecutor;

    @Value("${spring.ai.openai.chat.options.model:deepseek-chat}")
    private String model;

    public AiChatService(ChatClient.Builder chatClientBuilder,
                         AiPromptProperties promptProperties,
                         AiRuntimeProperties runtimeProperties,
                         TcmGraphTools tcmGraphTools,
                         ToolExecutionRecorder toolExecutionRecorder,
                         AiSessionManager sessionManager,
                         AiGraphBuilder graphBuilder,
                         CsvExportStore csvExportStore,
                         ToolFallbackService toolFallbackService,
                         ToolResultCompressor toolResultCompressor,
                         ObjectMapper objectMapper,
                         ExecutorService aiModelExecutor) {
        this.chatClientBuilder = chatClientBuilder;
        this.promptProperties = promptProperties;
        this.runtimeProperties = runtimeProperties;
        this.tcmGraphTools = tcmGraphTools;
        this.toolExecutionRecorder = toolExecutionRecorder;
        this.sessionManager = sessionManager;
        this.graphBuilder = graphBuilder;
        this.csvExportStore = csvExportStore;
        this.toolFallbackService = toolFallbackService;
        this.toolResultCompressor = toolResultCompressor;
        this.objectMapper = objectMapper;
        this.aiModelExecutor = aiModelExecutor;
    }

    public AiChatResponse chat(AiChatRequest request) {
        return chat(request, AiRequestContext.empty());
    }

    public AiChatResponse chat(AiChatRequest request, AiRequestContext context) {
        AiRequestContext requestContext = context == null ? AiRequestContext.empty() : context;
        long startedAt = System.currentTimeMillis();
        List<AiMessage> messageContext = sessionManager.buildContext(
                request != null ? request.getSessionId() : null,
                request != null ? request.getMessages() : null,
                requestContext);
        String userPrompt = buildUserPrompt(messageContext);
        String latestQuestion = latestUserQuestion(request);
        String sessionId = request == null ? null : request.getSessionId();
        log.info("ai chat started requestId={} userId={} username={} account={} sessionId={} question={}",
                requestContext.getRequestId(), requestContext.getUserId(), requestContext.getUsername(),
                requestContext.getAccount(), sessionId, abbreviate(latestQuestion));

        List<ToolCallResult> toolResults = executeFallback(latestQuestion);
        String reply = null;
        String finishReason = "model_answer";
        if (toolResults.isEmpty()) {
            toolExecutionRecorder.start();
            try {
                reply = callModel(userPrompt);
            } catch (RuntimeException ex) {
                throw downstreamException(ex);
            } finally {
                toolResults = toolExecutionRecorder.finish();
            }
        }

        String exportUrl = buildCsvDownloadUrl(toolResults, requestContext);
        List<ToolCallResult> responseToolResults = toolResultCompressor.forAnswer(toolResults);
        if (!responseToolResults.isEmpty()) {
            if (!hasAnyToolData(responseToolResults)) {
                finishReason = "tool_no_result";
                reply = noToolResultReply(responseToolResults);
            } else {
                try {
                    reply = summarizeToolResults(latestQuestion, responseToolResults, exportUrl);
                    finishReason = "tool_summarized";
                } catch (RuntimeException ex) {
                    log.warn("ai summarize failed, fallback to deterministic tool summary requestId={} userId={} sessionId={} message={}",
                            requestContext.getRequestId(), requestContext.getUserId(), sessionId, ex.getMessage(), ex);
                    reply = fallbackToolReply(responseToolResults, exportUrl);
                    finishReason = "tool_summary_fallback";
                }
            }
        }
        reply = limitReplyLength(reply, exportUrl);
        reply = sanitizeUserFacingReply(reply);

        AiChatResponse response = new AiChatResponse(reply, "deepseek", model);
        response.setId(UUID.randomUUID().toString());
        response.setFinishReason(finishReason);
        response.setToolResults(responseToolResults);
        enrichToolPayload(response, toolResults, exportUrl);

        if (request != null) {
            sessionManager.appendExchange(
                    request.getSessionId(),
                    request.getMessages(),
                    reply,
                    requestContext,
                    response,
                    responseToolResults,
                    System.currentTimeMillis() - startedAt);
        }
        log.info("ai chat completed requestId={} userId={} sessionId={} finishReason={} toolCalls={} tools={} exportUrl={} costMs={}",
                requestContext.getRequestId(), requestContext.getUserId(), sessionId, finishReason,
                toolResults.size(), toolNames(toolResults), exportUrl, System.currentTimeMillis() - startedAt);
        return response;
    }

    private String sanitizeUserFacingReply(String reply) {
        if (!StringUtils.hasText(reply)) {
            return reply;
        }
        return reply
                .replaceAll("[（(]\\s*`?[a-z]+(?:[A-Z][A-Za-z0-9]*)+`?\\s*[)）]", "")
                .replaceAll("`[a-z]+(?:[A-Z][A-Za-z0-9]*)+`", "知识图谱查询")
                .replaceAll("(?i)CSV[:：]\\s*/(?:api/)?ai/exports/[a-zA-Z0-9_-]+", "完整明细可点击下载完整结果")
                .replaceAll("/(?:api/)?ai/exports/[a-zA-Z0-9_-]+", "完整明细可点击下载完整结果");
    }

    private List<ToolCallResult> executeFallback(String latestQuestion) {
        List<ToolCallResult> fallbackResults;
        toolExecutionRecorder.start();
        try {
            toolFallbackService.tryExecute(latestQuestion);
        } catch (RuntimeException ex) {
            throw downstreamException(ex);
        } finally {
            fallbackResults = toolExecutionRecorder.finish();
        }
        return fallbackResults;
    }

    private String callModel(String userPrompt) {
        return executeModelCall("chat", () -> chatClientBuilder.build()
                .prompt()
                .system(promptProperties.getSystemPrompt())
                .user(userPrompt)
                .tools(tcmGraphTools)
                .call()
                .content());
    }

    private String summarizeToolResults(String question, List<ToolCallResult> toolResults, String csvDownloadUrl) {
        String json;
        try {
            json = objectMapper.writeValueAsString(toolResults);
        } catch (JsonProcessingException e) {
            json = String.valueOf(toolResults);
        }
        String prompt = summaryPrompt(question, json, csvDownloadUrl);
        return executeModelCall("summarize-tools", () -> chatClientBuilder.build()
                .prompt()
                .system(promptProperties.getSystemPrompt())
                .user(prompt)
                .call()
                .content());
    }

    private String fallbackToolReply(List<ToolCallResult> toolResults, String csvDownloadUrl) {
        StringBuilder reply = new StringBuilder("知识图谱查询已完成，但摘要生成暂时不可用。\n\n");
        ToolCallResult visibleResult = firstResultWithData(toolResults);
        if (visibleResult == null || visibleResult.getResult() == null) {
            reply.append("核心结论：知识图谱未查询到相关数据。");
            return reply.toString();
        }

        List<Map<String, Object>> items = visibleResult.getResult().getItems();
        int displayed = Math.min(items.size(), toolResultCompressor.getAnswerItemLimit());
        int total = Math.max(visibleResult.getResult().getTotal(), items.size());
        reply.append("核心结论：本次查询共返回 ").append(total).append(" 条相关结果。");
        if (total > displayed) {
            reply.append(" 下方仅展示前 ").append(displayed).append(" 条代表性结果。");
        }

        reply.append("\n\n代表性结果：");
        for (int i = 0; i < displayed; i++) {
            reply.append("\n").append(i + 1).append(". ").append(formatFallbackItem(items.get(i)));
        }

        if (StringUtils.hasText(csvDownloadUrl)) {
            reply.append("\n\n完整明细可点击下载完整结果。");
        }
        return reply.toString();
    }

    private ToolCallResult firstResultWithData(List<ToolCallResult> toolResults) {
        if (toolResults == null || toolResults.isEmpty()) {
            return null;
        }
        for (ToolCallResult toolResult : toolResults) {
            if (toolResult != null
                    && toolResult.getResult() != null
                    && toolResult.getResult().getItems() != null
                    && !toolResult.getResult().getItems().isEmpty()) {
                return toolResult;
            }
        }
        return null;
    }

    private String formatFallbackItem(Map<String, Object> item) {
        if (item == null || item.isEmpty()) {
            return "结果项";
        }
        String preferred = FALLBACK_FIELD_PRIORITY.stream()
                .filter(item::containsKey)
                .map(field -> formatFallbackField(field, item.get(field)))
                .filter(StringUtils::hasText)
                .limit(3)
                .collect(Collectors.joining("；"));
        if (StringUtils.hasText(preferred)) {
            return preferred;
        }
        String generic = item.entrySet().stream()
                .filter(entry -> isReadableFallbackField(entry.getKey(), entry.getValue()))
                .limit(3)
                .map(entry -> formatFallbackField(entry.getKey(), entry.getValue()))
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("；"));
        return StringUtils.hasText(generic) ? generic : "结果项";
    }

    private String formatFallbackField(String field, Object value) {
        String text = value == null ? "" : value.toString();
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return fallbackFieldLabel(field) + "：" + text;
    }

    private boolean isReadableFallbackField(String field, Object value) {
        if (!StringUtils.hasText(field) || value == null || !StringUtils.hasText(value.toString())) {
            return false;
        }
        String normalized = field.toLowerCase(Locale.ROOT);
        return !normalized.endsWith("id") && !"matchscore".equals(normalized);
    }

    private String fallbackFieldLabel(String field) {
        return switch (field) {
            case "herb", "herbName" -> "中药";
            case "prescription", "prescriptionName" -> "方剂";
            case "disease" -> "疾病";
            case "compound" -> "化合物";
            case "target" -> "靶点";
            case "formula" -> "分子式";
            case "symptom" -> "症状";
            case "syndrome" -> "证候";
            case "pathway" -> "通路";
            case "evidenceType" -> "证据类型";
            case "inchikey" -> "InChIKey";
            default -> field;
        };
    }

    private String noToolResultReply(List<ToolCallResult> toolResults) {
        String message = StringUtils.hasText(promptProperties.getNoToolResultMessage())
                ? promptProperties.getNoToolResultMessage()
                : "知识图谱未查询到相关数据。";
        return message + "\n\n可以尝试使用更规范的中药、方剂、疾病或化合物名称，或减少查询实体后重试。";
    }

    private void enrichToolPayload(AiChatResponse response, List<ToolCallResult> toolResults, String exportUrl) {
        AiGraphData graph = graphBuilder.build(toolResults);
        if (graph != null) {
            response.setGraph(graph);
        }

        Integer total = toolResultCompressor.firstTotal(toolResults);
        Integer displayed = toolResultCompressor.firstDisplayed(toolResults);
        response.setTotalResults(total == null || total == 0 ? null : total);
        response.setDisplayedResults(displayed == null || displayed == 0 ? null : displayed);

        if (StringUtils.hasText(exportUrl)) {
            response.setCsvDownloadUrl(exportUrl);
        }
    }

    private String buildCsvDownloadUrl(List<ToolCallResult> toolResults, AiRequestContext requestContext) {
        String exportId = csvExportStore.saveFirstExport(
                toolResults,
                requestContext == null ? null : requestContext.getUserId());
        if (StringUtils.hasText(exportId)) {
            return "/ai/exports/" + exportId;
        }
        return null;
    }

    private String buildUserPrompt(List<AiMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        return messages.stream()
                .filter(message -> message != null && StringUtils.hasText(message.getContent()))
                .map(this::formatMessage)
                .collect(Collectors.joining("\n"));
    }

    private String formatMessage(AiMessage message) {
        String role = StringUtils.hasText(message.getRole())
                ? message.getRole().toLowerCase(Locale.ROOT)
                : "user";
        return role + ": " + message.getContent();
    }

    private String latestUserQuestion(AiChatRequest request) {
        if (request == null || request.getMessages() == null) {
            return "";
        }
        for (int i = request.getMessages().size() - 1; i >= 0; i--) {
            AiMessage message = request.getMessages().get(i);
            if (message != null && StringUtils.hasText(message.getContent())) {
                return message.getContent();
            }
        }
        return "";
    }

    private AiServiceException downstreamException(RuntimeException ex) {
        if (ex instanceof AiServiceException aiServiceException) {
            return aiServiceException;
        }
        if (hasCauseClassName(ex, "org.neo4j.driver")) {
            return new AiServiceException(HttpStatus.SERVICE_UNAVAILABLE, "NEO4J_UNAVAILABLE",
                    "知识图谱服务暂时不可用，请确认 Neo4j 已启动且连接配置正确。", ex);
        }
        if (hasCauseClassName(ex, "timeout")) {
            return new AiServiceException(HttpStatus.GATEWAY_TIMEOUT, "AI_PROVIDER_TIMEOUT",
                    "AI 模型响应超时，请稍后重试。", ex);
        }
        return new AiServiceException(HttpStatus.BAD_GATEWAY, "AI_PROVIDER_UNAVAILABLE",
                "AI 模型服务暂时不可用，请确认 DeepSeek API Key、网络和模型配置正确。", ex);
    }

    private boolean hasCauseClassName(Throwable throwable, String keyword) {
        Throwable current = throwable;
        String expected = keyword.toLowerCase(Locale.ROOT);
        while (current != null) {
            String className = current.getClass().getName().toLowerCase(Locale.ROOT);
            String message = current.getMessage() == null ? "" : current.getMessage().toLowerCase(Locale.ROOT);
            if (className.contains(expected) || message.contains(expected)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String abbreviate(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        int maxLength = Math.max(20, runtimeProperties.getQuestionLogMaxLength());
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }

    private String executeModelCall(String operation, Supplier<String> supplier) {
        int attempts = Math.max(1, runtimeProperties.getMaxRetries() + 1);
        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            long startedAt = System.currentTimeMillis();
            try {
                String content = callWithTimeout(supplier);
                log.info("ai model call succeeded operation={} attempt={} costMs={}",
                        operation, attempt, System.currentTimeMillis() - startedAt);
                return content;
            } catch (RuntimeException ex) {
                lastError = ex;
                boolean retry = attempt < attempts;
                log.warn("ai model call failed operation={} attempt={}/{} retry={} costMs={} message={}",
                        operation, attempt, attempts, retry, System.currentTimeMillis() - startedAt, ex.getMessage());
                if (retry) {
                    sleepBeforeRetry();
                }
            }
        }
        throw lastError == null
                ? new AiServiceException(HttpStatus.BAD_GATEWAY, "AI_PROVIDER_UNAVAILABLE",
                "AI 模型服务暂时不可用。", null)
                : lastError;
    }

    private String callWithTimeout(Supplier<String> supplier) {
        Duration timeout = runtimeProperties.getRequestTimeout();
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            return supplier.get();
        }
        CompletableFuture<String> future = CompletableFuture.supplyAsync(supplier, aiModelExecutor);
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            future.cancel(true);
            throw new AiServiceException(HttpStatus.GATEWAY_TIMEOUT, "AI_PROVIDER_TIMEOUT",
                    "AI 模型响应超时，请稍后重试。", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AiServiceException(HttpStatus.SERVICE_UNAVAILABLE, "AI_REQUEST_INTERRUPTED",
                    "AI 请求被中断，请稍后重试。", ex);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException(cause);
        }
    }

    private void sleepBeforeRetry() {
        Duration backoff = runtimeProperties.getRetryBackoff();
        if (backoff == null || backoff.isZero() || backoff.isNegative()) {
            return;
        }
        try {
            Thread.sleep(backoff.toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private String summaryPrompt(String question, String toolResultJson, String csvDownloadUrl) {
        int answerItemLimit = toolResultCompressor.getAnswerItemLimit();
        String template = promptProperties.getSummaryPromptTemplate();
        if (!StringUtils.hasText(template)) {
            template = ""
                    + "用户问题：\n{question}\n\n"
                    + "知识图谱工具查询结果 JSON：\n{toolResultJson}\n\n"
                    + "CSV下载地址：\n{csvDownloadUrl}\n\n"
                    + "请只根据工具结果回答。正文不要输出 CSV 原始 URL，前端会单独展示下载按钮。"
                    + "输出格式固定为：1）一句核心结论；2）3 个以内要点；3）最多 {answerItemLimit} 行的精简 Markdown 表格。"
                    + "表格列数控制在 3 列以内，列名要短；长 ID 可以放在第一列，但不要额外解释每一行。"
                    + "如果 total 大于 items 数量，只说“完整明细可点击下载完整结果”，不要写下载地址。"
                    + "不要在正文中罗列超过 {answerItemLimit} 个实体；不要重复表格中已经出现的长 ID。";
        }
        return template
                .replace("{question}", question == null ? "" : question)
                .replace("{toolResultJson}", toolResultJson == null ? "[]" : toolResultJson)
                .replace("{csvDownloadUrl}", StringUtils.hasText(csvDownloadUrl) ? csvDownloadUrl : "无")
                .replace("{answerItemLimit}", String.valueOf(answerItemLimit));
    }

    private boolean hasAnyToolData(List<ToolCallResult> toolResults) {
        if (toolResults == null || toolResults.isEmpty()) {
            return false;
        }
        return toolResults.stream()
                .anyMatch(toolResult -> toolResult != null
                        && toolResult.getResult() != null
                        && toolResult.getResult().getItems() != null
                        && !toolResult.getResult().getItems().isEmpty());
    }

    private String limitReplyLength(String reply, String csvDownloadUrl) {
        if (!StringUtils.hasText(reply)) {
            return reply;
        }
        int maxChars = runtimeProperties.getMaxReplyChars();
        if (maxChars <= 0 || reply.length() <= maxChars) {
            return reply;
        }
        String message = StringUtils.hasText(promptProperties.getReplyTruncatedMessage())
                ? promptProperties.getReplyTruncatedMessage()
                : "回答已压缩。";
        String suffix = "\n\n" + message;
        if (StringUtils.hasText(csvDownloadUrl) && !message.contains("下载")) {
            suffix += " 完整明细可点击下载完整结果。";
        }
        int end = Math.max(0, Math.min(reply.length(), maxChars - suffix.length()));
        return reply.substring(0, end).trim() + suffix;
    }

    private String toolNames(List<ToolCallResult> toolResults) {
        if (toolResults == null || toolResults.isEmpty()) {
            return "";
        }
        return toolResults.stream()
                .filter(toolResult -> toolResult != null && StringUtils.hasText(toolResult.getToolName()))
                .map(toolResult -> {
                    int total = toolResult.getResult() == null ? 0 : toolResult.getResult().getTotal();
                    return toolResult.getToolName() + "(" + total + ")";
                })
                .collect(Collectors.joining(","));
    }
}
