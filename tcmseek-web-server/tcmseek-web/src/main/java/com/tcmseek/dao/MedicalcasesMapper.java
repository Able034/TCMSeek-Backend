package com.tcmseek.dao;

import com.tcmseek.pojo.entity.medicalCases;

import java.util.List;

public interface MedicalcasesMapper {
    /**
     * 对中药相关联的中医医案分页查询
     * @param herbId
     * @return
     */
    List<medicalCases> selectMedicalCasesByHerbId(String herbId);
}
