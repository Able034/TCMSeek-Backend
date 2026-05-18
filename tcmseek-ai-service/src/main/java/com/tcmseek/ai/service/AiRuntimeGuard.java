package com.tcmseek.ai.service;

import com.tcmseek.ai.config.AiRuntimeProperties;
import com.tcmseek.ai.exception.AiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Supplier;

@Component
public class AiRuntimeGuard {

    private static final Logger log = LoggerFactory.getLogger(AiRuntimeGuard.class);

    private final AiRuntimeProperties runtimeProperties;

    public AiRuntimeGuard(AiRuntimeProperties runtimeProperties) {
        this.runtimeProperties = runtimeProperties;
    }

    public <T> T executeModelCall(String operation, Supplier<T> supplier) {
        int attempts = Math.max(1, runtimeProperties.getMaxRetries() + 1);
        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            long startedAt = System.currentTimeMillis();
            try {
                T result = supplier.get();
                log.info("ai model call succeeded operation={} attempt={} costMs={}",
                        operation, attempt, System.currentTimeMillis() - startedAt);
                return result;
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
}
