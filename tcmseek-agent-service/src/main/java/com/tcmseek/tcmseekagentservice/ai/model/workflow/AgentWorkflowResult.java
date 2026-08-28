package com.tcmseek.tcmseekagentservice.ai.model.workflow;

import com.tcmseek.tcmseekagentservice.ai.model.ClassifyCodeResult;
import com.tcmseek.tcmseekagentservice.ai.model.NormalizeEntityResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 多 Agent 工作流对外返回结果。
 *
 * <p>除了最终答案，也返回中间状态和调用记录，方便前端展示执行过程，
 * 以及后端排查某个节点为什么没有查到结果。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentWorkflowResult {

    /**
     * 本次请求链路 ID。
     */
    private String traceId;

    /**
     * 用户原始问题。
     */
    private String query;

    /**
     * 最终回答内容。
     */
    private String finalAnswer;

    /**
     * 意图识别结果。
     */
    private ClassifyCodeResult intentResult;

    /**
     * 实体识别结果。
     */
    private NormalizeEntityResult entityResult;

    /**
     * 主 Agent 的最近一次执行计划。
     */
    private PlanResult planResult;

    /**
     * 证据完整性判断结果。
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
     * 实际执行的重规划次数。
     */
    private Integer replanTimes;

    /**
     * 每个节点和 Agent 调用的过程记录。
     */
    private List<WorkflowCallRecord> callRecords;
}
