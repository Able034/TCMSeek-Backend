package com.tcmseek.controller;

import com.tcmseek.common.annotation.Anonymous;
import com.tcmseek.common.core.controller.BaseController;
import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.common.core.page.TableDataInfo;
import com.tcmseek.pojo.entity.CompoundAdmet;
import com.tcmseek.pojo.entity.Target;
import com.tcmseek.pojo.entity.TcmCompound;
import com.tcmseek.pojo.vo.TargetD3VO;
import com.tcmseek.service.CompoundService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 化合物信息Controller
 */
@RestController
@RequestMapping("/tcmseek/compounds")
@Slf4j
@Api(tags = "化合物信息")
public class CompoundController extends BaseController {

    @Autowired
    private CompoundService compoundService;

    /**
     * 0. 获取化合物列表（分页）
     */
    @Anonymous
    @GetMapping("/list")
    @ApiOperation(value = "获取化合物列表", notes = "查询化合物列表，支持关键词搜索和分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词（InChIKey或分子式）", required = false, dataType = "string", paramType = "query")
    })
    @Cacheable(
        value = "compound:list:page1",
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
        
        log.info("获取化合物列表, page: {}, pageSize: {}, keyword: {}", currentPage, pageSize, keyword);
        
        List<TcmCompound> list = compoundService.selectCompoundsList(keyword);
        
        log.info("查询到化合物数量: {}", list != null ? list.size() : 0);
        return getDataTable(list);
    }

    /**
     * 1. 获取化合物详情
     */
    @Anonymous
    @GetMapping("/{inchikey}")
    @ApiOperation(value = "获取化合物详情", notes = "根据InChIKey获取化合物详细信息")
    @ApiImplicitParam(name = "inchikey", value = "InChIKey", required = true, dataType = "string", paramType = "path")
    @Cacheable(value = "compound:detail" , key = "'compound' + #inchikey" , unless = "#result == null")
    public AjaxResult getDetail(@PathVariable("inchikey") String inchikey) {
        TcmCompound compound = compoundService.selectCompoundByInchikey(inchikey);
        if (compound == null) {
            return AjaxResult.error("化合物不存在");
        }
        return AjaxResult.success(compound);
    }

    /**
     * 2. 获取化合物的靶标列表（分页）
     */
    @Anonymous
    @GetMapping("/{inchikey}/targets")
    @ApiOperation(value = "获取化合物的靶标列表", notes = "查询化合物作用的靶标基因")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "inchikey", value = "InChIKey", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getTargets(
            @PathVariable("inchikey") String inchikey,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        List<Target> list = compoundService.selectTargetsByInchikey(inchikey);
        return getDataTable(list);
    }

    /**
     * 3. 获取化合物的ADMET性质
     */
    @Anonymous
    @GetMapping("/{inchikey}/admet")
    @ApiOperation(value = "获取化合物的ADMET性质", notes = "查询化合物的药物代谢动力学性质（28个参数）")
    @ApiImplicitParam(name = "inchikey", value = "InChIKey", required = true, dataType = "string", paramType = "path")
    public AjaxResult getAdmet(@PathVariable("inchikey") String inchikey) {
        CompoundAdmet admet = compoundService.selectAdmetByInchikey(inchikey);
        if (admet == null) {
            return AjaxResult.error("ADMET数据不存在");
        }
        return AjaxResult.success(admet);
    }

    /**
     * 4. 获取化合物的靶标
     */
    @Anonymous
    @GetMapping("/{inchikey}/alltargets")
    @ApiOperation(value = "获取化合物的全部靶标", notes = "查询化合物作用的靶标基因")
    @ApiImplicitParams({@ApiImplicitParam(name = "inchikey", value = "InChIKey", required = true, dataType = "string", paramType = "path"),})
    public AjaxResult getAllTargets(@PathVariable("inchikey") String inchikey) {
        List<TargetD3VO> list = compoundService.getAllTargets(inchikey);
        return AjaxResult.success( list);
    }

    /**
     * 5. 批量获取多个化合物的靶标信息（优化版：去重传输）
     */
    @Anonymous
    @PostMapping("/batch/targets")
    @ApiOperation(value = "批量获取化合物靶标", notes = "一次性查询多个化合物的靶标信息，返回优化的去重结构")
    public AjaxResult batchGetTargets(@RequestBody Map<String, Object> requestBody) {
        @SuppressWarnings("unchecked")
        List<String> inchikeys = (List<String>) requestBody.get("inchikeys");
        
        if (inchikeys == null || inchikeys.isEmpty()) {
            return AjaxResult.error("inchikeys参数不能为空");
        }
        
        log.info("批量查询化合物靶标, 数量: {}", inchikeys.size());
        
        // 调用服务层批量查询（返回优化的去重结构）
        Map<String, Object> result = compoundService.batchGetAllTargets(inchikeys);
        
        log.info("批量查询完成, 返回优化数据结构");
        
        return AjaxResult.success(result);
    }

    /**
     * 6. 获取化合物的关联中药材列表（分页）
     */
    @Anonymous
    @GetMapping("/{inchikey}/herbs")
    @ApiOperation(value = "获取化合物的关联中药材", notes = "查询包含该化合物的中药材列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "inchikey", value = "InChIKey", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getHerbs(
            @PathVariable("inchikey") String inchikey,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        log.info("获取化合物的关联中药材, inchikey: {}, page: {}, pageSize: {}", inchikey, currentPage, pageSize);
        
        List<com.tcmseek.pojo.entity.coreTcmHerbs> list = compoundService.selectHerbsByInchikey(inchikey);
        
        log.info("查询到中药材数量: {}", list != null ? list.size() : 0);
        return getDataTable(list);
    }
    
    /**
     * 7. 获取化合物的转录组学数据
     */
    @Anonymous
    @GetMapping("/{inchikey}/transcriptomics")
    @ApiOperation(value = "获取化合物的转录组学数据", notes = "查询化合物对基因表达的影响数据（log2FC, p-value等）")
    @ApiImplicitParam(name = "inchikey", value = "InChIKey", required = true, dataType = "string", paramType = "path")
    public AjaxResult getTranscriptomics(@PathVariable("inchikey") String inchikey) {
        log.info("获取化合物的转录组学数据, inchikey: {}", inchikey);
        
        List<Map<String, Object>> data = compoundService.getTranscriptomicsData(inchikey);
        if (data == null || data.isEmpty()) {
            return AjaxResult.success("暂无转录组学数据", new ArrayList<>());
        }
        
        log.info("查询到转录组学数据数量: {}", data.size());
        return AjaxResult.success(data);
    }
    
    /**
     * 8. 获取化合物的转录组学统计信息
     */
    @Anonymous
    @GetMapping("/{inchikey}/transcriptomics/statistics")
    @ApiOperation(value = "获取化合物的转录组学统计信息", notes = "统计上调、下调基因数量等")
    @ApiImplicitParam(name = "inchikey", value = "InChIKey", required = true, dataType = "string", paramType = "path")
    public AjaxResult getTranscriptomicsStatistics(@PathVariable("inchikey") String inchikey) {
        log.info("获取化合物的转录组学统计信息, inchikey: {}", inchikey);
        
        Map<String, Object> statistics = compoundService.getTranscriptomicsStatistics(inchikey);
        return AjaxResult.success(statistics);
    }
}

