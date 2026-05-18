package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.MedicalCasesMapper;
import com.tcmseek.webmanage.domain.MedicalCases;
import com.tcmseek.webmanage.service.IMedicalCasesService;

/**
 * 医案信息Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class MedicalCasesServiceImpl implements IMedicalCasesService 
{
    @Autowired
    private MedicalCasesMapper medicalCasesMapper;

    /**
     * 查询医案信息
     * 
     * @param id 医案信息主键
     * @return 医案信息
     */
    @Override
    public MedicalCases selectMedicalCasesById(Long id)
    {
        return medicalCasesMapper.selectMedicalCasesById(id);
    }

    /**
     * 查询医案信息列表
     * 
     * @param medicalCases 医案信息
     * @return 医案信息
     */
    @Override
    public List<MedicalCases> selectMedicalCasesList(MedicalCases medicalCases)
    {
        return medicalCasesMapper.selectMedicalCasesList(medicalCases);
    }

    /**
     * 新增医案信息
     * 
     * @param medicalCases 医案信息
     * @return 结果
     */
    @Override
    public int insertMedicalCases(MedicalCases medicalCases)
    {
        return medicalCasesMapper.insertMedicalCases(medicalCases);
    }

    /**
     * 修改医案信息
     * 
     * @param medicalCases 医案信息
     * @return 结果
     */
    @Override
    public int updateMedicalCases(MedicalCases medicalCases)
    {
        return medicalCasesMapper.updateMedicalCases(medicalCases);
    }

    /**
     * 批量删除医案信息
     * 
     * @param ids 需要删除的医案信息主键
     * @return 结果
     */
    @Override
    public int deleteMedicalCasesByIds(Long[] ids)
    {
        return medicalCasesMapper.deleteMedicalCasesByIds(ids);
    }

    /**
     * 删除医案信息信息
     * 
     * @param id 医案信息主键
     * @return 结果
     */
    @Override
    public int deleteMedicalCasesById(Long id)
    {
        return medicalCasesMapper.deleteMedicalCasesById(id);
    }
}
