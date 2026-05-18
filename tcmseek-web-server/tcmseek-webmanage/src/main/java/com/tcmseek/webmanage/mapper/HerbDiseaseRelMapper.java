package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.HerbDiseaseRel;

/**
 * 中药-疾病关联Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface HerbDiseaseRelMapper 
{
    /**
     * 查询中药-疾病关联
     * 
     * @param id 中药-疾病关联主键
     * @return 中药-疾病关联
     */
    public HerbDiseaseRel selectHerbDiseaseRelById(Long id);

    /**
     * 查询中药-疾病关联列表
     * 
     * @param herbDiseaseRel 中药-疾病关联
     * @return 中药-疾病关联集合
     */
    public List<HerbDiseaseRel> selectHerbDiseaseRelList(HerbDiseaseRel herbDiseaseRel);

    /**
     * 新增中药-疾病关联
     * 
     * @param herbDiseaseRel 中药-疾病关联
     * @return 结果
     */
    public int insertHerbDiseaseRel(HerbDiseaseRel herbDiseaseRel);

    /**
     * 修改中药-疾病关联
     * 
     * @param herbDiseaseRel 中药-疾病关联
     * @return 结果
     */
    public int updateHerbDiseaseRel(HerbDiseaseRel herbDiseaseRel);

    /**
     * 删除中药-疾病关联
     * 
     * @param id 中药-疾病关联主键
     * @return 结果
     */
    public int deleteHerbDiseaseRelById(Long id);

    /**
     * 批量删除中药-疾病关联
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteHerbDiseaseRelByIds(Long[] ids);
}
