package com.tcmseek.webmanage.service;

import java.util.List;
import com.tcmseek.webmanage.domain.PrescriptionSyndromeRel;

/**
 * 方剂-证候关联Service接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface IPrescriptionSyndromeRelService 
{
    /**
     * 查询方剂-证候关联
     * 
     * @param id 方剂-证候关联主键
     * @return 方剂-证候关联
     */
    public PrescriptionSyndromeRel selectPrescriptionSyndromeRelById(Long id);

    /**
     * 查询方剂-证候关联列表
     * 
     * @param prescriptionSyndromeRel 方剂-证候关联
     * @return 方剂-证候关联集合
     */
    public List<PrescriptionSyndromeRel> selectPrescriptionSyndromeRelList(PrescriptionSyndromeRel prescriptionSyndromeRel);

    /**
     * 新增方剂-证候关联
     * 
     * @param prescriptionSyndromeRel 方剂-证候关联
     * @return 结果
     */
    public int insertPrescriptionSyndromeRel(PrescriptionSyndromeRel prescriptionSyndromeRel);

    /**
     * 修改方剂-证候关联
     * 
     * @param prescriptionSyndromeRel 方剂-证候关联
     * @return 结果
     */
    public int updatePrescriptionSyndromeRel(PrescriptionSyndromeRel prescriptionSyndromeRel);

    /**
     * 批量删除方剂-证候关联
     * 
     * @param ids 需要删除的方剂-证候关联主键集合
     * @return 结果
     */
    public int deletePrescriptionSyndromeRelByIds(Long[] ids);

    /**
     * 删除方剂-证候关联信息
     * 
     * @param id 方剂-证候关联主键
     * @return 结果
     */
    public int deletePrescriptionSyndromeRelById(Long id);
}
