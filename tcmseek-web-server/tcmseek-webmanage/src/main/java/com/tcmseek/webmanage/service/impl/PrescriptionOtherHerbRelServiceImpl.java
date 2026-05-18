package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.PrescriptionOtherHerbRelMapper;
import com.tcmseek.webmanage.domain.PrescriptionOtherHerbRel;
import com.tcmseek.webmanage.service.IPrescriptionOtherHerbRelService;

/**
 * 方剂-其他中药关联Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class PrescriptionOtherHerbRelServiceImpl implements IPrescriptionOtherHerbRelService 
{
    @Autowired
    private PrescriptionOtherHerbRelMapper prescriptionOtherHerbRelMapper;

    /**
     * 查询方剂-其他中药关联
     * 
     * @param id 方剂-其他中药关联主键
     * @return 方剂-其他中药关联
     */
    @Override
    public PrescriptionOtherHerbRel selectPrescriptionOtherHerbRelById(Long id)
    {
        return prescriptionOtherHerbRelMapper.selectPrescriptionOtherHerbRelById(id);
    }

    /**
     * 查询方剂-其他中药关联列表
     * 
     * @param prescriptionOtherHerbRel 方剂-其他中药关联
     * @return 方剂-其他中药关联
     */
    @Override
    public List<PrescriptionOtherHerbRel> selectPrescriptionOtherHerbRelList(PrescriptionOtherHerbRel prescriptionOtherHerbRel)
    {
        return prescriptionOtherHerbRelMapper.selectPrescriptionOtherHerbRelList(prescriptionOtherHerbRel);
    }

    /**
     * 新增方剂-其他中药关联
     * 
     * @param prescriptionOtherHerbRel 方剂-其他中药关联
     * @return 结果
     */
    @Override
    public int insertPrescriptionOtherHerbRel(PrescriptionOtherHerbRel prescriptionOtherHerbRel)
    {
        return prescriptionOtherHerbRelMapper.insertPrescriptionOtherHerbRel(prescriptionOtherHerbRel);
    }

    /**
     * 修改方剂-其他中药关联
     * 
     * @param prescriptionOtherHerbRel 方剂-其他中药关联
     * @return 结果
     */
    @Override
    public int updatePrescriptionOtherHerbRel(PrescriptionOtherHerbRel prescriptionOtherHerbRel)
    {
        return prescriptionOtherHerbRelMapper.updatePrescriptionOtherHerbRel(prescriptionOtherHerbRel);
    }

    /**
     * 批量删除方剂-其他中药关联
     * 
     * @param ids 需要删除的方剂-其他中药关联主键
     * @return 结果
     */
    @Override
    public int deletePrescriptionOtherHerbRelByIds(Long[] ids)
    {
        return prescriptionOtherHerbRelMapper.deletePrescriptionOtherHerbRelByIds(ids);
    }

    /**
     * 删除方剂-其他中药关联信息
     * 
     * @param id 方剂-其他中药关联主键
     * @return 结果
     */
    @Override
    public int deletePrescriptionOtherHerbRelById(Long id)
    {
        return prescriptionOtherHerbRelMapper.deletePrescriptionOtherHerbRelById(id);
    }
}
