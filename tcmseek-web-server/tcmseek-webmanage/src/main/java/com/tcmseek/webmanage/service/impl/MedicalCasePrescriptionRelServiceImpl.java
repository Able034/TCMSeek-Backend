package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.MedicalCasePrescriptionRelMapper;
import com.tcmseek.webmanage.domain.MedicalCasePrescriptionRel;
import com.tcmseek.webmanage.service.IMedicalCasePrescriptionRelService;

/**
 * 医案-方剂关联Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class MedicalCasePrescriptionRelServiceImpl implements IMedicalCasePrescriptionRelService 
{
    @Autowired
    private MedicalCasePrescriptionRelMapper medicalCasePrescriptionRelMapper;

    /**
     * 查询医案-方剂关联
     * 
     * @param id 医案-方剂关联主键
     * @return 医案-方剂关联
     */
    @Override
    public MedicalCasePrescriptionRel selectMedicalCasePrescriptionRelById(Long id)
    {
        return medicalCasePrescriptionRelMapper.selectMedicalCasePrescriptionRelById(id);
    }

    /**
     * 查询医案-方剂关联列表
     * 
     * @param medicalCasePrescriptionRel 医案-方剂关联
     * @return 医案-方剂关联
     */
    @Override
    public List<MedicalCasePrescriptionRel> selectMedicalCasePrescriptionRelList(MedicalCasePrescriptionRel medicalCasePrescriptionRel)
    {
        return medicalCasePrescriptionRelMapper.selectMedicalCasePrescriptionRelList(medicalCasePrescriptionRel);
    }

    /**
     * 新增医案-方剂关联
     * 
     * @param medicalCasePrescriptionRel 医案-方剂关联
     * @return 结果
     */
    @Override
    public int insertMedicalCasePrescriptionRel(MedicalCasePrescriptionRel medicalCasePrescriptionRel)
    {
        return medicalCasePrescriptionRelMapper.insertMedicalCasePrescriptionRel(medicalCasePrescriptionRel);
    }

    /**
     * 修改医案-方剂关联
     * 
     * @param medicalCasePrescriptionRel 医案-方剂关联
     * @return 结果
     */
    @Override
    public int updateMedicalCasePrescriptionRel(MedicalCasePrescriptionRel medicalCasePrescriptionRel)
    {
        return medicalCasePrescriptionRelMapper.updateMedicalCasePrescriptionRel(medicalCasePrescriptionRel);
    }

    /**
     * 批量删除医案-方剂关联
     * 
     * @param ids 需要删除的医案-方剂关联主键
     * @return 结果
     */
    @Override
    public int deleteMedicalCasePrescriptionRelByIds(Long[] ids)
    {
        return medicalCasePrescriptionRelMapper.deleteMedicalCasePrescriptionRelByIds(ids);
    }

    /**
     * 删除医案-方剂关联信息
     * 
     * @param id 医案-方剂关联主键
     * @return 结果
     */
    @Override
    public int deleteMedicalCasePrescriptionRelById(Long id)
    {
        return medicalCasePrescriptionRelMapper.deleteMedicalCasePrescriptionRelById(id);
    }
}
