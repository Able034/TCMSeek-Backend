package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.HerbSymptomRelMapper;
import com.tcmseek.webmanage.domain.HerbSymptomRel;
import com.tcmseek.webmanage.service.IHerbSymptomRelService;

/**
 * 中药-中医症状关联Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class HerbSymptomRelServiceImpl implements IHerbSymptomRelService 
{
    @Autowired
    private HerbSymptomRelMapper herbSymptomRelMapper;

    /**
     * 查询中药-中医症状关联
     * 
     * @param id 中药-中医症状关联主键
     * @return 中药-中医症状关联
     */
    @Override
    public HerbSymptomRel selectHerbSymptomRelById(Long id)
    {
        return herbSymptomRelMapper.selectHerbSymptomRelById(id);
    }

    /**
     * 查询中药-中医症状关联列表
     * 
     * @param herbSymptomRel 中药-中医症状关联
     * @return 中药-中医症状关联
     */
    @Override
    public List<HerbSymptomRel> selectHerbSymptomRelList(HerbSymptomRel herbSymptomRel)
    {
        return herbSymptomRelMapper.selectHerbSymptomRelList(herbSymptomRel);
    }

    /**
     * 新增中药-中医症状关联
     * 
     * @param herbSymptomRel 中药-中医症状关联
     * @return 结果
     */
    @Override
    public int insertHerbSymptomRel(HerbSymptomRel herbSymptomRel)
    {
        return herbSymptomRelMapper.insertHerbSymptomRel(herbSymptomRel);
    }

    /**
     * 修改中药-中医症状关联
     * 
     * @param herbSymptomRel 中药-中医症状关联
     * @return 结果
     */
    @Override
    public int updateHerbSymptomRel(HerbSymptomRel herbSymptomRel)
    {
        return herbSymptomRelMapper.updateHerbSymptomRel(herbSymptomRel);
    }

    /**
     * 批量删除中药-中医症状关联
     * 
     * @param ids 需要删除的中药-中医症状关联主键
     * @return 结果
     */
    @Override
    public int deleteHerbSymptomRelByIds(Long[] ids)
    {
        return herbSymptomRelMapper.deleteHerbSymptomRelByIds(ids);
    }

    /**
     * 删除中药-中医症状关联信息
     * 
     * @param id 中药-中医症状关联主键
     * @return 结果
     */
    @Override
    public int deleteHerbSymptomRelById(Long id)
    {
        return herbSymptomRelMapper.deleteHerbSymptomRelById(id);
    }
}
