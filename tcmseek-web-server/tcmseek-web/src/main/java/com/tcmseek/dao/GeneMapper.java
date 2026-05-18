package com.tcmseek.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 基因/靶标数据访问层
 * @author TCMSeek
 */
@Mapper
public interface GeneMapper {

    /**
     * 获取基因列表
     * @param keyword 搜索关键词
     * @return 基因列表
     */
    List<Map<String, Object>> getGeneList(@Param("keyword") String keyword);

    /**
     * 根据ID获取基因详情
     * @param geneId 基因ID (tcm_tar_id)
     * @return 基因信息
     */
    Map<String, Object> getGeneById(@Param("geneId") String geneId);

    /**
     * 根据Entrez ID获取基因详情
     * @param entrezId 基因Entrez ID (gene_entrez_id)
     * @return 基因信息
     */
    Map<String, Object> getGeneByEntrezId(@Param("entrezId") Integer entrezId);

    /**
     * 获取基因的关联化合物
     * @param geneId 基因ID
     * @return 化合物列表
     */
    List<Map<String, Object>> getGeneCompounds(@Param("geneId") String geneId);

    /**
     * 获取基因的关联疾病
     * @param geneId 基因ID
     * @return 疾病列表
     */
    List<Map<String, Object>> getGeneDiseases(@Param("geneId") String geneId);

    /**
     * 获取基因的关联证候
     * @param geneId 基因ID
     * @return 证候列表
     */
    List<Map<String, Object>> getGeneSyndromes(@Param("geneId") String geneId);

    /**
     * 获取基因的关联通路
     * @param geneId 基因ID
     * @return 通路列表
     */
    List<Map<String, Object>> getGenePathways(@Param("geneId") String geneId);

    /**
     * 获取基因的关联表型
     * @param geneId 基因ID
     * @return 表型列表
     */
    List<Map<String, Object>> getGenePhenotypes(@Param("geneId") String geneId);

    /**
     * 获取基因的关联西医症状
     * @param geneId 基因ID
     * @return 西医症状列表
     */
    List<Map<String, Object>> getGeneWmSymptoms(@Param("geneId") String geneId);
}

