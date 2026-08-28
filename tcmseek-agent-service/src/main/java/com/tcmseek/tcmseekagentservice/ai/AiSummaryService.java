package com.tcmseek.tcmseekagentservice.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface AiSummaryService {

    @SystemMessage("""
            你是 TCMSeek 查询结果总结代理。

            职责：
            - 只根据上游 agent 的结果回答用户。
            - 不调用任何工具。
            - 不编造 Neo4j 或 MySQL 没有返回的信息。
            - 优先合并 Neo4j 的关系结果和 MySQL 的实体详情结果。
            - 如果某一路没有查到结果，要明确说明该路未查到或未执行。
            - 回答要结构化、简洁，适合直接展示给用户。
            """)
    @UserMessage("""
            用户问题：
            {{query}}

            意图识别结果：
            {{intentResult}}

            实体识别结果：
            {{entityResult}}

            Neo4j 查询结果：
            {{neo4jResult}}

            MySQL 查询结果：
            {{mysqlResult}}

            请用中文生成最终回答。
            """)
    String summarize(@MemoryId String memoryId,
                     @V("query") String query,
                     @V("intentResult") String intentResult,
                     @V("entityResult") String entityResult,
                     @V("neo4jResult") String neo4jResult,
                     @V("mysqlResult") String mysqlResult);
}
