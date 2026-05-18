package com.tcmseek.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmseek.pojo.entity.TargetPredictionJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class TargetPredictionCacheManager {

    private static final String KEY_PREFIX = "job:status:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TargetPredictionJob get(Long jobId) {
        String key = KEY_PREFIX + jobId;
        String json = redisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, TargetPredictionJob.class);
        } catch (Exception ex) {
            log.warn("解析缓存失败，清理 key={}, err={}", key, ex.getMessage());
            redisTemplate.delete(key);
            return null;
        }
    }

    public void put(TargetPredictionJob job) {
        if (job == null || job.getId() == null) {
            return;
        }
        String key = KEY_PREFIX + job.getId();
        Duration ttl = isFinal(job) ? Duration.ofMinutes(30) : Duration.ofSeconds(30);
        try {
            String json = objectMapper.writeValueAsString(job);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (JsonProcessingException e) {
            log.warn("写入缓存失败 key={}, err={}", key, e.getMessage());
        }
    }

    public void evict(Long jobId) {
        redisTemplate.delete(KEY_PREFIX + jobId);
    }

    private boolean isFinal(TargetPredictionJob job) {
        String status = job.getStatus();
        return TargetPredictionConstants.STATUS_COMPLETED.equals(status)
                || TargetPredictionConstants.STATUS_FAILED.equals(status);
    }
}
