package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.HerbDiseaseRelMapper;
import com.tcmseek.webmanage.domain.HerbDiseaseRel;
import com.tcmseek.webmanage.service.IHerbDiseaseRelService;

/**
 * 中药-疾病关联Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class HerbDiseaseRelServiceImpl implements IHerbDiseaseRelService 
{
    @Autowired
    private HerbDiseaseRelMapper herbDiseaseRelMapper;

    /**
     * 查询中药-疾病关联
     * 
     * @param id 中药-疾病关联主键
     * @return 中药-疾病关联
     */
    @Override
    public HerbDiseaseRel selectHerbDiseaseRelById(Long id)
    {
        return herbDiseaseRelMapper.selectHerbDiseaseRelById(id);
    }

    /**
     * 查询中药-疾病关联列表
     * 
     * @param herbDiseaseRel 中药-疾病关联
     * @return 中药-疾病关联
     */
    @Override
    public List<HerbDiseaseRel> selectHerbDiseaseRelList(HerbDiseaseRel herbDiseaseRel)
    {
        return herbDiseaseRelMapper.selectHerbDiseaseRelList(herbDiseaseRel);
    }

    /**
     * 新增中药-疾病关联
     * 
     * @param herbDiseaseRel 中药-疾病关联
     * @return 结果
     */
    @Override
    public int insertHerbDiseaseRel(HerbDiseaseRel herbDiseaseRel)
    {
        return herbDiseaseRelMapper.insertHerbDiseaseRel(herbDiseaseRel);
    }

    /**
     * 修改中药-疾病关联
     * 
     * @param herbDiseaseRel 中药-疾病关联
     * @return 结果
     */
    @Override
    public int updateHerbDiseaseRel(HerbDiseaseRel herbDiseaseRel)
    {
        return herbDiseaseRelMapper.updateHerbDiseaseRel(herbDiseaseRel);
    }

    /**
     * 批量删除中药-疾病关联
     * 
     * @param ids 需要删除的中药-疾病关联主键
     * @return 结果
     */
    @Override
    public int deleteHerbDiseaseRelByIds(Long[] ids)
    {
        return herbDiseaseRelMapper.deleteHerbDiseaseRelByIds(ids);
    }

    /**
     * 删除中药-疾病关联信息
     * 
     * @param id 中药-疾病关联主键
     * @return 结果
     */
    @Override
    public int deleteHerbDiseaseRelById(Long id)
    {
        return herbDiseaseRelMapper.deleteHerbDiseaseRelById(id);
    }
}
