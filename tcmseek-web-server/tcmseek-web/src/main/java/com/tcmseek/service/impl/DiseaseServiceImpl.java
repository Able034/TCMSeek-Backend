package com.tcmseek.service.impl;

import com.tcmseek.dao.DiseaseMapper;
import com.tcmseek.pojo.entity.Disease;
import com.tcmseek.pojo.entity.TcmPrescription;
import com.tcmseek.pojo.entity.coreTcmHerbs;
import com.tcmseek.service.DiseaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 疾病信息Service实现类
 */
@Service
public class DiseaseServiceImpl implements DiseaseService {

    @Autowired
    private DiseaseMapper diseaseMapper;

    @Override
    public Disease selectDiseaseById(String diseaseId) {
        return diseaseMapper.selectDiseaseById(diseaseId);
    }

    @Override
    public List<Map<String, Object>> selectGenesByDiseaseId(String diseaseId) {
        return diseaseMapper.selectGenesByDiseaseId(diseaseId);
    }

    @Override
    public List<TcmPrescription> selectPrescriptionsByDiseaseId(String diseaseId) {
        return diseaseMapper.selectPrescriptionsByDiseaseId(diseaseId);
    }

    @Override
    public List<coreTcmHerbs> selectHerbsByDiseaseId(String diseaseId) {
        return diseaseMapper.selectHerbsByDiseaseId(diseaseId);
    }
}



