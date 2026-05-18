package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.PathwayTargetRel;

/**
 * 通路-靶标关联Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface PathwayTargetRelMapper 
{
    /**
     * 查询通路-靶标关联
     * 
     * @param id 通路-靶标关联主键
     * @return 通路-靶标关联
     */
    public PathwayTargetRel selectPathwayTargetRelById(Long id);

    /**
     * 查询通路-靶标关联列表
     * 
     * @param pathwayTargetRel 通路-靶标关联
     * @return 通路-靶标关联集合
     */
    public List<PathwayTargetRel> selectPathwayTargetRelList(PathwayTargetRel pathwayTargetRel);

    /**
     * 新增通路-靶标关联
     * 
     * @param pathwayTargetRel 通路-靶标关联
     * @return 结果
     */
    public int insertPathwayTargetRel(PathwayTargetRel pathwayTargetRel);

    /**
     * 修改通路-靶标关联
     * 
     * @param pathwayTargetRel 通路-靶标关联
     * @return 结果
     */
    public int updatePathwayTargetRel(PathwayTargetRel pathwayTargetRel);

    /**
     * 删除通路-靶标关联
     * 
     * @param id 通路-靶标关联主键
     * @return 结果
     */
    public int deletePathwayTargetRelById(Long id);

    /**
     * 批量删除通路-靶标关联
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePathwayTargetRelByIds(Long[] ids);
}
