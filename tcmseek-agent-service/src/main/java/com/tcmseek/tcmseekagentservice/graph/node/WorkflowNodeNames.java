package com.tcmseek.tcmseekagentservice.graph.node;

/**
 * TCMSeek Agent 工作流中的 LangGraph4j 节点名称常量。
 *
 * <p>节点名会同时用于图结构、路由返回值、调用记录和 memoryId 拼接，
 * 集中定义可以避免多个类之间手写字符串不一致。</p>
 */
public final class WorkflowNodeNames {

    /**
     * LangGraph4j 内置开始节点名称。
     */
    public static final String START = "__START__";

    /**
     * LangGraph4j 内置结束节点名称。
     */
    public static final String END = "__END__";

    /**
     * 意图识别节点：判断问题类型和需要的数据源。
     */
    public static final String INTENT = "intent";

    /**
     * 实体识别节点：抽取可用于图谱查询的起始实体。
     */
    public static final String ENTITY = "entity";

    /**
     * 统筹规划节点：由主 Agent 生成下一步执行计划。
     */
    public static final String PLANNER = "planner";

    /**
     * Neo4j 子 Agent 节点：只负责图谱关系查询。
     */
    public static final String NEO4J = "neo4j_agent";

    /**
     * MySQL 子 Agent 节点：只负责实体详情查询。
     */
    public static final String MYSQL = "mysql_agent";

    /**
     * 证据判断节点：判断当前查询结果是否足够回答。
     */
    public static final String JUDGE = "answer_judge";

    /**
     * 重规划节点：证据不足时重新规划补查路径。
     */
    public static final String REPLAN = "replan";

    /**
     * 最终总结节点：根据已有证据生成面向用户的回答。
     */
    public static final String SUMMARY = "summary";

    private WorkflowNodeNames() {
    }
}
