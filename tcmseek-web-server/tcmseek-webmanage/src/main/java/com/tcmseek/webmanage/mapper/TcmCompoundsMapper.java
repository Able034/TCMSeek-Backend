package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.TcmCompounds;
import org.apache.ibatis.annotations.Param;

/**
 * 中药化合物理化性质Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface TcmCompoundsMapper 
{
    /**
     * 查询中药化合物理化性质
     * 
     * @param id 中药化合物理化性质主键
     * @return 中药化合物理化性质
     */
    public TcmCompounds selectTcmCompoundsById(Long id);

    /**
     * 查询中药化合物理化性质列表
     * 
     * @param tcmCompounds 中药化合物理化性质
     * @return 中药化合物理化性质集合
     */
    public List<TcmCompounds> selectTcmCompoundsList(TcmCompounds tcmCompounds);

    /**
     * 新增中药化合物理化性质
     * 
     * @param tcmCompounds 中药化合物理化性质
     * @return 结果
     */
    public int insertTcmCompounds(TcmCompounds tcmCompounds);

    /**
     * 修改中药化合物理化性质
     * 
     * @param tcmCompounds 中药化合物理化性质
     * @return 结果
     */
    public int updateTcmCompounds(TcmCompounds tcmCompounds);

    /**
     * 删除中药化合物理化性质
     * 
     * @param id 中药化合物理化性质主键
     * @return 结果
     */
    public int deleteTcmCompoundsById(Long id);

    /**
     * 批量删除中药化合物理化性质
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTcmCompoundsByIds(Long[] ids);

    List<TcmCompounds> selectTcmCompoundsListexport(@Param("filter") TcmCompounds tcmCompounds,
                                                    @Param("lastId") Long lastId,
                                                    @Param("pageSize") int pageSize);
}
