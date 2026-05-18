package com.tcmseek.service.impl;

import com.tcmseek.dao.WmSymptomMapper;
import com.tcmseek.pojo.entity.TcmSymptom;
import com.tcmseek.pojo.entity.Target;
import com.tcmseek.pojo.entity.WmSymptom;
import com.tcmseek.service.WmSymptomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 西医症状Service实现类
 */
@Service
@Slf4j
public class WmSymptomServiceImpl implements WmSymptomService {

    @Autowired
    private WmSymptomMapper wmSymptomMapper;

    @Override
    public List<WmSymptom> selectWmSymptomList(String keyword) {
        log.info("查询西医症状列表, 关键词: {}", keyword);
        return wmSymptomMapper.selectWmSymptomList(keyword);
    }

    @Override
    public WmSymptom selectWmSymptomById(String wmSymptomId) {
        log.info("查询西医症状详情, ID: {}", wmSymptomId);
        return wmSymptomMapper.selectWmSymptomById(wmSymptomId);
    }

    @Override
    public List<TcmSymptom> selectTcmSymptomsByWmSymptomId(String wmSymptomId) {
        log.info("查询西医症状{}关联的中医症状", wmSymptomId);
        return wmSymptomMapper.selectTcmSymptomsByWmSymptomId(wmSymptomId);
    }

    @Override
    public List<Target> selectGenesByWmSymptomId(String wmSymptomId) {
        log.info("查询西医症状{}关联的基因", wmSymptomId);
        return wmSymptomMapper.selectGenesByWmSymptomId(wmSymptomId);
    }
}



