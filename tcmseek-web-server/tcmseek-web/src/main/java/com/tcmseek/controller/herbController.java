package com.tcmseek.controller;

import com.github.pagehelper.PageHelper;
import com.tcmseek.common.annotation.Anonymous;
import com.tcmseek.common.core.controller.BaseController;
import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.common.core.page.TableDataInfo;
import com.tcmseek.pojo.entity.*;
import com.tcmseek.pojo.vo.TcmCompoundD3;
import com.tcmseek.service.HerbService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 中药材信息Controller
 */
@RestController
@RequestMapping("/tcmseek/herbs")
@Slf4j
@Api(tags = "中药材信息")
public class herbController extends BaseController {

    @Autowired
    private HerbService herbService;

    /**
     * 1. 查询中药材列表（分页）
     * @param page 页码（前端传page，兼容pageNum）
     * @param pageNum 页码（框架标准参数）
     * @param pageSize 每页条数
     * @param keyword 搜索关键词（中药名称、拼音、拉丁名）
     * @param type 药材类型
     * @param efficacyCategory 功效分类
     * @return 分页数据
     */
    @Anonymous
    @GetMapping("/list")
    @ApiOperation(value = "查询中药材列表", notes = "根据关键词、类型和功效分类查询中药材信息，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码（前端使用page参数）", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageNum", value = "页码（也支持pageNum参数）", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词（中药名称、拼音、拉丁名）", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "type", value = "药材类型", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "efficacyCategory", value = "功效分类", required = false, dataType = "string", paramType = "query")
    })
    @Cacheable(
        value = "herb:list:page1",
        key = "'list:' + #pageSize + ':' + T(java.lang.String).valueOf(#keyword ?: '') + ':' + T(java.lang.String).valueOf(#type ?: '') + ':' + T(java.lang.String).valueOf(#efficacyCategory ?: '')",
        condition = "((#page != null && #page == 1) || (#page == null && #pageNum == null))",
        unless = "#result == null "
    )
    public TableDataInfo list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "efficacyCategory", required = false) String efficacyCategory
    ) {
        // 兼容前端的page参数和框架的pageNum参数
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);

        // 手动启动分页
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);

        // 查询列表
        List<coreTcmHerbs> list = herbService.selectHerbList(keyword, type, efficacyCategory);

        // 返回分页数据
        return getDataTable(list);
    }

    /**
     * 2. 获取中药材详情
     * @param herbId 中药ID
     * @return 中药材详细信息
     */
    @Anonymous
    @GetMapping("/{herbId}")
    @ApiOperation(value = "获取中药材详情", notes = "根据中药ID获取详细信息")
    @ApiImplicitParam(name = "herbId", value = "中药ID（如HERB_1）", required = true, dataType = "string", paramType = "path")
    @Cacheable(value = "herb:detail",key = "'herb:' + #herbId" , unless = "#result == null")
    public AjaxResult getDetail(@PathVariable("herbId") String herbId) {
        coreTcmHerbs herb = herbService.selectHerbById(herbId);
        if (herb == null) {
            return AjaxResult.error("中药材不存在");
        }
        return AjaxResult.success(herb);
    }

    /**
     * 3. 全文搜索中药材（分页）
     * @param keyword 搜索关键词
     * @param page 页码
     * @param pageNum 页码（框架标准）
     * @param pageSize 每页条数
     * @return 分页数据
     */
    @Anonymous
    @PostMapping("/search")
    @ApiOperation(value = "全文搜索中药材", notes = "搜索功效和主治字段，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "keyword", value = "搜索关键词", required = true, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo search(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        // 兼容前端的page参数
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);

        // 手动启动分页
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);

        // 全文搜索
        List<coreTcmHerbs> list = herbService.searchHerbsByFulltext(keyword);

        // 返回分页数据
        return getDataTable(list);
    }

    /**
     * 4. 获取中药材的化合物列表（分页）
     * @param herbId 中药ID
     * @param page 页码
     * @param pageNum 页码（框架标准）
     * @param pageSize 每页条数
     * @return 分页数据
     */
    @Anonymous
    @GetMapping("/{herbId}/compounds")
    @ApiOperation(value = "获取中药材的化合物列表", notes = "根据中药ID查询其包含的所有化合物，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "herbId", value = "中药ID（如HERB_1）", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getCompounds(
            @PathVariable("herbId") String herbId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        // 兼容前端的page参数
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);

        // 手动启动分页
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);

        // 查询化合物列表
        List<TcmCompound> list = herbService.selectCompoundsByHerbId(herbId);

        // 返回分页数据
        return getDataTable(list);
    }

    /***
     *  获取中药材的化合物列表
     *   *
     *  * @param herbId 中药ID
     */
    @Anonymous
    @GetMapping("/{herbId}/allcompounds")
    @ApiOperation(value = "获取中药材的化合物列表", notes = "根据中药ID查询其包含的所有化合物")
    public AjaxResult getAllCompounds(@PathVariable("herbId") String herbId) {
        List<TcmCompoundD3> list = herbService.getAllCompounds(herbId);
        return AjaxResult.success(list);
    }

    /**
     * 5. 获取中药材的疾病关联（分页）
     * @param herbId 中药ID
     * @param page 页码
     * @param pageNum 页码（框架标准）
     * @param pageSize 每页条数
     * @return 分页数据
     */
    @Anonymous
    @GetMapping("/{herbId}/diseases")
    @ApiOperation(value = "获取中药材的疾病关联", notes = "根据中药ID查询可治疗的疾病列表，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "herbId", value = "中药ID（如HERB_1）", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getDiseases(
            @PathVariable("herbId") String herbId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        // 兼容前端的page参数
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);

        // 手动启动分页
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);

        // 查询疾病关联
        List<Disease> list = herbService.selectDiseasesByHerbId(herbId);

        // 返回分页数据
        return getDataTable(list);
    }

    /**
     * 6. 获取中药材的症状关联（分页）
     */
    @Anonymous
    @GetMapping("/{herbId}/symptoms")
    @ApiOperation(value = "获取中药材的症状关联", notes = "根据中药ID查询相关症状列表，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "herbId", value = "中药ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getSymptoms(
            @PathVariable("herbId") String herbId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);

        log.info("获取中药材的症状关联, herbId: {}, page: {}, pageSize: {}", herbId, currentPage, pageSize);

        List<TcmSymptom> list = herbService.selectSymptomsByHerbId(herbId);

        log.info("查询到症状数量: {}", list != null ? list.size() : 0);
        return getDataTable(list);
    }

    /**
     * 7. 获取中药材的证候关联（分页）
     */
    @Anonymous
    @GetMapping("/{herbId}/syndromes")
    @ApiOperation(value = "获取中药材的证候关联", notes = "根据中药ID查询相关证候列表，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "herbId", value = "中药ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getSyndromes(
            @PathVariable("herbId") String herbId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);

        log.info("获取中药材的证候关联, herbId: {}, page: {}, pageSize: {}", herbId, currentPage, pageSize);

        List<TcmSyndrome> list = herbService.selectSyndromesByHerbId(herbId);

        log.info("查询到证候数量: {}", list != null ? list.size() : 0);
        return getDataTable(list);
    }

    /**
     * 8. 获取中药的方剂关联（分页）
     *
     */
    @Anonymous
    @GetMapping("/{herbId}/formulas")
    @ApiOperation(value = "获取中药的方剂关联", notes = "根据中药ID查询相关方剂列表，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "herbId", value = "中药ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageNum", value = "页码（框架标准）", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getFormulas(
            @PathVariable("herbId") String herbId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);

        List<tcmPrescriptions> list = herbService.selectFormulasByHerbId(herbId);
        return getDataTable(list);
    }

    /**
     * 9. 获取中药的中医医案关联（分页）
     */
    @Anonymous
    @GetMapping("/{herbId}/medicalCases")
    @ApiOperation(value = "获取中药的中医医案关联", notes = "根据中药ID查询相关中医医案列表，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "herbId", value = "中药ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageNum", value = "页码（框架标准）", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getMedicalCases(
            @PathVariable("herbId") String herbId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        List<medicalCases> list = herbService.selectMedicalCasesByHerbId(herbId);
        return getDataTable(list);
    }

    /**
     * 10. 获取中药转录组学数据
     */
    @Anonymous
    @GetMapping("/{herbId}/transcriptomics")
    @ApiOperation(value = "获取中药转录组学数据", notes = "根据中药ID查询转录组学数据（基因表达差异）")
    @ApiImplicitParam(name = "herbId", value = "中药ID", required = true, dataType = "string", paramType = "path")
    public AjaxResult getTranscriptomics(@PathVariable("herbId") String herbId) {
        log.info("获取中药转录组学数据, herbId: {}", herbId);
        List<java.util.Map<String, Object>> data = herbService.getTranscriptomicsData(herbId);
        return AjaxResult.success(data);
    }

    /**
     * 11. 获取中药转录组学统计信息
     */
    @Anonymous
    @GetMapping("/{herbId}/transcriptomics/statistics")
    @ApiOperation(value = "获取中药转录组学统计信息", notes = "根据中药ID查询转录组学统计数据")
    @ApiImplicitParam(name = "herbId", value = "中药ID", required = true, dataType = "string", paramType = "path")
    public AjaxResult getTranscriptomicsStatistics(@PathVariable("herbId") String herbId) {
        log.info("获取中药转录组学统计信息, herbId: {}", herbId);
        java.util.Map<String, Object> stats = herbService.getTranscriptomicsStatistics(herbId);
        return AjaxResult.success(stats);
    }

}
