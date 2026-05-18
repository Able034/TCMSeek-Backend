package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.SyndromeSymptomRel;

/**
 * 证候-中医症状关联Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface SyndromeSymptomRelMapper 
{
    /**
     * 查询证候-中医症状关联
     * 
     * @param id 证候-中医症状关联主键
     * @return 证候-中医症状关联
     */
    public SyndromeSymptomRel selectSyndromeSymptomRelById(Long id);

    /**
     * 查询证候-中医症状关联列表
     * 
     * @param syndromeSymptomRel 证候-中医症状关联
     * @return 证候-中医症状关联集合
     */
    public List<SyndromeSymptomRel> selectSyndromeSymptomRelList(SyndromeSymptomRel syndromeSymptomRel);

    /**
     * 新增证候-中医症状关联
     * 
     * @param syndromeSymptomRel 证候-中医症状关联
     * @return 结果
     */
    public int insertSyndromeSymptomRel(SyndromeSymptomRel syndromeSymptomRel);

    /**
     * 修改证候-中医症状关联
     * 
     * @param syndromeSymptomRel 证候-中医症状关联
     * @return 结果
     */
    public int updateSyndromeSymptomRel(SyndromeSymptomRel syndromeSymptomRel);

    /**
     * 删除证候-中医症状关联
     * 
     * @param id 证候-中医症状关联主键
     * @return 结果
     */
    public int deleteSyndromeSymptomRelById(Long id);

    /**
     * 批量删除证候-中医症状关联
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSyndromeSymptomRelByIds(Long[] ids);
}
