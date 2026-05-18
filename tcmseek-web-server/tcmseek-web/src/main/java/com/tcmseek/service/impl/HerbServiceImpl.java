package com.tcmseek.service.impl;

import com.tcmseek.dao.*;
import com.tcmseek.pojo.entity.*;
import com.tcmseek.pojo.vo.TcmCompoundD3;
import com.tcmseek.service.HerbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * 中药材信息Service实现类
 */
@Service
public class HerbServiceImpl implements HerbService {

    // 中药材信息Mapper接口
    @Autowired
    private HerbMapper herbMapper;

    // 化合物信息Mapper接口
    @Autowired
    private CompoundMapper compoundMapper;

    // 疾病信息Mapper接口
    @Autowired
    private DiseaseMapper diseaseMapper;


    // 症状信息Mapper接口
    @Autowired
    private SymptomMapper symptomMapper;

    // 中医证候信息Mapper接口
    @Autowired
    private SyndromeMapper syndromeMapper;

    // 中医医案信息Mapper接口
    @Autowired
    private MedicalcasesMapper medicalcasesMapper;
    /**
     * 查询中药材列表
     * @param keyword 搜索关键词（中药名称、拼音、拉丁名）
     * @param type 药材类型
     * @param efficacyCategory 功效分类
     * @return 中药材列表
     */
    @Override

    public List<coreTcmHerbs> selectHerbList(String keyword, String type, String efficacyCategory) {
        return herbMapper.selectHerbList(keyword, type, efficacyCategory);
    }

    /**
     * 根据ID查询中药材详情
     * @param tcmHerbId 中药ID
     * @return 中药材信息
     */
    @Override

    public coreTcmHerbs selectHerbById(String tcmHerbId) {
        return herbMapper.selectHerbById(tcmHerbId);
    }

    /**
     * 全文搜索中药材
     * @param keyword 搜索关键词
     * @return 中药材列表
     */
    @Override
    public List<coreTcmHerbs> searchHerbsByFulltext(String keyword) {
        return herbMapper.searchHerbsByFulltext(keyword);
    }

    /**
     * 查询中药材的化合物列表
     * @param tcmHerbId 中药ID
     * @return 化合物列表
     */
    @Override
    public List<TcmCompound> selectCompoundsByHerbId(String tcmHerbId) {
        return compoundMapper.selectCompoundsByHerbId(tcmHerbId);
    }

    /**
     * 查询中药材的疾病关联
     * @param tcmHerbId 中药ID
     * @return 疾病列表
     */
    @Override
    public List<Disease> selectDiseasesByHerbId(String tcmHerbId) {
        return diseaseMapper.selectDiseasesByHerbId(tcmHerbId);
    }

    /**
     * 查询中药材的症状关联
     * @param tcmHerbId 中药ID
     * @return 症状列表
     */
    @Override
    public List<TcmSymptom> selectSymptomsByHerbId(String tcmHerbId) {
        return symptomMapper.selectSymptomsByHerbId(tcmHerbId);
    }

    /**
     * 查询中药材的证候关联
     * @param tcmHerbId 中药ID
     * @return 证候列表
     */
    @Override
    public List<TcmSyndrome> selectSyndromesByHerbId(String tcmHerbId) {
        return syndromeMapper.selectSyndromesByHerbId(tcmHerbId);
    }

    /**
     * 获取所有 compound
     * @param herbId
     * @return
     */
    @Override
    public List<TcmCompoundD3> getAllCompounds(String herbId) {

//        List<TcmCompound> compounds = compoundMapper.selectCompoundsByHerbId(herbId);
//        List<TcmCompoundD3> compoundsD3 = compounds.stream()
//                .map(compound -> new TcmCompoundD3(compound.getInchikey(), compound.getMolecularFormula()))
//                .collect(Collectors.toList());
//        return compoundsD3;
        return compoundMapper.getAllCompounds(herbId);
    }

    /**
     * 获取处方分页
     * @param herbId
     * @return
     */
    @Override
    public List<tcmPrescriptions> selectFormulasByHerbId(String herbId) {
        return herbMapper.selectFormulasByHerbId(herbId);
    }

    /**
     * 对中药相关联的中医医案分页查询
     * @param herbId
     * @return
     */
    @Override
    public List<medicalCases> selectMedicalCasesByHerbId(String herbId) {
        return medicalcasesMapper.selectMedicalCasesByHerbId(herbId);
    }

    /**
     * 获取中药转录组学数据
     * @param tcmHerbId 中药ID
     * @return 转录组学数据列表
     */
    @Override
    public List<java.util.Map<String, Object>> getTranscriptomicsData(String tcmHerbId) {
        return herbMapper.selectTranscriptomicsByHerbId(tcmHerbId);
    }

    /**
     * 获取中药转录组学统计信息
     * @param tcmHerbId 中药ID
     * @return 统计信息
     */
    @Override
    public java.util.Map<String, Object> getTranscriptomicsStatistics(String tcmHerbId) {
        java.util.Map<String, Object> stats = herbMapper.getTranscriptomicsStatistics(tcmHerbId);
        if (stats == null) {
            // 返回默认值（兼容 Java 8）
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("totalGenes", 0);
            defaultStats.put("upregulatedGenes", 0);
            defaultStats.put("downregulatedGenes", 0);
            defaultStats.put("significantGenes", 0);
            defaultStats.put("significantUpregulated", 0);
            defaultStats.put("significantDownregulated", 0);
            defaultStats.put("maxAbsLog2Fc", 0.0);
            defaultStats.put("minPValue", 1.0);
            return defaultStats;
        }
        return stats;
    }

}

