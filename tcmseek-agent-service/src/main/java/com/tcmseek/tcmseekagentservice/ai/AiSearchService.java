package com.tcmseek.tcmseekagentservice.ai;

import com.tcmseek.tcmseekagentservice.ai.model.Neo4jEntity;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;

public interface AiSearchService {

    @SystemMessage("""
            你是中医知识图谱查询代理。请根据用户原始问题和已识别实体调用工具查询 Neo4j，再用查询结果回答用户。

            工具调用规则：
            1. 需要了解图谱结构时调用 knowNeo4jFrame。
            2. 查询图谱关系时调用 neo4jSearch，参数必须使用 startLabel、startName、relationships、limit。
            3. startLabel 和 startName 必须来自已识别实体，不要编造实体。
            4. relationships 只能使用图谱 schema 中存在的关系名。
            5. 如果实体为空或工具没有查到结果，请如实说明没有查到，不要编造答案。

            常见问题词到关系名的映射：
            - 化合物、成分、Canonical smiles、canonical_smiles -> HAS_Canonical_smiles
            - InChIKey、inchikey -> HAS_INCHIKEY
            - 靶标、靶点、基因 -> 靶标基因为
            - 组成、包含哪些药材 -> 组成
            - 治疗、主治症状 -> 治疗、主治症状
            - 证候 -> 主治证候、病因、表象、证候详情
            - 西医病症 -> 对应西医病症
            """)
    @UserMessage("""
            用户原始问题：{{query}}
            已识别实体：{{entity}}
            请先选择合适关系调用工具查询，再用中文回答用户。
            """)
//    @Agent()
    String searchByNeo4j(@MemoryId String memoryId,
            @V("entity") List<Neo4jEntity> entity,
            @V("query")String query);
}
