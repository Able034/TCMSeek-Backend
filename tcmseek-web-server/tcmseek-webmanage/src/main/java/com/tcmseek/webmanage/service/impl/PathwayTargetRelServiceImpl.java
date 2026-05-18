package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.PathwayTargetRelMapper;
import com.tcmseek.webmanage.domain.PathwayTargetRel;
import com.tcmseek.webmanage.service.IPathwayTargetRelService;

/**
 * 通路-靶标关联Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class PathwayTargetRelServiceImpl implements IPathwayTargetRelService 
{
    @Autowired
    private PathwayTargetRelMapper pathwayTargetRelMapper;

    /**
     * 查询通路-靶标关联
     * 
     * @param id 通路-靶标关联主键
     * @return 通路-靶标关联
     */
    @Override
    public PathwayTargetRel selectPathwayTargetRelById(Long id)
    {
        return pathwayTargetRelMapper.selectPathwayTargetRelById(id);
    }

    /**
     * 查询通路-靶标关联列表
     * 
     * @param pathwayTargetRel 通路-靶标关联
     * @return 通路-靶标关联
     */
    @Override
    public List<PathwayTargetRel> selectPathwayTargetRelList(PathwayTargetRel pathwayTargetRel)
    {
        return pathwayTargetRelMapper.selectPathwayTargetRelList(pathwayTargetRel);
    }

    /**
     * 新增通路-靶标关联
     * 
     * @param pathwayTargetRel 通路-靶标关联
     * @return 结果
     */
    @Override
    public int insertPathwayTargetRel(PathwayTargetRel pathwayTargetRel)
    {
        return pathwayTargetRelMapper.insertPathwayTargetRel(pathwayTargetRel);
    }

    /**
     * 修改通路-靶标关联
     * 
     * @param pathwayTargetRel 通路-靶标关联
     * @return 结果
     */
    @Override
    public int updatePathwayTargetRel(PathwayTargetRel pathwayTargetRel)
    {
        return pathwayTargetRelMapper.updatePathwayTargetRel(pathwayTargetRel);
    }

    /**
     * 批量删除通路-靶标关联
     * 
     * @param ids 需要删除的通路-靶标关联主键
     * @return 结果
     */
    @Override
    public int deletePathwayTargetRelByIds(Long[] ids)
    {
        return pathwayTargetRelMapper.deletePathwayTargetRelByIds(ids);
    }

    /**
     * 删除通路-靶标关联信息
     * 
     * @param id 通路-靶标关联主键
     * @return 结果
     */
    @Override
    public int deletePathwayTargetRelById(Long id)
    {
        return pathwayTargetRelMapper.deletePathwayTargetRelById(id);
    }
}
