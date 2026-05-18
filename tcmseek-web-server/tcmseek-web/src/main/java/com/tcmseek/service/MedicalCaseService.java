package com.tcmseek.service;

import com.tcmseek.pojo.entity.MedicalCase;
import com.tcmseek.pojo.entity.TcmPrescription;
import com.tcmseek.pojo.entity.coreTcmHerbs;

import java.util.List;

/**
 * 中医医案Service接口
 * Medical Case Service
 */
public interface MedicalCaseService {

    /**
     * 根据医案ID查询医案详情
     * @param caseId 医案ID
     * @return 医案信息
     */
    MedicalCase selectMedicalCaseById(String caseId);

    /**
     * 查询医案的关联方剂列表
     * @param caseId 医案ID
     * @return 方剂列表
     */
    List<TcmPrescription> selectPrescriptionsByCaseId(String caseId);

    /**
     * 查询医案的关联中药列表
     * @param caseId 医案ID
     * @return 中药列表
     */
    List<coreTcmHerbs> selectHerbsByCaseId(String caseId);

    /**
     * 查询所有医案列表
     * @return 医案列表
     */
    List<MedicalCase> selectMedicalCaseList(String  keyword);
}








