package com.tcmseek.webmanage.service;

import java.util.List;
import com.tcmseek.webmanage.domain.PrescriptionCoreHerbRel;

/**
 * 方剂-核心中药关联Service接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface IPrescriptionCoreHerbRelService 
{
    /**
     * 查询方剂-核心中药关联
     * 
     * @param id 方剂-核心中药关联主键
     * @return 方剂-核心中药关联
     */
    public PrescriptionCoreHerbRel selectPrescriptionCoreHerbRelById(Long id);

    /**
     * 查询方剂-核心中药关联列表
     * 
     * @param prescriptionCoreHerbRel 方剂-核心中药关联
     * @return 方剂-核心中药关联集合
     */
    public List<PrescriptionCoreHerbRel> selectPrescriptionCoreHerbRelList(PrescriptionCoreHerbRel prescriptionCoreHerbRel);

    /**
     * 新增方剂-核心中药关联
     * 
     * @param prescriptionCoreHerbRel 方剂-核心中药关联
     * @return 结果
     */
    public int insertPrescriptionCoreHerbRel(PrescriptionCoreHerbRel prescriptionCoreHerbRel);

    /**
     * 修改方剂-核心中药关联
     * 
     * @param prescriptionCoreHerbRel 方剂-核心中药关联
     * @return 结果
     */
    public int updatePrescriptionCoreHerbRel(PrescriptionCoreHerbRel prescriptionCoreHerbRel);

    /**
     * 批量删除方剂-核心中药关联
     * 
     * @param ids 需要删除的方剂-核心中药关联主键集合
     * @return 结果
     */
    public int deletePrescriptionCoreHerbRelByIds(Long[] ids);

    /**
     * 删除方剂-核心中药关联信息
     * 
     * @param id 方剂-核心中药关联主键
     * @return 结果
     */
    public int deletePrescriptionCoreHerbRelById(Long id);
}
