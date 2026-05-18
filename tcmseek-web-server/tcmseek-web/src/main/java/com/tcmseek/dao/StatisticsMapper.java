package com.tcmseek.dao;

import java.util.Map;
import java.util.List;

/**
 * 统计信息Mapper接口
 */
public interface StatisticsMapper {

    /**
     * 统计核心中药数量
     */
    Long countCoreHerbs();

    /**
     * 统计其他中药数量
     */
    Long countOtherHerbs();

    /**
     * 统计方剂数量
     */
    Long countPrescriptions();

    /**
     * 统计症状数量
     */
    Long countSymptoms();

    /**
     * 统计证候数量
     */
    Long countSyndromes();

    /**
     * 统计化合物数量
     */
    Long countCompounds();

    /**
     * 统计靶标数量
     */
    Long countTargets();

    /**
     * 统计疾病数量
     */
    Long countDiseases();

    /**
     * 统计医案数量
     */
    Long countMedicalCases();

    /**
     * 统计中药分类数量
     */
    List<Map<String, Object>> countHerbsByCategory();

    /**
     * 统计方剂来源数量
     */
    List<Map<String, Object>> countPrescriptionsBySource();
}

