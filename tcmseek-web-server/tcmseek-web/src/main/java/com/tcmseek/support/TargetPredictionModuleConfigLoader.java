package com.tcmseek.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * 读取 target-prediction-module.json，用于前端展示配置
 */
@Component
@Slf4j
public class TargetPredictionModuleConfigLoader {

    private static final String CONFIG_PATH = "target-prediction-module.json";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Map<String, Object> cachedConfig = Collections.emptyMap();

    @PostConstruct
    public void init() {
        reload();
    }

    public synchronized void reload() {
        try (InputStream inputStream = new ClassPathResource(CONFIG_PATH).getInputStream()) {
            cachedConfig = objectMapper.readValue(inputStream, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException ex) {
            log.error("Failed to load target prediction module config", ex);
        }
    }

    public Map<String, Object> getConfig() {
        return cachedConfig;
    }
}
