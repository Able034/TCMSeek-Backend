package com.tcmseek.ai.service;

import com.tcmseek.ai.dto.GraphToolResult;
import com.tcmseek.ai.dto.ToolCallResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class ToolExecutionRecorder {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutionRecorder.class);

    private static final ThreadLocal<List<ToolCallResult>> RECORDED_CALLS = new ThreadLocal<>();

    public void start() {
        RECORDED_CALLS.set(new ArrayList<>());
    }

    public void record(String toolName, Map<String, Object> arguments, GraphToolResult result) {
        List<ToolCallResult> calls = RECORDED_CALLS.get();
        if (calls == null) {
            calls = new ArrayList<>();
            RECORDED_CALLS.set(calls);
        }
        calls.add(new ToolCallResult(toolName, arguments, result));
        int total = result == null ? 0 : result.getTotal();
        log.info("ai graph tool executed tool={} total={} args={}", toolName, total, arguments);
    }

    public <T> Supplier<T> propagate(Supplier<T> supplier) {
        List<ToolCallResult> calls = RECORDED_CALLS.get();
        return () -> {
            List<ToolCallResult> previous = RECORDED_CALLS.get();
            boolean hadPrevious = previous != null;
            RECORDED_CALLS.set(calls);
            try {
                return supplier.get();
            } finally {
                if (hadPrevious) {
                    RECORDED_CALLS.set(previous);
                } else {
                    RECORDED_CALLS.remove();
                }
            }
        };
    }

    public List<ToolCallResult> finish() {
        List<ToolCallResult> current = RECORDED_CALLS.get();
        List<ToolCallResult> calls = current == null ? List.of() : new ArrayList<>(current);
        RECORDED_CALLS.remove();
        return calls;
    }
}
