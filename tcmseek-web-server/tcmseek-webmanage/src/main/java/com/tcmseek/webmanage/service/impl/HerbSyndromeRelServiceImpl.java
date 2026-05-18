package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.HerbSyndromeRelMapper;
import com.tcmseek.webmanage.domain.HerbSyndromeRel;
import com.tcmseek.webmanage.service.IHerbSyndromeRelService;

/**
 * 中药-证候关联Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class HerbSyndromeRelServiceImpl implements IHerbSyndromeRelService 
{
    @Autowired
    private HerbSyndromeRelMapper herbSyndromeRelMapper;

    /**
     * 查询中药-证候关联
     * 
     * @param id 中药-证候关联主键
     * @return 中药-证候关联
     */
    @Override
    public HerbSyndromeRel selectHerbSyndromeRelById(Long id)
    {
        return herbSyndromeRelMapper.selectHerbSyndromeRelById(id);
    }

    /**
     * 查询中药-证候关联列表
     * 
     * @param herbSyndromeRel 中药-证候关联
     * @return 中药-证候关联
     */
    @Override
    public List<HerbSyndromeRel> selectHerbSyndromeRelList(HerbSyndromeRel herbSyndromeRel)
    {
        return herbSyndromeRelMapper.selectHerbSyndromeRelList(herbSyndromeRel);
    }

    /**
     * 新增中药-证候关联
     * 
     * @param herbSyndromeRel 中药-证候关联
     * @return 结果
     */
    @Override
    public int insertHerbSyndromeRel(HerbSyndromeRel herbSyndromeRel)
    {
        return herbSyndromeRelMapper.insertHerbSyndromeRel(herbSyndromeRel);
    }

    /**
     * 修改中药-证候关联
     * 
     * @param herbSyndromeRel 中药-证候关联
     * @return 结果
     */
    @Override
    public int updateHerbSyndromeRel(HerbSyndromeRel herbSyndromeRel)
    {
        return herbSyndromeRelMapper.updateHerbSyndromeRel(herbSyndromeRel);
    }

    /**
     * 批量删除中药-证候关联
     * 
     * @param ids 需要删除的中药-证候关联主键
     * @return 结果
     */
    @Override
    public int deleteHerbSyndromeRelByIds(Long[] ids)
    {
        return herbSyndromeRelMapper.deleteHerbSyndromeRelByIds(ids);
    }

    /**
     * 删除中药-证候关联信息
     * 
     * @param id 中药-证候关联主键
     * @return 结果
     */
    @Override
    public int deleteHerbSyndromeRelById(Long id)
    {
        return herbSyndromeRelMapper.deleteHerbSyndromeRelById(id);
    }
}
