package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.HerbSymptomRel;

/**
 * 中药-中医症状关联Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface HerbSymptomRelMapper 
{
    /**
     * 查询中药-中医症状关联
     * 
     * @param id 中药-中医症状关联主键
     * @return 中药-中医症状关联
     */
    public HerbSymptomRel selectHerbSymptomRelById(Long id);

    /**
     * 查询中药-中医症状关联列表
     * 
     * @param herbSymptomRel 中药-中医症状关联
     * @return 中药-中医症状关联集合
     */
    public List<HerbSymptomRel> selectHerbSymptomRelList(HerbSymptomRel herbSymptomRel);

    /**
     * 新增中药-中医症状关联
     * 
     * @param herbSymptomRel 中药-中医症状关联
     * @return 结果
     */
    public int insertHerbSymptomRel(HerbSymptomRel herbSymptomRel);

    /**
     * 修改中药-中医症状关联
     * 
     * @param herbSymptomRel 中药-中医症状关联
     * @return 结果
     */
    public int updateHerbSymptomRel(HerbSymptomRel herbSymptomRel);

    /**
     * 删除中药-中医症状关联
     * 
     * @param id 中药-中医症状关联主键
     * @return 结果
     */
    public int deleteHerbSymptomRelById(Long id);

    /**
     * 批量删除中药-中医症状关联
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteHerbSymptomRelByIds(Long[] ids);
}
