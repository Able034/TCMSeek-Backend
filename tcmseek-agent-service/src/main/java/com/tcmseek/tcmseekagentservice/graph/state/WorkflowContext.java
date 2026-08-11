package com.tcmseek.tcmseekagentservice.graph.state;

import cn.hutool.json.JSONUtil;
import com.tcmseek.tcmseekagentservice.ai.model.ClassifyCodeResult;
import com.tcmseek.tcmseekagentservice.ai.model.NormalizeEntityResult;
import com.tcmseek.tcmseekagentservice.ai.model.workflow.AnswerJudgeResult;
import com.tcmseek.tcmseekagentservice.ai.model.workflow.PlanResult;
import com.tcmseek.tcmseekagentservice.ai.model.workflow.WorkflowCallRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 工作流上下文 - 存储 LangGraph4j 节点之间传递的全部状态。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 在 LangGraph4j 的 MessagesState 中保存 WorkflowContext 的 key。
     */
    public static final String WORKFLOW_CONTEXT_KEY = "workflowContext";

    /**
     * 当前请求链路 ID，用于隔离 LangChain4j memoryId，也用于关联调用记录。
     */
    private String traceId;

    /**
     * 当前正在执行或最近完成的节点名称。
     */
    private String currentStep;

    /**
     * 用户原始问题。
     */
    private String originalPrompt;

    /**
     * 增强后的用户问题，预留给后续 query rewrite 或上下文增强使用。
     */
    private String enhancedPrompt;

    /**
     * 意图识别节点输出。
     */
    private ClassifyCodeResult intentResult;

    /**
     * 实体识别节点输出。
     */
    private NormalizeEntityResult entityResult;

    /**
     * 主 Agent 规划结果，决定后续执行哪个子 Agent。
     */
    private PlanResult planResult;

    /**
     * 证据完整性判断结果，决定回答、重规划或追问用户。
     */
    private AnswerJudgeResult answerJudgeResult;

    /**
     * Neo4j 子 Agent 查询结果。
     */
    private String neo4jResult;

    /**
     * MySQL 子 Agent 查询结果。
     */
    private String mysqlResult;

    /**
     * 最终给用户展示的回答。
     */
    private String finalAnswer;

    /**
     * 已经执行的重规划次数。
     */
    private Integer replanTimes;

    /**
     * 最大允许重规划次数，防止图流程无限循环。
     */
    private Integer maxReplanTimes;

    /**
     * 节点和 Agent 调用记录，用于接口返回、排障和后续落库。
     */
    @Builder.Default
    private List<WorkflowCallRecord> callRecords = new ArrayList<>();

    /**
     * 最近一次异常信息。
     */
    private String errorMessage;

    /**
     * 创建一次工作流执行的初始上下文。
     *
     * @param query 用户原始问题
     * @param maxReplanTimes 最大重规划次数，空值时默认 2 次
     * @return 初始化后的上下文对象
     */
    public static WorkflowContext start(String query, Integer maxReplanTimes) {
        return WorkflowContext.builder()
                .traceId(UUID.randomUUID().toString())
                .currentStep("START")
                .originalPrompt(query)
                .enhancedPrompt(query)
                .neo4jResult("未执行 Neo4j 查询")
                .mysqlResult("未执行 MySQL 查询")
                .replanTimes(0)
                .maxReplanTimes(maxReplanTimes == null ? 2 : Math.max(0, maxReplanTimes))
                .callRecords(new ArrayList<>())
                .build();
    }

    /**
     * 记录一次成功的节点或 Agent 调用。
     *
     * @param nodeName 当前图节点名称
     * @param agentName 执行调用的 Agent 名称
     * @param action 具体动作或方法名
     * @param input 调用输入，会被压缩成短 JSON
     * @param output 调用输出，会被压缩成短 JSON
     * @param latencyMs 调用耗时，单位毫秒
     */
    public void recordCall(String nodeName,
                           String agentName,
                           String action,
                           Object input,
                           Object output,
                           long latencyMs) {
        ensureCallRecords();
        callRecords.add(WorkflowCallRecord.builder()
                .traceId(traceId)
                .nodeName(nodeName)
                .agentName(agentName)
                .action(action)
                .inputSummary(shortJson(input))
                .outputSummary(shortJson(output))
                .status("SUCCESS")
                .latencyMs(latencyMs)
                .createdAt(LocalDateTime.now())
                .build());
    }

    /**
     * 记录一次失败的节点或 Agent 调用。
     *
     * @param nodeName 当前图节点名称
     * @param agentName 执行调用的 Agent 名称
     * @param action 具体动作或方法名
     * @param input 调用输入，会被压缩成短 JSON
     * @param error 捕获到的异常
     * @param latencyMs 调用耗时，单位毫秒
     */
    public void recordError(String nodeName,
                            String agentName,
                            String action,
                            Object input,
                            Exception error,
                            long latencyMs) {
        ensureCallRecords();
        callRecords.add(WorkflowCallRecord.builder()
                .traceId(traceId)
                .nodeName(nodeName)
                .agentName(agentName)
                .action(action)
                .inputSummary(shortJson(input))
                .status("FAILED")
                .latencyMs(latencyMs)
                .errorMessage(error.getMessage())
                .createdAt(LocalDateTime.now())
                .build());
        this.errorMessage = error.getMessage();
    }

    /**
     * 重规划节点每执行一次就递增计数。
     */
    public void increaseReplanTimes() {
        this.replanTimes = this.replanTimes == null ? 1 : this.replanTimes + 1;
    }

    /**
     * 判断是否仍有重规划预算。
     */
    public boolean canReplan() {
        int current = replanTimes == null ? 0 : replanTimes;
        int max = maxReplanTimes == null ? 2 : maxReplanTimes;
        return current < max;
    }

    /**
     * 从 LangGraph4j state 中读取业务上下文。
     */
    public static WorkflowContext getContext(MessagesState<String> state) {
        return (WorkflowContext) state.data().get(WORKFLOW_CONTEXT_KEY);
    }

    /**
     * 从 LangGraph4j state 中读取业务上下文；不存在时直接抛错。
     */
    public static WorkflowContext requireContext(MessagesState<String> state) {
        WorkflowContext context = getContext(state);
        if (context == null) {
            throw new IllegalStateException("WorkflowContext not found in graph state");
        }
        return context;
    }

    /**
     * 将业务上下文保存为 LangGraph4j 节点返回的增量状态。
     */
    public static Map<String, Object> saveContext(WorkflowContext context) {
        return Map.of(WORKFLOW_CONTEXT_KEY, context);
    }

    /**
     * 确保调用记录列表存在，兼容反序列化或旧状态未初始化的情况。
     */
    private void ensureCallRecords() {
        if (callRecords == null) {
            callRecords = new ArrayList<>();
        }
    }

    /**
     * 将输入/输出压缩成短 JSON，避免调用记录过大撑爆响应体或模型上下文。
     */
    private String shortJson(Object value) {
        if (value == null) {
            return "";
        }
        String text;
        try {
            text = value instanceof String stringValue ? stringValue : JSONUtil.toJsonStr(value);
        } catch (Exception e) {
            text = String.valueOf(value);
        }
        return text.length() <= 1200 ? text : text.substring(0, 1200) + "...";
    }
}
