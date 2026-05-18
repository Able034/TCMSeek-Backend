package com.tcmseek.service;

import com.tcmseek.common.core.domain.AjaxResult;

/**
 * 基因/靶标服务接口
 * @author TCMSeek
 */
public interface GeneService {

    /**
     * 获取基因列表
     * @param page 页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键词
     * @return 基因列表
     */
    AjaxResult getGeneList(Integer page, Integer pageSize, String keyword);

    /**
     * 获取基因详情（通过 tcm_tar_id）
     * @param geneId 基因ID (tcm_tar_id)
     * @return 基因详情
     */
    AjaxResult getGeneDetail(String geneId);

    /**
     * 获取基因详情（通过 gene_entrez_id）
     * @param entrezId 基因Entrez ID
     * @return 基因详情
     */
    AjaxResult getGeneDetailByEntrezId(Integer entrezId);

    /**
     * 获取基因的关联化合物
     * @param geneId 基因ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 化合物列表
     */
    AjaxResult getGeneCompounds(String geneId, Integer page, Integer pageSize);

    /**
     * 获取基因的关联疾病
     * @param geneId 基因ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 疾病列表
     */
    AjaxResult getGeneDiseases(String geneId, Integer page, Integer pageSize);

    /**
     * 获取基因的关联证候
     * @param geneId 基因ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 证候列表
     */
    AjaxResult getGeneSyndromes(String geneId, Integer page, Integer pageSize);

    /**
     * 获取基因的关联通路
     * @param geneId 基因ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 通路列表
     */
    AjaxResult getGenePathways(String geneId, Integer page, Integer pageSize);

    /**
     * 获取基因的关联表型
     * @param geneId 基因ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 表型列表
     */
    AjaxResult getGenePhenotypes(String geneId, Integer page, Integer pageSize);

    /**
     * 获取基因的关联西医症状
     * @param geneId 基因ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 西医症状列表
     */
    AjaxResult getGeneWmSymptoms(String geneId, Integer page, Integer pageSize);
}

