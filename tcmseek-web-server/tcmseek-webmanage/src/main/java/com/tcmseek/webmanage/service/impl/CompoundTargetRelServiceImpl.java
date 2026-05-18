package com.tcmseek.webmanage.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.tcmseek.common.utils.StringUtils;
import com.tcmseek.common.utils.poi.ExcelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.CompoundTargetRelMapper;
import com.tcmseek.webmanage.domain.CompoundTargetRel;
import com.tcmseek.webmanage.service.ICompoundTargetRelService;
import org.springframework.web.multipart.MultipartFile;


/**
 * 化合物-靶标关联Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class CompoundTargetRelServiceImpl implements ICompoundTargetRelService 
{
    private static final Logger log = LoggerFactory.getLogger(CompoundTargetRelServiceImpl.class);
    @Autowired
    private CompoundTargetRelMapper compoundTargetRelMapper;

    private static final int BATCH_SIZE = 500;
    private static final int QUERY_BATCH_SIZE = 800;


    /**
     * 查询化合物-靶标关联
     * 
     * @param id 化合物-靶标关联主键
     * @return 化合物-靶标关联
     */
    @Override
    public CompoundTargetRel selectCompoundTargetRelById(Long id)
    {
        return compoundTargetRelMapper.selectCompoundTargetRelById(id);
    }

    /**
     * 查询化合物-靶标关联列表
     * 
     * @param compoundTargetRel 化合物-靶标关联
     * @return 化合物-靶标关联
     */
    @Override
    public List<CompoundTargetRel> selectCompoundTargetRelList(CompoundTargetRel compoundTargetRel)
    {
        return compoundTargetRelMapper.selectCompoundTargetRelList(compoundTargetRel);
    }

    /**
     * 新增化合物-靶标关联
     * 
     * @param compoundTargetRel 化合物-靶标关联
     * @return 结果
     */
    @Override
    public int insertCompoundTargetRel(CompoundTargetRel compoundTargetRel)
    {
        return compoundTargetRelMapper.insertCompoundTargetRel(compoundTargetRel);
    }

    /**
     * 修改化合物-靶标关联
     * 
     * @param compoundTargetRel 化合物-靶标关联
     * @return 结果
     */
    @Override
    public int updateCompoundTargetRel(CompoundTargetRel compoundTargetRel)
    {
        return compoundTargetRelMapper.updateCompoundTargetRel(compoundTargetRel);
    }

    /**
     * 批量删除化合物-靶标关联
     * 
     * @param ids 需要删除的化合物-靶标关联主键
     * @return 结果
     */
    @Override
    public int deleteCompoundTargetRelByIds(Long[] ids)
    {
        return compoundTargetRelMapper.deleteCompoundTargetRelByIds(ids);
    }

    /**
     * 删除化合物-靶标关联信息
     * 
     * @param id 化合物-靶标关联主键
     * @return 结果
     */
    @Override
    public int deleteCompoundTargetRelById(Long id)
    {
        return compoundTargetRelMapper.deleteCompoundTargetRelById(id);
    }

    /**
     * 导入数据
     * @param file
     */
    @Override
    public void excelinput(MultipartFile file) {
        //校验
        if(file == null|| file.isEmpty()){
            throw new RuntimeException("CompoundTargetRel的excel文件不能为空");

        }

        String fileName = file.getOriginalFilename();
        String suffix = "";
        if (StringUtils.isNotEmpty(fileName) && fileName.contains(".")) {
            suffix = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        }

        if(!".xlsx".equals(suffix)&& !".xls".equals(suffix)){
            throw new RuntimeException("CompoundTargetRel的excel文件格式错误");

        }

        //解析excel
        List<CompoundTargetRel> importList;
        try{
            ExcelUtil<CompoundTargetRel> util = new ExcelUtil<>(CompoundTargetRel.class);
            importList = util.importExcel(file.getInputStream());
        }catch(Exception e){
            throw new RuntimeException("CompoundTargetRel的excel文件解析错误", e);
        }

        if(importList == null || importList.isEmpty()){
            throw new RuntimeException("CompoundTargetRel的excel文件数据为空");
        }

        //去重，记录重复数据
        Map<String, CompoundTargetRel> excelUniqueMap = new LinkedHashMap<>();

        int duplicatedate = 0;
        int missingdate = 0;
        Date now = new Date();

        for(CompoundTargetRel item : importList) {

            if (item == null || StringUtils.isEmpty(item.getInchikey()) || StringUtils.isEmpty(item.getTcmTarId())) {
                missingdate++;
                continue;
            }
            String inchikey = item.getInchikey().trim();
            String tcmTarId = item.getTcmTarId().trim();
            String sourceforexcel = item.getSource();
            String unikey = inchikey + "_" + tcmTarId;

            if (StringUtils.isEmpty(inchikey) || StringUtils.isEmpty(tcmTarId)) {
                missingdate++;
                continue;
            }

            if(excelUniqueMap.containsKey(unikey)){
                duplicatedate++;
                continue;
            }

            item.setInchikey(inchikey);
            item.setTcmTarId(tcmTarId);
            item.setCreatedAt(now);
            item.setSource(sourceforexcel);

            excelUniqueMap.put(unikey, item);
        }

        if(excelUniqueMap.isEmpty()){
            throw new RuntimeException("Excel 中有效 inchikey 为空");
        }

        //批量查询数据库中已存在的数据
        List<String> allkey = new ArrayList<>(excelUniqueMap.keySet());
        List<CompoundTargetRel> exists = new ArrayList<>();
        for(int i = 0; i < allkey.size(); i +=QUERY_BATCH_SIZE){
            int end = Math.min(i + QUERY_BATCH_SIZE, allkey.size());
            List<String> part = allkey.subList(i, end);
            exists.addAll(compoundTargetRelMapper.selectByCompositeKeys(part));
        }

        Map<String, CompoundTargetRel> existsMap = new LinkedHashMap<>();
        for (CompoundTargetRel existed : exists)
        {
            String key = existed.getInchikey() + "_" + existed.getTcmTarId();
            if (!existsMap.containsKey(key))
            {
                existsMap.put(key, existed);
            }
        }

        List<CompoundTargetRel> insertList = new ArrayList<>();
        List<CompoundTargetRel> updateList = new ArrayList<>();

        for (CompoundTargetRel item : excelUniqueMap.values()){
            CompoundTargetRel existsItem = existsMap.get(item.getInchikey() + "_" + item.getTcmTarId());
            if (existsItem == null)
            {
                insertList.add(item);
            }
            else {
                item.setId(existsItem.getId());
                item.setCreatedAt(existsItem.getCreatedAt());
                updateList.add(item);
            }
        }

        for(int i = 0; i < insertList.size(); i += BATCH_SIZE){
            int end = Math.min(i + BATCH_SIZE, insertList.size());
            List<CompoundTargetRel> subList = insertList.subList(i, end);
            if(!subList.isEmpty()){
                compoundTargetRelMapper.batchInsertCompoundTargetRel(subList);
            }
        }

        for (CompoundTargetRel item : updateList){
            compoundTargetRelMapper.updateCompoundTargetRel(item);
        }

        log.info("CompoundTargetRel Excel 导入完成: 总行={}, 有效={}, 新增={}, 更新={}, Excel重复={}, 缺少inchikey和tcmTarId={}，",
                importList.size(), importList.size() - duplicatedate - missingdate, insertList.size(), updateList.size(), duplicatedate, missingdate);
        //sql数据库建库结构
        //    `id` INT AUTO_INCREMENT PRIMARY KEY,
        //    `inchikey` VARCHAR(100) NOT NULL COMMENT '化合物InChIKey',
        //    `tcm_tar_id` VARCHAR(50) NOT NULL COMMENT '靶标ID',
        //    `source` VARCHAR(100) DEFAULT NULL COMMENT '数据来源',
        //    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,






    }
}
