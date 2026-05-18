package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.MedicalCaseHerbRelMapper;
import com.tcmseek.webmanage.domain.MedicalCaseHerbRel;
import com.tcmseek.webmanage.service.IMedicalCaseHerbRelService;

/**
 * 医案-核心中药关联Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class MedicalCaseHerbRelServiceImpl implements IMedicalCaseHerbRelService 
{
    @Autowired
    private MedicalCaseHerbRelMapper medicalCaseHerbRelMapper;

    /**
     * 查询医案-核心中药关联
     * 
     * @param id 医案-核心中药关联主键
     * @return 医案-核心中药关联
     */
    @Override
    public MedicalCaseHerbRel selectMedicalCaseHerbRelById(Long id)
    {
        return medicalCaseHerbRelMapper.selectMedicalCaseHerbRelById(id);
    }

    /**
     * 查询医案-核心中药关联列表
     * 
     * @param medicalCaseHerbRel 医案-核心中药关联
     * @return 医案-核心中药关联
     */
    @Override
    public List<MedicalCaseHerbRel> selectMedicalCaseHerbRelList(MedicalCaseHerbRel medicalCaseHerbRel)
    {
        return medicalCaseHerbRelMapper.selectMedicalCaseHerbRelList(medicalCaseHerbRel);
    }

    /**
     * 新增医案-核心中药关联
     * 
     * @param medicalCaseHerbRel 医案-核心中药关联
     * @return 结果
     */
    @Override
    public int insertMedicalCaseHerbRel(MedicalCaseHerbRel medicalCaseHerbRel)
    {
        return medicalCaseHerbRelMapper.insertMedicalCaseHerbRel(medicalCaseHerbRel);
    }

    /**
     * 修改医案-核心中药关联
     * 
     * @param medicalCaseHerbRel 医案-核心中药关联
     * @return 结果
     */
    @Override
    public int updateMedicalCaseHerbRel(MedicalCaseHerbRel medicalCaseHerbRel)
    {
        return medicalCaseHerbRelMapper.updateMedicalCaseHerbRel(medicalCaseHerbRel);
    }

    /**
     * 批量删除医案-核心中药关联
     * 
     * @param ids 需要删除的医案-核心中药关联主键
     * @return 结果
     */
    @Override
    public int deleteMedicalCaseHerbRelByIds(Long[] ids)
    {
        return medicalCaseHerbRelMapper.deleteMedicalCaseHerbRelByIds(ids);
    }

    /**
     * 删除医案-核心中药关联信息
     * 
     * @param id 医案-核心中药关联主键
     * @return 结果
     */
    @Override
    public int deleteMedicalCaseHerbRelById(Long id)
    {
        return medicalCaseHerbRelMapper.deleteMedicalCaseHerbRelById(id);
    }
}
