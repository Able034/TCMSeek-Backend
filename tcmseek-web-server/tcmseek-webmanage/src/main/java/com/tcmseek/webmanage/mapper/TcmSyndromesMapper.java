package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.TcmSyndromes;

/**
 * 中医证候信息Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface TcmSyndromesMapper 
{
    /**
     * 查询中医证候信息
     * 
     * @param id 中医证候信息主键
     * @return 中医证候信息
     */
    public TcmSyndromes selectTcmSyndromesById(Long id);

    /**
     * 查询中医证候信息列表
     * 
     * @param tcmSyndromes 中医证候信息
     * @return 中医证候信息集合
     */
    public List<TcmSyndromes> selectTcmSyndromesList(TcmSyndromes tcmSyndromes);

    /**
     * 新增中医证候信息
     * 
     * @param tcmSyndromes 中医证候信息
     * @return 结果
     */
    public int insertTcmSyndromes(TcmSyndromes tcmSyndromes);

    /**
     * 修改中医证候信息
     * 
     * @param tcmSyndromes 中医证候信息
     * @return 结果
     */
    public int updateTcmSyndromes(TcmSyndromes tcmSyndromes);

    /**
     * 删除中医证候信息
     * 
     * @param id 中医证候信息主键
     * @return 结果
     */
    public int deleteTcmSyndromesById(Long id);

    /**
     * 批量删除中医证候信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTcmSyndromesByIds(Long[] ids);
}
