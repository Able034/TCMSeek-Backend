package com.tcmseek.tcmseekagentservice.ai.tools.neo4j;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tcmseek.tcmseekagentservice.ai.tools.BaseTool;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Neo4jSearchTool extends BaseTool {

    private final Neo4jQueryService neo4jQueryService;

    public Neo4jSearchTool(Neo4jQueryService neo4jQueryService) {
        this.neo4jQueryService = neo4jQueryService;
    }

    @Tool(name = "neo4jSearch", value = "查询中医知识图谱。根据起始节点 label、实体名称、关系列表，返回相关实体。")
    public String neo4jSearch(
            @P(name = "startLabel", value = "起始节点的标签，只能是：药材、方剂、病症、症状、证候、靶标、Canonical_smiles、Inchikey、西医病症、典籍、人体部位、症状详情、病症详情、证候详情")
            String startLabel,

            @P(name = "startName", value = "起始实体名称或关键词，例如：咳嗽、麻黄汤、黄芪")
            String startName,

            @P(name = "relationships", value = "要遍历的关系列表。每个关系必须来自图谱 schema，例如：组成、主治症状、主治证候、治疗、病因、表象、对应西医病症、来源于、HAS_Canonical_smiles、HAS_INCHIKEY、靶标基因为")
            List<String> relationships,

            @P(name = "limit", value = "返回结果数量限制，默认 20，最大 500")
            Integer limit

//            @ToolMemoryId Long aiId
    ) {
        log.info("[工具调用]neo4jSearch");
        try {
            if (startLabel == null || startLabel.isBlank()) {
                return "请输入起始节点标签";
            }
            if (startName == null || startName.isBlank()) {
                return "请输入查询的起始实体名称";
            }
            if (relationships == null || relationships.isEmpty()) {
                return "请输入关系列表";
            }

            int safeLimit = limit == null ? 20 : limit;
            if (safeLimit <= 0) {
                safeLimit = 20;
            }
            safeLimit = Math.min(safeLimit, 500);

            if (!ALLOWED_LABELS.contains(startLabel)) {
                return "不支持的节点标签：" + startLabel;
            }

            for (String relationship : relationships) {
                if (relationship == null || relationship.isBlank()) {
                    return "关系类型不能为空";
                }
                if (!ALLOWED_RELATIONSHIPS.contains(relationship)) {
                    return "不支持的关系类型：" + relationship;
                }
            }

            String relationshipPattern = relationships.stream()
                    .distinct()
                    .map(this::quoteNeo4jIdentifier)
                    .collect(Collectors.joining("|"));

            String cypher = ""
                    + "MATCH (s:" + quoteNeo4jIdentifier(startLabel) + ")-[r:" + relationshipPattern + "]-(e) "
                    + "WHERE s.name CONTAINS $startName "
                    + "RETURN DISTINCT "
                    + "labels(s) AS startLabels, "
                    + "s.name AS startName, "
                    + "type(r) AS relationship, "
                    + "labels(e) AS endLabels, "
                    + "e.name AS endName "
                    + "LIMIT $limit";


            List<Map<String, Object>> rows = neo4jQueryService.query(cypher, startName, safeLimit);
//            log.debug("neo4j的查询信息为"+rows);
            return JSONUtil.toJsonStr(rows);
        } catch (Exception e) {
            log.error("查询关系失败", e);
            return "查询关系失败：" + e.getMessage();
        }

    }

    private static final Set<String> ALLOWED_LABELS = Set.of(
            "药材",
            "方剂",
            "病症",
            "症状",
            "证候",
            "靶标",
            "Canonical_smiles",
            "Inchikey",
            "西医病症",
            "典籍",
            "人体部位",
            "症状详情",
            "病症详情",
            "证候详情"
    );

    private static final Set<String> ALLOWED_RELATIONSHIPS = Set.of(
            "组成",
            "主治症状",
            "主治证候",
            "来源于",
            "治疗",
            "HAS_Canonical_smiles",
            "HAS_INCHIKEY",
            "病因",
            "表象",
            "对应西医病症",
            "病因所在部位",
            "症状详情",
            "病症详情",
            "证候详情",
            "靶标基因为"
    );




    private String quoteNeo4jIdentifier(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }


    @Override
    public String getToolName() {
        return "neo4jSearch";
    }

    @Override
    public String getDisplayName() {
        return "通过neo4j查询关系";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        log.info("[工具调用] Neo4jSearch 参数：%s", arguments);
        return String.format("[工具调用] Neo4jSearch 参数：%s", arguments);
    }
}
