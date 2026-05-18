package com.tcmseek.webmanage.service;

import java.util.List;
import com.tcmseek.webmanage.domain.OtherTcmHerbs;

/**
 * 其他中药信息Service接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface IOtherTcmHerbsService 
{
    /**
     * 查询其他中药信息
     * 
     * @param id 其他中药信息主键
     * @return 其他中药信息
     */
    public OtherTcmHerbs selectOtherTcmHerbsById(Long id);

    /**
     * 查询其他中药信息列表
     * 
     * @param otherTcmHerbs 其他中药信息
     * @return 其他中药信息集合
     */
    public List<OtherTcmHerbs> selectOtherTcmHerbsList(OtherTcmHerbs otherTcmHerbs);

    /**
     * 新增其他中药信息
     * 
     * @param otherTcmHerbs 其他中药信息
     * @return 结果
     */
    public int insertOtherTcmHerbs(OtherTcmHerbs otherTcmHerbs);

    /**
     * 修改其他中药信息
     * 
     * @param otherTcmHerbs 其他中药信息
     * @return 结果
     */
    public int updateOtherTcmHerbs(OtherTcmHerbs otherTcmHerbs);

    /**
     * 批量删除其他中药信息
     * 
     * @param ids 需要删除的其他中药信息主键集合
     * @return 结果
     */
    public int deleteOtherTcmHerbsByIds(Long[] ids);

    /**
     * 删除其他中药信息信息
     * 
     * @param id 其他中药信息主键
     * @return 结果
     */
    public int deleteOtherTcmHerbsById(Long id);
}
