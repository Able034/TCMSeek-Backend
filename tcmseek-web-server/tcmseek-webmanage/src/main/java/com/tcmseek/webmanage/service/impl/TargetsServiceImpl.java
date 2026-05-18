package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.TargetsMapper;
import com.tcmseek.webmanage.domain.Targets;
import com.tcmseek.webmanage.service.ITargetsService;

/**
 * 靶标/基因信息Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class TargetsServiceImpl implements ITargetsService 
{
    @Autowired
    private TargetsMapper targetsMapper;

    /**
     * 查询靶标/基因信息
     * 
     * @param id 靶标/基因信息主键
     * @return 靶标/基因信息
     */
    @Override
    public Targets selectTargetsById(Long id)
    {
        return targetsMapper.selectTargetsById(id);
    }

    /**
     * 查询靶标/基因信息列表
     * 
     * @param targets 靶标/基因信息
     * @return 靶标/基因信息
     */
    @Override
    public List<Targets> selectTargetsList(Targets targets)
    {
        return targetsMapper.selectTargetsList(targets);
    }

    /**
     * 新增靶标/基因信息
     * 
     * @param targets 靶标/基因信息
     * @return 结果
     */
    @Override
    public int insertTargets(Targets targets)
    {
        return targetsMapper.insertTargets(targets);
    }

    /**
     * 修改靶标/基因信息
     * 
     * @param targets 靶标/基因信息
     * @return 结果
     */
    @Override
    public int updateTargets(Targets targets)
    {
        return targetsMapper.updateTargets(targets);
    }

    /**
     * 批量删除靶标/基因信息
     * 
     * @param ids 需要删除的靶标/基因信息主键
     * @return 结果
     */
    @Override
    public int deleteTargetsByIds(Long[] ids)
    {
        return targetsMapper.deleteTargetsByIds(ids);
    }

    /**
     * 删除靶标/基因信息信息
     * 
     * @param id 靶标/基因信息主键
     * @return 结果
     */
    @Override
    public int deleteTargetsById(Long id)
    {
        return targetsMapper.deleteTargetsById(id);
    }
}
