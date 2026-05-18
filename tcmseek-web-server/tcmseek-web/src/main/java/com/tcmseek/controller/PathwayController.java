package com.tcmseek.controller;

import com.github.pagehelper.PageHelper;
import com.tcmseek.common.annotation.Anonymous;
import com.tcmseek.common.core.controller.BaseController;
import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.common.core.page.TableDataInfo;
import com.tcmseek.pojo.entity.Pathway;
import com.tcmseek.service.PathwayService;
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
 * 通路信息Controller
 */
@RestController
@RequestMapping("/tcmseek/pathways")
@Slf4j
@Api(tags = "通路信息")
public class PathwayController extends BaseController {

    @Autowired
    private PathwayService pathwayService;

    /**
     * 1. 获取通路列表（分页）
     */
    @Anonymous
    @GetMapping("/list")
    @ApiOperation(value = "获取通路列表", notes = "根据关键词查询通路信息，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词（通路名称或KEGG ID）", required = false, dataType = "string", paramType = "query")
    })
    @Cacheable(
        value = "pathway:list:page1",
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
        PageHelper.startPage(currentPage, pageSize);
        
        List<Map<String, Object>> list = pathwayService.selectPathwayList(keyword);
        return getDataTable(list);
    }

    /**
     * 2. 获取通路详情
     */
    @Anonymous
    @GetMapping("/{pathwayId}")
    @ApiOperation(value = "获取通路详情", notes = "根据通路ID获取详细信息")
    @ApiImplicitParam(name = "pathwayId", value = "通路ID（如hsa01100）", required = true, dataType = "string", paramType = "path")
    public AjaxResult getDetail(@PathVariable("pathwayId") String pathwayId) {
        Pathway pathway = pathwayService.selectPathwayById(pathwayId);
        if (pathway == null) {
            return AjaxResult.error("通路不存在");
        }
        return AjaxResult.success(pathway);
    }

    /**
     * 3. 获取通路的关联靶标/基因（分页）
     */
    @Anonymous
    @GetMapping("/{pathwayId}/targets")
    @ApiOperation(value = "获取通路的关联靶标/基因", notes = "查询通路相关的靶标/基因列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pathwayId", value = "通路ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词（基因符号或描述）", required = false, dataType = "string", paramType = "query")
    })
    public TableDataInfo getTargets(
            @PathVariable("pathwayId") String pathwayId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        PageHelper.startPage(currentPage, pageSize);
        
        List<Map<String, Object>> list = pathwayService.selectTargetsByPathwayId(pathwayId, keyword);
        return getDataTable(list);
    }

    /**
     * 4. 获取影响此通路的化合物（分页）
     */
    @Anonymous
    @GetMapping("/{pathwayId}/compounds")
    @ApiOperation(value = "获取影响此通路的化合物", notes = "查询通过靶标影响此通路的化合物列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pathwayId", value = "通路ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词", required = false, dataType = "string", paramType = "query")
    })
    public TableDataInfo getCompounds(
            @PathVariable("pathwayId") String pathwayId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        PageHelper.startPage(currentPage, pageSize);
        
        List<Map<String, Object>> list = pathwayService.selectCompoundsByPathwayId(pathwayId, keyword);
        return getDataTable(list);
    }

    /**
     * 5. 获取影响此通路的中药（分页）
     */
    @Anonymous
    @GetMapping("/{pathwayId}/herbs")
    @ApiOperation(value = "获取影响此通路的中药", notes = "查询通过化合物-靶标影响此通路的中药列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pathwayId", value = "通路ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词", required = false, dataType = "string", paramType = "query")
    })
    public TableDataInfo getHerbs(
            @PathVariable("pathwayId") String pathwayId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);

        PageHelper.startPage(currentPage, pageSize);
        
        List<Map<String, Object>> list = pathwayService.selectHerbsByPathwayId(pathwayId, keyword);
        return getDataTable(list);
    }

    /**
     * 6. 获取通路的统计信息
     */
    @Anonymous
    @GetMapping("/{pathwayId}/statistics")
    @ApiOperation(value = "获取通路的统计信息", notes = "获取通路关联的靶标、化合物、中药数量等统计数据")
    @ApiImplicitParam(name = "pathwayId", value = "通路ID", required = true, dataType = "string", paramType = "path")
    public AjaxResult getStatistics(@PathVariable("pathwayId") String pathwayId) {
        Map<String, Object> statistics = pathwayService.getStatistics(pathwayId);
        return AjaxResult.success(statistics);
    }

    /**
     * 7. 获取通路的知识图谱数据
     */
    @Anonymous
    @GetMapping("/{pathwayId}/graph")
    @ApiOperation(value = "获取通路的知识图谱数据", notes = "获取通路-靶标关系网络数据")
    @ApiImplicitParam(name = "pathwayId", value = "通路ID", required = true, dataType = "string", paramType = "path")
    public AjaxResult getGraph(@PathVariable("pathwayId") String pathwayId) {
        Map<String, Object> graphData = pathwayService.getGraphData(pathwayId);
        return AjaxResult.success(graphData);
    }
}

