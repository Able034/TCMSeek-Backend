package com.tcmseek.service;

import java.util.List;
import java.util.Map;

/**
 * TCM Neo4j查询服务
 */
public interface TcmNeo4jService {

    /**
     * 执行Cypher查询
     * @param cypher Cypher查询语句
     * @return 查询结果列表
     */
    List<Map<String, Object>> executeQuery(String cypher);

    /**
     * 执行带参数的Cypher查询
     * @param cypher Cypher查询语句
     * @param params 参数Map
     * @return 查询结果列表
     */
    List<Map<String, Object>> executeQuery(String cypher, Map<String, Object> params);

    /**
     * 从Cypher查询中提取实体名称和属性类型
     * @param cypherQuery Cypher查询语句
     * @return 实体列表，格式: [{"propType": "herb_name_zh", "entity": "人参"}, ...]
     */
    List<Map<String, String>> extractEntities(String cypherQuery);

    /**
     * 根据属性类型查找相似实体（模糊匹配）
     * @param entity 实体名称
     * @param propType 属性类型
     * @param topK 返回数量
     * @return 相似实体名称列表
     */
    List<String> findSimilarEntities(String entity, String propType, int topK);

    /**
     * 在Cypher查询中替换实体名称
     * @param cypherQuery 原始Cypher查询
     * @param entityMapping 实体映射，格式: {"propType:entity": "newEntity"}
     * @return 替换后的Cypher查询
     */
    String replaceEntities(String cypherQuery, Map<String, String> entityMapping);
}
