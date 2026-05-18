package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.WmSymptomGeneRelMapper;
import com.tcmseek.webmanage.domain.WmSymptomGeneRel;
import com.tcmseek.webmanage.service.IWmSymptomGeneRelService;

/**
 * 西医症状-基因关联Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class WmSymptomGeneRelServiceImpl implements IWmSymptomGeneRelService 
{
    @Autowired
    private WmSymptomGeneRelMapper wmSymptomGeneRelMapper;

    /**
     * 查询西医症状-基因关联
     * 
     * @param id 西医症状-基因关联主键
     * @return 西医症状-基因关联
     */
    @Override
    public WmSymptomGeneRel selectWmSymptomGeneRelById(Long id)
    {
        return wmSymptomGeneRelMapper.selectWmSymptomGeneRelById(id);
    }

    /**
     * 查询西医症状-基因关联列表
     * 
     * @param wmSymptomGeneRel 西医症状-基因关联
     * @return 西医症状-基因关联
     */
    @Override
    public List<WmSymptomGeneRel> selectWmSymptomGeneRelList(WmSymptomGeneRel wmSymptomGeneRel)
    {
        return wmSymptomGeneRelMapper.selectWmSymptomGeneRelList(wmSymptomGeneRel);
    }

    /**
     * 新增西医症状-基因关联
     * 
     * @param wmSymptomGeneRel 西医症状-基因关联
     * @return 结果
     */
    @Override
    public int insertWmSymptomGeneRel(WmSymptomGeneRel wmSymptomGeneRel)
    {
        return wmSymptomGeneRelMapper.insertWmSymptomGeneRel(wmSymptomGeneRel);
    }

    /**
     * 修改西医症状-基因关联
     * 
     * @param wmSymptomGeneRel 西医症状-基因关联
     * @return 结果
     */
    @Override
    public int updateWmSymptomGeneRel(WmSymptomGeneRel wmSymptomGeneRel)
    {
        return wmSymptomGeneRelMapper.updateWmSymptomGeneRel(wmSymptomGeneRel);
    }

    /**
     * 批量删除西医症状-基因关联
     * 
     * @param ids 需要删除的西医症状-基因关联主键
     * @return 结果
     */
    @Override
    public int deleteWmSymptomGeneRelByIds(Long[] ids)
    {
        return wmSymptomGeneRelMapper.deleteWmSymptomGeneRelByIds(ids);
    }

    /**
     * 删除西医症状-基因关联信息
     * 
     * @param id 西医症状-基因关联主键
     * @return 结果
     */
    @Override
    public int deleteWmSymptomGeneRelById(Long id)
    {
        return wmSymptomGeneRelMapper.deleteWmSymptomGeneRelById(id);
    }
}
