package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.MedicalCasePrescriptionRel;

/**
 * 医案-方剂关联Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface MedicalCasePrescriptionRelMapper 
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
     * 删除医案-方剂关联
     * 
     * @param id 医案-方剂关联主键
     * @return 结果
     */
    public int deleteMedicalCasePrescriptionRelById(Long id);

    /**
     * 批量删除医案-方剂关联
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMedicalCasePrescriptionRelByIds(Long[] ids);
}
