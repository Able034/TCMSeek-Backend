package com.tcmseek.tcmseekagentservice.ai;

import com.tcmseek.tcmseekagentservice.ai.model.workflow.PlanResult;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 工作流统筹规划 Agent。
 *
 * <p>该 Agent 是“主控大脑”，只负责规划下一步走哪个节点，
 * 不直接调用 Neo4j/MySQL 工具，也不生成最终回答。</p>
 */
public interface AiPlannerService {

    /**
     * 根据用户问题、意图、实体和已有证据生成下一步计划。
     *
     * @param memoryId LangChain4j 记忆隔离 ID
     * @param query 用户原始问题
     * @param intentResult 意图识别结果 JSON
     * @param entityResult 实体识别结果 JSON
     * @param neo4jResult 当前 Neo4j 查询结果
     * @param mysqlResult 当前 MySQL 查询结果
     * @param replanReason 初始规划或重规划原因
     * @param replanTimes 当前重规划次数
     * @return 结构化工作流计划
     */
    @SystemMessage("""
            你是 TCMSeek 多 Agent 工作流的统筹主 Agent。
            你不直接回答用户，也不调用数据库工具，只负责根据意图、实体和已有证据规划下一步。

            可用子 Agent：
            - NEO4J：查询知识图谱关系、路径、共同邻居、关联对象。
            - MYSQL：查询实体详细字段，例如中药、方剂、靶点、疾病等详情。
            - JUDGE：判断现有证据是否足够回答问题。
            - SUMMARY：生成最终回答。

            规划原则：
            1. 如果需要实体关系，优先安排 NEO4J。
            2. 如果只查实体详情，安排 MYSQL。
            3. 如果 MySQL 需要基于 Neo4j 结果补充详情，先 NEO4J 后 MYSQL。
            4. 如果已有证据足够或没有可执行查询，安排 JUDGE。
            5. 重规划时只能补齐缺失信息，不要重复已经失败且没有新条件的查询。
            6. nextAction 只能输出 NEO4J、MYSQL、JUDGE、SUMMARY、ASK_USER。
            """)
    @UserMessage("""
            用户问题：{{query}}

            意图识别结果：
            {{intentResult}}

            实体识别结果：
            {{entityResult}}

            Neo4j 当前结果：
            {{neo4jResult}}

            MySQL 当前结果：
            {{mysqlResult}}

            上次判断或重规划原因：
            {{replanReason}}

            当前重规划次数：{{replanTimes}}

            请输出结构化执行计划。
            """)
    @Agent("TCMSeek 工作流统筹规划 Agent")
    PlanResult plan(@MemoryId String memoryId,
                    @V("query") String query,
                    @V("intentResult") String intentResult,
                    @V("entityResult") String entityResult,
                    @V("neo4jResult") String neo4jResult,
                    @V("mysqlResult") String mysqlResult,
                    @V("replanReason") String replanReason,
                    @V("replanTimes") Integer replanTimes);
}
