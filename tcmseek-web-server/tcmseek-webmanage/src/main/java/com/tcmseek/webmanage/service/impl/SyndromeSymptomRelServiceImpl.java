package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.SyndromeSymptomRelMapper;
import com.tcmseek.webmanage.domain.SyndromeSymptomRel;
import com.tcmseek.webmanage.service.ISyndromeSymptomRelService;

/**
 * 证候-中医症状关联Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class SyndromeSymptomRelServiceImpl implements ISyndromeSymptomRelService 
{
    @Autowired
    private SyndromeSymptomRelMapper syndromeSymptomRelMapper;

    /**
     * 查询证候-中医症状关联
     * 
     * @param id 证候-中医症状关联主键
     * @return 证候-中医症状关联
     */
    @Override
    public SyndromeSymptomRel selectSyndromeSymptomRelById(Long id)
    {
        return syndromeSymptomRelMapper.selectSyndromeSymptomRelById(id);
    }

    /**
     * 查询证候-中医症状关联列表
     * 
     * @param syndromeSymptomRel 证候-中医症状关联
     * @return 证候-中医症状关联
     */
    @Override
    public List<SyndromeSymptomRel> selectSyndromeSymptomRelList(SyndromeSymptomRel syndromeSymptomRel)
    {
        return syndromeSymptomRelMapper.selectSyndromeSymptomRelList(syndromeSymptomRel);
    }

    /**
     * 新增证候-中医症状关联
     * 
     * @param syndromeSymptomRel 证候-中医症状关联
     * @return 结果
     */
    @Override
    public int insertSyndromeSymptomRel(SyndromeSymptomRel syndromeSymptomRel)
    {
        return syndromeSymptomRelMapper.insertSyndromeSymptomRel(syndromeSymptomRel);
    }

    /**
     * 修改证候-中医症状关联
     * 
     * @param syndromeSymptomRel 证候-中医症状关联
     * @return 结果
     */
    @Override
    public int updateSyndromeSymptomRel(SyndromeSymptomRel syndromeSymptomRel)
    {
        return syndromeSymptomRelMapper.updateSyndromeSymptomRel(syndromeSymptomRel);
    }

    /**
     * 批量删除证候-中医症状关联
     * 
     * @param ids 需要删除的证候-中医症状关联主键
     * @return 结果
     */
    @Override
    public int deleteSyndromeSymptomRelByIds(Long[] ids)
    {
        return syndromeSymptomRelMapper.deleteSyndromeSymptomRelByIds(ids);
    }

    /**
     * 删除证候-中医症状关联信息
     * 
     * @param id 证候-中医症状关联主键
     * @return 结果
     */
    @Override
    public int deleteSyndromeSymptomRelById(Long id)
    {
        return syndromeSymptomRelMapper.deleteSyndromeSymptomRelById(id);
    }
}
