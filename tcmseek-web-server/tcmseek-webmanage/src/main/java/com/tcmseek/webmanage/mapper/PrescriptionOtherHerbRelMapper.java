package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.PrescriptionOtherHerbRel;

/**
 * 方剂-其他中药关联Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface PrescriptionOtherHerbRelMapper 
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
     * 删除方剂-其他中药关联
     * 
     * @param id 方剂-其他中药关联主键
     * @return 结果
     */
    public int deletePrescriptionOtherHerbRelById(Long id);

    /**
     * 批量删除方剂-其他中药关联
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePrescriptionOtherHerbRelByIds(Long[] ids);
}
