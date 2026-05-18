package com.tcmseek.service;

import com.tcmseek.pojo.entity.Phenotype;
import java.util.List;
import java.util.Map;

/**
 * 表型信息Service接口
 */
public interface PhenotypeService {

    /**
     * 查询表型列表
     * @param keyword 搜索关键词（表型名称或HPO ID）
     * @return 表型列表（包含关联基因数量）
     */
    List<Map<String, Object>> selectPhenotypeList(String keyword);

    /**
     * 根据表型ID查询表型详情
     * @param phenotypeId 表型ID（如HP:0000006）
     * @return 表型详情
     */
    Phenotype selectPhenotypeById(String phenotypeId);

    /**
     * 查询表型关联的靶标/基因列表
     * @param phenotypeId 表型ID
     * @param keyword 搜索关键词（基因符号或描述）
     * @return 靶标/基因列表
     */
    List<Map<String, Object>> selectTargetsByPhenotypeId(String phenotypeId, String keyword);

    /**
     * 获取表型的统计信息
     * @param phenotypeId 表型ID
     * @return 统计信息（关联靶标数量等）
     */
    Map<String, Object> getStatistics(String phenotypeId);

    /**
     * 获取表型的知识图谱数据
     * @param phenotypeId 表型ID
     * @return 图谱数据（节点和边）
     */
    Map<String, Object> getGraphData(String phenotypeId);
}



