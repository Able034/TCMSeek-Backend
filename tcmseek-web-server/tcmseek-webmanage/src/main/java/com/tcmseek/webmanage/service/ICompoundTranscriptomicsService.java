package com.tcmseek.webmanage.service;

import java.util.List;
import com.tcmseek.webmanage.domain.CompoundTranscriptomics;
import org.springframework.web.multipart.MultipartFile;

/**
 * 化合物转录组学数据Service接口
 * 
 * @author Able
 * @date 2025-11-15
 */
public interface ICompoundTranscriptomicsService 
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
     * 批量删除化合物转录组学数据
     * 
     * @param ids 需要删除的化合物转录组学数据主键集合
     * @return 结果
     */
    public int deleteCompoundTranscriptomicsByIds(Long[] ids);

    /**
     * 删除化合物转录组学数据信息
     * 
     * @param id 化合物转录组学数据主键
     * @return 结果
     */
    public int deleteCompoundTranscriptomicsById(Long id);

    /**
     * excel导入数据
     * @param file
     */
    void excelinput(MultipartFile file);
}
