package com.tcmseek.webmanage.service;

import java.util.List;
import com.tcmseek.webmanage.domain.PrescriptionSymptomRel;

/**
 * 方剂-中医症状关联Service接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface IPrescriptionSymptomRelService 
{
    /**
     * 查询方剂-中医症状关联
     * 
     * @param id 方剂-中医症状关联主键
     * @return 方剂-中医症状关联
     */
    public PrescriptionSymptomRel selectPrescriptionSymptomRelById(Long id);

    /**
     * 查询方剂-中医症状关联列表
     * 
     * @param prescriptionSymptomRel 方剂-中医症状关联
     * @return 方剂-中医症状关联集合
     */
    public List<PrescriptionSymptomRel> selectPrescriptionSymptomRelList(PrescriptionSymptomRel prescriptionSymptomRel);

    /**
     * 新增方剂-中医症状关联
     * 
     * @param prescriptionSymptomRel 方剂-中医症状关联
     * @return 结果
     */
    public int insertPrescriptionSymptomRel(PrescriptionSymptomRel prescriptionSymptomRel);

    /**
     * 修改方剂-中医症状关联
     * 
     * @param prescriptionSymptomRel 方剂-中医症状关联
     * @return 结果
     */
    public int updatePrescriptionSymptomRel(PrescriptionSymptomRel prescriptionSymptomRel);

    /**
     * 批量删除方剂-中医症状关联
     * 
     * @param ids 需要删除的方剂-中医症状关联主键集合
     * @return 结果
     */
    public int deletePrescriptionSymptomRelByIds(Long[] ids);

    /**
     * 删除方剂-中医症状关联信息
     * 
     * @param id 方剂-中医症状关联主键
     * @return 结果
     */
    public int deletePrescriptionSymptomRelById(Long id);
}
