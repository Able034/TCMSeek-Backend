package com.tcmseek.tcmseekagentservice.service;

import com.tcmseek.tcmseekagentservice.ai.model.workflow.AgentWorkflowResult;
import com.tcmseek.tcmseekagentservice.ai.model.workflow.WorkflowStreamEvent;
import com.tcmseek.tcmseekagentservice.graph.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 工作流 SSE 事件发布器。
 *
 * <p>每个流式请求创建一个 publisher，并绑定一个 {@link SseEmitter}。
 * 同步接口使用 {@link #noop()}，这样节点逻辑不用分成两套。</p>
 */
@Slf4j
public class WorkflowEventPublisher {

    /**
     * 空发布器，用于非流式工作流。
     */
    private static final WorkflowEventPublisher NOOP = new WorkflowEventPublisher(null, null);

    /**
     * SSE 连接对象。
     */
    private final SseEmitter emitter;

    /**
     * 当前请求链路 ID。
     */
    private final String traceId;

    /**
     * SseEmitter 不是为多线程并发 send 设计的，这里用锁串行化发送。
     */
    private final Object sendMonitor = new Object();

    /**
     * 创建绑定 SSE 的事件发布器。
     */
    public WorkflowEventPublisher(SseEmitter emitter, String traceId) {
        this.emitter = emitter;
        this.traceId = traceId;
    }

    /**
     * 获取空发布器。
     */
    public static WorkflowEventPublisher noop() {
        return NOOP;
    }

    /**
     * 当前是否绑定了真实 SSE 连接。
     */
    public boolean isEnabled() {
        return emitter != null;
    }

    /**
     * 推送工作流开始事件。
     */
    public void workflowStarted(WorkflowContext context) {
        send("workflow_started", null, Map.of(
                "query", context.getOriginalPrompt(),
                "maxReplanTimes", context.getMaxReplanTimes()
        ));
    }

    /**
     * 推送节点开始事件。
     */
    public void nodeStarted(WorkflowContext context, String nodeName) {
        send("node_started", nodeName, Map.of(
                "currentStep", context.getCurrentStep()
        ));
    }

    /**
     * 推送节点完成事件。
     */
    public void nodeFinished(WorkflowContext context, String nodeName, Object data) {
        send("node_finished", nodeName, data);
    }

    /**
     * 推送节点失败事件；节点内部可能会降级后继续执行，所以这里不一定代表工作流终止。
     */
    public void nodeFailed(WorkflowContext context, String nodeName, Throwable error) {
        send("node_failed", nodeName, Map.of(
                "message", error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage()
        ));
    }

    /**
     * 推送最终回答 token 片段。
     */
    public void answerDelta(WorkflowContext context, String delta) {
        send("answer_delta", null, Map.of("delta", delta));
    }

    /**
     * 推送最终回答完成事件。
     */
    public void answerDone(WorkflowContext context, String finalAnswer) {
        send("answer_done", null, Map.of("finalAnswer", finalAnswer));
    }

    /**
     * 推送工作流完成事件。
     */
    public void workflowDone(AgentWorkflowResult result) {
        send("workflow_done", null, result);
    }

    /**
     * 推送工作流级错误事件。
     */
    public void error(Throwable error) {
        send("error", null, Map.of(
                "message", error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage()
        ));
    }

    /**
     * 正常关闭 SSE 连接。
     */
    public void complete() {
        if (emitter != null) {
            emitter.complete();
        }
    }

    /**
     * 异常关闭 SSE 连接。
     */
    public void completeWithError(Throwable error) {
        if (emitter != null) {
            emitter.completeWithError(error);
        }
    }

    /**
     * 统一构造并发送 SSE 事件。
     */
    private void send(String type, String nodeName, Object data) {
        if (emitter == null) {
            return;
        }
        WorkflowStreamEvent event = WorkflowStreamEvent.builder()
                .type(type)
                .traceId(traceId)
                .nodeName(nodeName)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
        try {
            synchronized (sendMonitor) {
                emitter.send(SseEmitter.event()
                        .name(type)
                        .data(event));
            }
        } catch (IOException | IllegalStateException e) {
            log.warn("Failed to send workflow SSE event, type={}, traceId={}", type, traceId, e);
        }
    }
}
