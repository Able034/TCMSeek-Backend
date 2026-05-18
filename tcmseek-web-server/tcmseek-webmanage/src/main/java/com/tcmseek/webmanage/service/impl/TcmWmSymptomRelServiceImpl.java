package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.TcmWmSymptomRelMapper;
import com.tcmseek.webmanage.domain.TcmWmSymptomRel;
import com.tcmseek.webmanage.service.ITcmWmSymptomRelService;

/**
 * 中医症状-西医症状关联Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class TcmWmSymptomRelServiceImpl implements ITcmWmSymptomRelService 
{
    @Autowired
    private TcmWmSymptomRelMapper tcmWmSymptomRelMapper;

    /**
     * 查询中医症状-西医症状关联
     * 
     * @param id 中医症状-西医症状关联主键
     * @return 中医症状-西医症状关联
     */
    @Override
    public TcmWmSymptomRel selectTcmWmSymptomRelById(Long id)
    {
        return tcmWmSymptomRelMapper.selectTcmWmSymptomRelById(id);
    }

    /**
     * 查询中医症状-西医症状关联列表
     * 
     * @param tcmWmSymptomRel 中医症状-西医症状关联
     * @return 中医症状-西医症状关联
     */
    @Override
    public List<TcmWmSymptomRel> selectTcmWmSymptomRelList(TcmWmSymptomRel tcmWmSymptomRel)
    {
        return tcmWmSymptomRelMapper.selectTcmWmSymptomRelList(tcmWmSymptomRel);
    }

    /**
     * 新增中医症状-西医症状关联
     * 
     * @param tcmWmSymptomRel 中医症状-西医症状关联
     * @return 结果
     */
    @Override
    public int insertTcmWmSymptomRel(TcmWmSymptomRel tcmWmSymptomRel)
    {
        return tcmWmSymptomRelMapper.insertTcmWmSymptomRel(tcmWmSymptomRel);
    }

    /**
     * 修改中医症状-西医症状关联
     * 
     * @param tcmWmSymptomRel 中医症状-西医症状关联
     * @return 结果
     */
    @Override
    public int updateTcmWmSymptomRel(TcmWmSymptomRel tcmWmSymptomRel)
    {
        return tcmWmSymptomRelMapper.updateTcmWmSymptomRel(tcmWmSymptomRel);
    }

    /**
     * 批量删除中医症状-西医症状关联
     * 
     * @param ids 需要删除的中医症状-西医症状关联主键
     * @return 结果
     */
    @Override
    public int deleteTcmWmSymptomRelByIds(Long[] ids)
    {
        return tcmWmSymptomRelMapper.deleteTcmWmSymptomRelByIds(ids);
    }

    /**
     * 删除中医症状-西医症状关联信息
     * 
     * @param id 中医症状-西医症状关联主键
     * @return 结果
     */
    @Override
    public int deleteTcmWmSymptomRelById(Long id)
    {
        return tcmWmSymptomRelMapper.deleteTcmWmSymptomRelById(id);
    }
}
