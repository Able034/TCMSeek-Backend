package com.tcmseek.webmanage.service;

import java.util.List;
import com.tcmseek.webmanage.domain.Targets;

/**
 * 靶标/基因信息Service接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface ITargetsService 
{
    /**
     * 查询靶标/基因信息
     * 
     * @param id 靶标/基因信息主键
     * @return 靶标/基因信息
     */
    public Targets selectTargetsById(Long id);

    /**
     * 查询靶标/基因信息列表
     * 
     * @param targets 靶标/基因信息
     * @return 靶标/基因信息集合
     */
    public List<Targets> selectTargetsList(Targets targets);

    /**
     * 新增靶标/基因信息
     * 
     * @param targets 靶标/基因信息
     * @return 结果
     */
    public int insertTargets(Targets targets);

    /**
     * 修改靶标/基因信息
     * 
     * @param targets 靶标/基因信息
     * @return 结果
     */
    public int updateTargets(Targets targets);

    /**
     * 批量删除靶标/基因信息
     * 
     * @param ids 需要删除的靶标/基因信息主键集合
     * @return 结果
     */
    public int deleteTargetsByIds(Long[] ids);

    /**
     * 删除靶标/基因信息信息
     * 
     * @param id 靶标/基因信息主键
     * @return 结果
     */
    public int deleteTargetsById(Long id);
}
