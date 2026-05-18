package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.SyndromeGeneRelMapper;
import com.tcmseek.webmanage.domain.SyndromeGeneRel;
import com.tcmseek.webmanage.service.ISyndromeGeneRelService;

/**
 * 证候-基因关联Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class SyndromeGeneRelServiceImpl implements ISyndromeGeneRelService 
{
    @Autowired
    private SyndromeGeneRelMapper syndromeGeneRelMapper;

    /**
     * 查询证候-基因关联
     * 
     * @param id 证候-基因关联主键
     * @return 证候-基因关联
     */
    @Override
    public SyndromeGeneRel selectSyndromeGeneRelById(Long id)
    {
        return syndromeGeneRelMapper.selectSyndromeGeneRelById(id);
    }

    /**
     * 查询证候-基因关联列表
     * 
     * @param syndromeGeneRel 证候-基因关联
     * @return 证候-基因关联
     */
    @Override
    public List<SyndromeGeneRel> selectSyndromeGeneRelList(SyndromeGeneRel syndromeGeneRel)
    {
        return syndromeGeneRelMapper.selectSyndromeGeneRelList(syndromeGeneRel);
    }

    /**
     * 新增证候-基因关联
     * 
     * @param syndromeGeneRel 证候-基因关联
     * @return 结果
     */
    @Override
    public int insertSyndromeGeneRel(SyndromeGeneRel syndromeGeneRel)
    {
        return syndromeGeneRelMapper.insertSyndromeGeneRel(syndromeGeneRel);
    }

    /**
     * 修改证候-基因关联
     * 
     * @param syndromeGeneRel 证候-基因关联
     * @return 结果
     */
    @Override
    public int updateSyndromeGeneRel(SyndromeGeneRel syndromeGeneRel)
    {
        return syndromeGeneRelMapper.updateSyndromeGeneRel(syndromeGeneRel);
    }

    /**
     * 批量删除证候-基因关联
     * 
     * @param ids 需要删除的证候-基因关联主键
     * @return 结果
     */
    @Override
    public int deleteSyndromeGeneRelByIds(Long[] ids)
    {
        return syndromeGeneRelMapper.deleteSyndromeGeneRelByIds(ids);
    }

    /**
     * 删除证候-基因关联信息
     * 
     * @param id 证候-基因关联主键
     * @return 结果
     */
    @Override
    public int deleteSyndromeGeneRelById(Long id)
    {
        return syndromeGeneRelMapper.deleteSyndromeGeneRelById(id);
    }
}
