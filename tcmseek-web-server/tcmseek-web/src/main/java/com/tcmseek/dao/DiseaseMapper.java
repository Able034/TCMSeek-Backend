package com.tcmseek.dao;

import com.tcmseek.pojo.entity.Disease;
import com.tcmseek.pojo.entity.TcmPrescription;
import com.tcmseek.pojo.entity.coreTcmHerbs;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 疾病信息Mapper接口
 */
public interface DiseaseMapper {

    /**
     * 根据中药ID查询疾病关联
     * @param tcmHerbId 中药ID
     * @return 疾病列表
     */
    List<Disease> selectDiseasesByHerbId(@Param("tcmHerbId") String tcmHerbId);

    /**
     * 根据疾病ID查询详情
     * @param diseaseId 疾病ID
     * @return 疾病信息
     */
    Disease selectDiseaseById(@Param("diseaseId") String diseaseId);

    /**
     * 查询疾病的基因关联
     * @param diseaseId 疾病ID
     * @return 基因列表
     */
    List<Map<String, Object>> selectGenesByDiseaseId(@Param("diseaseId") String diseaseId);

    /**
     * 查询疾病的方剂关联
     * @param diseaseId 疾病ID
     * @return 方剂列表
     */
    List<TcmPrescription> selectPrescriptionsByDiseaseId(@Param("diseaseId") String diseaseId);

    /**
     * 查询疾病的中药关联
     * @param diseaseId 疾病ID
     * @return 中药列表
     */
    List<coreTcmHerbs> selectHerbsByDiseaseId(@Param("diseaseId") String diseaseId);
}

