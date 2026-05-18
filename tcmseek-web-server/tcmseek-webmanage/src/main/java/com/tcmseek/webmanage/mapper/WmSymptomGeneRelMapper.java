package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.WmSymptomGeneRel;

/**
 * 西医症状-基因关联Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface WmSymptomGeneRelMapper 
{
    /**
     * 查询西医症状-基因关联
     * 
     * @param id 西医症状-基因关联主键
     * @return 西医症状-基因关联
     */
    public WmSymptomGeneRel selectWmSymptomGeneRelById(Long id);

    /**
     * 查询西医症状-基因关联列表
     * 
     * @param wmSymptomGeneRel 西医症状-基因关联
     * @return 西医症状-基因关联集合
     */
    public List<WmSymptomGeneRel> selectWmSymptomGeneRelList(WmSymptomGeneRel wmSymptomGeneRel);

    /**
     * 新增西医症状-基因关联
     * 
     * @param wmSymptomGeneRel 西医症状-基因关联
     * @return 结果
     */
    public int insertWmSymptomGeneRel(WmSymptomGeneRel wmSymptomGeneRel);

    /**
     * 修改西医症状-基因关联
     * 
     * @param wmSymptomGeneRel 西医症状-基因关联
     * @return 结果
     */
    public int updateWmSymptomGeneRel(WmSymptomGeneRel wmSymptomGeneRel);

    /**
     * 删除西医症状-基因关联
     * 
     * @param id 西医症状-基因关联主键
     * @return 结果
     */
    public int deleteWmSymptomGeneRelById(Long id);

    /**
     * 批量删除西医症状-基因关联
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWmSymptomGeneRelByIds(Long[] ids);
}
