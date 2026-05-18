package com.tcmseek.service;

import com.tcmseek.pojo.entity.*;
import com.tcmseek.pojo.vo.TcmCompoundD3;

import java.util.List;

/**
 * 中药材信息Service接口
 */
public interface HerbService {

    /**
     * 查询中药材列表
     * @param keyword 搜索关键词（中药名称、拼音、拉丁名）
     * @param type 药材类型
     * @param efficacyCategory 功效分类
     * @return 中药材列表
     */
    List<coreTcmHerbs> selectHerbList(String keyword, String type, String efficacyCategory);

    /**
     * 根据ID查询中药材详情
     * @param tcmHerbId 中药ID
     * @return 中药材信息
     */
    coreTcmHerbs selectHerbById(String tcmHerbId);

    /**
     * 全文搜索中药材
     * @param keyword 搜索关键词
     * @return 中药材列表
     */
    List<coreTcmHerbs> searchHerbsByFulltext(String keyword);

    /**
     * 查询中药材的化合物列表
     * @param tcmHerbId 中药ID
     * @return 化合物列表
     */
    List<TcmCompound> selectCompoundsByHerbId(String tcmHerbId);

    /**
     * 查询中药材的疾病关联
     * @param tcmHerbId 中药ID
     * @return 疾病列表
     */
    List<Disease> selectDiseasesByHerbId(String tcmHerbId);

    /**
     * 查询中药材的症状关联
     * @param tcmHerbId 中药ID
     * @return 症状列表
     */
    List<TcmSymptom> selectSymptomsByHerbId(String tcmHerbId);

    /**
     * 查询中药材的证候关联
     * @param tcmHerbId 中药ID
     * @return 证候列表
     */
    List<TcmSyndrome> selectSyndromesByHerbId(String tcmHerbId);

    /**
     * 获取所有 compound
     * @param herbId
     * @return
     */
    List<TcmCompoundD3> getAllCompounds(String herbId);

    /**
     * 获取 TCM 处方分页
     * @param herbId
     * @return
     */
    List<tcmPrescriptions> selectFormulasByHerbId(String herbId);

    /**
     * 获取 TCM 医案分页
     * @param herbId
     * @return
     */
    List<medicalCases> selectMedicalCasesByHerbId(String herbId);

    /**
     * 获取中药转录组学数据
     * @param tcmHerbId 中药ID
     * @return 转录组学数据列表
     */
    List<java.util.Map<String, Object>> getTranscriptomicsData(String tcmHerbId);

    /**
     * 获取中药转录组学统计信息
     * @param tcmHerbId 中药ID
     * @return 统计信息
     */
    java.util.Map<String, Object> getTranscriptomicsStatistics(String tcmHerbId);
}

