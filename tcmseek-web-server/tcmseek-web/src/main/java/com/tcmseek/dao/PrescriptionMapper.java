package com.tcmseek.dao;

import com.tcmseek.pojo.entity.Disease;
import com.tcmseek.pojo.entity.MedicalCase;
import com.tcmseek.pojo.entity.TcmPrescription;
import com.tcmseek.pojo.entity.TcmSymptom;
import com.tcmseek.pojo.entity.TcmSyndrome;
import com.tcmseek.pojo.entity.coreTcmHerbs;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 方剂信息Mapper接口
 */
public interface PrescriptionMapper {

    /**
     * 查询方剂列表
     * @param keyword 搜索关键词（方剂名称、拼音）
     * @param source 来源典籍
     * @return 方剂列表
     */
    List<TcmPrescription> selectPrescriptionList(
            @Param("keyword") String keyword,
            @Param("source") String source
    );

    /**
     * 根据ID查询方剂详情
     * @param tcmPrescriptionId 方剂ID
     * @return 方剂信息
     */
    TcmPrescription selectPrescriptionById(@Param("tcmPrescriptionId") String tcmPrescriptionId);

    /**
     * 查询方剂的中药组成（核心中药）- 已废弃
     * @deprecated 请使用 selectCoreHerbsByPrescriptionId
     * @param tcmPrescriptionId 方剂ID
     * @return 中药列表
     */
    @Deprecated
    List<coreTcmHerbs> selectHerbsByPrescriptionId(@Param("tcmPrescriptionId") String tcmPrescriptionId);

    /**
     * 查询方剂的核心中药组成
     * @param tcmPrescriptionId 方剂ID
     * @return 核心中药列表
     */
    List<coreTcmHerbs> selectCoreHerbsByPrescriptionId(@Param("tcmPrescriptionId") String tcmPrescriptionId);

    /**
     * 查询方剂的其他中药组成
     * @param tcmPrescriptionId 方剂ID
     * @return 其他中药列表
     */
    List<Map<String, Object>> selectOtherHerbsByPrescriptionId(@Param("tcmPrescriptionId") String tcmPrescriptionId);

    /**
     * 查询方剂的疾病关联
     * @param tcmPrescriptionId 方剂ID
     * @return 疾病列表
     */
    List<Disease> selectDiseasesByPrescriptionId(@Param("tcmPrescriptionId") String tcmPrescriptionId);

    /**
     * 查询方剂的症状关联
     * @param tcmPrescriptionId 方剂ID
     * @return 症状列表
     */
    List<TcmSymptom> selectSymptomsByPrescriptionId(@Param("tcmPrescriptionId") String tcmPrescriptionId);

    /**
     * 查询方剂的证候关联
     * @param tcmPrescriptionId 方剂ID
     * @return 证候列表
     */
    List<TcmSyndrome> selectSyndromesByPrescriptionId(@Param("tcmPrescriptionId") String tcmPrescriptionId);

    /**
     * 查询方剂的医案关联
     * @param tcmPrescriptionId 方剂ID
     * @return 医案列表
     */
    List<MedicalCase> selectMedicalCasesByPrescriptionId(@Param("tcmPrescriptionId") String tcmPrescriptionId);
    
    /**
     * 查询方剂的转录组学数据
     * @param tcmPrescriptionId 方剂ID
     * @return 转录组学数据列表
     */
    List<Map<String, Object>> selectTranscriptomicsByPrescriptionId(@Param("tcmPrescriptionId") String tcmPrescriptionId);
    
    /**
     * 获取方剂转录组学统计信息
     * @param tcmPrescriptionId 方剂ID
     * @return 统计信息（上调、下调、显著基因数量等）
     */
    Map<String, Object> getTranscriptomicsStatistics(@Param("tcmPrescriptionId") String tcmPrescriptionId);
    
    /**
     * 查询方剂组成中药的转录组学数据（用于对比分析）
     * @param tcmPrescriptionId 方剂ID
     * @return 各中药的转录组学数据
     */
    List<Map<String, Object>> selectHerbsTranscriptomicsByPrescriptionId(@Param("tcmPrescriptionId") String tcmPrescriptionId);
}

