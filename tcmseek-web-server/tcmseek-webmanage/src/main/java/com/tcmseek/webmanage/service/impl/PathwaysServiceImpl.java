package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.PathwaysMapper;
import com.tcmseek.webmanage.domain.Pathways;
import com.tcmseek.webmanage.service.IPathwaysService;

/**
 * 通路信息Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class PathwaysServiceImpl implements IPathwaysService 
{
    @Autowired
    private PathwaysMapper pathwaysMapper;

    /**
     * 查询通路信息
     * 
     * @param id 通路信息主键
     * @return 通路信息
     */
    @Override
    public Pathways selectPathwaysById(Long id)
    {
        return pathwaysMapper.selectPathwaysById(id);
    }

    /**
     * 查询通路信息列表
     * 
     * @param pathways 通路信息
     * @return 通路信息
     */
    @Override
    public List<Pathways> selectPathwaysList(Pathways pathways)
    {
        return pathwaysMapper.selectPathwaysList(pathways);
    }

    /**
     * 新增通路信息
     * 
     * @param pathways 通路信息
     * @return 结果
     */
    @Override
    public int insertPathways(Pathways pathways)
    {
        return pathwaysMapper.insertPathways(pathways);
    }

    /**
     * 修改通路信息
     * 
     * @param pathways 通路信息
     * @return 结果
     */
    @Override
    public int updatePathways(Pathways pathways)
    {
        return pathwaysMapper.updatePathways(pathways);
    }

    /**
     * 批量删除通路信息
     * 
     * @param ids 需要删除的通路信息主键
     * @return 结果
     */
    @Override
    public int deletePathwaysByIds(Long[] ids)
    {
        return pathwaysMapper.deletePathwaysByIds(ids);
    }

    /**
     * 删除通路信息信息
     * 
     * @param id 通路信息主键
     * @return 结果
     */
    @Override
    public int deletePathwaysById(Long id)
    {
        return pathwaysMapper.deletePathwaysById(id);
    }
}
