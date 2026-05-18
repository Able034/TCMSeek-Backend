package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.TcmCompoundsMapper;
import com.tcmseek.webmanage.domain.TcmCompounds;
import com.tcmseek.webmanage.service.ITcmCompoundsService;

/**
 * 中药化合物理化性质Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class TcmCompoundsServiceImpl implements ITcmCompoundsService 
{
    @Autowired
    private TcmCompoundsMapper tcmCompoundsMapper;

    /**
     * 查询中药化合物理化性质
     * 
     * @param id 中药化合物理化性质主键
     * @return 中药化合物理化性质
     */
    @Override
    public TcmCompounds selectTcmCompoundsById(Long id)
    {
        return tcmCompoundsMapper.selectTcmCompoundsById(id);
    }

    /**
     * 查询中药化合物理化性质列表
     * 
     * @param tcmCompounds 中药化合物理化性质
     * @return 中药化合物理化性质
     */
    @Override
    public List<TcmCompounds> selectTcmCompoundsList(TcmCompounds tcmCompounds )
    {
        return tcmCompoundsMapper.selectTcmCompoundsList(tcmCompounds);
    }

    /**
     * 新增中药化合物理化性质
     * 
     * @param tcmCompounds 中药化合物理化性质
     * @return 结果
     */
    @Override
    public int insertTcmCompounds(TcmCompounds tcmCompounds)
    {
        return tcmCompoundsMapper.insertTcmCompounds(tcmCompounds);
    }

    /**
     * 修改中药化合物理化性质
     * 
     * @param tcmCompounds 中药化合物理化性质
     * @return 结果
     */
    @Override
    public int updateTcmCompounds(TcmCompounds tcmCompounds)
    {
        return tcmCompoundsMapper.updateTcmCompounds(tcmCompounds);
    }

    /**
     * 批量删除中药化合物理化性质
     * 
     * @param ids 需要删除的中药化合物理化性质主键
     * @return 结果
     */
    @Override
    public int deleteTcmCompoundsByIds(Long[] ids)
    {
        return tcmCompoundsMapper.deleteTcmCompoundsByIds(ids);
    }

    /**
     * 删除中药化合物理化性质信息
     * 
     * @param id 中药化合物理化性质主键
     * @return 结果
     */
    @Override
    public int deleteTcmCompoundsById(Long id)
    {
        return tcmCompoundsMapper.deleteTcmCompoundsById(id);
    }

    @Override
    public List<TcmCompounds> selectTcmCompoundsListexport(TcmCompounds tcmCompounds, Long lastId, int pageSize) {
        return tcmCompoundsMapper.selectTcmCompoundsListexport(tcmCompounds,lastId,pageSize);
    }
}
