package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.TcmSymptomsMapper;
import com.tcmseek.webmanage.domain.TcmSymptoms;
import com.tcmseek.webmanage.service.ITcmSymptomsService;

/**
 * 中医症状信息Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class TcmSymptomsServiceImpl implements ITcmSymptomsService 
{
    @Autowired
    private TcmSymptomsMapper tcmSymptomsMapper;

    /**
     * 查询中医症状信息
     * 
     * @param id 中医症状信息主键
     * @return 中医症状信息
     */
    @Override
    public TcmSymptoms selectTcmSymptomsById(Long id)
    {
        return tcmSymptomsMapper.selectTcmSymptomsById(id);
    }

    /**
     * 查询中医症状信息列表
     * 
     * @param tcmSymptoms 中医症状信息
     * @return 中医症状信息
     */
    @Override
    public List<TcmSymptoms> selectTcmSymptomsList(TcmSymptoms tcmSymptoms)
    {
        return tcmSymptomsMapper.selectTcmSymptomsList(tcmSymptoms);
    }

    /**
     * 新增中医症状信息
     * 
     * @param tcmSymptoms 中医症状信息
     * @return 结果
     */
    @Override
    public int insertTcmSymptoms(TcmSymptoms tcmSymptoms)
    {
        return tcmSymptomsMapper.insertTcmSymptoms(tcmSymptoms);
    }

    /**
     * 修改中医症状信息
     * 
     * @param tcmSymptoms 中医症状信息
     * @return 结果
     */
    @Override
    public int updateTcmSymptoms(TcmSymptoms tcmSymptoms)
    {
        return tcmSymptomsMapper.updateTcmSymptoms(tcmSymptoms);
    }

    /**
     * 批量删除中医症状信息
     * 
     * @param ids 需要删除的中医症状信息主键
     * @return 结果
     */
    @Override
    public int deleteTcmSymptomsByIds(Long[] ids)
    {
        return tcmSymptomsMapper.deleteTcmSymptomsByIds(ids);
    }

    /**
     * 删除中医症状信息信息
     * 
     * @param id 中医症状信息主键
     * @return 结果
     */
    @Override
    public int deleteTcmSymptomsById(Long id)
    {
        return tcmSymptomsMapper.deleteTcmSymptomsById(id);
    }
}
