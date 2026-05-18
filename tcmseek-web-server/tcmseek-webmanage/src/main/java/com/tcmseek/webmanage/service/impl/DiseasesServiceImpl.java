package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.DiseasesMapper;
import com.tcmseek.webmanage.domain.Diseases;
import com.tcmseek.webmanage.service.IDiseasesService;

/**
 * 疾病信息Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class DiseasesServiceImpl implements IDiseasesService 
{
    @Autowired
    private DiseasesMapper diseasesMapper;

    /**
     * 查询疾病信息
     * 
     * @param id 疾病信息主键
     * @return 疾病信息
     */
    @Override
    public Diseases selectDiseasesById(Long id)
    {
        return diseasesMapper.selectDiseasesById(id);
    }

    /**
     * 查询疾病信息列表
     * 
     * @param diseases 疾病信息
     * @return 疾病信息
     */
    @Override
    public List<Diseases> selectDiseasesList(Diseases diseases)
    {
        return diseasesMapper.selectDiseasesList(diseases);
    }

    /**
     * 新增疾病信息
     * 
     * @param diseases 疾病信息
     * @return 结果
     */
    @Override
    public int insertDiseases(Diseases diseases)
    {
        return diseasesMapper.insertDiseases(diseases);
    }

    /**
     * 修改疾病信息
     * 
     * @param diseases 疾病信息
     * @return 结果
     */
    @Override
    public int updateDiseases(Diseases diseases)
    {
        return diseasesMapper.updateDiseases(diseases);
    }

    /**
     * 批量删除疾病信息
     * 
     * @param ids 需要删除的疾病信息主键
     * @return 结果
     */
    @Override
    public int deleteDiseasesByIds(Long[] ids)
    {
        return diseasesMapper.deleteDiseasesByIds(ids);
    }

    /**
     * 删除疾病信息信息
     * 
     * @param id 疾病信息主键
     * @return 结果
     */
    @Override
    public int deleteDiseasesById(Long id)
    {
        return diseasesMapper.deleteDiseasesById(id);
    }
}
