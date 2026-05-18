package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.PrescriptionDiseaseRel;

/**
 * 方剂-疾病关联Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface PrescriptionDiseaseRelMapper 
{
    /**
     * 查询方剂-疾病关联
     * 
     * @param id 方剂-疾病关联主键
     * @return 方剂-疾病关联
     */
    public PrescriptionDiseaseRel selectPrescriptionDiseaseRelById(Long id);

    /**
     * 查询方剂-疾病关联列表
     * 
     * @param prescriptionDiseaseRel 方剂-疾病关联
     * @return 方剂-疾病关联集合
     */
    public List<PrescriptionDiseaseRel> selectPrescriptionDiseaseRelList(PrescriptionDiseaseRel prescriptionDiseaseRel);

    /**
     * 新增方剂-疾病关联
     * 
     * @param prescriptionDiseaseRel 方剂-疾病关联
     * @return 结果
     */
    public int insertPrescriptionDiseaseRel(PrescriptionDiseaseRel prescriptionDiseaseRel);

    /**
     * 修改方剂-疾病关联
     * 
     * @param prescriptionDiseaseRel 方剂-疾病关联
     * @return 结果
     */
    public int updatePrescriptionDiseaseRel(PrescriptionDiseaseRel prescriptionDiseaseRel);

    /**
     * 删除方剂-疾病关联
     * 
     * @param id 方剂-疾病关联主键
     * @return 结果
     */
    public int deletePrescriptionDiseaseRelById(Long id);

    /**
     * 批量删除方剂-疾病关联
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePrescriptionDiseaseRelByIds(Long[] ids);
}
