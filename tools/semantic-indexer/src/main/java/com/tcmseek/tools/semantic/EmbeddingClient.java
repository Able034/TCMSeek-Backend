package com.tcmseek.tools.semantic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class EmbeddingClient {

    private final IndexerConfig config;

    private final List<String> apiKeys;

    private final AtomicInteger keyCursor = new AtomicInteger();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public EmbeddingClient(IndexerConfig config) {
        this.config = config;
        this.apiKeys = config.embeddingApiKeys();
    }

    public String embedAsVectorLiteral(String input) throws IOException, InterruptedException {
        if (apiKeys == null || apiKeys.isEmpty()) {
            throw new IllegalStateException("embedding API key is required; use 'local' for local service");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", config.embeddingModel());
        body.put("input", input);
        body.put("dimensions", config.embeddingDimension());
        String requestBody = objectMapper.writeValueAsString(body);

        HttpResponse<String> response = null;
        int maxAttempts = Math.max(3, apiKeys.size());
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.embeddingBaseUrl() + "/embeddings"))
                    .version(HttpClient.Version.HTTP_1_1)
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + nextApiKey())
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody.getBytes(StandardCharsets.UTF_8)))
                    .build();
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                break;
            }
            if (attempt < maxAttempts && isRetryable(response.statusCode())) {
                Thread.sleep(backoffMillis(attempt));
                continue;
            }
            throw new IllegalStateException("embedding request failed status=" + response.statusCode()
                    + " body=" + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode embedding = root.path("data").path(0).path("embedding");
        if (!embedding.isArray()) {
            throw new IllegalStateException("embedding response missing data[0].embedding");
        }
        if (embedding.size() != config.embeddingDimension()) {
            throw new IllegalStateException("embedding dimension mismatch expected="
                    + config.embeddingDimension() + " actual=" + embedding.size());
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(embedding.get(i).asDouble());
        }
        return sb.append(']').toString();
    }

    private String nextApiKey() {
        int index = Math.floorMod(keyCursor.getAndIncrement(), apiKeys.size());
        return apiKeys.get(index);
    }

    private boolean isRetryable(int statusCode) {
        return statusCode == 429 || statusCode == 408 || statusCode >= 500;
    }

    private long backoffMillis(int attempt) {
        return Math.min(10_000L, 500L * attempt * attempt);
    }
}
