package com.tcmseek.service.impl;

import com.tcmseek.dao.SymptomMapper;
import com.tcmseek.pojo.entity.TcmPrescription;
import com.tcmseek.pojo.entity.TcmSymptom;
import com.tcmseek.pojo.entity.coreTcmHerbs;
import com.tcmseek.service.SymptomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 中医症状Service实现类
 */
@Service
public class SymptomServiceImpl implements SymptomService {

    @Autowired
    private SymptomMapper symptomMapper;

    @Override
    public List<TcmSymptom> selectSymptomList(String keyword, String locus, String type) {
        return symptomMapper.selectSymptomList(keyword, locus, type);
    }

    @Override
    public TcmSymptom selectSymptomById(String tcmSymptomId) {
        return symptomMapper.selectSymptomById(tcmSymptomId);
    }

    @Override
    public List<coreTcmHerbs> selectHerbsBySymptomId(String tcmSymptomId) {
        return symptomMapper.selectHerbsBySymptomId(tcmSymptomId);
    }

    @Override
    public List<TcmPrescription> selectPrescriptionsBySymptomId(String tcmSymptomId) {
        return symptomMapper.selectPrescriptionsBySymptomId(tcmSymptomId);
    }

    @Override
    public List<com.tcmseek.pojo.entity.TcmSyndrome> selectSyndromesBySymptomId(String tcmSymptomId) {
        return symptomMapper.selectSyndromesBySymptomId(tcmSymptomId);
    }

    @Override
    public List<com.tcmseek.pojo.entity.WmSymptom> selectWmSymptomsBySymptomId(String tcmSymptomId) {
        return symptomMapper.selectWmSymptomsBySymptomId(tcmSymptomId);
    }
}

