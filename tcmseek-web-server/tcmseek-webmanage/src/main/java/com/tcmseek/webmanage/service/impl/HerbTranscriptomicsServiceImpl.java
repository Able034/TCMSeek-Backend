package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.HerbTranscriptomicsMapper;
import com.tcmseek.webmanage.domain.HerbTranscriptomics;
import com.tcmseek.webmanage.service.IHerbTranscriptomicsService;

/**
 * 中药材转录组学数据Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class HerbTranscriptomicsServiceImpl implements IHerbTranscriptomicsService 
{
    @Autowired
    private HerbTranscriptomicsMapper herbTranscriptomicsMapper;

    /**
     * 查询中药材转录组学数据
     * 
     * @param id 中药材转录组学数据主键
     * @return 中药材转录组学数据
     */
    @Override
    public HerbTranscriptomics selectHerbTranscriptomicsById(Long id)
    {
        return herbTranscriptomicsMapper.selectHerbTranscriptomicsById(id);
    }

    /**
     * 查询中药材转录组学数据列表
     * 
     * @param herbTranscriptomics 中药材转录组学数据
     * @return 中药材转录组学数据
     */
    @Override
    public List<HerbTranscriptomics> selectHerbTranscriptomicsList(HerbTranscriptomics herbTranscriptomics)
    {
        return herbTranscriptomicsMapper.selectHerbTranscriptomicsList(herbTranscriptomics);
    }

    /**
     * 新增中药材转录组学数据
     * 
     * @param herbTranscriptomics 中药材转录组学数据
     * @return 结果
     */
    @Override
    public int insertHerbTranscriptomics(HerbTranscriptomics herbTranscriptomics)
    {
        return herbTranscriptomicsMapper.insertHerbTranscriptomics(herbTranscriptomics);
    }

    /**
     * 修改中药材转录组学数据
     * 
     * @param herbTranscriptomics 中药材转录组学数据
     * @return 结果
     */
    @Override
    public int updateHerbTranscriptomics(HerbTranscriptomics herbTranscriptomics)
    {
        return herbTranscriptomicsMapper.updateHerbTranscriptomics(herbTranscriptomics);
    }

    /**
     * 批量删除中药材转录组学数据
     * 
     * @param ids 需要删除的中药材转录组学数据主键
     * @return 结果
     */
    @Override
    public int deleteHerbTranscriptomicsByIds(Long[] ids)
    {
        return herbTranscriptomicsMapper.deleteHerbTranscriptomicsByIds(ids);
    }

    /**
     * 删除中药材转录组学数据信息
     * 
     * @param id 中药材转录组学数据主键
     * @return 结果
     */
    @Override
    public int deleteHerbTranscriptomicsById(Long id)
    {
        return herbTranscriptomicsMapper.deleteHerbTranscriptomicsById(id);
    }
}
