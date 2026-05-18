package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.Pathways;

/**
 * 通路信息Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface PathwaysMapper 
{
    /**
     * 查询通路信息
     * 
     * @param id 通路信息主键
     * @return 通路信息
     */
    public Pathways selectPathwaysById(Long id);

    /**
     * 查询通路信息列表
     * 
     * @param pathways 通路信息
     * @return 通路信息集合
     */
    public List<Pathways> selectPathwaysList(Pathways pathways);

    /**
     * 新增通路信息
     * 
     * @param pathways 通路信息
     * @return 结果
     */
    public int insertPathways(Pathways pathways);

    /**
     * 修改通路信息
     * 
     * @param pathways 通路信息
     * @return 结果
     */
    public int updatePathways(Pathways pathways);

    /**
     * 删除通路信息
     * 
     * @param id 通路信息主键
     * @return 结果
     */
    public int deletePathwaysById(Long id);

    /**
     * 批量删除通路信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePathwaysByIds(Long[] ids);
}
