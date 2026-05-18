package com.tcmseek.service.impl;

import com.tcmseek.dao.SyndromeMapper;
import com.tcmseek.pojo.entity.TcmSymptom;
import com.tcmseek.pojo.entity.TcmSyndrome;
import com.tcmseek.pojo.entity.coreTcmHerbs;
import com.tcmseek.service.SyndromeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SyndromeServiceImpl implements SyndromeService {

    @Autowired
    private SyndromeMapper syndromeMapper;

    @Override
    public List<TcmSyndrome> selectSyndromeList(String keyword, String category) {
        return syndromeMapper.selectSyndromeList(keyword, category);
    }

    @Override
    public TcmSyndrome selectSyndromeById(String tcmSyndromeId) {
        return syndromeMapper.selectSyndromeById(tcmSyndromeId);
    }

    @Override
    public List<TcmSymptom> selectSymptomsBySyndromeId(String tcmSyndromeId) {
        return syndromeMapper.selectSymptomsBySyndromeId(tcmSyndromeId);
    }

    @Override
    public List<coreTcmHerbs> selectHerbsBySyndromeId(String tcmSyndromeId) {
        return syndromeMapper.selectHerbsBySyndromeId(tcmSyndromeId);
    }

    @Override
    public List<com.tcmseek.pojo.entity.TcmPrescription> selectPrescriptionsBySyndromeId(String tcmSyndromeId) {
        return syndromeMapper.selectPrescriptionsBySyndromeId(tcmSyndromeId);
    }
}

