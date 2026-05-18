package com.tcmseek.webmanage.service;

import java.util.List;
import com.tcmseek.webmanage.domain.DiseaseGeneRel;

/**
 * 疾病-基因关联Service接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface IDiseaseGeneRelService 
{
    /**
     * 查询疾病-基因关联
     * 
     * @param id 疾病-基因关联主键
     * @return 疾病-基因关联
     */
    public DiseaseGeneRel selectDiseaseGeneRelById(Long id);

    /**
     * 查询疾病-基因关联列表
     * 
     * @param diseaseGeneRel 疾病-基因关联
     * @return 疾病-基因关联集合
     */
    public List<DiseaseGeneRel> selectDiseaseGeneRelList(DiseaseGeneRel diseaseGeneRel);

    /**
     * 新增疾病-基因关联
     * 
     * @param diseaseGeneRel 疾病-基因关联
     * @return 结果
     */
    public int insertDiseaseGeneRel(DiseaseGeneRel diseaseGeneRel);

    /**
     * 修改疾病-基因关联
     * 
     * @param diseaseGeneRel 疾病-基因关联
     * @return 结果
     */
    public int updateDiseaseGeneRel(DiseaseGeneRel diseaseGeneRel);

    /**
     * 批量删除疾病-基因关联
     * 
     * @param ids 需要删除的疾病-基因关联主键集合
     * @return 结果
     */
    public int deleteDiseaseGeneRelByIds(Long[] ids);

    /**
     * 删除疾病-基因关联信息
     * 
     * @param id 疾病-基因关联主键
     * @return 结果
     */
    public int deleteDiseaseGeneRelById(Long id);
}
