package com.tcmseek.webmanage.service;

import java.util.List;
import com.tcmseek.webmanage.domain.HerbTranscriptomics;

/**
 * 中药材转录组学数据Service接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface IHerbTranscriptomicsService 
{
    /**
     * 查询中药材转录组学数据
     * 
     * @param id 中药材转录组学数据主键
     * @return 中药材转录组学数据
     */
    public HerbTranscriptomics selectHerbTranscriptomicsById(Long id);

    /**
     * 查询中药材转录组学数据列表
     * 
     * @param herbTranscriptomics 中药材转录组学数据
     * @return 中药材转录组学数据集合
     */
    public List<HerbTranscriptomics> selectHerbTranscriptomicsList(HerbTranscriptomics herbTranscriptomics);

    /**
     * 新增中药材转录组学数据
     * 
     * @param herbTranscriptomics 中药材转录组学数据
     * @return 结果
     */
    public int insertHerbTranscriptomics(HerbTranscriptomics herbTranscriptomics);

    /**
     * 修改中药材转录组学数据
     * 
     * @param herbTranscriptomics 中药材转录组学数据
     * @return 结果
     */
    public int updateHerbTranscriptomics(HerbTranscriptomics herbTranscriptomics);

    /**
     * 批量删除中药材转录组学数据
     * 
     * @param ids 需要删除的中药材转录组学数据主键集合
     * @return 结果
     */
    public int deleteHerbTranscriptomicsByIds(Long[] ids);

    /**
     * 删除中药材转录组学数据信息
     * 
     * @param id 中药材转录组学数据主键
     * @return 结果
     */
    public int deleteHerbTranscriptomicsById(Long id);
}
