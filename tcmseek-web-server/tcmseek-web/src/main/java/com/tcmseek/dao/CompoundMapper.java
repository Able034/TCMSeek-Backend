package com.tcmseek.dao;

import com.tcmseek.pojo.entity.CompoundAdmet;
import com.tcmseek.pojo.entity.Target;
import com.tcmseek.pojo.entity.TcmCompound;
import com.tcmseek.pojo.vo.TargetD3VO;
import com.tcmseek.pojo.vo.TcmCompoundD3;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 化合物信息Mapper接口
 */
public interface CompoundMapper {

    /**
     * 查询化合物列表
     * @param keyword 搜索关键词
     * @return 化合物列表
     */
    List<TcmCompound> selectCompoundsList(@Param("keyword") String keyword);

    /**
     * 根据中药ID查询化合物列表
     * @param tcmHerbId 中药ID
     * @return 化合物列表
     */
    List<TcmCompound> selectCompoundsByHerbId(@Param("tcmHerbId") String tcmHerbId);

    /**
     * 根据InChIKey查询化合物详情
     * @param inchikey InChIKey
     * @return 化合物信息
     */
    TcmCompound selectCompoundByInchikey(@Param("inchikey") String inchikey);

    /**
     * 查询化合物的靶标列表
     * @param inchikey InChIKey
     * @return 靶标列表
     */
    List<Target> selectTargetsByInchikey(@Param("inchikey") String inchikey);

    /**
     * 查询化合物的ADMET性质
     * @param inchikey InChIKey
     * @return ADMET数据
     */
    CompoundAdmet selectAdmetByInchikey(@Param("inchikey") String inchikey);

    /**
     * 获取所有 compound
     * @param herbId
     * @return
     */
    List<TcmCompoundD3> getAllCompounds(String herbId);

    /**
     * 获取所有 target
     * @param inchikey
     * @return
     */
    List<TargetD3VO> getAllTargets(String inchikey);

    /**
     * 批量获取多个化合物的靶标信息
     * @param inchikeys 化合物InChIKey列表
     * @return 包含inchikey分组字段的靶标列表
     */
    List<TargetD3VO> batchGetAllTargets(@Param("inchikeys") List<String> inchikeys);

    /**
     * 查询包含该化合物的中药材列表
     * @param inchikey 化合物InChIKey
     * @return 中药材列表
     */
    List<com.tcmseek.pojo.entity.coreTcmHerbs> selectHerbsByInchikey(@Param("inchikey") String inchikey);
    
    /**
     * 查询化合物的转录组学数据
     * @param inchikey 化合物InChIKey
     * @return 转录组学数据列表
     */
    List<Map<String, Object>> selectTranscriptomicsByInchikey(@Param("inchikey") String inchikey);
    
    /**
     * 获取化合物转录组学统计信息
     * @param inchikey 化合物InChIKey
     * @return 统计信息（上调、下调、显著基因数量等）
     */
    Map<String, Object> getTranscriptomicsStatistics(@Param("inchikey") String inchikey);
}

