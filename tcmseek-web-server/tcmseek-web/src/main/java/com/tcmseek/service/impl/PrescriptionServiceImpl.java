package com.tcmseek.service.impl;

import com.tcmseek.dao.PrescriptionMapper;
import com.tcmseek.pojo.entity.Disease;
import com.tcmseek.pojo.entity.MedicalCase;
import com.tcmseek.pojo.entity.TcmPrescription;
import com.tcmseek.pojo.entity.TcmSymptom;
import com.tcmseek.pojo.entity.TcmSyndrome;
import com.tcmseek.pojo.entity.coreTcmHerbs;
import com.tcmseek.service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 方剂信息Service实现类
 */
@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    @Autowired
    private PrescriptionMapper prescriptionMapper;

    @Override
    public List<TcmPrescription> selectPrescriptionList(String keyword, String source) {
        return prescriptionMapper.selectPrescriptionList(keyword, source);
    }

    @Override
    public TcmPrescription selectPrescriptionById(String tcmPrescriptionId) {
        return prescriptionMapper.selectPrescriptionById(tcmPrescriptionId);
    }

    @Override
    @Deprecated
    public List<coreTcmHerbs> selectHerbsByPrescriptionId(String tcmPrescriptionId) {
        // 重定向到核心中药方法
        return selectCoreHerbsByPrescriptionId(tcmPrescriptionId);
    }

    @Override
    public List<coreTcmHerbs> selectCoreHerbsByPrescriptionId(String tcmPrescriptionId) {
        return prescriptionMapper.selectCoreHerbsByPrescriptionId(tcmPrescriptionId);
    }

    @Override
    public List<Map<String, Object>> selectOtherHerbsByPrescriptionId(String tcmPrescriptionId) {
        return prescriptionMapper.selectOtherHerbsByPrescriptionId(tcmPrescriptionId);
    }

    @Override
    public List<Disease> selectDiseasesByPrescriptionId(String tcmPrescriptionId) {
        return prescriptionMapper.selectDiseasesByPrescriptionId(tcmPrescriptionId);
    }

    @Override
    public List<TcmSymptom> selectSymptomsByPrescriptionId(String tcmPrescriptionId) {
        return prescriptionMapper.selectSymptomsByPrescriptionId(tcmPrescriptionId);
    }

    @Override
    public List<TcmSyndrome> selectSyndromesByPrescriptionId(String tcmPrescriptionId) {
        return prescriptionMapper.selectSyndromesByPrescriptionId(tcmPrescriptionId);
    }

    @Override
    public List<MedicalCase> selectMedicalCasesByPrescriptionId(String tcmPrescriptionId) {
        return prescriptionMapper.selectMedicalCasesByPrescriptionId(tcmPrescriptionId);
    }
    
    @Override
    public List<Map<String, Object>> getTranscriptomicsData(String tcmPrescriptionId) {
        return prescriptionMapper.selectTranscriptomicsByPrescriptionId(tcmPrescriptionId);
    }
    
    @Override
    public Map<String, Object> getTranscriptomicsStatistics(String tcmPrescriptionId) {
        Map<String, Object> statistics = prescriptionMapper.getTranscriptomicsStatistics(tcmPrescriptionId);
        
        // 如果没有数据，返回默认的空统计
        if (statistics == null || statistics.get("totalGenes") == null) {
            statistics = new HashMap<>();
            statistics.put("totalGenes", 0);
            statistics.put("upregulatedGenes", 0);
            statistics.put("downregulatedGenes", 0);
            statistics.put("significantGenes", 0);
            statistics.put("significantUpregulated", 0);
            statistics.put("significantDownregulated", 0);
            statistics.put("maxAbsLog2Fc", 0.0);
            statistics.put("minPValue", 1.0);
        }
        
        return statistics;
    }
    
    @Override
    public List<Map<String, Object>> getHerbsTranscriptomicsData(String tcmPrescriptionId) {
        return prescriptionMapper.selectHerbsTranscriptomicsByPrescriptionId(tcmPrescriptionId);
    }
}

