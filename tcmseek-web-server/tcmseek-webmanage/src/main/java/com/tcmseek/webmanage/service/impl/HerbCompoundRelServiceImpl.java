package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.HerbCompoundRelMapper;
import com.tcmseek.webmanage.domain.HerbCompoundRel;
import com.tcmseek.webmanage.service.IHerbCompoundRelService;

/**
 * 中药-化合物关联Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class HerbCompoundRelServiceImpl implements IHerbCompoundRelService 
{
    @Autowired
    private HerbCompoundRelMapper herbCompoundRelMapper;

    /**
     * 查询中药-化合物关联
     * 
     * @param id 中药-化合物关联主键
     * @return 中药-化合物关联
     */
    @Override
    public HerbCompoundRel selectHerbCompoundRelById(Long id)
    {
        return herbCompoundRelMapper.selectHerbCompoundRelById(id);
    }

    /**
     * 查询中药-化合物关联列表
     * 
     * @param herbCompoundRel 中药-化合物关联
     * @return 中药-化合物关联
     */
    @Override
    public List<HerbCompoundRel> selectHerbCompoundRelList(HerbCompoundRel herbCompoundRel)
    {
        return herbCompoundRelMapper.selectHerbCompoundRelList(herbCompoundRel);
    }

    /**
     * 新增中药-化合物关联
     * 
     * @param herbCompoundRel 中药-化合物关联
     * @return 结果
     */
    @Override
    public int insertHerbCompoundRel(HerbCompoundRel herbCompoundRel)
    {
        return herbCompoundRelMapper.insertHerbCompoundRel(herbCompoundRel);
    }

    /**
     * 修改中药-化合物关联
     * 
     * @param herbCompoundRel 中药-化合物关联
     * @return 结果
     */
    @Override
    public int updateHerbCompoundRel(HerbCompoundRel herbCompoundRel)
    {
        return herbCompoundRelMapper.updateHerbCompoundRel(herbCompoundRel);
    }

    /**
     * 批量删除中药-化合物关联
     * 
     * @param ids 需要删除的中药-化合物关联主键
     * @return 结果
     */
    @Override
    public int deleteHerbCompoundRelByIds(Long[] ids)
    {
        return herbCompoundRelMapper.deleteHerbCompoundRelByIds(ids);
    }

    /**
     * 删除中药-化合物关联信息
     * 
     * @param id 中药-化合物关联主键
     * @return 结果
     */
    @Override
    public int deleteHerbCompoundRelById(Long id)
    {
        return herbCompoundRelMapper.deleteHerbCompoundRelById(id);
    }
}
