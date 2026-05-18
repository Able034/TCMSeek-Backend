package com.tcmseek.service.impl;

import com.tcmseek.dao.MedicalCaseMapper;
import com.tcmseek.pojo.entity.MedicalCase;
import com.tcmseek.pojo.entity.TcmPrescription;
import com.tcmseek.pojo.entity.coreTcmHerbs;
import com.tcmseek.service.MedicalCaseService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 中医医案Service实现类
 * Medical Case Service Implementation
 */
@Service
public class MedicalCaseServiceImpl implements MedicalCaseService {

    @Resource
    private MedicalCaseMapper medicalCaseMapper;

    /**
     * 根据医案ID查询医案详情
     */
    @Override
    public MedicalCase selectMedicalCaseById(String caseId) {
        return medicalCaseMapper.selectMedicalCaseById(caseId);
    }

    /**
     * 查询医案的关联方剂列表
     */
    @Override
    public List<TcmPrescription> selectPrescriptionsByCaseId(String caseId) {
        return medicalCaseMapper.selectPrescriptionsByCaseId(caseId);
    }

    /**
     * 查询医案的关联中药列表
     */
    @Override
    public List<coreTcmHerbs> selectHerbsByCaseId(String caseId) {
        return medicalCaseMapper.selectHerbsByCaseId(caseId);
    }

    /**
     * 获取医案列表
     */
    @Override
    public List<MedicalCase> selectMedicalCaseList(String  keyword) {

        return medicalCaseMapper.selectMedicalCaseList(keyword);
    }
}








