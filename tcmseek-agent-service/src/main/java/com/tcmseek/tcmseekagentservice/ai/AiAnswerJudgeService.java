package com.tcmseek.tcmseekagentservice.ai;

import com.tcmseek.tcmseekagentservice.ai.model.workflow.AnswerJudgeResult;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 查询证据完整性判断 Agent。
 *
 * <p>该 Agent 只判断“现有证据够不够回答”，不调用工具、不补查数据、不生成最终自然语言答案。</p>
 */
public interface AiAnswerJudgeService {

    /**
     * 判断 Neo4j/MySQL 结果是否已经覆盖用户核心问题。
     *
     * @param memoryId LangChain4j 记忆隔离 ID
     * @param query 用户原始问题
     * @param intentResult 意图识别结果 JSON
     * @param entityResult 实体识别结果 JSON
     * @param planResult 最近一次执行计划 JSON
     * @param neo4jResult Neo4j 查询结果
     * @param mysqlResult MySQL 查询结果
     * @return 结构化证据判断结果
     */
    @SystemMessage("""
            你是 TCMSeek 查询证据完整性判断 Agent。
            你不调用任何工具，也不生成最终回答，只判断上游结果是否足够回答用户问题。

            判断标准：
            1. 如果用户问实体关系，Neo4j 结果需要包含相关关系或明确未查到。
            2. 如果用户问实体详细字段，MySQL 结果需要包含对应详情或明确未查到。
            3. 如果结果为空、只有错误、或没有覆盖用户核心问题，应判定 answerable=false。
            4. 如果继续查询也需要用户提供更明确实体或问题，应 nextAction=ASK_USER。
            5. 如果可以基于现有证据回答，应 nextAction=FINISH。
            6. 如果还可以通过补查 Neo4j/MySQL 改善结果，应 nextAction=REPLAN。
            """)
    @UserMessage("""
            用户问题：
            {{query}}

            意图识别结果：
            {{intentResult}}

            实体识别结果：
            {{entityResult}}

            执行计划：
            {{planResult}}

            Neo4j 查询结果：
            {{neo4jResult}}

            MySQL 查询结果：
            {{mysqlResult}}

            请判断是否足够回答用户。
            """)
    @Agent("TCMSeek 证据完整性判断 Agent")
    AnswerJudgeResult judge(@MemoryId String memoryId,
                            @V("query") String query,
                            @V("intentResult") String intentResult,
                            @V("entityResult") String entityResult,
                            @V("planResult") String planResult,
                            @V("neo4jResult") String neo4jResult,
                            @V("mysqlResult") String mysqlResult);
}
