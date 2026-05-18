package com.tcmseek.webmanage.service;

import java.util.List;
import com.tcmseek.webmanage.domain.HerbCompoundRel;

/**
 * 中药-化合物关联Service接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface IHerbCompoundRelService 
{
    /**
     * 查询中药-化合物关联
     * 
     * @param id 中药-化合物关联主键
     * @return 中药-化合物关联
     */
    public HerbCompoundRel selectHerbCompoundRelById(Long id);

    /**
     * 查询中药-化合物关联列表
     * 
     * @param herbCompoundRel 中药-化合物关联
     * @return 中药-化合物关联集合
     */
    public List<HerbCompoundRel> selectHerbCompoundRelList(HerbCompoundRel herbCompoundRel);

    /**
     * 新增中药-化合物关联
     * 
     * @param herbCompoundRel 中药-化合物关联
     * @return 结果
     */
    public int insertHerbCompoundRel(HerbCompoundRel herbCompoundRel);

    /**
     * 修改中药-化合物关联
     * 
     * @param herbCompoundRel 中药-化合物关联
     * @return 结果
     */
    public int updateHerbCompoundRel(HerbCompoundRel herbCompoundRel);

    /**
     * 批量删除中药-化合物关联
     * 
     * @param ids 需要删除的中药-化合物关联主键集合
     * @return 结果
     */
    public int deleteHerbCompoundRelByIds(Long[] ids);

    /**
     * 删除中药-化合物关联信息
     * 
     * @param id 中药-化合物关联主键
     * @return 结果
     */
    public int deleteHerbCompoundRelById(Long id);
}
