package com.tcmseek.service.impl;

import com.tcmseek.dao.PathwayMapper;
import com.tcmseek.pojo.entity.Pathway;
import com.tcmseek.service.PathwayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 通路信息Service实现类
 */
@Service
public class PathwayServiceImpl implements PathwayService {

    @Autowired
    private PathwayMapper pathwayMapper;

    @Override
    public List<Map<String, Object>> selectPathwayList(String keyword) {
        return pathwayMapper.selectPathwayList(keyword);
    }

    @Override
    public Pathway selectPathwayById(String pathwayId) {
        return pathwayMapper.selectPathwayById(pathwayId);
    }

    @Override
    public List<Map<String, Object>> selectTargetsByPathwayId(String pathwayId, String keyword) {
        return pathwayMapper.selectTargetsByPathwayId(pathwayId, keyword);
    }

    @Override
    public List<Map<String, Object>> selectCompoundsByPathwayId(String pathwayId, String keyword) {
        return pathwayMapper.selectCompoundsByPathwayId(pathwayId, keyword);
    }

    @Override
    public List<Map<String, Object>> selectHerbsByPathwayId(String pathwayId, String keyword) {
        return pathwayMapper.selectHerbsByPathwayId(pathwayId, keyword);
    }

    @Override
    public Map<String, Object> getStatistics(String pathwayId) {
        return pathwayMapper.getStatistics(pathwayId);
    }

    @Override
    public Map<String, Object> getGraphData(String pathwayId) {
        Map<String, Object> graphData = new HashMap<>();
        
        // 获取节点数据
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        
        // 中心节点（通路）
        Pathway pathway = pathwayMapper.selectPathwayById(pathwayId);
        if (pathway != null) {
            Map<String, Object> centerNode = new HashMap<>();
            centerNode.put("id", pathway.getPathwayId());
            centerNode.put("label", pathway.getName());
            centerNode.put("type", "pathway");
            centerNode.put("isCenter", true);
            nodes.add(centerNode);
            
            // 关联的靶标节点（限制数量避免过大）
            List<Map<String, Object>> targets = pathwayMapper.selectTargetsByPathwayId(pathwayId, null);
            int count = 0;
            int maxTargets = 100; // 最多显示100个靶标节点
            
            for (Map<String, Object> target : targets) {
                if (count >= maxTargets) break;
                
                Map<String, Object> targetNode = new HashMap<>();
                targetNode.put("id", target.get("tcmTarId"));
                targetNode.put("label", target.get("symbol"));
                targetNode.put("type", "gene");
                targetNode.put("isCenter", false);
                nodes.add(targetNode);
                
                // 边
                Map<String, Object> edge = new HashMap<>();
                edge.put("source", pathway.getPathwayId());
                edge.put("target", target.get("tcmTarId"));
                edge.put("relation", "pathway-gene");
                edges.add(edge);
                
                count++;
            }
        }
        
        graphData.put("nodes", nodes);
        graphData.put("edges", edges);
        
        return graphData;
    }
}

