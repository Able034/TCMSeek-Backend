package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.PrescriptionTranscriptomics;

/**
 * 方剂转录组学数据Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface PrescriptionTranscriptomicsMapper 
{
    /**
     * 查询方剂转录组学数据
     * 
     * @param id 方剂转录组学数据主键
     * @return 方剂转录组学数据
     */
    public PrescriptionTranscriptomics selectPrescriptionTranscriptomicsById(Long id);

    /**
     * 查询方剂转录组学数据列表
     * 
     * @param prescriptionTranscriptomics 方剂转录组学数据
     * @return 方剂转录组学数据集合
     */
    public List<PrescriptionTranscriptomics> selectPrescriptionTranscriptomicsList(PrescriptionTranscriptomics prescriptionTranscriptomics);

    /**
     * 新增方剂转录组学数据
     * 
     * @param prescriptionTranscriptomics 方剂转录组学数据
     * @return 结果
     */
    public int insertPrescriptionTranscriptomics(PrescriptionTranscriptomics prescriptionTranscriptomics);

    /**
     * 修改方剂转录组学数据
     * 
     * @param prescriptionTranscriptomics 方剂转录组学数据
     * @return 结果
     */
    public int updatePrescriptionTranscriptomics(PrescriptionTranscriptomics prescriptionTranscriptomics);

    /**
     * 删除方剂转录组学数据
     * 
     * @param id 方剂转录组学数据主键
     * @return 结果
     */
    public int deletePrescriptionTranscriptomicsById(Long id);

    /**
     * 批量删除方剂转录组学数据
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePrescriptionTranscriptomicsByIds(Long[] ids);
}
