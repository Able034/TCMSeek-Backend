package com.tcmseek.service;

import com.tcmseek.pojo.entity.Disease;
import com.tcmseek.pojo.entity.TcmPrescription;
import com.tcmseek.pojo.entity.coreTcmHerbs;

import java.util.List;
import java.util.Map;

/**
 * 疾病信息Service接口
 */
public interface DiseaseService {

    /**
     * 根据疾病ID查询详情
     * @param diseaseId 疾病ID
     * @return 疾病信息
     */
    Disease selectDiseaseById(String diseaseId);

    /**
     * 查询疾病的基因关联
     * @param diseaseId 疾病ID
     * @return 基因列表
     */
    List<Map<String, Object>> selectGenesByDiseaseId(String diseaseId);

    /**
     * 查询疾病的方剂关联
     * @param diseaseId 疾病ID
     * @return 方剂列表
     */
    List<TcmPrescription> selectPrescriptionsByDiseaseId(String diseaseId);

    /**
     * 查询疾病的中药关联
     * @param diseaseId 疾病ID
     * @return 中药列表
     */
    List<coreTcmHerbs> selectHerbsByDiseaseId(String diseaseId);
}



