package com.tcmseek.webmanage.service;

import java.util.List;
import com.tcmseek.webmanage.domain.PrescriptionOtherHerbRel;

/**
 * 方剂-其他中药关联Service接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface IPrescriptionOtherHerbRelService 
{
    /**
     * 查询方剂-其他中药关联
     * 
     * @param id 方剂-其他中药关联主键
     * @return 方剂-其他中药关联
     */
    public PrescriptionOtherHerbRel selectPrescriptionOtherHerbRelById(Long id);

    /**
     * 查询方剂-其他中药关联列表
     * 
     * @param prescriptionOtherHerbRel 方剂-其他中药关联
     * @return 方剂-其他中药关联集合
     */
    public List<PrescriptionOtherHerbRel> selectPrescriptionOtherHerbRelList(PrescriptionOtherHerbRel prescriptionOtherHerbRel);

    /**
     * 新增方剂-其他中药关联
     * 
     * @param prescriptionOtherHerbRel 方剂-其他中药关联
     * @return 结果
     */
    public int insertPrescriptionOtherHerbRel(PrescriptionOtherHerbRel prescriptionOtherHerbRel);

    /**
     * 修改方剂-其他中药关联
     * 
     * @param prescriptionOtherHerbRel 方剂-其他中药关联
     * @return 结果
     */
    public int updatePrescriptionOtherHerbRel(PrescriptionOtherHerbRel prescriptionOtherHerbRel);

    /**
     * 批量删除方剂-其他中药关联
     * 
     * @param ids 需要删除的方剂-其他中药关联主键集合
     * @return 结果
     */
    public int deletePrescriptionOtherHerbRelByIds(Long[] ids);

    /**
     * 删除方剂-其他中药关联信息
     * 
     * @param id 方剂-其他中药关联主键
     * @return 结果
     */
    public int deletePrescriptionOtherHerbRelById(Long id);
}
