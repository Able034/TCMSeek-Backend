package com.tcmseek.webmanage.service;

import java.util.List;
import com.tcmseek.webmanage.domain.SyndromeGeneRel;

/**
 * 证候-基因关联Service接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface ISyndromeGeneRelService 
{
    /**
     * 查询证候-基因关联
     * 
     * @param id 证候-基因关联主键
     * @return 证候-基因关联
     */
    public SyndromeGeneRel selectSyndromeGeneRelById(Long id);

    /**
     * 查询证候-基因关联列表
     * 
     * @param syndromeGeneRel 证候-基因关联
     * @return 证候-基因关联集合
     */
    public List<SyndromeGeneRel> selectSyndromeGeneRelList(SyndromeGeneRel syndromeGeneRel);

    /**
     * 新增证候-基因关联
     * 
     * @param syndromeGeneRel 证候-基因关联
     * @return 结果
     */
    public int insertSyndromeGeneRel(SyndromeGeneRel syndromeGeneRel);

    /**
     * 修改证候-基因关联
     * 
     * @param syndromeGeneRel 证候-基因关联
     * @return 结果
     */
    public int updateSyndromeGeneRel(SyndromeGeneRel syndromeGeneRel);

    /**
     * 批量删除证候-基因关联
     * 
     * @param ids 需要删除的证候-基因关联主键集合
     * @return 结果
     */
    public int deleteSyndromeGeneRelByIds(Long[] ids);

    /**
     * 删除证候-基因关联信息
     * 
     * @param id 证候-基因关联主键
     * @return 结果
     */
    public int deleteSyndromeGeneRelById(Long id);
}
