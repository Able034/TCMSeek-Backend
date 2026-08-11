package com.tcmseek.tcmseekagentservice.ai.model.workflow;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 主 Agent 输出的工作流计划。
 *
 * <p>该对象只表达“下一步怎么走”，不承载最终回答。
 * LangGraph4j 条件边会读取 nextAction 来决定进入哪个节点。</p>
 */
@Data
@Description("主 Agent 生成的工作流执行计划")
public class PlanResult implements Serializable {

    /**
     * 下一步动作，只能是 NEO4J、MYSQL、JUDGE、SUMMARY、ASK_USER 之一。
     */
    @Description("下一步动作，只能是 NEO4J、MYSQL、JUDGE、SUMMARY、ASK_USER 之一")
    private String nextAction;

    /**
     * 本轮计划步骤，用中文短句描述。
     */
    @Description("本轮计划步骤，用中文短句描述")
    private List<String> steps;

    /**
     * 本轮计划需要使用的数据源。
     */
    @Description("需要使用的数据源，只能包含 NEO4J、MYSQL")
    private List<String> requiredSources;

    /**
     * 当前缺失或需要补充的信息。
     */
    @Description("当前已经缺失或需要补充的信息")
    private List<String> missingInfo;

    /**
     * 停止继续查询并进入总结的条件。
     */
    @Description("停止查询并进入总结的条件")
    private String stopCondition;

    /**
     * 计划或重规划原因。
     */
    @Description("计划或重规划原因")
    private String reason;
}
