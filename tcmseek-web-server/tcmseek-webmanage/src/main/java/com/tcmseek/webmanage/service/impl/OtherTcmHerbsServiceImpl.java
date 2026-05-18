package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.OtherTcmHerbsMapper;
import com.tcmseek.webmanage.domain.OtherTcmHerbs;
import com.tcmseek.webmanage.service.IOtherTcmHerbsService;

/**
 * 其他中药信息Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class OtherTcmHerbsServiceImpl implements IOtherTcmHerbsService 
{
    @Autowired
    private OtherTcmHerbsMapper otherTcmHerbsMapper;

    /**
     * 查询其他中药信息
     * 
     * @param id 其他中药信息主键
     * @return 其他中药信息
     */
    @Override
    public OtherTcmHerbs selectOtherTcmHerbsById(Long id)
    {
        return otherTcmHerbsMapper.selectOtherTcmHerbsById(id);
    }

    /**
     * 查询其他中药信息列表
     * 
     * @param otherTcmHerbs 其他中药信息
     * @return 其他中药信息
     */
    @Override
    public List<OtherTcmHerbs> selectOtherTcmHerbsList(OtherTcmHerbs otherTcmHerbs)
    {
        return otherTcmHerbsMapper.selectOtherTcmHerbsList(otherTcmHerbs);
    }

    /**
     * 新增其他中药信息
     * 
     * @param otherTcmHerbs 其他中药信息
     * @return 结果
     */
    @Override
    public int insertOtherTcmHerbs(OtherTcmHerbs otherTcmHerbs)
    {
        return otherTcmHerbsMapper.insertOtherTcmHerbs(otherTcmHerbs);
    }

    /**
     * 修改其他中药信息
     * 
     * @param otherTcmHerbs 其他中药信息
     * @return 结果
     */
    @Override
    public int updateOtherTcmHerbs(OtherTcmHerbs otherTcmHerbs)
    {
        return otherTcmHerbsMapper.updateOtherTcmHerbs(otherTcmHerbs);
    }

    /**
     * 批量删除其他中药信息
     * 
     * @param ids 需要删除的其他中药信息主键
     * @return 结果
     */
    @Override
    public int deleteOtherTcmHerbsByIds(Long[] ids)
    {
        return otherTcmHerbsMapper.deleteOtherTcmHerbsByIds(ids);
    }

    /**
     * 删除其他中药信息信息
     * 
     * @param id 其他中药信息主键
     * @return 结果
     */
    @Override
    public int deleteOtherTcmHerbsById(Long id)
    {
        return otherTcmHerbsMapper.deleteOtherTcmHerbsById(id);
    }
}
