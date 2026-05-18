package com.tcmseek.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.common.core.page.TableDataInfo;
import com.tcmseek.dao.GeneMapper;
import com.tcmseek.service.GeneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 基因/靶标服务实现
 * @author TCMSeek
 */
@Service
public class GeneServiceImpl implements GeneService {

    @Autowired
    private GeneMapper geneMapper;

    @Override
    public AjaxResult getGeneList(Integer page, Integer pageSize, String keyword) {
        try {
            PageHelper.startPage(page, pageSize);
            List<Map<String, Object>> genes = geneMapper.getGeneList(keyword);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(genes);
            
            TableDataInfo dataInfo = new TableDataInfo();
            dataInfo.setRows(pageInfo.getList());
            dataInfo.setTotal(pageInfo.getTotal());
            dataInfo.setCode(200);
            dataInfo.setMsg("查询成功");
            
            return AjaxResult.success("查询成功", dataInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("获取基因列表失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult getGeneDetail(String geneId) {
        try {
            Map<String, Object> gene = geneMapper.getGeneById(geneId);
            if (gene == null) {
                return AjaxResult.error("未找到该基因信息");
            }
            return AjaxResult.success(gene);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("获取基因详情失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult getGeneDetailByEntrezId(Integer entrezId) {
        try {
            Map<String, Object> gene = geneMapper.getGeneByEntrezId(entrezId);
            if (gene == null) {
                return AjaxResult.error("未找到该基因信息");
            }
            return AjaxResult.success(gene);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("获取基因详情失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult getGeneCompounds(String geneId, Integer page, Integer pageSize) {
        try {
            PageHelper.startPage(page, pageSize);
            List<Map<String, Object>> compounds = geneMapper.getGeneCompounds(geneId);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(compounds);
            
            TableDataInfo dataInfo = new TableDataInfo();
            dataInfo.setRows(pageInfo.getList());
            dataInfo.setTotal(pageInfo.getTotal());
            dataInfo.setCode(200);
            dataInfo.setMsg("查询成功");
            
            return AjaxResult.success("查询成功", dataInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("获取关联化合物失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult getGeneDiseases(String geneId, Integer page, Integer pageSize) {
        try {
            PageHelper.startPage(page, pageSize);
            List<Map<String, Object>> diseases = geneMapper.getGeneDiseases(geneId);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(diseases);
            
            TableDataInfo dataInfo = new TableDataInfo();
            dataInfo.setRows(pageInfo.getList());
            dataInfo.setTotal(pageInfo.getTotal());
            dataInfo.setCode(200);
            dataInfo.setMsg("查询成功");
            
            return AjaxResult.success("查询成功", dataInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("获取关联疾病失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult getGeneSyndromes(String geneId, Integer page, Integer pageSize) {
        try {
            PageHelper.startPage(page, pageSize);
            List<Map<String, Object>> syndromes = geneMapper.getGeneSyndromes(geneId);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(syndromes);
            
            TableDataInfo dataInfo = new TableDataInfo();
            dataInfo.setRows(pageInfo.getList());
            dataInfo.setTotal(pageInfo.getTotal());
            dataInfo.setCode(200);
            dataInfo.setMsg("查询成功");
            
            return AjaxResult.success("查询成功", dataInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("获取关联证候失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult getGenePathways(String geneId, Integer page, Integer pageSize) {
        try {
            PageHelper.startPage(page, pageSize);
            List<Map<String, Object>> pathways = geneMapper.getGenePathways(geneId);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(pathways);
            
            TableDataInfo dataInfo = new TableDataInfo();
            dataInfo.setRows(pageInfo.getList());
            dataInfo.setTotal(pageInfo.getTotal());
            dataInfo.setCode(200);
            dataInfo.setMsg("查询成功");
            
            return AjaxResult.success("查询成功", dataInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("获取关联通路失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult getGenePhenotypes(String geneId, Integer page, Integer pageSize) {
        try {
            PageHelper.startPage(page, pageSize);
            List<Map<String, Object>> phenotypes = geneMapper.getGenePhenotypes(geneId);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(phenotypes);
            
            TableDataInfo dataInfo = new TableDataInfo();
            dataInfo.setRows(pageInfo.getList());
            dataInfo.setTotal(pageInfo.getTotal());
            dataInfo.setCode(200);
            dataInfo.setMsg("查询成功");
            
            return AjaxResult.success("查询成功", dataInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("获取关联表型失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult getGeneWmSymptoms(String geneId, Integer page, Integer pageSize) {
        try {
            PageHelper.startPage(page, pageSize);
            List<Map<String, Object>> wmSymptoms = geneMapper.getGeneWmSymptoms(geneId);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(wmSymptoms);
            
            TableDataInfo dataInfo = new TableDataInfo();
            dataInfo.setRows(pageInfo.getList());
            dataInfo.setTotal(pageInfo.getTotal());
            dataInfo.setCode(200);
            dataInfo.setMsg("查询成功");
            
            return AjaxResult.success("查询成功", dataInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("获取关联西医症状失败: " + e.getMessage());
        }
    }
}

