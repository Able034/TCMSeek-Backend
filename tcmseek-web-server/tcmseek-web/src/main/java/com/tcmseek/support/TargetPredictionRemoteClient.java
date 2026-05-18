package com.tcmseek.support;

import com.tcmseek.common.exception.ServiceException;
import com.tcmseek.dto.TargetPredictionJobRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;
import java.util.Map;

@Component
@Slf4j
public class TargetPredictionRemoteClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${target-prediction.service-url:http://127.0.0.1:9000}")
    private String serviceBaseUrl;

    public Map<String, Object> submitJob(TargetPredictionJobRequest request, Path sdfPath, String wemolUserName) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("sdfFile", new FileSystemResource(sdfPath));
        body.add("referenceDatabase", safeValue(request.getReferenceDatabase()));
        body.add("queryConformations", safeValue(request.getQueryConformations()));
        body.add("similarityThreshold", safeValue(request.getSimilarityThreshold()));
        body.add("activityThreshold", safeValue(request.getActivityThreshold()));
        body.add("ranking", safeValue(request.getRanking()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        return exchange(HttpMethod.POST, "/api/target-prediction/jobs", entity, wemolUserName);
    }

    public Map<String, Object> getJobStatus(Long wemolJobId, String wemolUserName) {
        return exchange(HttpMethod.GET, "/api/target-prediction/jobs/" + wemolJobId, null, wemolUserName);
    }

    public Map<String, Object> fetchResult(Long wemolJobId, String wemolUserName) {
        return exchange(HttpMethod.POST, "/api/target-prediction/jobs/" + wemolJobId + "/result", null, wemolUserName);
    }

    public Map<String, Object> listJobs(String wemolUserName) {
        return exchange(HttpMethod.GET, "/api/target-prediction/jobs", null, wemolUserName);
    }

    public Map<String, Object> getModuleProfile(String wemolUserName) {
        return exchange(HttpMethod.GET, "/api/target-prediction/module", null, wemolUserName);
    }

    public void login(String wemolUserName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> payload = Map.of("username", wemolUserName);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);
        exchange(HttpMethod.POST, "/api/target-prediction/login", entity, null);
    }

    private Map<String, Object> exchange(HttpMethod method, String path, HttpEntity<?> entity, String wemolUserName) {
        String targetUrl = formatBaseUrl() + path;
        try {
            HttpEntity<?> payload = attachUserHeader(entity, wemolUserName);
            ResponseEntity<Map> exchange = restTemplate.exchange(targetUrl, method, payload, Map.class);
            Map<String, Object> body = exchange.getBody();
            if (body == null) {
                throw new ServiceException("靶点预测服务返回空响应");
            }
            return body;
        } catch (RestClientException ex) {
            log.error("调用靶点预测服务失败: {}", ex.getMessage());
            throw new ServiceException("靶点预测服务暂不可用");
        }
    }

    private HttpEntity<?> attachUserHeader(HttpEntity<?> entity, String wemolUserName) {
        HttpHeaders headers = new HttpHeaders();
        if (entity != null && entity.getHeaders() != null) {
            headers.putAll(entity.getHeaders());
        }
        if (wemolUserName != null) {
            headers.set("X-WEMOL-USER", wemolUserName);
        }
        Object body = entity != null ? entity.getBody() : null;
        return body == null ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
    }

    private String formatBaseUrl() {
        if (serviceBaseUrl == null) {
            return "";
        }
        return serviceBaseUrl.endsWith("/") ? serviceBaseUrl.substring(0, serviceBaseUrl.length() - 1) : serviceBaseUrl;
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }
}
