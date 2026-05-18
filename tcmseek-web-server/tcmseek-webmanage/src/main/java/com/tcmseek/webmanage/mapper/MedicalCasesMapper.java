package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.MedicalCases;

/**
 * 医案信息Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface MedicalCasesMapper 
{
    /**
     * 查询医案信息
     * 
     * @param id 医案信息主键
     * @return 医案信息
     */
    public MedicalCases selectMedicalCasesById(Long id);

    /**
     * 查询医案信息列表
     * 
     * @param medicalCases 医案信息
     * @return 医案信息集合
     */
    public List<MedicalCases> selectMedicalCasesList(MedicalCases medicalCases);

    /**
     * 新增医案信息
     * 
     * @param medicalCases 医案信息
     * @return 结果
     */
    public int insertMedicalCases(MedicalCases medicalCases);

    /**
     * 修改医案信息
     * 
     * @param medicalCases 医案信息
     * @return 结果
     */
    public int updateMedicalCases(MedicalCases medicalCases);

    /**
     * 删除医案信息
     * 
     * @param id 医案信息主键
     * @return 结果
     */
    public int deleteMedicalCasesById(Long id);

    /**
     * 批量删除医案信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMedicalCasesByIds(Long[] ids);
}
