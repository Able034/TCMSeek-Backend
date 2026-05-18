package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.DiseaseGeneRelMapper;
import com.tcmseek.webmanage.domain.DiseaseGeneRel;
import com.tcmseek.webmanage.service.IDiseaseGeneRelService;

/**
 * 疾病-基因关联Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class DiseaseGeneRelServiceImpl implements IDiseaseGeneRelService 
{
    @Autowired
    private DiseaseGeneRelMapper diseaseGeneRelMapper;

    /**
     * 查询疾病-基因关联
     * 
     * @param id 疾病-基因关联主键
     * @return 疾病-基因关联
     */
    @Override
    public DiseaseGeneRel selectDiseaseGeneRelById(Long id)
    {
        return diseaseGeneRelMapper.selectDiseaseGeneRelById(id);
    }

    /**
     * 查询疾病-基因关联列表
     * 
     * @param diseaseGeneRel 疾病-基因关联
     * @return 疾病-基因关联
     */
    @Override
    public List<DiseaseGeneRel> selectDiseaseGeneRelList(DiseaseGeneRel diseaseGeneRel)
    {
        return diseaseGeneRelMapper.selectDiseaseGeneRelList(diseaseGeneRel);
    }

    /**
     * 新增疾病-基因关联
     * 
     * @param diseaseGeneRel 疾病-基因关联
     * @return 结果
     */
    @Override
    public int insertDiseaseGeneRel(DiseaseGeneRel diseaseGeneRel)
    {
        return diseaseGeneRelMapper.insertDiseaseGeneRel(diseaseGeneRel);
    }

    /**
     * 修改疾病-基因关联
     * 
     * @param diseaseGeneRel 疾病-基因关联
     * @return 结果
     */
    @Override
    public int updateDiseaseGeneRel(DiseaseGeneRel diseaseGeneRel)
    {
        return diseaseGeneRelMapper.updateDiseaseGeneRel(diseaseGeneRel);
    }

    /**
     * 批量删除疾病-基因关联
     * 
     * @param ids 需要删除的疾病-基因关联主键
     * @return 结果
     */
    @Override
    public int deleteDiseaseGeneRelByIds(Long[] ids)
    {
        return diseaseGeneRelMapper.deleteDiseaseGeneRelByIds(ids);
    }

    /**
     * 删除疾病-基因关联信息
     * 
     * @param id 疾病-基因关联主键
     * @return 结果
     */
    @Override
    public int deleteDiseaseGeneRelById(Long id)
    {
        return diseaseGeneRelMapper.deleteDiseaseGeneRelById(id);
    }
}
