package com.tcmseek.webmanage.service;

import java.util.List;
import com.tcmseek.webmanage.domain.CompoundAdmet;
import org.springframework.web.multipart.MultipartFile;

/**
 * 化合物ADMET预测结果Service接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface ICompoundAdmetService 
{
    /**
     * 查询化合物ADMET预测结果
     * 
     * @param id 化合物ADMET预测结果主键
     * @return 化合物ADMET预测结果
     */
    public CompoundAdmet selectCompoundAdmetById(Long id);

    /**
     * 查询化合物ADMET预测结果列表
     * 
     * @param compoundAdmet 化合物ADMET预测结果
     * @return 化合物ADMET预测结果集合
     */
    public List<CompoundAdmet> selectCompoundAdmetList(CompoundAdmet compoundAdmet);

    /**
     * 新增化合物ADMET预测结果
     * 
     * @param compoundAdmet 化合物ADMET预测结果
     * @return 结果
     */
    public int insertCompoundAdmet(CompoundAdmet compoundAdmet);

    /**
     * 修改化合物ADMET预测结果
     * 
     * @param compoundAdmet 化合物ADMET预测结果
     * @return 结果
     */
    public int updateCompoundAdmet(CompoundAdmet compoundAdmet);

    /**
     * 批量删除化合物ADMET预测结果
     * 
     * @param ids 需要删除的化合物ADMET预测结果主键集合
     * @return 结果
     */
    public int deleteCompoundAdmetByIds(Long[] ids);

    /**
     * 删除化合物ADMET预测结果信息
     * 
     * @param id 化合物ADMET预测结果主键
     * @return 结果
     */
    public int deleteCompoundAdmetById(Long id);

    /**
     * excel 导入数据
     * @param file
     */
    void excelinput(MultipartFile file);
}
