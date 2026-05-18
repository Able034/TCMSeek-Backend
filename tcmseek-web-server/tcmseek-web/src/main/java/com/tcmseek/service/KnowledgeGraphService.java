package com.tcmseek.service;

import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.pojo.vo.GraphDataVO;

/**
 * 知识图谱服务接口
 * @author TCMSeek
 */
public interface KnowledgeGraphService {
    
    /**
     * 构建知识图谱
     * @param centerType 中心节点类型
     * @param centerId 中心节点ID
     * @param relations 关系类型数组
     * @return 图谱数据
     */
    GraphDataVO buildGraph(String centerType, String centerId, String[] relations);
    
    /**
     * 搜索中心节点
     * @param centerType 节点类型
     * @param keyword 关键词
     * @param page 页码
     * @param pageSize 每页数量
     * @return 搜索结果
     */
    AjaxResult searchCenterNodes(String centerType, String keyword, Integer page, Integer pageSize);
}








