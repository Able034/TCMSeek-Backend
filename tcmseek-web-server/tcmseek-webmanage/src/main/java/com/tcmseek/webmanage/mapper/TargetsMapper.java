package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.Targets;

/**
 * 靶标/基因信息Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface TargetsMapper 
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
     * 删除靶标/基因信息
     * 
     * @param id 靶标/基因信息主键
     * @return 结果
     */
    public int deleteTargetsById(Long id);

    /**
     * 批量删除靶标/基因信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTargetsByIds(Long[] ids);
}
