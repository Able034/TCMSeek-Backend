package com.tcmseek.webmanage.service;

import java.util.List;
import com.tcmseek.webmanage.domain.CoreTcmHerbs;

/**
 * 核心中药信息Service接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface ICoreTcmHerbsService 
{
    /**
     * 查询核心中药信息
     * 
     * @param id 核心中药信息主键
     * @return 核心中药信息
     */
    public CoreTcmHerbs selectCoreTcmHerbsById(Long id);

    /**
     * 查询核心中药信息列表
     * 
     * @param coreTcmHerbs 核心中药信息
     * @return 核心中药信息集合
     */
    public List<CoreTcmHerbs> selectCoreTcmHerbsList(CoreTcmHerbs coreTcmHerbs);

    /**
     * 新增核心中药信息
     * 
     * @param coreTcmHerbs 核心中药信息
     * @return 结果
     */
    public int insertCoreTcmHerbs(CoreTcmHerbs coreTcmHerbs);

    /**
     * 修改核心中药信息
     * 
     * @param coreTcmHerbs 核心中药信息
     * @return 结果
     */
    public int updateCoreTcmHerbs(CoreTcmHerbs coreTcmHerbs);

    /**
     * 批量删除核心中药信息
     * 
     * @param ids 需要删除的核心中药信息主键集合
     * @return 结果
     */
    public int deleteCoreTcmHerbsByIds(Long[] ids);

    /**
     * 删除核心中药信息信息
     * 
     * @param id 核心中药信息主键
     * @return 结果
     */
    public int deleteCoreTcmHerbsById(Long id);
}
