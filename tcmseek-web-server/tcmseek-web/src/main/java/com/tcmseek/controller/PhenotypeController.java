package com.tcmseek.controller;

import com.tcmseek.common.annotation.Anonymous;
import com.tcmseek.common.core.controller.BaseController;
import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.common.core.page.TableDataInfo;
import com.tcmseek.pojo.entity.Phenotype;
import com.tcmseek.service.PhenotypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 表型信息Controller
 */
@RestController
@RequestMapping("/tcmseek/phenotypes")
@Slf4j
@Api(tags = "表型信息")
public class PhenotypeController extends BaseController {

    @Autowired
    private PhenotypeService phenotypeService;

    /**
     * 1. 获取表型列表（分页）
     */
    @Anonymous
    @GetMapping("/list")
    @ApiOperation(value = "获取表型列表", notes = "根据关键词查询表型信息，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词（表型名称或HPO ID）", required = false, dataType = "string", paramType = "query")
    })
    @Cacheable(
        value = "phenotype:list:page1",
        key = "'list:' + #pageSize + ':' + T(java.lang.String).valueOf(#keyword ?: '')",
        condition = "((#page != null && #page == 1) || (#page == null && #pageNum == null))",
        unless = "#result == null"
    )
    public TableDataInfo list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        List<Map<String, Object>> list = phenotypeService.selectPhenotypeList(keyword);
        return getDataTable(list);
    }

    /**
     * 2. 获取表型详情
     */
    @Anonymous
    @GetMapping("/{phenotypeId}")
    @ApiOperation(value = "获取表型详情", notes = "根据表型ID获取详细信息")
    @ApiImplicitParam(name = "phenotypeId", value = "表型ID（如HP:0000006）", required = true, dataType = "string", paramType = "path")
    public AjaxResult getDetail(@PathVariable("phenotypeId") String phenotypeId) {
        Phenotype phenotype = phenotypeService.selectPhenotypeById(phenotypeId);
        if (phenotype == null) {
            return AjaxResult.error("表型不存在");
        }
        return AjaxResult.success(phenotype);
    }

    /**
     * 3. 获取表型的关联靶标/基因（分页）
     */
    @Anonymous
    @GetMapping("/{phenotypeId}/targets")
    @ApiOperation(value = "获取表型的关联靶标/基因", notes = "查询表型相关的靶标/基因列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phenotypeId", value = "表型ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词（基因符号或描述）", required = false, dataType = "string", paramType = "query")
    })
    public TableDataInfo getTargets(
            @PathVariable("phenotypeId") String phenotypeId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        List<Map<String, Object>> list = phenotypeService.selectTargetsByPhenotypeId(phenotypeId, keyword);
        return getDataTable(list);
    }

    /**
     * 4. 获取表型的统计信息
     */
    @Anonymous
    @GetMapping("/{phenotypeId}/statistics")
    @ApiOperation(value = "获取表型的统计信息", notes = "获取表型关联的靶标数量等统计数据")
    @ApiImplicitParam(name = "phenotypeId", value = "表型ID", required = true, dataType = "string", paramType = "path")
    public AjaxResult getStatistics(@PathVariable("phenotypeId") String phenotypeId) {
        Map<String, Object> statistics = phenotypeService.getStatistics(phenotypeId);
        return AjaxResult.success(statistics);
    }

    /**
     * 5. 获取表型的知识图谱数据
     */
    @Anonymous
    @GetMapping("/{phenotypeId}/graph")
    @ApiOperation(value = "获取表型的知识图谱数据", notes = "获取表型-靶标关系网络数据")
    @ApiImplicitParam(name = "phenotypeId", value = "表型ID", required = true, dataType = "string", paramType = "path")
    public AjaxResult getGraph(@PathVariable("phenotypeId") String phenotypeId) {
        Map<String, Object> graphData = phenotypeService.getGraphData(phenotypeId);
        return AjaxResult.success(graphData);
    }
}



