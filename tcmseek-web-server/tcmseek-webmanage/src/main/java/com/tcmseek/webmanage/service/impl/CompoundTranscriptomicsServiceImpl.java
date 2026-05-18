package com.tcmseek.webmanage.service.impl;

import java.util.Arrays;
import java.util.List;

import com.tcmseek.common.utils.poi.ExcelImportTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.CompoundTranscriptomicsMapper;
import com.tcmseek.webmanage.domain.CompoundTranscriptomics;
import com.tcmseek.webmanage.service.ICompoundTranscriptomicsService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 化合物转录组学数据Service业务层处理
 * 
 * @author Able
 * @date 2025-11-15
 */
@Service
public class CompoundTranscriptomicsServiceImpl implements ICompoundTranscriptomicsService 
{
    private static final Logger log = LoggerFactory.getLogger(CompoundTranscriptomicsServiceImpl.class);
    @Autowired
    private CompoundTranscriptomicsMapper compoundTranscriptomicsMapper;

    /**
     * 查询化合物转录组学数据
     * 
     * @param id 化合物转录组学数据主键
     * @return 化合物转录组学数据
     */
    @Override
    public CompoundTranscriptomics selectCompoundTranscriptomicsById(Long id)
    {
        return compoundTranscriptomicsMapper.selectCompoundTranscriptomicsById(id);
    }

    /**
     * 查询化合物转录组学数据列表
     * 
     * @param compoundTranscriptomics 化合物转录组学数据
     * @return 化合物转录组学数据
     */
    @Override
    public List<CompoundTranscriptomics> selectCompoundTranscriptomicsList(CompoundTranscriptomics compoundTranscriptomics)
    {
        return compoundTranscriptomicsMapper.selectCompoundTranscriptomicsList(compoundTranscriptomics);
    }

    /**
     * 新增化合物转录组学数据
     * 
     * @param compoundTranscriptomics 化合物转录组学数据
     * @return 结果
     */
    @Override
    public int insertCompoundTranscriptomics(CompoundTranscriptomics compoundTranscriptomics)
    {
        return compoundTranscriptomicsMapper.insertCompoundTranscriptomics(compoundTranscriptomics);
    }

    /**
     * 修改化合物转录组学数据
     * 
     * @param compoundTranscriptomics 化合物转录组学数据
     * @return 结果
     */
    @Override
    public int updateCompoundTranscriptomics(CompoundTranscriptomics compoundTranscriptomics)
    {
        return compoundTranscriptomicsMapper.updateCompoundTranscriptomics(compoundTranscriptomics);
    }

    /**
     * 批量删除化合物转录组学数据
     * 
     * @param ids 需要删除的化合物转录组学数据主键
     * @return 结果
     */
    @Override
    public int deleteCompoundTranscriptomicsByIds(Long[] ids)
    {
        return compoundTranscriptomicsMapper.deleteCompoundTranscriptomicsByIds(ids);
    }

    /**
     * 删除化合物转录组学数据信息
     * 
     * @param id 化合物转录组学数据主键
     * @return 结果
     */
    @Override
    public int deleteCompoundTranscriptomicsById(Long id)
    {
        return compoundTranscriptomicsMapper.deleteCompoundTranscriptomicsById(id);
    }

    /**
     * excel导入
     * @param file
     */
    @Override
    public void excelinput(MultipartFile file) {
        ExcelImportTemplate.ImportResult result = ExcelImportTemplate.importExcel(
                file,
                CompoundTranscriptomics.class,
                Arrays.asList("inchikey"),
                Arrays.asList("inchikey", "geneEntrezId"),
                keys -> compoundTranscriptomicsMapper.selectByCompositeKeys(keys),
                batch -> compoundTranscriptomicsMapper.batchInsertCompoundTranscriptomics(batch),
                batch -> batch.forEach(compoundTranscriptomicsMapper::updateCompoundTranscriptomics));
        log.info("excel导入结果：total={}, valid={}, insert={}, update={}, dupExcel={}, missingRequired={}",
                result.getTotal(), result.getValid(), result.getInserted(), result.getUpdated(),
                result.getDuplicatedInExcel(), result.getMissingRequired());
    }
}
