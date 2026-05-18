package com.tcmseek.webmanage.service;

import java.util.List;
import com.tcmseek.webmanage.domain.HerbSyndromeRel;

/**
 * 中药-证候关联Service接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface IHerbSyndromeRelService 
{
    /**
     * 查询中药-证候关联
     * 
     * @param id 中药-证候关联主键
     * @return 中药-证候关联
     */
    public HerbSyndromeRel selectHerbSyndromeRelById(Long id);

    /**
     * 查询中药-证候关联列表
     * 
     * @param herbSyndromeRel 中药-证候关联
     * @return 中药-证候关联集合
     */
    public List<HerbSyndromeRel> selectHerbSyndromeRelList(HerbSyndromeRel herbSyndromeRel);

    /**
     * 新增中药-证候关联
     * 
     * @param herbSyndromeRel 中药-证候关联
     * @return 结果
     */
    public int insertHerbSyndromeRel(HerbSyndromeRel herbSyndromeRel);

    /**
     * 修改中药-证候关联
     * 
     * @param herbSyndromeRel 中药-证候关联
     * @return 结果
     */
    public int updateHerbSyndromeRel(HerbSyndromeRel herbSyndromeRel);

    /**
     * 批量删除中药-证候关联
     * 
     * @param ids 需要删除的中药-证候关联主键集合
     * @return 结果
     */
    public int deleteHerbSyndromeRelByIds(Long[] ids);

    /**
     * 删除中药-证候关联信息
     * 
     * @param id 中药-证候关联主键
     * @return 结果
     */
    public int deleteHerbSyndromeRelById(Long id);
}
