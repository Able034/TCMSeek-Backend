package com.tcmseek.webmanage.service;

import java.util.List;
import com.tcmseek.webmanage.domain.CompoundTargetRel;
import org.springframework.web.multipart.MultipartFile;

/**
 * 化合物-靶标关联Service接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface ICompoundTargetRelService 
{
    /**
     * 查询化合物-靶标关联
     * 
     * @param id 化合物-靶标关联主键
     * @return 化合物-靶标关联
     */
    public CompoundTargetRel selectCompoundTargetRelById(Long id);

    /**
     * 查询化合物-靶标关联列表
     * 
     * @param compoundTargetRel 化合物-靶标关联
     * @return 化合物-靶标关联集合
     */
    public List<CompoundTargetRel> selectCompoundTargetRelList(CompoundTargetRel compoundTargetRel);

    /**
     * 新增化合物-靶标关联
     * 
     * @param compoundTargetRel 化合物-靶标关联
     * @return 结果
     */
    public int insertCompoundTargetRel(CompoundTargetRel compoundTargetRel);

    /**
     * 修改化合物-靶标关联
     * 
     * @param compoundTargetRel 化合物-靶标关联
     * @return 结果
     */
    public int updateCompoundTargetRel(CompoundTargetRel compoundTargetRel);

    /**
     * 批量删除化合物-靶标关联
     * 
     * @param ids 需要删除的化合物-靶标关联主键集合
     * @return 结果
     */
    public int deleteCompoundTargetRelByIds(Long[] ids);

    /**
     * 删除化合物-靶标关联信息
     * 
     * @param id 化合物-靶标关联主键
     * @return 结果
     */
    public int deleteCompoundTargetRelById(Long id);

    /**
     * excel导入化合物-靶标关联
     * @param file
     */
    void excelinput(MultipartFile file);
}
