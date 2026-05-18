package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.CompoundAdmet;
import org.apache.ibatis.annotations.Param;

/**
 * 化合物ADMET预测结果Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface CompoundAdmetMapper 
{
    /**
     * 查询化合物ADMET预测结果
     * 
     * @param id 化合物ADMET预测结果主键
     * @return 化合物ADMET预测结果
     */
    public CompoundAdmet selectCompoundAdmetById(Long id);

    /**
     * 查询化合物ADMET预测结果列表
     * 
     * @param compoundAdmet 化合物ADMET预测结果
     * @return 化合物ADMET预测结果集合
     */
    public List<CompoundAdmet> selectCompoundAdmetList(CompoundAdmet compoundAdmet);

    /**
     * 新增化合物ADMET预测结果
     * 
     * @param compoundAdmet 化合物ADMET预测结果
     * @return 结果
     */
    public int insertCompoundAdmet(CompoundAdmet compoundAdmet);

    /**
     * 修改化合物ADMET预测结果
     * 
     * @param compoundAdmet 化合物ADMET预测结果
     * @return 结果
     */
    public int updateCompoundAdmet(CompoundAdmet compoundAdmet);

    /**
     * 删除化合物ADMET预测结果
     * 
     * @param id 化合物ADMET预测结果主键
     * @return 结果
     */
    public int deleteCompoundAdmetById(Long id);

    /**
     * 批量删除化合物ADMET预测结果
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompoundAdmetByIds(Long[] ids);

    /**
     * 根据 inchikey 集合查询已存在的 ADMET 记录
     *
     * @param inchikeys inchikey 列表
     * @return 已存在的记录
     */
    List<CompoundAdmet> selectByInchikeys(@Param("inchikeys") List<String> inchikeys);

    /**
     * 批量新增化合物ADMET预测结果
     *
     * @param list 数据集合
     * @return 影响行数
     */
    int batchInsertCompoundAdmet(@Param("list") List<CompoundAdmet> list);
}
