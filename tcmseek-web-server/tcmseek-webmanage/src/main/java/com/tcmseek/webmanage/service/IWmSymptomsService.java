package com.tcmseek.webmanage.service;

import java.util.List;
import com.tcmseek.webmanage.domain.WmSymptoms;

/**
 * 西医症状信息Service接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface IWmSymptomsService 
{
    /**
     * 查询西医症状信息
     * 
     * @param id 西医症状信息主键
     * @return 西医症状信息
     */
    public WmSymptoms selectWmSymptomsById(Long id);

    /**
     * 查询西医症状信息列表
     * 
     * @param wmSymptoms 西医症状信息
     * @return 西医症状信息集合
     */
    public List<WmSymptoms> selectWmSymptomsList(WmSymptoms wmSymptoms);

    /**
     * 新增西医症状信息
     * 
     * @param wmSymptoms 西医症状信息
     * @return 结果
     */
    public int insertWmSymptoms(WmSymptoms wmSymptoms);

    /**
     * 修改西医症状信息
     * 
     * @param wmSymptoms 西医症状信息
     * @return 结果
     */
    public int updateWmSymptoms(WmSymptoms wmSymptoms);

    /**
     * 批量删除西医症状信息
     * 
     * @param ids 需要删除的西医症状信息主键集合
     * @return 结果
     */
    public int deleteWmSymptomsByIds(Long[] ids);

    /**
     * 删除西医症状信息信息
     * 
     * @param id 西医症状信息主键
     * @return 结果
     */
    public int deleteWmSymptomsById(Long id);
}
