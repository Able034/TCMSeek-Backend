package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.TcmSymptoms;

/**
 * 中医症状信息Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface TcmSymptomsMapper 
{
    /**
     * 查询中医症状信息
     * 
     * @param id 中医症状信息主键
     * @return 中医症状信息
     */
    public TcmSymptoms selectTcmSymptomsById(Long id);

    /**
     * 查询中医症状信息列表
     * 
     * @param tcmSymptoms 中医症状信息
     * @return 中医症状信息集合
     */
    public List<TcmSymptoms> selectTcmSymptomsList(TcmSymptoms tcmSymptoms);

    /**
     * 新增中医症状信息
     * 
     * @param tcmSymptoms 中医症状信息
     * @return 结果
     */
    public int insertTcmSymptoms(TcmSymptoms tcmSymptoms);

    /**
     * 修改中医症状信息
     * 
     * @param tcmSymptoms 中医症状信息
     * @return 结果
     */
    public int updateTcmSymptoms(TcmSymptoms tcmSymptoms);

    /**
     * 删除中医症状信息
     * 
     * @param id 中医症状信息主键
     * @return 结果
     */
    public int deleteTcmSymptomsById(Long id);

    /**
     * 批量删除中医症状信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTcmSymptomsByIds(Long[] ids);
}
