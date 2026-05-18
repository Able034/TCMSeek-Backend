package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.PrescriptionSymptomRelMapper;
import com.tcmseek.webmanage.domain.PrescriptionSymptomRel;
import com.tcmseek.webmanage.service.IPrescriptionSymptomRelService;

/**
 * 方剂-中医症状关联Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class PrescriptionSymptomRelServiceImpl implements IPrescriptionSymptomRelService 
{
    @Autowired
    private PrescriptionSymptomRelMapper prescriptionSymptomRelMapper;

    /**
     * 查询方剂-中医症状关联
     * 
     * @param id 方剂-中医症状关联主键
     * @return 方剂-中医症状关联
     */
    @Override
    public PrescriptionSymptomRel selectPrescriptionSymptomRelById(Long id)
    {
        return prescriptionSymptomRelMapper.selectPrescriptionSymptomRelById(id);
    }

    /**
     * 查询方剂-中医症状关联列表
     * 
     * @param prescriptionSymptomRel 方剂-中医症状关联
     * @return 方剂-中医症状关联
     */
    @Override
    public List<PrescriptionSymptomRel> selectPrescriptionSymptomRelList(PrescriptionSymptomRel prescriptionSymptomRel)
    {
        return prescriptionSymptomRelMapper.selectPrescriptionSymptomRelList(prescriptionSymptomRel);
    }

    /**
     * 新增方剂-中医症状关联
     * 
     * @param prescriptionSymptomRel 方剂-中医症状关联
     * @return 结果
     */
    @Override
    public int insertPrescriptionSymptomRel(PrescriptionSymptomRel prescriptionSymptomRel)
    {
        return prescriptionSymptomRelMapper.insertPrescriptionSymptomRel(prescriptionSymptomRel);
    }

    /**
     * 修改方剂-中医症状关联
     * 
     * @param prescriptionSymptomRel 方剂-中医症状关联
     * @return 结果
     */
    @Override
    public int updatePrescriptionSymptomRel(PrescriptionSymptomRel prescriptionSymptomRel)
    {
        return prescriptionSymptomRelMapper.updatePrescriptionSymptomRel(prescriptionSymptomRel);
    }

    /**
     * 批量删除方剂-中医症状关联
     * 
     * @param ids 需要删除的方剂-中医症状关联主键
     * @return 结果
     */
    @Override
    public int deletePrescriptionSymptomRelByIds(Long[] ids)
    {
        return prescriptionSymptomRelMapper.deletePrescriptionSymptomRelByIds(ids);
    }

    /**
     * 删除方剂-中医症状关联信息
     * 
     * @param id 方剂-中医症状关联主键
     * @return 结果
     */
    @Override
    public int deletePrescriptionSymptomRelById(Long id)
    {
        return prescriptionSymptomRelMapper.deletePrescriptionSymptomRelById(id);
    }
}
