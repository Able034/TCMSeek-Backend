package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.PrescriptionTranscriptomicsMapper;
import com.tcmseek.webmanage.domain.PrescriptionTranscriptomics;
import com.tcmseek.webmanage.service.IPrescriptionTranscriptomicsService;

/**
 * 方剂转录组学数据Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class PrescriptionTranscriptomicsServiceImpl implements IPrescriptionTranscriptomicsService 
{
    @Autowired
    private PrescriptionTranscriptomicsMapper prescriptionTranscriptomicsMapper;

    /**
     * 查询方剂转录组学数据
     * 
     * @param id 方剂转录组学数据主键
     * @return 方剂转录组学数据
     */
    @Override
    public PrescriptionTranscriptomics selectPrescriptionTranscriptomicsById(Long id)
    {
        return prescriptionTranscriptomicsMapper.selectPrescriptionTranscriptomicsById(id);
    }

    /**
     * 查询方剂转录组学数据列表
     * 
     * @param prescriptionTranscriptomics 方剂转录组学数据
     * @return 方剂转录组学数据
     */
    @Override
    public List<PrescriptionTranscriptomics> selectPrescriptionTranscriptomicsList(PrescriptionTranscriptomics prescriptionTranscriptomics)
    {
        return prescriptionTranscriptomicsMapper.selectPrescriptionTranscriptomicsList(prescriptionTranscriptomics);
    }

    /**
     * 新增方剂转录组学数据
     * 
     * @param prescriptionTranscriptomics 方剂转录组学数据
     * @return 结果
     */
    @Override
    public int insertPrescriptionTranscriptomics(PrescriptionTranscriptomics prescriptionTranscriptomics)
    {
        return prescriptionTranscriptomicsMapper.insertPrescriptionTranscriptomics(prescriptionTranscriptomics);
    }

    /**
     * 修改方剂转录组学数据
     * 
     * @param prescriptionTranscriptomics 方剂转录组学数据
     * @return 结果
     */
    @Override
    public int updatePrescriptionTranscriptomics(PrescriptionTranscriptomics prescriptionTranscriptomics)
    {
        return prescriptionTranscriptomicsMapper.updatePrescriptionTranscriptomics(prescriptionTranscriptomics);
    }

    /**
     * 批量删除方剂转录组学数据
     * 
     * @param ids 需要删除的方剂转录组学数据主键
     * @return 结果
     */
    @Override
    public int deletePrescriptionTranscriptomicsByIds(Long[] ids)
    {
        return prescriptionTranscriptomicsMapper.deletePrescriptionTranscriptomicsByIds(ids);
    }

    /**
     * 删除方剂转录组学数据信息
     * 
     * @param id 方剂转录组学数据主键
     * @return 结果
     */
    @Override
    public int deletePrescriptionTranscriptomicsById(Long id)
    {
        return prescriptionTranscriptomicsMapper.deletePrescriptionTranscriptomicsById(id);
    }
}
