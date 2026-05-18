package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.PhenotypeTargetRelMapper;
import com.tcmseek.webmanage.domain.PhenotypeTargetRel;
import com.tcmseek.webmanage.service.IPhenotypeTargetRelService;

/**
 * 型-靶标关联Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class PhenotypeTargetRelServiceImpl implements IPhenotypeTargetRelService 
{
    @Autowired
    private PhenotypeTargetRelMapper phenotypeTargetRelMapper;

    /**
     * 查询型-靶标关联
     * 
     * @param id 型-靶标关联主键
     * @return 型-靶标关联
     */
    @Override
    public PhenotypeTargetRel selectPhenotypeTargetRelById(Long id)
    {
        return phenotypeTargetRelMapper.selectPhenotypeTargetRelById(id);
    }

    /**
     * 查询型-靶标关联列表
     * 
     * @param phenotypeTargetRel 型-靶标关联
     * @return 型-靶标关联
     */
    @Override
    public List<PhenotypeTargetRel> selectPhenotypeTargetRelList(PhenotypeTargetRel phenotypeTargetRel)
    {
        return phenotypeTargetRelMapper.selectPhenotypeTargetRelList(phenotypeTargetRel);
    }

    /**
     * 新增型-靶标关联
     * 
     * @param phenotypeTargetRel 型-靶标关联
     * @return 结果
     */
    @Override
    public int insertPhenotypeTargetRel(PhenotypeTargetRel phenotypeTargetRel)
    {
        return phenotypeTargetRelMapper.insertPhenotypeTargetRel(phenotypeTargetRel);
    }

    /**
     * 修改型-靶标关联
     * 
     * @param phenotypeTargetRel 型-靶标关联
     * @return 结果
     */
    @Override
    public int updatePhenotypeTargetRel(PhenotypeTargetRel phenotypeTargetRel)
    {
        return phenotypeTargetRelMapper.updatePhenotypeTargetRel(phenotypeTargetRel);
    }

    /**
     * 批量删除型-靶标关联
     * 
     * @param ids 需要删除的型-靶标关联主键
     * @return 结果
     */
    @Override
    public int deletePhenotypeTargetRelByIds(Long[] ids)
    {
        return phenotypeTargetRelMapper.deletePhenotypeTargetRelByIds(ids);
    }

    /**
     * 删除型-靶标关联信息
     * 
     * @param id 型-靶标关联主键
     * @return 结果
     */
    @Override
    public int deletePhenotypeTargetRelById(Long id)
    {
        return phenotypeTargetRelMapper.deletePhenotypeTargetRelById(id);
    }
}
