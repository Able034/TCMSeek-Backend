package com.tcmseek.webmanage.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tcmseek.webmanage.mapper.CompoundAdmetMapper;
import com.tcmseek.webmanage.domain.CompoundAdmet;
import com.tcmseek.webmanage.service.ICompoundAdmetService;
import org.springframework.web.multipart.MultipartFile;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.utils.StringUtils;
import com.tcmseek.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 化合物ADMET预测结果Service业务层处理
 *
 * @author Able
 * @date 2025-11-13
 */
@Service
public class CompoundAdmetServiceImpl implements ICompoundAdmetService
{
    private static final Logger log = LoggerFactory.getLogger(CompoundAdmetServiceImpl.class);
    private static final int BATCH_SIZE = 500;
    private static final int QUERY_BATCH_SIZE = 800;

    @Autowired
    private CompoundAdmetMapper compoundAdmetMapper;

    /**
     * 查询化合物ADMET预测结果
     *
     * @param id 化合物ADMET预测结果主键
     * @return 化合物ADMET预测结果
     */
    @Override
    public CompoundAdmet selectCompoundAdmetById(Long id)
    {
        return compoundAdmetMapper.selectCompoundAdmetById(id);
    }

    /**
     * 查询化合物ADMET预测结果列表
     *
     * @param compoundAdmet 化合物ADMET预测结果
     * @return 化合物ADMET预测结果
     */
    @Override
    public List<CompoundAdmet> selectCompoundAdmetList(CompoundAdmet compoundAdmet)
    {
        return compoundAdmetMapper.selectCompoundAdmetList(compoundAdmet);
    }

    /**
     * 新增化合物ADMET预测结果
     *
     * @param compoundAdmet 化合物ADMET预测结果
     * @return 结果
     */
    @Override
    public int insertCompoundAdmet(CompoundAdmet compoundAdmet)
    {
        return compoundAdmetMapper.insertCompoundAdmet(compoundAdmet);
    }

    /**
     * 修改化合物ADMET预测结果
     *
     * @param compoundAdmet 化合物ADMET预测结果
     * @return 结果
     */
    @Override
    public int updateCompoundAdmet(CompoundAdmet compoundAdmet)
    {
        return compoundAdmetMapper.updateCompoundAdmet(compoundAdmet);
    }

    /**
     * 批量删除化合物ADMET预测结果
     *
     * @param ids 需要删除的化合物ADMET预测结果主键
     * @return 结果
     */
    @Override
    public int deleteCompoundAdmetByIds(Long[] ids)
    {
        return compoundAdmetMapper.deleteCompoundAdmetByIds(ids);
    }

    /**
     * 删除化合物ADMET预测结果信息
     *
     * @param id 化合物ADMET预测结果主键
     * @return 结果
     */
    @Override
    public int deleteCompoundAdmetById(Long id)
    {
        return compoundAdmetMapper.deleteCompoundAdmetById(id);
    }

    /**
     * excel导入化合物ADMET预测结果
     *
     * @param file excel 文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void excelinput(MultipartFile file)
    {
        // 校验文件
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("上传文件为空");
        }

        String fileName = file.getOriginalFilename();
        if (StringUtils.isNotEmpty(fileName))
        {
            String lowerName = fileName.toLowerCase();
            if (!lowerName.endsWith(".xlsx") && !lowerName.endsWith(".xls"))
            {
                throw new ServiceException("仅支持 xls 或 xlsx 文件");
            }
        }

        // 解析 Excel 数据
        List<CompoundAdmet> importedList;
        try
        {
            ExcelUtil<CompoundAdmet> util = new ExcelUtil<>(CompoundAdmet.class);
            importedList = util.importEasyExcel(file.getInputStream());
        }
        catch (Exception e)
        {
            log.error("解析 ADMET Excel 失败", e);
            throw new ServiceException("解析 Excel 失败：" + e.getMessage());
        }

        if (importedList == null || importedList.isEmpty())
        {
            throw new ServiceException("Excel 中没有可导入的数据");
        }

        // 去重并记录重复数据
        Map<String, CompoundAdmet> excelUniqueMap = new LinkedHashMap<>();
        // 统计 Excel 中的重复数据和缺少 inchikey 的数据
        int duplicateInExcel = 0;
        int missingInchikey = 0;
        Date now = new Date();
        for (CompoundAdmet item : importedList)
        {
            if (item == null || StringUtils.isEmpty(item.getInchikey()))
            {
                missingInchikey++;
                continue;
            }
            String inchikey = item.getInchikey().trim();
            if (StringUtils.isEmpty(inchikey))
            {
                missingInchikey++;
                continue;
            }

            if (excelUniqueMap.containsKey(inchikey))
            {
                duplicateInExcel++;
                continue;
            }

            item.setInchikey(inchikey);
            item.setCreatedAt(now);
            item.setUpdatedAt(now);

            excelUniqueMap.put(inchikey, item);
        }

        if (excelUniqueMap.isEmpty())
        {
            throw new ServiceException("Excel 中有效 inchikey 为空");
        }

        // 批量查询数据库中存在的数据
        List<String> allInchikeys = new ArrayList<>(excelUniqueMap.keySet());
        List<CompoundAdmet> exists = new ArrayList<>();
        for (int i = 0; i < allInchikeys.size(); i += QUERY_BATCH_SIZE)
        {
            int end = Math.min(i + QUERY_BATCH_SIZE, allInchikeys.size());
            List<String> part = allInchikeys.subList(i, end);
            exists.addAll(compoundAdmetMapper.selectByInchikeys(part));
        }

        Map<String, CompoundAdmet> existsMap = exists.stream()
                .collect(Collectors.toMap(CompoundAdmet::getInchikey, item -> item, (a, b) -> a));

        List<CompoundAdmet> toInsert = new ArrayList<>();
        List<CompoundAdmet> toUpdate = new ArrayList<>();
        for (CompoundAdmet item : excelUniqueMap.values())
        {
            CompoundAdmet existed = existsMap.get(item.getInchikey());
            if (existed != null)
            {
                item.setId(existed.getId());
                item.setCreatedAt(existed.getCreatedAt());
                item.setUpdatedAt(now);
                toUpdate.add(item);
            }
            else
            {
                toInsert.add(item);
            }
        }

        for (int i = 0; i < toInsert.size(); i += BATCH_SIZE)
        {
            int end = Math.min(i + BATCH_SIZE, toInsert.size());
            List<CompoundAdmet> subList = toInsert.subList(i, end);
            if (!subList.isEmpty())
            {
                compoundAdmetMapper.batchInsertCompoundAdmet(subList);
            }
        }

        for (CompoundAdmet item : toUpdate)
        {
            compoundAdmetMapper.updateCompoundAdmet(item);
        }

        log.info("ADMET Excel 导入完成: 总行={}, 有效={}, 新增={}, 更新={}, Excel重复={}, 缺少inchikey={}",
                importedList.size(), excelUniqueMap.size(), toInsert.size(), toUpdate.size(),
                duplicateInExcel, missingInchikey);
    }
}
