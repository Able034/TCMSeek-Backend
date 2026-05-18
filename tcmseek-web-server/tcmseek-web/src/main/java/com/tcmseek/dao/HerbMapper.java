package com.tcmseek.dao;

import com.tcmseek.pojo.entity.coreTcmHerbs;
import com.tcmseek.pojo.entity.tcmPrescriptions;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 中药材信息Mapper接口
 */
public interface HerbMapper {

    /**
     * 查询中药材列表
     * @param keyword 搜索关键词（中药名称、拼音、拉丁名）
     * @param type 药材类型
     * @param efficacyCategory 功效分类
     * @return 中药材列表
     */
    List<coreTcmHerbs> selectHerbList(
            @Param("keyword") String keyword,
            @Param("type") String type,
            @Param("efficacyCategory") String efficacyCategory
    );

    /**
     * 根据ID查询中药材详情
     * @param tcmHerbId 中药ID
     * @return 中药材信息
     */
    coreTcmHerbs selectHerbById(@Param("tcmHerbId") String tcmHerbId);

    /**
     * 全文搜索中药材（功效和主治）
     * @param keyword 搜索关键词
     * @return 中药材列表
     */
    List<coreTcmHerbs> searchHerbsByFulltext(@Param("keyword") String keyword);

    /**
     * 获取所有 compound
     * @param herbId
     * @return
     */
    List<tcmPrescriptions> selectFormulasByHerbId(String herbId);

    /**
     * 获取中药转录组学数据
     * @param tcmHerbId 中药ID
     * @return 转录组学数据列表
     */
    List<java.util.Map<String, Object>> selectTranscriptomicsByHerbId(@Param("tcmHerbId") String tcmHerbId);

    /**
     * 获取中药转录组学统计信息
     * @param tcmHerbId 中药ID
     * @return 统计信息
     */
    java.util.Map<String, Object> getTranscriptomicsStatistics(@Param("tcmHerbId") String tcmHerbId);
}

