package com.tcmseek.service.impl;

import com.tcmseek.mapper.PhenotypeMapper;
import com.tcmseek.pojo.entity.Phenotype;
import com.tcmseek.service.PhenotypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 表型信息Service实现类
 */
@Service
public class PhenotypeServiceImpl implements PhenotypeService {

    @Autowired
    private PhenotypeMapper phenotypeMapper;

    @Override
    public List<Map<String, Object>> selectPhenotypeList(String keyword) {
        return phenotypeMapper.selectPhenotypeList(keyword);
    }

    @Override
    public Phenotype selectPhenotypeById(String phenotypeId) {
        return phenotypeMapper.selectPhenotypeById(phenotypeId);
    }

    @Override
    public List<Map<String, Object>> selectTargetsByPhenotypeId(String phenotypeId, String keyword) {
        return phenotypeMapper.selectTargetsByPhenotypeId(phenotypeId, keyword);
    }

    @Override
    public Map<String, Object> getStatistics(String phenotypeId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 关联靶标数量
        Integer targetCount = phenotypeMapper.countTargetsByPhenotypeId(phenotypeId);
        statistics.put("targetCount", targetCount);
        
        // 基因类型分布（如果有）
        List<Map<String, Object>> geneTypeDistribution = phenotypeMapper.getGeneTypeDistribution(phenotypeId);
        statistics.put("geneTypeDistribution", geneTypeDistribution);
        
        return statistics;
    }

    @Override
    public Map<String, Object> getGraphData(String phenotypeId) {
        Map<String, Object> graphData = new HashMap<>();
        
        // 获取节点数据
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        
        // 中心节点（表型）
        Phenotype phenotype = phenotypeMapper.selectPhenotypeById(phenotypeId);
        if (phenotype != null) {
            Map<String, Object> centerNode = new HashMap<>();
            centerNode.put("id", phenotype.getPhenotypeId());
            centerNode.put("label", phenotype.getPhenotypeName());
            centerNode.put("type", "phenotype");
            centerNode.put("isCenter", true);
            nodes.add(centerNode);
            
            // 关联的靶标节点
            List<Map<String, Object>> targets = phenotypeMapper.selectTargetsByPhenotypeId(phenotypeId, null);
            for (Map<String, Object> target : targets) {
                Map<String, Object> targetNode = new HashMap<>();
                targetNode.put("id", target.get("tcmTarId"));
                targetNode.put("label", target.get("symbol"));
                targetNode.put("type", "gene");
                targetNode.put("isCenter", false);
                nodes.add(targetNode);
                
                // 边
                Map<String, Object> edge = new HashMap<>();
                edge.put("source", phenotype.getPhenotypeId());
                edge.put("target", target.get("tcmTarId"));
                edge.put("relation", "phenotype-gene");
                edges.add(edge);
            }
        }
        
        graphData.put("nodes", nodes);
        graphData.put("edges", edges);
        
        return graphData;
    }
}



