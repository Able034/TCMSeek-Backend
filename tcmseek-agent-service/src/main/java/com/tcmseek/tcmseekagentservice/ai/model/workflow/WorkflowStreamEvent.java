package com.tcmseek.tcmseekagentservice.ai.model.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SSE 流式事件模型。
 *
 * <p>流式接口不会只返回最终文本，而是把工作流过程也按事件发给前端。
 * 前端可以通过 type 区分节点进度、最终回答 token、完成事件和错误事件。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStreamEvent {

    /**
     * 事件类型，例如 workflow_started、node_started、node_finished、answer_delta。
     */
    private String type;

    /**
     * 本次请求链路 ID。
     */
    private String traceId;

    /**
     * 当前节点名称；非节点事件可以为空。
     */
    private String nodeName;

    /**
     * 事件负载，可以是结构化结果、token 片段、错误信息或最终结果摘要。
     */
    private Object data;

    /**
     * 事件创建时间。
     */
    private LocalDateTime timestamp;
}
