package com.tcmseek.service;

import com.tcmseek.pojo.entity.GraphResult;

public interface KnowledgeGraphNeo4jService {
    /**
     * 构建知识图谱
     * @param centerType 中心节点类型
     * @param centerId 中心节点ID
     * @param relations 关系类型
     * @return 图谱数据
     */
    GraphResult buildKnowledgeGraph(String centerType, String centerId, String relations);
}
