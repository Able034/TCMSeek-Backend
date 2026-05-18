package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.CoreTcmHerbsMapper;
import com.tcmseek.webmanage.domain.CoreTcmHerbs;
import com.tcmseek.webmanage.service.ICoreTcmHerbsService;

/**
 * 核心中药信息Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class CoreTcmHerbsServiceImpl implements ICoreTcmHerbsService 
{
    @Autowired
    private CoreTcmHerbsMapper coreTcmHerbsMapper;

    /**
     * 查询核心中药信息
     * 
     * @param id 核心中药信息主键
     * @return 核心中药信息
     */
    @Override
    public CoreTcmHerbs selectCoreTcmHerbsById(Long id)
    {
        return coreTcmHerbsMapper.selectCoreTcmHerbsById(id);
    }

    /**
     * 查询核心中药信息列表
     * 
     * @param coreTcmHerbs 核心中药信息
     * @return 核心中药信息
     */
    @Override
    public List<CoreTcmHerbs> selectCoreTcmHerbsList(CoreTcmHerbs coreTcmHerbs)
    {
        return coreTcmHerbsMapper.selectCoreTcmHerbsList(coreTcmHerbs);
    }

    /**
     * 新增核心中药信息
     * 
     * @param coreTcmHerbs 核心中药信息
     * @return 结果
     */
    @Override
    public int insertCoreTcmHerbs(CoreTcmHerbs coreTcmHerbs)
    {
        return coreTcmHerbsMapper.insertCoreTcmHerbs(coreTcmHerbs);
    }

    /**
     * 修改核心中药信息
     * 
     * @param coreTcmHerbs 核心中药信息
     * @return 结果
     */
    @Override
    public int updateCoreTcmHerbs(CoreTcmHerbs coreTcmHerbs)
    {
        return coreTcmHerbsMapper.updateCoreTcmHerbs(coreTcmHerbs);
    }

    /**
     * 批量删除核心中药信息
     * 
     * @param ids 需要删除的核心中药信息主键
     * @return 结果
     */
    @Override
    public int deleteCoreTcmHerbsByIds(Long[] ids)
    {
        return coreTcmHerbsMapper.deleteCoreTcmHerbsByIds(ids);
    }

    /**
     * 删除核心中药信息信息
     * 
     * @param id 核心中药信息主键
     * @return 结果
     */
    @Override
    public int deleteCoreTcmHerbsById(Long id)
    {
        return coreTcmHerbsMapper.deleteCoreTcmHerbsById(id);
    }
}
