package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.HerbTranscriptomics;

/**
 * 中药材转录组学数据Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface HerbTranscriptomicsMapper 
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
     * 删除中药材转录组学数据
     * 
     * @param id 中药材转录组学数据主键
     * @return 结果
     */
    public int deleteHerbTranscriptomicsById(Long id);

    /**
     * 批量删除中药材转录组学数据
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteHerbTranscriptomicsByIds(Long[] ids);
}
