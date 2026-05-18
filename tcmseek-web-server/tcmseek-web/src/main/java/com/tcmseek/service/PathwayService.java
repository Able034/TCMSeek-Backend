package com.tcmseek.service;

import com.tcmseek.pojo.entity.Pathway;

import java.util.List;
import java.util.Map;

/**
 * 通路信息Service接口
 */
public interface PathwayService {
    
    /**
     * 查询通路列表
     * @param keyword 搜索关键词
     * @return 通路列表
     */
    List<Map<String, Object>> selectPathwayList(String keyword);
    
    /**
     * 根据通路ID查询通路详情
     * @param pathwayId 通路ID
     * @return 通路信息
     */
    Pathway selectPathwayById(String pathwayId);
    
    /**
     * 查询通路的关联靶标/基因
     * @param pathwayId 通路ID
     * @param keyword 搜索关键词
     * @return 靶标列表
     */
    List<Map<String, Object>> selectTargetsByPathwayId(String pathwayId, String keyword);
    
    /**
     * 查询影响此通路的化合物
     * @param pathwayId 通路ID
     * @param keyword 搜索关键词
     * @return 化合物列表
     */
    List<Map<String, Object>> selectCompoundsByPathwayId(String pathwayId, String keyword);
    
    /**
     * 查询影响此通路的中药
     * @param pathwayId 通路ID
     * @param keyword 搜索关键词
     * @return 中药列表
     */
    List<Map<String, Object>> selectHerbsByPathwayId(String pathwayId, String keyword);
    
    /**
     * 获取通路的统计信息
     * @param pathwayId 通路ID
     * @return 统计信息
     */
    Map<String, Object> getStatistics(String pathwayId);
    
    /**
     * 获取通路的知识图谱数据
     * @param pathwayId 通路ID
     * @return 图谱数据（节点和边）
     */
    Map<String, Object> getGraphData(String pathwayId);
}

