package com.tcmseek.service;

import com.tcmseek.common.annotation.DataSource;
import com.tcmseek.common.enums.DataSourceType;
import com.tcmseek.dto.StructureSearchRequest;
import com.tcmseek.dto.StructureSearchResult;
import com.tcmseek.mapper.CompoundStructureMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 化合物结构搜索服务
 * Compound Structure Search Service
 */
@Service
@Slf4j
@DataSource(DataSourceType.RDKIT)  // 使用 PostgreSQL + RDKit 数据源
public class CompoundStructureService {
    
    @Autowired
    private CompoundStructureMapper structureMapper;
    
    /**
     * 根据分子结构搜索化合物
     * @param request 搜索请求
     * @return 搜索结果列表
     */
    public List<StructureSearchResult> searchByStructure(StructureSearchRequest request) {
        String searchType = request.getType().toLowerCase();
        String molText = request.getMolFile();
        
        log.info("执行分子结构搜索: type={}, molFileLength={}", searchType, molText.length());
        
        List<StructureSearchResult> results;
        
        try {
            switch (searchType) {
                case "full":
                    // 全结构匹配
                    results = structureMapper.searchExactStructure(molText);
                    log.info("全结构匹配搜索完成，找到 {} 个结果", results.size());
                    break;
                    
                case "sub":
                    // 子结构搜索
                    results = structureMapper.searchSubstructure(molText);
                    log.info("子结构搜索完成，找到 {} 个结果", results.size());
                    break;
                    
                case "similarity":
                    // 相似性搜索
                    Double threshold = request.getThreshold();
                    if (threshold == null || threshold < 0.01 || threshold > 1.0) {
                        threshold = 0.8; // 默认阈值
                    }
                    results = structureMapper.searchSimilarity(molText, threshold);
                    log.info("相似性搜索完成（阈值={}），找到 {} 个结果", threshold, results.size());
                    break;
                    
                default:
                    throw new IllegalArgumentException("不支持的搜索类型: " + searchType);
            }
            
            // 限制结果数量
            if (request.getMaxResults() != null && results.size() > request.getMaxResults()) {
                results = results.subList(0, request.getMaxResults());
            }
            
            return results;
            
        } catch (Exception e) {
            log.error("分子结构搜索失败: {}", e.getMessage(), e);
            throw new RuntimeException("分子结构搜索失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 验证 RDKit 是否正常工作
     * @return RDKit 版本信息
     */
    public String checkRDKitStatus() {
        try {
            String version = structureMapper.getRDKitVersion();
            String sampleSmiles = structureMapper.getSampleSmiles();

            if (version == null || version.trim().isEmpty()) {
                throw new IllegalStateException("rdkit_version() returned empty result");
            }

            log.info("RDKit 状态检查完成: version={}, sampleSmiles={}", version, sampleSmiles);
            return "RDKit is available (version " + version.trim() + ")";
        } catch (Exception e) {
            log.error("RDKit 状态检查失败: {}", e.getMessage(), e);
            return "RDKit is not available: " + e.getMessage();
        }
    }
}
