package com.tcmseek.controller;

import com.tcmseek.common.annotation.Anonymous;
import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.dto.StructureSearchRequest;
import com.tcmseek.dto.StructureSearchResult;
import com.tcmseek.service.CompoundStructureService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 化合物结构搜索控制器
 * Compound Structure Search Controller
 */
@Anonymous
@RestController
@RequestMapping("/tcmseek/compounds/search")
@Api(tags = "化合物结构搜索")
@Slf4j
@CrossOrigin
public class CompoundStructureController {
    
    @Autowired
    private CompoundStructureService structureService;
    
    /**
     * 根据分子结构搜索化合物
     * 支持三种搜索类型：
     * 1. full - 全结构匹配（精确匹配）
     * 2. sub - 子结构搜索（查找包含指定子结构的化合物）
     * 3. similarity - 相似性搜索（基于 Tanimoto 相似度）
     *
     * @param request 搜索请求
     * @return 搜索结果列表
     */
    @PostMapping("/structure")
    @ApiOperation(value = "分子结构搜索", notes = "根据分子结构（MOL格式）搜索化合物")
    public AjaxResult searchByStructure(
            @Validated @RequestBody StructureSearchRequest request) {
        try {
            log.info("收到分子结构搜索请求: type={}", request.getType());
            
            List<StructureSearchResult> results = structureService.searchByStructure(request);
            
            return AjaxResult.success(results);
            
        } catch (IllegalArgumentException e) {
            log.warn("搜索参数错误: {}", e.getMessage());
            return AjaxResult.error(e.getMessage());
            
        } catch (Exception e) {
            log.error("分子结构搜索失败", e);
            return AjaxResult.error("搜索失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查 RDKit 状态
     * @return RDKit 版本和状态信息
     */
    @GetMapping("/rdkit/status")
    @ApiOperation(value = "检查 RDKit 状态")
    public AjaxResult checkRDKitStatus() {
        try {
            String status = structureService.checkRDKitStatus();
            return AjaxResult.success(status);
        } catch (Exception e) {
            return AjaxResult.error("RDKit 不可用: " + e.getMessage());
        }
    }
}

