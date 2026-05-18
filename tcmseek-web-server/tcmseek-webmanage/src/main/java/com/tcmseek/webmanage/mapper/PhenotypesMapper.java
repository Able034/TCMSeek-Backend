package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.Phenotypes;

/**
 * 表型信息Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface PhenotypesMapper 
{
    /**
     * 查询表型信息
     * 
     * @param id 表型信息主键
     * @return 表型信息
     */
    public Phenotypes selectPhenotypesById(Long id);

    /**
     * 查询表型信息列表
     * 
     * @param phenotypes 表型信息
     * @return 表型信息集合
     */
    public List<Phenotypes> selectPhenotypesList(Phenotypes phenotypes);

    /**
     * 新增表型信息
     * 
     * @param phenotypes 表型信息
     * @return 结果
     */
    public int insertPhenotypes(Phenotypes phenotypes);

    /**
     * 修改表型信息
     * 
     * @param phenotypes 表型信息
     * @return 结果
     */
    public int updatePhenotypes(Phenotypes phenotypes);

    /**
     * 删除表型信息
     * 
     * @param id 表型信息主键
     * @return 结果
     */
    public int deletePhenotypesById(Long id);

    /**
     * 批量删除表型信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePhenotypesByIds(Long[] ids);
}
