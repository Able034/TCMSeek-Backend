package com.tcmseek.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 知识图谱Mapper接口
 * @author TCMSeek
 */
@Mapper
public interface KnowledgeGraphMapper {
    
    // ==================== 中心节点查询 ====================
    
    /**
     * 根据ID获取中药材信息
     */
    Map<String, Object> getHerbById(@Param("herbId") String herbId);
    
    /**
     * 根据ID获取方剂信息
     */
    Map<String, Object> getPrescriptionById(@Param("prescriptionId") String prescriptionId);
    
    /**
     * 根据InChIKey获取化合物信息
     */
    Map<String, Object> getCompoundById(@Param("inchikey") String inchikey);
    
    /**
     * 根据ID获取基因信息
     */
    Map<String, Object> getGeneById(@Param("geneId") String geneId);
    
    /**
     * 根据ID获取疾病信息
     */
    Map<String, Object> getDiseaseById(@Param("diseaseId") String diseaseId);
    
    /**
     * 根据ID获取中医症状信息
     */
    Map<String, Object> getSymptomById(@Param("symptomId") String symptomId);
    
    /**
     * 根据ID获取中医证候信息
     */
    Map<String, Object> getSyndromeById(@Param("syndromeId") String syndromeId);
    
    /**
     * 根据ID获取西医症状信息
     */
    Map<String, Object> getWmSymptomById(@Param("wmSymptomId") String wmSymptomId);
    
    // ==================== 中药材关联查询 ====================
    
    /**
     * 获取中药材的化合物
     */
    List<Map<String, Object>> getHerbCompounds(@Param("herbId") String herbId);
    
    /**
     * 获取中药材的疾病
     */
    List<Map<String, Object>> getHerbDiseases(@Param("herbId") String herbId);
    
    /**
     * 获取中药材的证候
     */
    List<Map<String, Object>> getHerbSyndromes(@Param("herbId") String herbId);
    
    /**
     * 获取中药材的症状
     */
    List<Map<String, Object>> getHerbSymptoms(@Param("herbId") String herbId);
    
    /**
     * 获取中药材的方剂
     */
    List<Map<String, Object>> getHerbPrescriptions(@Param("herbId") String herbId);
    
    // ==================== 方剂关联查询 ====================
    
    /**
     * 获取方剂的中药材
     */
    List<Map<String, Object>> getPrescriptionHerbs(@Param("prescriptionId") String prescriptionId);
    
    /**
     * 获取方剂的疾病
     */
    List<Map<String, Object>> getPrescriptionDiseases(@Param("prescriptionId") String prescriptionId);
    
    /**
     * 获取方剂的证候
     */
    List<Map<String, Object>> getPrescriptionSyndromes(@Param("prescriptionId") String prescriptionId);
    
    /**
     * 获取方剂的症状
     */
    List<Map<String, Object>> getPrescriptionSymptoms(@Param("prescriptionId") String prescriptionId);
    
    // ==================== 化合物关联查询 ====================
    
    /**
     * 获取化合物的中药材
     */
    List<Map<String, Object>> getCompoundHerbs(@Param("inchikey") String inchikey);
    
    /**
     * 获取化合物的靶标
     */
    List<Map<String, Object>> getCompoundTargets(@Param("inchikey") String inchikey);
    
    // ==================== 基因关联查询 ====================
    
    /**
     * 获取基因的化合物
     */
    List<Map<String, Object>> getGeneCompounds(@Param("geneId") String geneId);
    
    /**
     * 获取基因的疾病
     */
    List<Map<String, Object>> getGeneDiseases(@Param("geneId") String geneId);
    
    /**
     * 获取基因的证候
     */
    List<Map<String, Object>> getGeneSyndromes(@Param("geneId") String geneId);
    
    /**
     * 获取基因的通路
     */
    List<Map<String, Object>> getGenePathways(@Param("geneId") String geneId);
    
    /**
     * 获取基因的表型
     */
    List<Map<String, Object>> getGenePhenotypes(@Param("geneId") String geneId);
    
    // ==================== 疾病关联查询 ====================
    
    /**
     * 获取疾病的基因
     */
    List<Map<String, Object>> getDiseaseGenes(@Param("diseaseId") String diseaseId);
    
    /**
     * 获取疾病的中药材
     */
    List<Map<String, Object>> getDiseaseHerbs(@Param("diseaseId") String diseaseId);
    
    /**
     * 获取疾病的证候（通过方剂间接关联）
     */
    List<Map<String, Object>> getDiseaseSyndromes(@Param("diseaseId") String diseaseId);
    
    // ==================== 中医症状关联查询 ====================
    
    /**
     * 获取中医症状的方剂
     */
    List<Map<String, Object>> getSymptomPrescriptions(@Param("symptomId") String symptomId);
    
    /**
     * 获取中医症状的中药材
     */
    List<Map<String, Object>> getSymptomHerbs(@Param("symptomId") String symptomId);
    
    /**
     * 获取中医症状的证候
     */
    List<Map<String, Object>> getSymptomSyndromes(@Param("symptomId") String symptomId);
    
    /**
     * 获取中医症状的西医症状
     */
    List<Map<String, Object>> getSymptomWmSymptoms(@Param("symptomId") String symptomId);
    
    // ==================== 中医证候关联查询 ====================
    
    /**
     * 获取中医证候的方剂
     */
    List<Map<String, Object>> getSyndromePrescriptions(@Param("syndromeId") String syndromeId);
    
    /**
     * 获取中医证候的中医症状
     */
    List<Map<String, Object>> getSyndromeSymptoms(@Param("syndromeId") String syndromeId);
    
    /**
     * 获取中医证候的中药材
     */
    List<Map<String, Object>> getSyndromeHerbs(@Param("syndromeId") String syndromeId);
    
    /**
     * 获取中医证候的基因
     */
    List<Map<String, Object>> getSyndromeGenes(@Param("syndromeId") String syndromeId);
    
    // ==================== 西医症状关联查询 ====================
    
    /**
     * 获取西医症状的中医症状
     */
    List<Map<String, Object>> getWmSymptomTcmSymptoms(@Param("wmSymptomId") String wmSymptomId);
    
    /**
     * 获取西医症状的基因
     */
    List<Map<String, Object>> getWmSymptomGenes(@Param("wmSymptomId") String wmSymptomId);
    
    // ==================== 搜索相关 ====================
    
    /**
     * 搜索中药材
     */
    List<Map<String, Object>> searchHerbs(Map<String, Object> params);
    
    /**
     * 统计中药材数量
     */
    int countHerbs(Map<String, Object> params);
    
    /**
     * 搜索方剂
     */
    List<Map<String, Object>> searchPrescriptions(Map<String, Object> params);
    
    /**
     * 统计方剂数量
     */
    int countPrescriptions(Map<String, Object> params);
    
    /**
     * 搜索化合物
     */
    List<Map<String, Object>> searchCompounds(Map<String, Object> params);
    
    /**
     * 统计化合物数量
     */
    int countCompounds(Map<String, Object> params);
    
    /**
     * 搜索基因
     */
    List<Map<String, Object>> searchGenes(Map<String, Object> params);
    
    /**
     * 统计基因数量
     */
    int countGenes(Map<String, Object> params);
    
    /**
     * 搜索疾病
     */
    List<Map<String, Object>> searchDiseases(Map<String, Object> params);
    
    /**
     * 统计疾病数量
     */
    int countDiseases(Map<String, Object> params);
    
    /**
     * 搜索中医症状
     */
    List<Map<String, Object>> searchSymptoms(Map<String, Object> params);
    
    /**
     * 统计中医症状数量
     */
    int countSymptoms(Map<String, Object> params);
    
    /**
     * 搜索中医证候
     */
    List<Map<String, Object>> searchSyndromes(Map<String, Object> params);
    
    /**
     * 统计中医证候数量
     */
    int countSyndromes(Map<String, Object> params);
    
    /**
     * 搜索西医症状
     */
    List<Map<String, Object>> searchWmSymptoms(Map<String, Object> params);
    
    /**
     * 统计西医症状数量
     */
    int countWmSymptoms(Map<String, Object> params);
}








