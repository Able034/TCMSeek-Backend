package com.tcmseek.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tcmseek.ai.config.TcmReasonProperties;
import com.tcmseek.ai.dto.AiChatRequest;
import com.tcmseek.ai.dto.AiChatResponse;
import com.tcmseek.ai.dto.AiMessage;
import com.tcmseek.ai.exception.AiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
public class TcmReasonChatService {

    private static final Logger log = LoggerFactory.getLogger(TcmReasonChatService.class);

    private final TcmReasonProperties properties;

    private final AiRuntimeGuard runtimeGuard;

    private final AiSessionManager sessionManager;

    private final ObjectMapper objectMapper;

    private final ExecutorService aiModelExecutor;

    private final HttpClient httpClient;

    public TcmReasonChatService(TcmReasonProperties properties,
                                AiRuntimeGuard runtimeGuard,
                                AiSessionManager sessionManager,
                                ObjectMapper objectMapper,
                                ExecutorService aiModelExecutor) {
        this.properties = properties;
        this.runtimeGuard = runtimeGuard;
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
        this.aiModelExecutor = aiModelExecutor;
        this.httpClient = createHttpClient(properties);
    }

    public AiChatResponse chat(AiChatRequest request, AiRequestContext context) {
        AiRequestContext requestContext = context == null ? AiRequestContext.empty() : context;
        long startedAt = System.currentTimeMillis();
        String sessionId = request == null ? null : request.getSessionId();
        List<AiMessage> messageContext = sessionManager.buildContext(
                sessionId,
                request == null ? null : request.getMessages(),
                requestContext);
        String latestQuestion = latestUserQuestion(request);
        log.info("tcmreason chat started requestId={} userId={} sessionId={} question={}",
                requestContext.getRequestId(), requestContext.getUserId(), sessionId, abbreviate(latestQuestion));

        AiChatResponse response = callTcmReason(messageContext);
        response.setProvider("tcmreason");
        response.setFinishReason("tcmreason_answer");
        if (!StringUtils.hasText(response.getId())) {
            response.setId(UUID.randomUUID().toString());
        }

        if (request != null) {
            sessionManager.appendExchange(
                    sessionId,
                    request.getMessages(),
                    response.getReply(),
                    requestContext,
                    response,
                    List.of(),
                    System.currentTimeMillis() - startedAt);
        }
        log.info("tcmreason chat completed requestId={} userId={} sessionId={} model={} costMs={}",
                requestContext.getRequestId(), requestContext.getUserId(), sessionId,
                response.getModel(), System.currentTimeMillis() - startedAt);
        return response;
    }

    private AiChatResponse callTcmReason(List<AiMessage> context) {
        String endpoint = chatCompletionsUrl();
        ObjectNode payload = buildPayload(context);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(readTimeout())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()));
        if (StringUtils.hasText(properties.getApiKey())) {
            requestBuilder.header("Authorization", "Bearer " + properties.getApiKey().trim());
        }
        HttpRequest request = requestBuilder.build();

        return runtimeGuard.executeModelCall("tcmreason-chat", () -> {
            try {
                HttpResponse<String> response = callWithTimeout(request);
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    String message = extractErrorMessage(response.body());
                    throw new AiServiceException(HttpStatus.BAD_GATEWAY, "TCMREASON_UNAVAILABLE",
                            StringUtils.hasText(message) ? message : "TCMReason 模型服务返回异常：" + response.statusCode(), null);
                }
                return parseResponse(response.body());
            } catch (AiServiceException ex) {
                throw ex;
            } catch (TimeoutException ex) {
                throw new AiServiceException(HttpStatus.GATEWAY_TIMEOUT, "TCMREASON_TIMEOUT",
                        "TCMReason 模型响应超时，请稍后重试。", ex);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new AiServiceException(HttpStatus.SERVICE_UNAVAILABLE, "TCMREASON_INTERRUPTED",
                        "TCMReason 请求被中断，请稍后重试。", ex);
            } catch (ExecutionException ex) {
                throw new AiServiceException(HttpStatus.BAD_GATEWAY, "TCMREASON_UNAVAILABLE",
                        "TCMReason 模型服务暂时不可用，请确认 TCMReason 地址、网络和模型配置正确。", ex.getCause());
            } catch (Exception ex) {
                throw new AiServiceException(HttpStatus.BAD_GATEWAY, "TCMREASON_UNAVAILABLE",
                        "TCMReason 模型服务暂时不可用，请确认 TCMReason 地址、网络和模型配置正确。", ex);
            }
        });
    }

    private ObjectNode buildPayload(List<AiMessage> context) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model());
        body.put("temperature", properties.getDefaultTemperature() == null ? 0.3d : properties.getDefaultTemperature());
        body.put("max_tokens", properties.getDefaultMaxTokens() == null ? 1024 : properties.getDefaultMaxTokens());
        body.put("stream", Boolean.TRUE.equals(properties.getStream()));

        ArrayNode messages = body.putArray("messages");
        if (StringUtils.hasText(properties.getSystemPrompt())) {
            ObjectNode system = messages.addObject();
            system.put("role", "system");
            system.put("content", properties.getSystemPrompt());
        }
        for (AiMessage message : sanitize(context)) {
            ObjectNode item = messages.addObject();
            item.put("role", role(message));
            item.put("content", message.getContent());
        }
        return body;
    }

    private AiChatResponse parseResponse(String responseBody) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new AiServiceException(HttpStatus.BAD_GATEWAY, "TCMREASON_BAD_RESPONSE",
                    "TCMReason 没有返回有效回答。", null);
        }
        String reply = choices.get(0).path("message").path("content").asText("").trim();
        if (!StringUtils.hasText(reply)) {
            throw new AiServiceException(HttpStatus.BAD_GATEWAY, "TCMREASON_BAD_RESPONSE",
                    "TCMReason 没有返回有效回答。", null);
        }

        AiChatResponse response = new AiChatResponse(reply, "tcmreason", root.path("model").asText(model()));
        response.setId(root.path("id").asText(null));
        response.setUsage(parseUsage(root.path("usage")));
        return response;
    }

    private AiChatResponse.Usage parseUsage(JsonNode usageNode) {
        if (usageNode == null || usageNode.isMissingNode() || usageNode.isNull()) {
            return null;
        }
        AiChatResponse.Usage usage = new AiChatResponse.Usage();
        usage.setPromptTokens(usageNode.path("prompt_tokens").isInt() ? usageNode.path("prompt_tokens").asInt() : null);
        usage.setCompletionTokens(usageNode.path("completion_tokens").isInt() ? usageNode.path("completion_tokens").asInt() : null);
        usage.setTotalTokens(usageNode.path("total_tokens").isInt() ? usageNode.path("total_tokens").asInt() : null);
        return usage;
    }

    private HttpResponse<String> callWithTimeout(HttpRequest request)
            throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<HttpResponse<String>> future = CompletableFuture.supplyAsync(() -> {
            try {
                return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }, aiModelExecutor);
        try {
            return future.get(readTimeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            future.cancel(true);
            throw ex;
        }
    }

    private String chatCompletionsUrl() {
        if (!StringUtils.hasText(properties.getBaseUrl())) {
            throw new AiServiceException(HttpStatus.SERVICE_UNAVAILABLE, "TCMREASON_NOT_CONFIGURED",
                    "TCMReason 模型地址未配置，请设置 TCM_REASON_BASE_URL 或 TCM_LLM_BASE_URL。", null);
        }
        String base = properties.getBaseUrl().trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (base.endsWith("/v1/chat/completions")) {
            return base;
        }
        if (base.endsWith("/v1")) {
            return base + "/chat/completions";
        }
        return base + "/v1/chat/completions";
    }

    private HttpClient createHttpClient(TcmReasonProperties properties) {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(connectTimeout());
        if (!properties.isVerify()) {
            try {
                builder.sslContext(trustAllSslContext());
                SSLParameters sslParameters = new SSLParameters();
                sslParameters.setEndpointIdentificationAlgorithm("");
                builder.sslParameters(sslParameters);
            } catch (Exception ex) {
                log.warn("configure TCMReason trust-all SSL failed, falling back to default verifier", ex);
            }
        }
        return builder.build();
    }

    private SSLContext trustAllSslContext() throws Exception {
        TrustManager[] trustManagers = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, trustManagers, new SecureRandom());
        return context;
    }

    private String extractErrorMessage(String body) {
        if (!StringUtils.hasText(body)) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            JsonNode error = node.path("error");
            if (error.isTextual()) {
                return error.asText();
            }
            if (StringUtils.hasText(error.path("message").asText(null))) {
                return error.path("message").asText();
            }
            if (StringUtils.hasText(node.path("message").asText(null))) {
                return node.path("message").asText();
            }
        } catch (Exception ignored) {
            log.debug("parse TCMReason error body failed body={}", body);
        }
        return null;
    }

    private List<AiMessage> sanitize(List<AiMessage> messages) {
        if (messages == null) {
            return new ArrayList<>();
        }
        return messages.stream()
                .filter(message -> message != null && StringUtils.hasText(message.getContent()))
                .collect(Collectors.toList());
    }

    private String role(AiMessage message) {
        String role = StringUtils.hasText(message.getRole()) ? message.getRole().toLowerCase(Locale.ROOT) : "user";
        if ("assistant".equals(role) || "system".equals(role) || "user".equals(role)) {
            return role;
        }
        return "user";
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

    private String abbreviate(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return text.length() <= 120 ? text : text.substring(0, 120) + "...";
    }

    private String model() {
        return StringUtils.hasText(properties.getModel()) ? properties.getModel() : "TCMReason";
    }

    private Duration connectTimeout() {
        Duration timeout = properties.getConnectTimeout();
        return timeout == null || timeout.isNegative() || timeout.isZero() ? Duration.ofSeconds(10) : timeout;
    }

    private Duration readTimeout() {
        Duration timeout = properties.getReadTimeout();
        return timeout == null || timeout.isNegative() || timeout.isZero() ? Duration.ofSeconds(120) : timeout;
    }
}
