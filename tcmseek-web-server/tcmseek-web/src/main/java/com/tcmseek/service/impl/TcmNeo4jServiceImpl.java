package com.tcmseek.service.impl;

import com.tcmseek.service.TcmNeo4jService;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TCM Neo4j查询服务实现
 */
@Slf4j
@Service
public class TcmNeo4jServiceImpl implements TcmNeo4jService {

    private final Driver driver;

    public TcmNeo4jServiceImpl(Driver driver) {
        this.driver = driver;
    }

    @Override
    public List<Map<String, Object>> executeQuery(String cypher) {
        return executeQuery(cypher, Collections.emptyMap());
    }

    @Override
    public List<Map<String, Object>> executeQuery(String cypher, Map<String, Object> params) {
        log.info("执行Cypher查询: {}, 参数: {}", cypher, params);
        List<Map<String, Object>> results = new ArrayList<>();

        try (Session session = driver.session()) {
            Result result = session.run(cypher, params);

            while (result.hasNext()) {
                Record record = result.next();
                Map<String, Object> row = new HashMap<>();

                for (String key : record.keys()) {
                    Value value = record.get(key);
                    row.put(key, convertValue(value));
                }
                results.add(row);
            }

            log.info("查询返回{}条结果", results.size());
        } catch (Exception e) {
            log.error("执行Cypher查询失败: {}", e.getMessage(), e);
        }

        return results;
    }

    /**
     * 将Neo4j Value对象转换为Java对象
     */
    private Object convertValue(Value value) {
        if (value.isNull()) {
            return null;
        }

        // Neo4j Driver 4.x 使用类型名字符串比较
        String typeName = value.type().name();

        if ("NODE".equals(typeName)) {
            Node node = value.asNode();
            Map<String, Object> nodeMap = new HashMap<>(node.asMap());
            List<String> labels = new ArrayList<>();
            for (String label : node.labels()) {
                labels.add(label);
            }
            nodeMap.put("_labels", labels);
            return nodeMap;
        }

        if ("RELATIONSHIP".equals(typeName)) {
            Relationship rel = value.asRelationship();
            Map<String, Object> relMap = new HashMap<>(rel.asMap());
            relMap.put("_type", rel.type());
            relMap.put("_id", rel.id());
            return relMap;
        }

        if ("LIST".equals(typeName)) {
            List<Object> list = new ArrayList<>();
            for (Value item : value.values()) {
                list.add(convertValue(item));
            }
            return list;
        }

        if ("PATH".equals(typeName)) {
            // 简化处理：返回路径的节点列表
            Path path = value.asPath();
            List<Map<String, Object>> pathNodes = new ArrayList<>();
            for (Node node : path.nodes()) {
                Map<String, Object> nodeMap = new HashMap<>(node.asMap());
                List<String> labels = new ArrayList<>();
                for (String label : node.labels()) {
                    labels.add(label);
                }
                nodeMap.put("_labels", labels);
                pathNodes.add(nodeMap);
            }
            return pathNodes;
        }

        // 基本类型直接返回
        return value.asObject();
    }

    @Override
    public List<Map<String, String>> extractEntities(String cypherQuery) {
        List<Map<String, String>> entities = new ArrayList<>();

        // 匹配各种属性名模式: herb_name_zh: 'xxx', name_zh: 'xxx', disease_name: 'xxx', symbol: 'xxx' 等
        List<PropertyPattern> patterns = Arrays.asList(
                new PropertyPattern("herb_name_zh:\\s*'([^']*)'", "herb_name_zh"),
                new PropertyPattern("name_zh:\\s*'([^']*)'", "name_zh"),
                new PropertyPattern("disease_name:\\s*'([^']*)'", "disease_name"),
                new PropertyPattern("symbol:\\s*'([^']*)'", "symbol"),
                new PropertyPattern("syndrome_name_zh:\\s*'([^']*)'", "syndrome_name_zh"),
                new PropertyPattern("symptom_name_zh:\\s*'([^']*)'", "symptom_name_zh"),
                new PropertyPattern("phenotype_name:\\s*'([^']*)'", "phenotype_name"),
                new PropertyPattern("name:\\s*'([^']*)'", "name"),
                new PropertyPattern("inchikey:\\s*'([^']*)'", "inchikey")
        );

        for (PropertyPattern pattern : patterns) {
            Pattern regex = Pattern.compile(pattern.pattern);
            Matcher matcher = regex.matcher(cypherQuery);

            while (matcher.find()) {
                String entity = matcher.group(1);
                if (entity != null && !entity.isEmpty()) {
                    Map<String, String> entityMap = new HashMap<>();
                    entityMap.put("propType", pattern.propType);
                    entityMap.put("entity", entity);
                    entities.add(entityMap);
                }
            }
        }

        log.info("从Cypher查询中提取到{}个实体: {}", entities.size(), entities);
        return entities;
    }

    @Override
    public List<String> findSimilarEntities(String entity, String propType, int topK) {
        log.info("查找相似实体: propType={}, entity={}, topK={}", propType, entity, topK);

        // 通用模糊匹配查询
        List<String> propCandidates = Arrays.asList(
                propType, "herb_name_zh", "name_zh", "disease_name", "symbol",
                "syndrome_name_zh", "symptom_name_zh", "phenotype_name", "name", "inchikey"
        );

        for (String prop : propCandidates) {
            String cypher = String.format(
                    "MATCH (n) WHERE n.%s CONTAINS $name RETURN n.%s AS similar_name LIMIT $limit",
                    prop, prop
            );

            try {
                List<Map<String, Object>> results = executeQuery(cypher,
                        Map.of("name", entity, "limit", topK));

                if (!results.isEmpty()) {
                    List<String> similarNames = new ArrayList<>();
                    for (Map<String, Object> result : results) {
                        Object name = result.get("similar_name");
                        if (name != null) {
                            similarNames.add(name.toString());
                        }
                    }
                    if (!similarNames.isEmpty()) {
                        log.info("找到{}个相似实体: {}", similarNames.size(), similarNames);
                        return similarNames;
                    }
                }
            } catch (Exception e) {
                log.debug("使用属性{}查找相似实体失败: {}", prop, e.getMessage());
            }
        }

        log.warn("未找到相似实体");
        return Collections.emptyList();
    }

    @Override
    public String replaceEntities(String cypherQuery, Map<String, String> entityMapping) {
        String result = cypherQuery;

        for (Map.Entry<String, String> entry : entityMapping.entrySet()) {
            String key = entry.getKey(); // 格式: "propType:original"
            String newEntity = entry.getValue();

            // 解析key
            String[] parts = key.split(":", 2);
            if (parts.length == 2) {
                String propType = parts[0];
                String original = parts[1];

                // 替换单引号格式
                result = result.replace(
                        String.format("%s: '%s'", propType, original),
                        String.format("%s: '%s'", propType, newEntity)
                );
                // 替换双引号格式
                result = result.replace(
                        String.format("%s: \"%s\"", propType, original),
                        String.format("%s: \"%s\"", propType, newEntity)
                );
            }
        }

        log.info("替换实体后的Cypher: {}", result);
        return result;
    }

    /**
     * 属性模式辅助类
     */
    private static class PropertyPattern {
        final String pattern;
        final String propType;

        PropertyPattern(String pattern, String propType) {
            this.pattern = pattern;
            this.propType = propType;
        }
    }
}
