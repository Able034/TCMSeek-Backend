package com.tcmseek.tcmseekagentservice.ai.model.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工作流调用记录。
 *
 * <p>一条记录对应一个节点/Agent 的一次成功或失败调用。
 * 当前先保存在 WorkflowContext 中，后续如果需要审计或可观测性，可以直接落库。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowCallRecord implements Serializable {

    /**
     * 本次请求链路 ID。
     */
    private String traceId;

    /**
     * LangGraph4j 节点名称。
     */
    private String nodeName;

    /**
     * 执行调用的 Agent 或系统名称。
     */
    private String agentName;

    /**
     * 具体动作或方法名。
     */
    private String action;

    /**
     * 输入摘要，过长内容会被截断。
     */
    private String inputSummary;

    /**
     * 输出摘要，过长内容会被截断。
     */
    private String outputSummary;

    /**
     * 调用状态，当前使用 SUCCESS 或 FAILED。
     */
    private String status;

    /**
     * 调用耗时，单位毫秒。
     */
    private Long latencyMs;

    /**
     * 失败时的异常信息。
     */
    private String errorMessage;

    /**
     * 记录创建时间。
     */
    private LocalDateTime createdAt;
}
