package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.TcmSyndromesMapper;
import com.tcmseek.webmanage.domain.TcmSyndromes;
import com.tcmseek.webmanage.service.ITcmSyndromesService;

/**
 * 中医证候信息Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class TcmSyndromesServiceImpl implements ITcmSyndromesService 
{
    @Autowired
    private TcmSyndromesMapper tcmSyndromesMapper;

    /**
     * 查询中医证候信息
     * 
     * @param id 中医证候信息主键
     * @return 中医证候信息
     */
    @Override
    public TcmSyndromes selectTcmSyndromesById(Long id)
    {
        return tcmSyndromesMapper.selectTcmSyndromesById(id);
    }

    /**
     * 查询中医证候信息列表
     * 
     * @param tcmSyndromes 中医证候信息
     * @return 中医证候信息
     */
    @Override
    public List<TcmSyndromes> selectTcmSyndromesList(TcmSyndromes tcmSyndromes)
    {
        return tcmSyndromesMapper.selectTcmSyndromesList(tcmSyndromes);
    }

    /**
     * 新增中医证候信息
     * 
     * @param tcmSyndromes 中医证候信息
     * @return 结果
     */
    @Override
    public int insertTcmSyndromes(TcmSyndromes tcmSyndromes)
    {
        return tcmSyndromesMapper.insertTcmSyndromes(tcmSyndromes);
    }

    /**
     * 修改中医证候信息
     * 
     * @param tcmSyndromes 中医证候信息
     * @return 结果
     */
    @Override
    public int updateTcmSyndromes(TcmSyndromes tcmSyndromes)
    {
        return tcmSyndromesMapper.updateTcmSyndromes(tcmSyndromes);
    }

    /**
     * 批量删除中医证候信息
     * 
     * @param ids 需要删除的中医证候信息主键
     * @return 结果
     */
    @Override
    public int deleteTcmSyndromesByIds(Long[] ids)
    {
        return tcmSyndromesMapper.deleteTcmSyndromesByIds(ids);
    }

    /**
     * 删除中医证候信息信息
     * 
     * @param id 中医证候信息主键
     * @return 结果
     */
    @Override
    public int deleteTcmSyndromesById(Long id)
    {
        return tcmSyndromesMapper.deleteTcmSyndromesById(id);
    }
}
