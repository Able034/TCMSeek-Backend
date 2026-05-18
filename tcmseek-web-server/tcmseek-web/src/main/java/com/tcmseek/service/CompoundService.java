package com.tcmseek.service;

import com.tcmseek.pojo.entity.CompoundAdmet;
import com.tcmseek.pojo.entity.Target;
import com.tcmseek.pojo.entity.TcmCompound;
import com.tcmseek.pojo.vo.TargetD3VO;

import java.util.List;
import java.util.Map;

/**
 * 化合物信息Service接口
 */
public interface CompoundService {

    /**
     * 查询化合物列表
     * @param keyword 搜索关键词
     * @return 化合物列表
     */
    List<TcmCompound> selectCompoundsList(String keyword);

    TcmCompound selectCompoundByInchikey(String inchikey);

    List<Target> selectTargetsByInchikey(String inchikey);

    CompoundAdmet selectAdmetByInchikey(String inchikey);

    /**
     * 获取化合物的全部靶标
     * @param inchikey
     * @return
     */
    List<TargetD3VO> getAllTargets(String inchikey);

    /**
     * 批量获取多个化合物的靶标信息（优化版：去重传输）
     * @param inchikeys 化合物InChIKey列表
     * @return Map包含targets和relations两个字段
     */
    Map<String, Object> batchGetAllTargets(List<String> inchikeys);

    /**
     * 获取化合物关联的中药材列表
     * @param inchikey 化合物InChIKey
     * @return 中药材列表
     */
    List<com.tcmseek.pojo.entity.coreTcmHerbs> selectHerbsByInchikey(String inchikey);
    
    /**
     * 获取化合物的转录组学数据
     * @param inchikey 化合物InChIKey
     * @return 转录组学数据列表
     */
    List<Map<String, Object>> getTranscriptomicsData(String inchikey);
    
    /**
     * 获取化合物转录组学统计信息
     * @param inchikey 化合物InChIKey
     * @return 统计信息
     */
    Map<String, Object> getTranscriptomicsStatistics(String inchikey);
}

