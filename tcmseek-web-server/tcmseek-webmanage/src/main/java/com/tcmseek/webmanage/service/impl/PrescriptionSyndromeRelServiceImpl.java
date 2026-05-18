package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.PrescriptionSyndromeRelMapper;
import com.tcmseek.webmanage.domain.PrescriptionSyndromeRel;
import com.tcmseek.webmanage.service.IPrescriptionSyndromeRelService;

/**
 * 方剂-证候关联Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class PrescriptionSyndromeRelServiceImpl implements IPrescriptionSyndromeRelService 
{
    @Autowired
    private PrescriptionSyndromeRelMapper prescriptionSyndromeRelMapper;

    /**
     * 查询方剂-证候关联
     * 
     * @param id 方剂-证候关联主键
     * @return 方剂-证候关联
     */
    @Override
    public PrescriptionSyndromeRel selectPrescriptionSyndromeRelById(Long id)
    {
        return prescriptionSyndromeRelMapper.selectPrescriptionSyndromeRelById(id);
    }

    /**
     * 查询方剂-证候关联列表
     * 
     * @param prescriptionSyndromeRel 方剂-证候关联
     * @return 方剂-证候关联
     */
    @Override
    public List<PrescriptionSyndromeRel> selectPrescriptionSyndromeRelList(PrescriptionSyndromeRel prescriptionSyndromeRel)
    {
        return prescriptionSyndromeRelMapper.selectPrescriptionSyndromeRelList(prescriptionSyndromeRel);
    }

    /**
     * 新增方剂-证候关联
     * 
     * @param prescriptionSyndromeRel 方剂-证候关联
     * @return 结果
     */
    @Override
    public int insertPrescriptionSyndromeRel(PrescriptionSyndromeRel prescriptionSyndromeRel)
    {
        return prescriptionSyndromeRelMapper.insertPrescriptionSyndromeRel(prescriptionSyndromeRel);
    }

    /**
     * 修改方剂-证候关联
     * 
     * @param prescriptionSyndromeRel 方剂-证候关联
     * @return 结果
     */
    @Override
    public int updatePrescriptionSyndromeRel(PrescriptionSyndromeRel prescriptionSyndromeRel)
    {
        return prescriptionSyndromeRelMapper.updatePrescriptionSyndromeRel(prescriptionSyndromeRel);
    }

    /**
     * 批量删除方剂-证候关联
     * 
     * @param ids 需要删除的方剂-证候关联主键
     * @return 结果
     */
    @Override
    public int deletePrescriptionSyndromeRelByIds(Long[] ids)
    {
        return prescriptionSyndromeRelMapper.deletePrescriptionSyndromeRelByIds(ids);
    }

    /**
     * 删除方剂-证候关联信息
     * 
     * @param id 方剂-证候关联主键
     * @return 结果
     */
    @Override
    public int deletePrescriptionSyndromeRelById(Long id)
    {
        return prescriptionSyndromeRelMapper.deletePrescriptionSyndromeRelById(id);
    }
}
