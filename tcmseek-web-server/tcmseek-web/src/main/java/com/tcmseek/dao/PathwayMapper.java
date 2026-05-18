package com.tcmseek.dao;

import com.tcmseek.pojo.entity.Pathway;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 通路信息Mapper接口
 */
@Mapper
public interface PathwayMapper {
    
    /**
     * 查询通路列表
     * @param keyword 搜索关键词（通路名称或KEGG ID）
     * @return 通路列表
     */
    List<Map<String, Object>> selectPathwayList(@Param("keyword") String keyword);
    
    /**
     * 根据通路ID查询通路详情
     * @param pathwayId 通路ID
     * @return 通路信息
     */
    Pathway selectPathwayById(@Param("pathwayId") String pathwayId);
    
    /**
     * 查询通路的关联靶标/基因
     * @param pathwayId 通路ID
     * @param keyword 搜索关键词（基因符号或描述）
     * @return 靶标列表
     */
    List<Map<String, Object>> selectTargetsByPathwayId(
            @Param("pathwayId") String pathwayId,
            @Param("keyword") String keyword
    );
    
    /**
     * 查询影响此通路的化合物
     * @param pathwayId 通路ID
     * @param keyword 搜索关键词
     * @return 化合物列表
     */
    List<Map<String, Object>> selectCompoundsByPathwayId(
            @Param("pathwayId") String pathwayId,
            @Param("keyword") String keyword
    );
    
    /**
     * 查询影响此通路的中药
     * @param pathwayId 通路ID
     * @param keyword 搜索关键词
     * @return 中药列表
     */
    List<Map<String, Object>> selectHerbsByPathwayId(
            @Param("pathwayId") String pathwayId,
            @Param("keyword") String keyword
    );
    
    /**
     * 获取通路的统计信息
     * @param pathwayId 通路ID
     * @return 统计信息
     */
    Map<String, Object> getStatistics(@Param("pathwayId") String pathwayId);
}

