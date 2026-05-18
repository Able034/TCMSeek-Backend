package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.PrescriptionDiseaseRelMapper;
import com.tcmseek.webmanage.domain.PrescriptionDiseaseRel;
import com.tcmseek.webmanage.service.IPrescriptionDiseaseRelService;

/**
 * 方剂-疾病关联Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class PrescriptionDiseaseRelServiceImpl implements IPrescriptionDiseaseRelService 
{
    @Autowired
    private PrescriptionDiseaseRelMapper prescriptionDiseaseRelMapper;

    /**
     * 查询方剂-疾病关联
     * 
     * @param id 方剂-疾病关联主键
     * @return 方剂-疾病关联
     */
    @Override
    public PrescriptionDiseaseRel selectPrescriptionDiseaseRelById(Long id)
    {
        return prescriptionDiseaseRelMapper.selectPrescriptionDiseaseRelById(id);
    }

    /**
     * 查询方剂-疾病关联列表
     * 
     * @param prescriptionDiseaseRel 方剂-疾病关联
     * @return 方剂-疾病关联
     */
    @Override
    public List<PrescriptionDiseaseRel> selectPrescriptionDiseaseRelList(PrescriptionDiseaseRel prescriptionDiseaseRel)
    {
        return prescriptionDiseaseRelMapper.selectPrescriptionDiseaseRelList(prescriptionDiseaseRel);
    }

    /**
     * 新增方剂-疾病关联
     * 
     * @param prescriptionDiseaseRel 方剂-疾病关联
     * @return 结果
     */
    @Override
    public int insertPrescriptionDiseaseRel(PrescriptionDiseaseRel prescriptionDiseaseRel)
    {
        return prescriptionDiseaseRelMapper.insertPrescriptionDiseaseRel(prescriptionDiseaseRel);
    }

    /**
     * 修改方剂-疾病关联
     * 
     * @param prescriptionDiseaseRel 方剂-疾病关联
     * @return 结果
     */
    @Override
    public int updatePrescriptionDiseaseRel(PrescriptionDiseaseRel prescriptionDiseaseRel)
    {
        return prescriptionDiseaseRelMapper.updatePrescriptionDiseaseRel(prescriptionDiseaseRel);
    }

    /**
     * 批量删除方剂-疾病关联
     * 
     * @param ids 需要删除的方剂-疾病关联主键
     * @return 结果
     */
    @Override
    public int deletePrescriptionDiseaseRelByIds(Long[] ids)
    {
        return prescriptionDiseaseRelMapper.deletePrescriptionDiseaseRelByIds(ids);
    }

    /**
     * 删除方剂-疾病关联信息
     * 
     * @param id 方剂-疾病关联主键
     * @return 结果
     */
    @Override
    public int deletePrescriptionDiseaseRelById(Long id)
    {
        return prescriptionDiseaseRelMapper.deletePrescriptionDiseaseRelById(id);
    }
}
