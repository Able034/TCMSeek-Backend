package com.tcmseek.service.impl;

import com.tcmseek.common.annotation.DataSource;
import com.tcmseek.common.enums.DataSourceType;
import com.tcmseek.common.utils.Neo4jTypeMapper;
import com.tcmseek.pojo.entity.GraphResult;
import com.tcmseek.service.KnowledgeGraphNeo4jService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@DataSource(DataSourceType.NEO4J)
public class KnowledgeGraphNeo4jServiceImpl implements KnowledgeGraphNeo4jService {

    @Autowired
    private Neo4jTypeMapper typeMapper;

    @Autowired
    private Neo4jClient neo4jClient;

    @Autowired
    public KnowledgeGraphNeo4jServiceImpl(Neo4jTypeMapper typeMapper,
                                          Neo4jClient neo4jClient) {
        this.typeMapper = typeMapper;
        this.neo4jClient = neo4jClient;
    }

    @Transactional
    public GraphResult buildKnowledgeGraph(String centerType, String centerId, String relations) {
        // 参数校验
        validateParameters(centerType, centerId, relations);

        // 类型转换
        List<String> relationTypes = Arrays.stream(relations.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        // 构建查询参数
        String centerLabel = typeMapper.getCenterLabel(centerType);
        String idProperty = typeMapper.getIdProperty(centerType);
        List<String> validRelations = typeMapper.getValidRelations(centerType, relationTypes);
        List<String> targetLabels = typeMapper.getTargetLabels(relationTypes);

        log.info("使用Neo4jClient查询知识图谱 - 中心节点: {}, ID: {}, 关系: {}",
                centerLabel, centerId, validRelations);

        return executeGraphQuery(centerLabel, idProperty, centerId, validRelations, targetLabels);
    }

    private static final List<String> BASIC_NODE_PROPS = Arrays.asList(
            "tcm_herb_id", "tcm_prescription_id", "inchikey", "tcm_tar_id",
            "disease_id", "tcm_symptom_id", "tcm_syndrome_id", "wm_symptom_id",
            "herb_name_zh", "name_zh", "name_en", "symbol",
            "disease_name", "syndrome_name_zh", "symptom_name_zh"
    );

    private GraphResult executeGraphQuery(String centerLabel, String idProperty,
                                          String centerId, List<String> validRelations,
                                          List<String> targetLabels) {
        try {
            String query = buildCypherQuery(centerLabel, idProperty, targetLabels);
            Map<String, Object> parameters = Map.of(
                    "centerId", centerId,
                    "relations", validRelations
            );

            return neo4jClient.query(query)
                    .bindAll(parameters)
                    .fetchAs(GraphResult.class)
                    .mappedBy((typeSystem, record) -> GraphResult.builder()
                            .centerNode(record.containsKey("centerNode")
                                    ? record.get("centerNode").asMap()
                                    : Map.of())
                            .relationships(record.containsKey("relationships")
                                    ? record.get("relationships").asList(value -> {
                                        Map<String, Object> relMap = value.asMap();
                                        return GraphResult.RelationshipData.builder()
                                                .relType((String) relMap.getOrDefault("relType", ""))
                                                .relProps(castToMap(relMap.get("relProps")))
                                                .targetNode(castToMap(relMap.get("targetNode")))
                                                .build();
                                    })
                                    : List.of())
                            .build())
                    .one()
                    .orElse(new GraphResult());
        } catch (Exception e) {
            log.error("使用Neo4jClient查询知识图谱失败: {}", e.getMessage());
            return new GraphResult();
        }
    }

    /**
     * 构建Cypher查询语句
     */
    private String buildCypherQuery(String centerLabel, String idProperty, List<String> targetLabels) {
        String labelPredicate = buildTargetLabelPredicate(targetLabels);
        String centerProjection = buildNodeProjection("center");
        String targetProjection = buildNodeProjection("target");

        return "MATCH (center:" + centerLabel + ") " +
                "USING INDEX center:" + centerLabel + "(" + idProperty + ") " +
                "WHERE center." + idProperty + " = $centerId " +
                "OPTIONAL MATCH (center)-[r]-(target) " +
                "WHERE type(r) IN $relations " +
                "AND target IS NOT NULL " +
                labelPredicate +
                "WITH center, collect({ " +
                "    relType: type(r), " +
                "    relProps: properties(r), " +
                "    targetNode: " + targetProjection +
                "}) AS relationships " +
                "RETURN " + centerProjection + " AS centerNode, relationships";
    }

    private String buildTargetLabelPredicate(List<String> targetLabels) {
        if (targetLabels == null || targetLabels.isEmpty()) {
            return "";
        }
        String predicate = targetLabels.stream()
                .map(label -> "target:" + label)
                .collect(Collectors.joining(" OR "));
        return "AND (" + predicate + ") ";
    }

    private String buildNodeProjection(String expression) {
        String props = BASIC_NODE_PROPS.stream()
                .map(prop -> "." + prop)
                .collect(Collectors.joining(", "));
        return expression + "{ " + props + ", _labels: labels(" + expression + ") }";
    }

    private void validateParameters(String centerType, String centerId, String relations) {
        if (StringUtils.isBlank(centerType)) {
            throw new IllegalArgumentException("中心节点类型不能为空");
        }
        if (StringUtils.isBlank(centerId)) {
            throw new IllegalArgumentException("中心节点ID不能为空");
        }
        if (StringUtils.isBlank(relations)) {
            throw new IllegalArgumentException("至少需要指定一种关系类型");
        }

        if (!typeMapper.isValidCenterType(centerType)) {
            throw new IllegalArgumentException("无效的中心节点类型: " + centerType);
        }

        Arrays.stream(relations.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(type -> {
                    if (!typeMapper.isValidRelationType(type)) {
                        throw new IllegalArgumentException("无效的关系类型: " + type);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Map.of();
    }
}
