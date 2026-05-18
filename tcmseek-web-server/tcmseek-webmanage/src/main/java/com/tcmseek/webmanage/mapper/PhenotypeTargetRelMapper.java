package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.PhenotypeTargetRel;

/**
 * 型-靶标关联Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface PhenotypeTargetRelMapper 
{
    /**
     * 查询型-靶标关联
     * 
     * @param id 型-靶标关联主键
     * @return 型-靶标关联
     */
    public PhenotypeTargetRel selectPhenotypeTargetRelById(Long id);

    /**
     * 查询型-靶标关联列表
     * 
     * @param phenotypeTargetRel 型-靶标关联
     * @return 型-靶标关联集合
     */
    public List<PhenotypeTargetRel> selectPhenotypeTargetRelList(PhenotypeTargetRel phenotypeTargetRel);

    /**
     * 新增型-靶标关联
     * 
     * @param phenotypeTargetRel 型-靶标关联
     * @return 结果
     */
    public int insertPhenotypeTargetRel(PhenotypeTargetRel phenotypeTargetRel);

    /**
     * 修改型-靶标关联
     * 
     * @param phenotypeTargetRel 型-靶标关联
     * @return 结果
     */
    public int updatePhenotypeTargetRel(PhenotypeTargetRel phenotypeTargetRel);

    /**
     * 删除型-靶标关联
     * 
     * @param id 型-靶标关联主键
     * @return 结果
     */
    public int deletePhenotypeTargetRelById(Long id);

    /**
     * 批量删除型-靶标关联
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePhenotypeTargetRelByIds(Long[] ids);
}
