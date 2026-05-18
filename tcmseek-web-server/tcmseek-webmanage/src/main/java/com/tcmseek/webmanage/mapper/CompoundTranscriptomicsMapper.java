package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.CompoundTranscriptomics;

/**
 * 化合物转录组学数据Mapper接口
 * 
 * @author Able
 * @date 2025-11-15
 */
public interface CompoundTranscriptomicsMapper 
{
    /**
     * 查询化合物转录组学数据
     * 
     * @param id 化合物转录组学数据主键
     * @return 化合物转录组学数据
     */
    public CompoundTranscriptomics selectCompoundTranscriptomicsById(Long id);

    /**
     * 查询化合物转录组学数据列表
     * 
     * @param compoundTranscriptomics 化合物转录组学数据
     * @return 化合物转录组学数据集合
     */
    public List<CompoundTranscriptomics> selectCompoundTranscriptomicsList(CompoundTranscriptomics compoundTranscriptomics);

    /**
     * 新增化合物转录组学数据
     * 
     * @param compoundTranscriptomics 化合物转录组学数据
     * @return 结果
     */
    public int insertCompoundTranscriptomics(CompoundTranscriptomics compoundTranscriptomics);

    /**
     * 修改化合物转录组学数据
     * 
     * @param compoundTranscriptomics 化合物转录组学数据
     * @return 结果
     */
    public int updateCompoundTranscriptomics(CompoundTranscriptomics compoundTranscriptomics);

    /**
     * 删除化合物转录组学数据
     * 
     * @param id 化合物转录组学数据主键
     * @return 结果
     */
    public int deleteCompoundTranscriptomicsById(Long id);

    /**
     * 批量删除化合物转录组学数据
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompoundTranscriptomicsByIds(Long[] ids);

    /**
     * 根据复合键查询
     * @param keys
     * @return
     */
    List<CompoundTranscriptomics> selectByCompositeKeys(List<String> keys);

    /**
     * 批量插入
     * @param list 待插入数据
     */
    void batchInsertCompoundTranscriptomics(List<CompoundTranscriptomics> list);
}
