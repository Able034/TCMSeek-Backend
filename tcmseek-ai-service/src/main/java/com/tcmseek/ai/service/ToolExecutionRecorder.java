package com.tcmseek.ai.service;

import com.tcmseek.ai.dto.GraphToolResult;
import com.tcmseek.ai.dto.ToolCallResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ToolExecutionRecorder {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutionRecorder.class);

    private static final ThreadLocal<List<ToolCallResult>> RECORDED_CALLS =
            ThreadLocal.withInitial(ArrayList::new);

    public void start() {
        RECORDED_CALLS.set(new ArrayList<>());
    }

    public void record(String toolName, Map<String, Object> arguments, GraphToolResult result) {
        RECORDED_CALLS.get().add(new ToolCallResult(toolName, arguments, result));
        int total = result == null ? 0 : result.getTotal();
        log.info("ai graph tool executed tool={} total={} args={}", toolName, total, arguments);
    }

    public List<ToolCallResult> finish() {
        List<ToolCallResult> calls = new ArrayList<>(RECORDED_CALLS.get());
        RECORDED_CALLS.remove();
        return calls;
    }
}
