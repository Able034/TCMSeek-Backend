package com.tcmseek.service;

import com.tcmseek.pojo.entity.Disease;
import com.tcmseek.pojo.entity.MedicalCase;
import com.tcmseek.pojo.entity.TcmPrescription;
import com.tcmseek.pojo.entity.TcmSymptom;
import com.tcmseek.pojo.entity.TcmSyndrome;
import com.tcmseek.pojo.entity.coreTcmHerbs;

import java.util.List;
import java.util.Map;

/**
 * 方剂信息Service接口
 */
public interface PrescriptionService {

    /**
     * 查询方剂列表
     * @param keyword 搜索关键词
     * @param source 来源典籍
     * @return 方剂列表
     */
    List<TcmPrescription> selectPrescriptionList(String keyword, String source);

    /**
     * 根据ID查询方剂详情
     * @param tcmPrescriptionId 方剂ID
     * @return 方剂信息
     */
    TcmPrescription selectPrescriptionById(String tcmPrescriptionId);

    /**
     * 查询方剂的中药组成（已废弃）
     * @deprecated 请使用 selectCoreHerbsByPrescriptionId
     * @param tcmPrescriptionId 方剂ID
     * @return 中药列表
     */
    @Deprecated
    List<coreTcmHerbs> selectHerbsByPrescriptionId(String tcmPrescriptionId);

    /**
     * 查询方剂的核心中药组成
     * @param tcmPrescriptionId 方剂ID
     * @return 核心中药列表
     */
    List<coreTcmHerbs> selectCoreHerbsByPrescriptionId(String tcmPrescriptionId);

    /**
     * 查询方剂的其他中药组成
     * @param tcmPrescriptionId 方剂ID
     * @return 其他中药列表
     */
    List<Map<String, Object>> selectOtherHerbsByPrescriptionId(String tcmPrescriptionId);

    /**
     * 查询方剂的疾病关联
     * @param tcmPrescriptionId 方剂ID
     * @return 疾病列表
     */
    List<Disease> selectDiseasesByPrescriptionId(String tcmPrescriptionId);

    /**
     * 查询方剂的症状关联
     * @param tcmPrescriptionId 方剂ID
     * @return 症状列表
     */
    List<TcmSymptom> selectSymptomsByPrescriptionId(String tcmPrescriptionId);

    /**
     * 查询方剂的证候关联
     * @param tcmPrescriptionId 方剂ID
     * @return 证候列表
     */
    List<TcmSyndrome> selectSyndromesByPrescriptionId(String tcmPrescriptionId);

    /**
     * 查询方剂的医案关联
     * @param tcmPrescriptionId 方剂ID
     * @return 医案列表
     */
    List<MedicalCase> selectMedicalCasesByPrescriptionId(String tcmPrescriptionId);
    
    /**
     * 获取方剂转录组学数据
     * @param tcmPrescriptionId 方剂ID
     * @return 转录组学数据列表
     */
    List<Map<String, Object>> getTranscriptomicsData(String tcmPrescriptionId);
    
    /**
     * 获取方剂转录组学统计信息
     * @param tcmPrescriptionId 方剂ID
     * @return 统计信息
     */
    Map<String, Object> getTranscriptomicsStatistics(String tcmPrescriptionId);
    
    /**
     * 获取方剂组成中药的转录组学数据（用于对比分析）
     * @param tcmPrescriptionId 方剂ID
     * @return 各中药的转录组学数据
     */
    List<Map<String, Object>> getHerbsTranscriptomicsData(String tcmPrescriptionId);
}

