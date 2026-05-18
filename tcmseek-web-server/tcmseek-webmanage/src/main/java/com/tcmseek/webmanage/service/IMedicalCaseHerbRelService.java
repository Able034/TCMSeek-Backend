package com.tcmseek.webmanage.service;

import java.util.List;
import com.tcmseek.webmanage.domain.MedicalCaseHerbRel;

/**
 * 医案-核心中药关联Service接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface IMedicalCaseHerbRelService 
{
    /**
     * 查询医案-核心中药关联
     * 
     * @param id 医案-核心中药关联主键
     * @return 医案-核心中药关联
     */
    public MedicalCaseHerbRel selectMedicalCaseHerbRelById(Long id);

    /**
     * 查询医案-核心中药关联列表
     * 
     * @param medicalCaseHerbRel 医案-核心中药关联
     * @return 医案-核心中药关联集合
     */
    public List<MedicalCaseHerbRel> selectMedicalCaseHerbRelList(MedicalCaseHerbRel medicalCaseHerbRel);

    /**
     * 新增医案-核心中药关联
     * 
     * @param medicalCaseHerbRel 医案-核心中药关联
     * @return 结果
     */
    public int insertMedicalCaseHerbRel(MedicalCaseHerbRel medicalCaseHerbRel);

    /**
     * 修改医案-核心中药关联
     * 
     * @param medicalCaseHerbRel 医案-核心中药关联
     * @return 结果
     */
    public int updateMedicalCaseHerbRel(MedicalCaseHerbRel medicalCaseHerbRel);

    /**
     * 批量删除医案-核心中药关联
     * 
     * @param ids 需要删除的医案-核心中药关联主键集合
     * @return 结果
     */
    public int deleteMedicalCaseHerbRelByIds(Long[] ids);

    /**
     * 删除医案-核心中药关联信息
     * 
     * @param id 医案-核心中药关联主键
     * @return 结果
     */
    public int deleteMedicalCaseHerbRelById(Long id);
}
