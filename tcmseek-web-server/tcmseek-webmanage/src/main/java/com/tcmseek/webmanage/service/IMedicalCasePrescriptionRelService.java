package com.tcmseek.webmanage.service;

import java.util.List;
import com.tcmseek.webmanage.domain.MedicalCasePrescriptionRel;

/**
 * 医案-方剂关联Service接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface IMedicalCasePrescriptionRelService 
{
    /**
     * 查询医案-方剂关联
     * 
     * @param id 医案-方剂关联主键
     * @return 医案-方剂关联
     */
    public MedicalCasePrescriptionRel selectMedicalCasePrescriptionRelById(Long id);

    /**
     * 查询医案-方剂关联列表
     * 
     * @param medicalCasePrescriptionRel 医案-方剂关联
     * @return 医案-方剂关联集合
     */
    public List<MedicalCasePrescriptionRel> selectMedicalCasePrescriptionRelList(MedicalCasePrescriptionRel medicalCasePrescriptionRel);

    /**
     * 新增医案-方剂关联
     * 
     * @param medicalCasePrescriptionRel 医案-方剂关联
     * @return 结果
     */
    public int insertMedicalCasePrescriptionRel(MedicalCasePrescriptionRel medicalCasePrescriptionRel);

    /**
     * 修改医案-方剂关联
     * 
     * @param medicalCasePrescriptionRel 医案-方剂关联
     * @return 结果
     */
    public int updateMedicalCasePrescriptionRel(MedicalCasePrescriptionRel medicalCasePrescriptionRel);

    /**
     * 批量删除医案-方剂关联
     * 
     * @param ids 需要删除的医案-方剂关联主键集合
     * @return 结果
     */
    public int deleteMedicalCasePrescriptionRelByIds(Long[] ids);

    /**
     * 删除医案-方剂关联信息
     * 
     * @param id 医案-方剂关联主键
     * @return 结果
     */
    public int deleteMedicalCasePrescriptionRelById(Long id);
}
