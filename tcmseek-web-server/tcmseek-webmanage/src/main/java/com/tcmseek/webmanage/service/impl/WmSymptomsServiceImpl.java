package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.WmSymptomsMapper;
import com.tcmseek.webmanage.domain.WmSymptoms;
import com.tcmseek.webmanage.service.IWmSymptomsService;

/**
 * 西医症状信息Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class WmSymptomsServiceImpl implements IWmSymptomsService 
{
    @Autowired
    private WmSymptomsMapper wmSymptomsMapper;

    /**
     * 查询西医症状信息
     * 
     * @param id 西医症状信息主键
     * @return 西医症状信息
     */
    @Override
    public WmSymptoms selectWmSymptomsById(Long id)
    {
        return wmSymptomsMapper.selectWmSymptomsById(id);
    }

    /**
     * 查询西医症状信息列表
     * 
     * @param wmSymptoms 西医症状信息
     * @return 西医症状信息
     */
    @Override
    public List<WmSymptoms> selectWmSymptomsList(WmSymptoms wmSymptoms)
    {
        return wmSymptomsMapper.selectWmSymptomsList(wmSymptoms);
    }

    /**
     * 新增西医症状信息
     * 
     * @param wmSymptoms 西医症状信息
     * @return 结果
     */
    @Override
    public int insertWmSymptoms(WmSymptoms wmSymptoms)
    {
        return wmSymptomsMapper.insertWmSymptoms(wmSymptoms);
    }

    /**
     * 修改西医症状信息
     * 
     * @param wmSymptoms 西医症状信息
     * @return 结果
     */
    @Override
    public int updateWmSymptoms(WmSymptoms wmSymptoms)
    {
        return wmSymptomsMapper.updateWmSymptoms(wmSymptoms);
    }

    /**
     * 批量删除西医症状信息
     * 
     * @param ids 需要删除的西医症状信息主键
     * @return 结果
     */
    @Override
    public int deleteWmSymptomsByIds(Long[] ids)
    {
        return wmSymptomsMapper.deleteWmSymptomsByIds(ids);
    }

    /**
     * 删除西医症状信息信息
     * 
     * @param id 西医症状信息主键
     * @return 结果
     */
    @Override
    public int deleteWmSymptomsById(Long id)
    {
        return wmSymptomsMapper.deleteWmSymptomsById(id);
    }
}
