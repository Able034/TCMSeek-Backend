package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.Diseases;

/**
 * 疾病信息Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface DiseasesMapper 
{
    /**
     * 查询疾病信息
     * 
     * @param id 疾病信息主键
     * @return 疾病信息
     */
    public Diseases selectDiseasesById(Long id);

    /**
     * 查询疾病信息列表
     * 
     * @param diseases 疾病信息
     * @return 疾病信息集合
     */
    public List<Diseases> selectDiseasesList(Diseases diseases);

    /**
     * 新增疾病信息
     * 
     * @param diseases 疾病信息
     * @return 结果
     */
    public int insertDiseases(Diseases diseases);

    /**
     * 修改疾病信息
     * 
     * @param diseases 疾病信息
     * @return 结果
     */
    public int updateDiseases(Diseases diseases);

    /**
     * 删除疾病信息
     * 
     * @param id 疾病信息主键
     * @return 结果
     */
    public int deleteDiseasesById(Long id);

    /**
     * 批量删除疾病信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDiseasesByIds(Long[] ids);
}
