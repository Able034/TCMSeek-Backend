package com.tcmseek.ai.service;

import com.tcmseek.ai.config.AiVectorProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class QwenEmbeddingClient {

    private final AiVectorProperties properties;

    private final RestClient restClient;

    public QwenEmbeddingClient(AiVectorProperties properties) {
        this.properties = properties;
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.restClient = RestClient.builder()
                .baseUrl(trimTrailingSlash(properties.getEmbeddingBaseUrl()))
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }

    public boolean isConfigured() {
        return properties.isEnabled()
                && StringUtils.hasText(properties.getEmbeddingApiKey())
                && StringUtils.hasText(properties.getEmbeddingModel());
    }

    public List<Double> embed(String input) {
        if (!isConfigured()) {
            throw new IllegalStateException("Qwen embedding is not configured");
        }
        if (!StringUtils.hasText(input)) {
            throw new IllegalArgumentException("embedding input must not be blank");
        }
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", properties.getEmbeddingModel());
        request.put("input", input);
        request.put("dimensions", properties.getDimension());

        EmbeddingResponse response = restClient.post()
                .uri("/embeddings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getEmbeddingApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(EmbeddingResponse.class);

        if (response == null || response.getData() == null || response.getData().isEmpty()
                || response.getData().get(0).getEmbedding() == null) {
            throw new IllegalStateException("Qwen embedding response is empty");
        }
        List<Double> embedding = response.getData().get(0).getEmbedding();
        if (embedding.size() != properties.getDimension()) {
            throw new IllegalStateException("Qwen embedding dimension mismatch: expected "
                    + properties.getDimension() + ", got " + embedding.size());
        }
        return embedding;
    }

    public String embedAsVectorLiteral(String input) {
        return toVectorLiteral(embed(input));
    }

    public static String toVectorLiteral(List<Double> embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            Double value = embedding.get(i);
            sb.append(value == null ? "0" : value);
        }
        return sb.append(']').toString();
    }

    private static String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String result = value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public static class EmbeddingResponse {

        private List<EmbeddingData> data;

        public List<EmbeddingData> getData() {
            return data;
        }

        public void setData(List<EmbeddingData> data) {
            this.data = data;
        }
    }

    public static class EmbeddingData {

        private List<Double> embedding;

        public List<Double> getEmbedding() {
            return embedding;
        }

        public void setEmbedding(List<Double> embedding) {
            this.embedding = embedding;
        }
    }
}
