package com.tcmseek.controller;

import com.tcmseek.common.annotation.Anonymous;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

/**
 * Proxy controller for the Expression2Kinases (X2K) API so that the frontend can bypass CORS limitations.
 */
@RestController
@RequestMapping("/tcmseek/tools")
@Slf4j
public class X2KProxyController {

    private static final String DEFAULT_X2K_URL = "https://maayanlab.cloud/X2K/api";

    @Value("${x2k.api.url:" + DEFAULT_X2K_URL + "}")
    private String x2kApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Anonymous
    @PostMapping(value = "/x2k", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> proxyX2K(@RequestParam MultiValueMap<String, String> formData) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        formData.forEach((key, values) -> values.forEach(value -> body.add(key, value)));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(x2kApiUrl, requestEntity, String.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception ex) {
            log.error("Failed to proxy X2K request", ex);
            Map<String, String> errorBody = Collections.singletonMap("message", "X2K 服务暂不可用，请稍后再试");
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorBody);
        }
    }
}
