package com.tcmseek.service.impl;

import com.tcmseek.dao.StatisticsMapper;
import com.tcmseek.pojo.vo.DatabaseStatisticsVO;
import com.tcmseek.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private StatisticsMapper statisticsMapper;

    @Override
    public DatabaseStatisticsVO getDatabaseStatistics() {
        DatabaseStatisticsVO vo = new DatabaseStatisticsVO();
        
        Long coreHerbs = statisticsMapper.countCoreHerbs();
        Long otherHerbs = statisticsMapper.countOtherHerbs();
        vo.setHerbsCount(coreHerbs + otherHerbs);
        
        vo.setFormulasCount(statisticsMapper.countPrescriptions());
        vo.setSymptomsCount(statisticsMapper.countSymptoms());
        vo.setSyndromesCount(statisticsMapper.countSyndromes());
        vo.setCompoundsCount(statisticsMapper.countCompounds());
        vo.setTargetsCount(statisticsMapper.countTargets());
        vo.setDiseasesCount(statisticsMapper.countDiseases());
        vo.setCasesCount(statisticsMapper.countMedicalCases());
        
        return vo;
    }

    @Override
    public List<Map<String, Object>> getHerbCategories() {
        return statisticsMapper.countHerbsByCategory();
    }

    @Override
    public List<Map<String, Object>> getPrescriptionSources() {
        return statisticsMapper.countPrescriptionsBySource();
    }
}

