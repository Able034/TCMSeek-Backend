package com.tcmseek.dao;

import com.tcmseek.pojo.entity.MedicalCase;
import com.tcmseek.pojo.entity.TcmPrescription;
import com.tcmseek.pojo.entity.coreTcmHerbs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 中医医案Mapper接口
 * Medical Case Mapper
 */
@Mapper
public interface MedicalCaseMapper {

    /**
     * 根据医案ID查询医案详情
     * @param caseId 医案ID
     * @return 医案信息
     */
    MedicalCase selectMedicalCaseById(@Param("caseId") String caseId);

    /**
     * 查询医案的关联方剂列表
     * @param caseId 医案ID
     * @return 方剂列表
     */
    List<TcmPrescription> selectPrescriptionsByCaseId(@Param("caseId") String caseId);

    /**
     * 查询医案的关联中药列表
     * @param caseId 医案ID
     * @return 中药列表
     */
    List<coreTcmHerbs> selectHerbsByCaseId(@Param("caseId") String caseId);

    /**
     * 查询所有医案
     * @return 医案列表
     */
    List<MedicalCase> selectMedicalCaseList(
            @Param("keyword") String keyword
    );
}








