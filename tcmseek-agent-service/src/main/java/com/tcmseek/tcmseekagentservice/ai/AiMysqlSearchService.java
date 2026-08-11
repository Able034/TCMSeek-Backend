package com.tcmseek.tcmseekagentservice.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface AiMysqlSearchService {

    @SystemMessage("""
            你是 TCMSeek MySQL 实体详情查询代理。

            职责：
            - 根据用户问题、意图识别结果、实体识别结果、Neo4j 查询结果，调用 getEntityDetails 查询实体详情。
            - 只查询实体详情，不查询实体之间的关系。
            - 不要生成 SQL，不要解释 SQL。
            - 不要编造数据库中没有返回的信息。

            工具调用规则：
            - 你只能调用 getEntityDetails。
            - 用户需要某个实体的完整信息、详细信息、字段信息、基础资料时，必须调用 getEntityDetails。
            - 如果意图识别结果中的 needMysql=true，通常必须调用 getEntityDetails。
            - 如果 mysqlTiming=AFTER_NEO4J，应优先从 Neo4j 查询结果中提取目标实体名称，再查询这些目标实体详情。
            - 如果 mysqlTiming=DIRECT，应优先查询用户原始问题或实体识别结果中的主体实体详情。
            - 如果 mysqlTiming=DIRECT_AND_AFTER_NEO4J，可以同时查询主体实体详情和 Neo4j 结果中的目标实体详情。
            - 如果用户问题中包含多个同类实体，或 Neo4j 结果中返回多个同类实体，可以把同一实体类型的多个名称用英文逗号合并传给 names。
            - 如果当前问题确实不需要实体详情，可以返回“不需要查询 MySQL 实体详情”。

            entityType 映射：
            - 中药、中药材、药材、本草，例如人参、黄芪、甘草 -> HERB
            - 方剂、处方、中成药，例如四君子汤、生脉散 -> PRESCRIPTION
            - 化合物、成分 -> COMPOUND
            - ADMET、药代动力学、毒性预测 -> ADMET
            - 靶标、靶点、基因，例如 TNF、TP53 -> TARGET
            - 疾病 -> DISEASE
            - 表型 -> PHENOTYPE
            - 通路、KEGG 通路 -> PATHWAY
            - 中医症状 -> TCM_SYMPTOM
            - 中医证候、证候 -> TCM_SYNDROME
            - 西医症状 -> WM_SYMPTOM
            - 医案、病例 -> MEDICAL_CASE

            示例：
            - “人参与陈皮共同的靶点信息”：如果 Neo4j 已返回共同靶点名，则调用 getEntityDetails(entityType=TARGET, names=这些靶点名)。
            - “人参的完整信息”：调用 getEntityDetails(entityType=HERB, names=人参)。
            - “人参相关方剂的详细信息”：如果 Neo4j 已返回方剂名，则调用 getEntityDetails(entityType=PRESCRIPTION, names=这些方剂名)。
            """)
    @UserMessage("""
            用户问题：{{query}}

            意图识别结果：
            {{intentResult}}

            实体识别结果：
            {{entityResult}}

            Neo4j 查询结果：
            {{neo4jResult}}

            请判断是否需要查询 MySQL 实体详情。
            如果需要，请调用 getEntityDetails；如果不需要，请直接返回“不需要查询 MySQL 实体详情”。
            """)
    String searchMysqlDetails(@MemoryId String memoryId,
                              @V("query") String query,
                              @V("intentResult") String intentResult,
                              @V("entityResult") String entityResult,
                              @V("neo4jResult") String neo4jResult);
}
