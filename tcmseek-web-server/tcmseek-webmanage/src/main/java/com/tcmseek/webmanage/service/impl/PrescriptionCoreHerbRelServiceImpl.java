package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.PrescriptionCoreHerbRelMapper;
import com.tcmseek.webmanage.domain.PrescriptionCoreHerbRel;
import com.tcmseek.webmanage.service.IPrescriptionCoreHerbRelService;

/**
 * 方剂-核心中药关联Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class PrescriptionCoreHerbRelServiceImpl implements IPrescriptionCoreHerbRelService 
{
    @Autowired
    private PrescriptionCoreHerbRelMapper prescriptionCoreHerbRelMapper;

    /**
     * 查询方剂-核心中药关联
     * 
     * @param id 方剂-核心中药关联主键
     * @return 方剂-核心中药关联
     */
    @Override
    public PrescriptionCoreHerbRel selectPrescriptionCoreHerbRelById(Long id)
    {
        return prescriptionCoreHerbRelMapper.selectPrescriptionCoreHerbRelById(id);
    }

    /**
     * 查询方剂-核心中药关联列表
     * 
     * @param prescriptionCoreHerbRel 方剂-核心中药关联
     * @return 方剂-核心中药关联
     */
    @Override
    public List<PrescriptionCoreHerbRel> selectPrescriptionCoreHerbRelList(PrescriptionCoreHerbRel prescriptionCoreHerbRel)
    {
        return prescriptionCoreHerbRelMapper.selectPrescriptionCoreHerbRelList(prescriptionCoreHerbRel);
    }

    /**
     * 新增方剂-核心中药关联
     * 
     * @param prescriptionCoreHerbRel 方剂-核心中药关联
     * @return 结果
     */
    @Override
    public int insertPrescriptionCoreHerbRel(PrescriptionCoreHerbRel prescriptionCoreHerbRel)
    {
        return prescriptionCoreHerbRelMapper.insertPrescriptionCoreHerbRel(prescriptionCoreHerbRel);
    }

    /**
     * 修改方剂-核心中药关联
     * 
     * @param prescriptionCoreHerbRel 方剂-核心中药关联
     * @return 结果
     */
    @Override
    public int updatePrescriptionCoreHerbRel(PrescriptionCoreHerbRel prescriptionCoreHerbRel)
    {
        return prescriptionCoreHerbRelMapper.updatePrescriptionCoreHerbRel(prescriptionCoreHerbRel);
    }

    /**
     * 批量删除方剂-核心中药关联
     * 
     * @param ids 需要删除的方剂-核心中药关联主键
     * @return 结果
     */
    @Override
    public int deletePrescriptionCoreHerbRelByIds(Long[] ids)
    {
        return prescriptionCoreHerbRelMapper.deletePrescriptionCoreHerbRelByIds(ids);
    }

    /**
     * 删除方剂-核心中药关联信息
     * 
     * @param id 方剂-核心中药关联主键
     * @return 结果
     */
    @Override
    public int deletePrescriptionCoreHerbRelById(Long id)
    {
        return prescriptionCoreHerbRelMapper.deletePrescriptionCoreHerbRelById(id);
    }
}
