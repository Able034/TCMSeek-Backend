package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.PhenotypesMapper;
import com.tcmseek.webmanage.domain.Phenotypes;
import com.tcmseek.webmanage.service.IPhenotypesService;

/**
 * 表型信息Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class PhenotypesServiceImpl implements IPhenotypesService 
{
    @Autowired
    private PhenotypesMapper phenotypesMapper;

    /**
     * 查询表型信息
     * 
     * @param id 表型信息主键
     * @return 表型信息
     */
    @Override
    public Phenotypes selectPhenotypesById(Long id)
    {
        return phenotypesMapper.selectPhenotypesById(id);
    }

    /**
     * 查询表型信息列表
     * 
     * @param phenotypes 表型信息
     * @return 表型信息
     */
    @Override
    public List<Phenotypes> selectPhenotypesList(Phenotypes phenotypes)
    {
        return phenotypesMapper.selectPhenotypesList(phenotypes);
    }

    /**
     * 新增表型信息
     * 
     * @param phenotypes 表型信息
     * @return 结果
     */
    @Override
    public int insertPhenotypes(Phenotypes phenotypes)
    {
        return phenotypesMapper.insertPhenotypes(phenotypes);
    }

    /**
     * 修改表型信息
     * 
     * @param phenotypes 表型信息
     * @return 结果
     */
    @Override
    public int updatePhenotypes(Phenotypes phenotypes)
    {
        return phenotypesMapper.updatePhenotypes(phenotypes);
    }

    /**
     * 批量删除表型信息
     * 
     * @param ids 需要删除的表型信息主键
     * @return 结果
     */
    @Override
    public int deletePhenotypesByIds(Long[] ids)
    {
        return phenotypesMapper.deletePhenotypesByIds(ids);
    }

    /**
     * 删除表型信息信息
     * 
     * @param id 表型信息主键
     * @return 结果
     */
    @Override
    public int deletePhenotypesById(Long id)
    {
        return phenotypesMapper.deletePhenotypesById(id);
    }
}
