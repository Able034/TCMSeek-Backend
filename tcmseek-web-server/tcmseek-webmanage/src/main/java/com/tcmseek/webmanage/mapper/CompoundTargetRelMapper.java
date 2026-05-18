package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.CompoundTargetRel;
import org.apache.ibatis.annotations.Param;

/**
 * 化合物-靶标关联Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface CompoundTargetRelMapper 
{
    /**
     * 查询化合物-靶标关联
     * 
     * @param id 化合物-靶标关联主键
     * @return 化合物-靶标关联
     */
    public CompoundTargetRel selectCompoundTargetRelById(Long id);

    /**
     * 查询化合物-靶标关联列表
     * 
     * @param compoundTargetRel 化合物-靶标关联
     * @return 化合物-靶标关联集合
     */
    public List<CompoundTargetRel> selectCompoundTargetRelList(CompoundTargetRel compoundTargetRel);

    /**
     * 新增化合物-靶标关联
     * 
     * @param compoundTargetRel 化合物-靶标关联
     * @return 结果
     */
    public int insertCompoundTargetRel(CompoundTargetRel compoundTargetRel);

    /**
     * 修改化合物-靶标关联
     * 
     * @param compoundTargetRel 化合物-靶标关联
     * @return 结果
     */
    public int updateCompoundTargetRel(CompoundTargetRel compoundTargetRel);

    /**
     * 删除化合物-靶标关联
     * 
     * @param id 化合物-靶标关联主键
     * @return 结果
     */
    public int deleteCompoundTargetRelById(Long id);

    /**
     * 批量删除化合物-靶标关联
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompoundTargetRelByIds(Long[] ids);

    /**
     * 按 inchikey+tcm_tar_id 组合键查询已存在的记录
     *
     * @param keys 组合键列表（inchikey_tcmTarId）
     * @return 已存在的记录
     */
    List<CompoundTargetRel> selectByCompositeKeys(@Param("keys") List<String> keys);

    /**
     * 批量新增化合物-靶标关联
     *
     * @param list 数据集合
     * @return 影响行数
     */
    int batchInsertCompoundTargetRel(@Param("list") List<CompoundTargetRel> list);
}
