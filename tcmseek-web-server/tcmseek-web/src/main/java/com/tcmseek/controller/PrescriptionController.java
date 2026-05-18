package com.tcmseek.controller;

import com.tcmseek.common.annotation.Anonymous;
import com.tcmseek.common.core.controller.BaseController;
import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.common.core.page.TableDataInfo;
import com.tcmseek.pojo.entity.Disease;
import com.tcmseek.pojo.entity.MedicalCase;
import com.tcmseek.pojo.entity.TcmPrescription;
import com.tcmseek.pojo.entity.TcmSymptom;
import com.tcmseek.pojo.entity.TcmSyndrome;
import com.tcmseek.pojo.entity.coreTcmHerbs;
import com.tcmseek.service.PrescriptionService;

import java.util.Map;
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
 * 方剂信息Controller
 */
@RestController
@RequestMapping("/tcmseek/prescriptions")
@Slf4j
@Api(tags = "方剂信息")
public class PrescriptionController extends BaseController {

    @Autowired
    private PrescriptionService prescriptionService;

    /**
     * 1. 获取方剂列表（分页）
     */
    @Anonymous
    @GetMapping("/list")
    @ApiOperation(value = "获取方剂列表", notes = "根据关键词、来源典籍查询方剂信息，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageNum", value = "页码（框架标准）", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词（方剂名称、拼音）", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "source", value = "来源典籍", required = false, dataType = "string", paramType = "query")
    })
    @Cacheable(
        value = "prescription:list:page1",
        key = "'list:' + #pageSize + ':' + T(java.lang.String).valueOf(#keyword ?: '') + ':' + T(java.lang.String).valueOf(#source ?: '')",
        condition = "((#page != null && #page == 1) || (#page == null && #pageNum == null))",
        unless = "#result == null"
    )
    public TableDataInfo list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "source", required = false) String source
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        List<TcmPrescription> list = prescriptionService.selectPrescriptionList(keyword, source);
        return getDataTable(list);
    }

    /**
     * 2. 获取方剂详情
     */
    @Anonymous
    @GetMapping("/{prescriptionId}")
    @ApiOperation(value = "获取方剂详情", notes = "根据方剂ID获取详细信息")
    @ApiImplicitParam(name = "prescriptionId", value = "方剂ID（如TCMSSD59734）", required = true, dataType = "string", paramType = "path")
    public AjaxResult getDetail(@PathVariable("prescriptionId") String prescriptionId) {
        log.info("获取方剂详情, prescriptionId: {}", prescriptionId);
        TcmPrescription prescription = prescriptionService.selectPrescriptionById(prescriptionId);
        if (prescription == null) {
            log.warn("方剂不存在, prescriptionId: {}", prescriptionId);
            return AjaxResult.error("方剂不存在");
        }
        return AjaxResult.success(prescription);
    }

    /**
     * 3. 获取方剂的中药组成（分页）- 已废弃，请使用 /core-herbs
     * @deprecated 请使用 getCoreHerbs 方法
     */
    @Deprecated
    @Anonymous
    @GetMapping("/{prescriptionId}/herbs")
    @ApiOperation(value = "获取方剂的中药组成（已废弃）", notes = "查询方剂包含的所有中药，请使用 /core-herbs")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "prescriptionId", value = "方剂ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getHerbs(
            @PathVariable("prescriptionId") String prescriptionId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        // 重定向到 getCoreHerbs
        return getCoreHerbs(prescriptionId, page, pageNum, pageSize);
    }

    /**
     * 3.1. 获取方剂的核心中药（分页）
     */
    @Anonymous
    @GetMapping("/{prescriptionId}/core-herbs")
    @ApiOperation(value = "获取方剂的核心中药", notes = "查询方剂的核心中药组成，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "prescriptionId", value = "方剂ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getCoreHerbs(
            @PathVariable("prescriptionId") String prescriptionId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        log.info("获取方剂的核心中药, prescriptionId: {}, page: {}, pageSize: {}", prescriptionId, currentPage, pageSize);
        
        List<coreTcmHerbs> list = prescriptionService.selectCoreHerbsByPrescriptionId(prescriptionId);
        
        log.info("查询到核心中药数量: {}", list != null ? list.size() : 0);
        return getDataTable(list);
    }

    /**
     * 3.2. 获取方剂的其他中药（分页）
     */
    @Anonymous
    @GetMapping("/{prescriptionId}/other-herbs")
    @ApiOperation(value = "获取方剂的其他中药", notes = "查询方剂的其他（非核心）中药组成，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "prescriptionId", value = "方剂ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getOtherHerbs(
            @PathVariable("prescriptionId") String prescriptionId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        log.info("获取方剂的其他中药, prescriptionId: {}, page: {}, pageSize: {}", prescriptionId, currentPage, pageSize);
        
        List<Map<String, Object>> list = prescriptionService.selectOtherHerbsByPrescriptionId(prescriptionId);
        
        log.info("查询到其他中药数量: {}", list != null ? list.size() : 0);
        return getDataTable(list);
    }

    /**
     * 4. 获取方剂的疾病关联（分页）
     */
    @Anonymous
    @GetMapping("/{prescriptionId}/diseases")
    @ApiOperation(value = "获取方剂的疾病关联", notes = "查询方剂可治疗的疾病")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "prescriptionId", value = "方剂ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getDiseases(
            @PathVariable("prescriptionId") String prescriptionId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        log.info("获取方剂的疾病关联, prescriptionId: {}, page: {}, pageSize: {}", prescriptionId, currentPage, pageSize);
        
        List<Disease> list = prescriptionService.selectDiseasesByPrescriptionId(prescriptionId);
        
        log.info("查询到疾病数量: {}", list != null ? list.size() : 0);
        return getDataTable(list);
    }

    /**
     * 5. 获取方剂的症状关联（分页）
     */
    @Anonymous
    @GetMapping("/{prescriptionId}/symptoms")
    @ApiOperation(value = "获取方剂的症状关联", notes = "查询方剂相关的症状列表，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "prescriptionId", value = "方剂ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getSymptoms(
            @PathVariable("prescriptionId") String prescriptionId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        log.info("获取方剂的症状关联, prescriptionId: {}, page: {}, pageSize: {}", prescriptionId, currentPage, pageSize);
        
        List<TcmSymptom> list = prescriptionService.selectSymptomsByPrescriptionId(prescriptionId);
        
        log.info("查询到症状数量: {}", list != null ? list.size() : 0);
        return getDataTable(list);
    }

    /**
     * 6. 获取方剂的证候关联（分页）
     */
    @Anonymous
    @GetMapping("/{prescriptionId}/syndromes")
    @ApiOperation(value = "获取方剂的证候关联", notes = "查询方剂相关的证候")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "prescriptionId", value = "方剂ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getSyndromes(
            @PathVariable("prescriptionId") String prescriptionId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        log.info("获取方剂的证候关联, prescriptionId: {}, page: {}, pageSize: {}", prescriptionId, currentPage, pageSize);
        
        List<TcmSyndrome> list = prescriptionService.selectSyndromesByPrescriptionId(prescriptionId);
        
        log.info("查询到证候数量: {}", list != null ? list.size() : 0);
        return getDataTable(list);
    }

    /**
     * 7. 获取方剂的医案关联（分页）
     */
    @Anonymous
    @GetMapping("/{prescriptionId}/medical-cases")
    @ApiOperation(value = "获取方剂的医案关联", notes = "查询使用该方剂的医案")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "prescriptionId", value = "方剂ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getMedicalCases(
            @PathVariable("prescriptionId") String prescriptionId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        log.info("获取方剂的医案关联, prescriptionId: {}, page: {}, pageSize: {}", prescriptionId, currentPage, pageSize);
        
        List<MedicalCase> list = prescriptionService.selectMedicalCasesByPrescriptionId(prescriptionId);
        
        log.info("查询到医案数量: {}", list != null ? list.size() : 0);
        return getDataTable(list);
    }
    
    /**
     * 8. 获取方剂的转录组学数据
     */
    @Anonymous
    @GetMapping("/{prescriptionId}/transcriptomics")
    @ApiOperation(value = "获取方剂的转录组学数据", notes = "查询方剂的基因表达数据")
    @ApiImplicitParam(name = "prescriptionId", value = "方剂ID", required = true, dataType = "string", paramType = "path")
    public AjaxResult getTranscriptomics(@PathVariable("prescriptionId") String prescriptionId) {
        log.info("获取方剂转录组学数据, prescriptionId: {}", prescriptionId);
        List<Map<String, Object>> data = prescriptionService.getTranscriptomicsData(prescriptionId);
        return AjaxResult.success(data);
    }
    
    /**
     * 9. 获取方剂转录组学统计信息
     */
    @Anonymous
    @GetMapping("/{prescriptionId}/transcriptomics/statistics")
    @ApiOperation(value = "获取方剂转录组学统计信息", notes = "获取上调、下调、显著基因数量等统计")
    @ApiImplicitParam(name = "prescriptionId", value = "方剂ID", required = true, dataType = "string", paramType = "path")
    public AjaxResult getTranscriptomicsStatistics(@PathVariable("prescriptionId") String prescriptionId) {
        log.info("获取方剂转录组学统计信息, prescriptionId: {}", prescriptionId);
        Map<String, Object> statistics = prescriptionService.getTranscriptomicsStatistics(prescriptionId);
        return AjaxResult.success(statistics);
    }
    
    /**
     * 10. 获取方剂组成中药的转录组学数据（用于对比分析）
     */
    @Anonymous
    @GetMapping("/{prescriptionId}/herbs-transcriptomics")
    @ApiOperation(value = "获取方剂组成中药的转录组学数据", notes = "查询方剂各组成中药的转录组学数据，用于对比分析")
    @ApiImplicitParam(name = "prescriptionId", value = "方剂ID", required = true, dataType = "string", paramType = "path")
    public AjaxResult getHerbsTranscriptomics(@PathVariable("prescriptionId") String prescriptionId) {
        log.info("获取方剂组成中药的转录组学数据, prescriptionId: {}", prescriptionId);
        List<Map<String, Object>> data = prescriptionService.getHerbsTranscriptomicsData(prescriptionId);
        return AjaxResult.success(data);
    }
}

