package com.tcmseek.service.impl;

import com.tcmseek.dao.CompoundMapper;
import com.tcmseek.pojo.entity.CompoundAdmet;
import com.tcmseek.pojo.entity.Target;
import com.tcmseek.pojo.entity.TcmCompound;
import com.tcmseek.pojo.vo.TargetD3VO;
import com.tcmseek.service.CompoundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CompoundServiceImpl implements CompoundService {

    @Autowired
    private CompoundMapper compoundMapper;

    @Override
    public List<TcmCompound> selectCompoundsList(String keyword) {
        return compoundMapper.selectCompoundsList(keyword);
    }

    @Override
    public TcmCompound selectCompoundByInchikey(String inchikey) {
        return compoundMapper.selectCompoundByInchikey(inchikey);
    }

    @Override
    public List<Target> selectTargetsByInchikey(String inchikey) {
        return compoundMapper.selectTargetsByInchikey(inchikey);
    }

    @Override
    public CompoundAdmet selectAdmetByInchikey(String inchikey) {
        return compoundMapper.selectAdmetByInchikey(inchikey);
    }

    /**
     * 获取 compound 的所有靶标
     * @param inchikey
     * @return
     */
    @Override
    public List<TargetD3VO> getAllTargets(String inchikey) {
        return compoundMapper.getAllTargets(inchikey);
    }

    /**
     * 批量获取多个化合物的靶标信息（优化版：去重后传输）
     * @param inchikeys 化合物InChIKey列表
     * @return 优化的数据结构：{ targets: [...], relations: [...] }
     */
    @Override
    public Map<String, Object> batchGetAllTargets(List<String> inchikeys) {
        if (inchikeys == null || inchikeys.isEmpty()) {
            log.warn("批量查询靶标: inchikeys 为空");
            return new HashMap<>();
        }

        List<String> sanitizedInchikeys = inchikeys.stream()
                .filter(item -> item != null && !item.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        if (sanitizedInchikeys.isEmpty()) {
            log.warn("批量查询靶标: 有效 inchikey 为空");
            return new HashMap<>();
        }

        final int batchSize = 200;
        int totalBatches = (sanitizedInchikeys.size() + batchSize - 1) / batchSize;
        log.info("开始批量查询靶标信息, 原始 {} 个, 去重后 {} 个, 拆成 {} 批执行", inchikeys.size(), sanitizedInchikeys.size(), totalBatches);

        List<TargetD3VO> allTargets = new ArrayList<>();
        for (int startIndex = 0; startIndex < sanitizedInchikeys.size(); startIndex += batchSize) {
            int endIndex = Math.min(startIndex + batchSize, sanitizedInchikeys.size());
            List<String> batchKeys = sanitizedInchikeys.subList(startIndex, endIndex);
            List<TargetD3VO> batchTargets = compoundMapper.batchGetAllTargets(batchKeys);
            if (batchTargets != null && !batchTargets.isEmpty()) {
                allTargets.addAll(batchTargets);
            }
        }

        log.info("从数据库查询了 {} 条化合物-靶标关系记录", allTargets.size());

        Map<String, TargetD3VO> uniqueTargetsMap = new HashMap<>();
        List<Map<String, String>> relations = new ArrayList<>();

        for (TargetD3VO target : allTargets) {
            String inchikey = target.getInchikey();
            String symbol = target.getSymbol();

            if (!uniqueTargetsMap.containsKey(symbol)) {
                TargetD3VO uniqueTarget = new TargetD3VO();
                uniqueTarget.setSymbol(symbol);
                uniqueTarget.setGeneEntrezId(target.getGeneEntrezId());
                uniqueTarget.setUniprotId(target.getUniprotId());
                uniqueTarget.setDescription(target.getDescription());
                uniqueTargetsMap.put(symbol, uniqueTarget);
            }

            Map<String, String> relation = new HashMap<>();
            relation.put("inchikey", inchikey);
            relation.put("targetSymbol", symbol);
            relations.add(relation);
        }

        List<TargetD3VO> uniqueTargets = new ArrayList<>(uniqueTargetsMap.values());

        log.info("优化后 {} 个唯一靶标, {} 条关系记录", uniqueTargets.size(), relations.size());
        if (!allTargets.isEmpty()) {
            double compressionRate = (1 - (double) uniqueTargets.size() / allTargets.size()) * 100;
            log.info("数据压缩率: {}%", String.format("%.1f", compressionRate));
        } else {
            log.info("数据压缩率: 0% (无靶标命中)");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("targets", uniqueTargets);
        result.put("relations", relations);

        return result;
    }

    @Override
    public List<com.tcmseek.pojo.entity.coreTcmHerbs> selectHerbsByInchikey(String inchikey) {
        return compoundMapper.selectHerbsByInchikey(inchikey);
    }
    
    @Override
    public List<Map<String, Object>> getTranscriptomicsData(String inchikey) {
        return compoundMapper.selectTranscriptomicsByInchikey(inchikey);
    }
    
    @Override
    public Map<String, Object> getTranscriptomicsStatistics(String inchikey) {
        Map<String, Object> stats = compoundMapper.getTranscriptomicsStatistics(inchikey);
        if (stats == null) {
            // 如果没有数据，返回空统计
            stats = new HashMap<>();
            stats.put("totalGenes", 0);
            stats.put("upregulatedGenes", 0);
            stats.put("downregulatedGenes", 0);
            stats.put("significantGenes", 0);
            stats.put("significantUpregulated", 0);
            stats.put("significantDownregulated", 0);
            stats.put("maxAbsLog2Fc", 0.0);
            stats.put("minPValue", 1.0);
        }
        return stats;
    }
}

