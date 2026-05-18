package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.TcmWmSymptomRel;

/**
 * 中医症状-西医症状关联Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface TcmWmSymptomRelMapper 
{
    /**
     * 查询中医症状-西医症状关联
     * 
     * @param id 中医症状-西医症状关联主键
     * @return 中医症状-西医症状关联
     */
    public TcmWmSymptomRel selectTcmWmSymptomRelById(Long id);

    /**
     * 查询中医症状-西医症状关联列表
     * 
     * @param tcmWmSymptomRel 中医症状-西医症状关联
     * @return 中医症状-西医症状关联集合
     */
    public List<TcmWmSymptomRel> selectTcmWmSymptomRelList(TcmWmSymptomRel tcmWmSymptomRel);

    /**
     * 新增中医症状-西医症状关联
     * 
     * @param tcmWmSymptomRel 中医症状-西医症状关联
     * @return 结果
     */
    public int insertTcmWmSymptomRel(TcmWmSymptomRel tcmWmSymptomRel);

    /**
     * 修改中医症状-西医症状关联
     * 
     * @param tcmWmSymptomRel 中医症状-西医症状关联
     * @return 结果
     */
    public int updateTcmWmSymptomRel(TcmWmSymptomRel tcmWmSymptomRel);

    /**
     * 删除中医症状-西医症状关联
     * 
     * @param id 中医症状-西医症状关联主键
     * @return 结果
     */
    public int deleteTcmWmSymptomRelById(Long id);

    /**
     * 批量删除中医症状-西医症状关联
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTcmWmSymptomRelByIds(Long[] ids);
}
