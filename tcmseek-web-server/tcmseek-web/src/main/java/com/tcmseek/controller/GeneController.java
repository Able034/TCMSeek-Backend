package com.tcmseek.controller;

import com.tcmseek.common.annotation.Anonymous;
import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.service.GeneService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

/**
 * 基因/靶标控制器
 * @author TCMSeek
 */
@Anonymous
@Api(tags = "基因/靶标管理")
@RestController
@RequestMapping("/tcmseek/genes")
public class GeneController {

    @Autowired
    private GeneService geneService;

    /**
     * 获取基因列表（分页）
     */
    @ApiOperation("获取基因列表")
    @GetMapping("/list")
    @Cacheable(
        value = "gene:list:page1",
        key = "'list:' + #pageSize + ':' + T(java.lang.String).valueOf(#keyword ?: '')",
        condition = "#page == 1",
        unless = "#result == null"
    )
    public AjaxResult getGeneList(
            @ApiParam("页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam("每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @ApiParam("搜索关键词（基因符号或描述）") @RequestParam(value = "keyword", required = false) String keyword) {
        return geneService.getGeneList(page, pageSize, keyword);
    }

    /**
     * 获取基因详情（通过 tcm_tar_id）
     */
    @ApiOperation("获取基因详情")
    @GetMapping("/{geneId}")
    @Cacheable(value = "gene:detail" , key = "'gene' + #geneId" , unless = "#result == null ")
    public AjaxResult getGeneDetail(@ApiParam("基因ID") @PathVariable("geneId") String geneId) {
        return geneService.getGeneDetail(geneId);
    }

    /**
     * 获取基因详情（通过 gene_entrez_id）
     */
    @Anonymous
    @ApiOperation("根据Entrez ID获取基因详情")
    @GetMapping("/entrez/{entrezId}")
    public AjaxResult getGeneDetailByEntrezId(
            @ApiParam("基因Entrez ID") @PathVariable("entrezId") Integer entrezId) {
        return geneService.getGeneDetailByEntrezId(entrezId);
    }

    /**
     * 获取基因的关联化合物
     */
    @ApiOperation("获取基因的关联化合物")
    @GetMapping("/{geneId}/compounds")
    public AjaxResult getGeneCompounds(
            @ApiParam("基因ID") @PathVariable("geneId") String geneId,
            @ApiParam("页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam("每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return geneService.getGeneCompounds(geneId, page, pageSize);
    }

    /**
     * 获取基因的关联疾病
     */
    @ApiOperation("获取基因的关联疾病")
    @GetMapping("/{geneId}/diseases")
    public AjaxResult getGeneDiseases(
            @ApiParam("基因ID") @PathVariable("geneId") String geneId,
            @ApiParam("页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam("每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return geneService.getGeneDiseases(geneId, page, pageSize);
    }

    /**
     * 获取基因的关联证候
     */
    @ApiOperation("获取基因的关联证候")
    @GetMapping("/{geneId}/syndromes")
    public AjaxResult getGeneSyndromes(
            @ApiParam("基因ID") @PathVariable("geneId") String geneId,
            @ApiParam("页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam("每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return geneService.getGeneSyndromes(geneId, page, pageSize);
    }

    /**
     * 获取基因的关联通路
     */
    @ApiOperation("获取基因的关联通路")
    @GetMapping("/{geneId}/pathways")
    public AjaxResult getGenePathways(
            @ApiParam("基因ID") @PathVariable("geneId") String geneId,
            @ApiParam("页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam("每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return geneService.getGenePathways(geneId, page, pageSize);
    }

    /**
     * 获取基因的关联表型
     */
    @ApiOperation("获取基因的关联表型")
    @GetMapping("/{geneId}/phenotypes")
    public AjaxResult getGenePhenotypes(
            @ApiParam("基因ID") @PathVariable("geneId") String geneId,
            @ApiParam("页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam("每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return geneService.getGenePhenotypes(geneId, page, pageSize);
    }

    /**
     * 获取基因的关联西医症状
     */
    @ApiOperation("获取基因的关联西医症状")
    @GetMapping("/{geneId}/wm-symptoms")
    public AjaxResult getGeneWmSymptoms(
            @ApiParam("基因ID") @PathVariable("geneId") String geneId,
            @ApiParam("页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam("每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return geneService.getGeneWmSymptoms(geneId, page, pageSize);
    }
}

