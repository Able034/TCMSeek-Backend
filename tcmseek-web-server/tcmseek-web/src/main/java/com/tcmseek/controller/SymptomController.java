package com.tcmseek.controller;

import com.tcmseek.common.annotation.Anonymous;
import com.tcmseek.common.core.controller.BaseController;
import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.common.core.page.TableDataInfo;
import com.tcmseek.pojo.entity.TcmPrescription;
import com.tcmseek.pojo.entity.TcmSymptom;
import com.tcmseek.pojo.entity.coreTcmHerbs;
import com.tcmseek.service.SymptomService;
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
 * 中医症状Controller
 */
@RestController
@RequestMapping("/tcmseek/symptoms")
@Slf4j
@Api(tags = "中医症状")
public class SymptomController extends BaseController {

    @Autowired
    private SymptomService symptomService;

    /**
     * 1. 获取中医症状列表（分页）
     */
    @Anonymous
    @GetMapping("/list")
    @ApiOperation(value = "获取中医症状列表", notes = "根据关键词、部位、类型查询症状信息，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageNum", value = "页码（框架标准）", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "locus", value = "症状部位", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "type", value = "症状类型", required = false, dataType = "string", paramType = "query")
    })
    @Cacheable(
        value = "symptom:list:page1",
        key = "'list:' + #pageSize + ':' + T(java.lang.String).valueOf(#keyword ?: '') + ':' + T(java.lang.String).valueOf(#locus ?: '') + ':' + T(java.lang.String).valueOf(#type ?: '')",
        condition = "((#page != null && #page == 1) || (#page == null && #pageNum == null))",
        unless = "#result == null"
    )
    public TableDataInfo list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "locus", required = false) String locus,
            @RequestParam(value = "type", required = false) String type
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        List<TcmSymptom> list = symptomService.selectSymptomList(keyword, locus, type);
        return getDataTable(list);
    }

    /**
     * 2. 获取症状详情
     */
    @Anonymous
    @GetMapping("/{symptomId}")
    @ApiOperation(value = "获取症状详情", notes = "根据症状ID获取详细信息")
    @ApiImplicitParam(name = "symptomId", value = "症状ID（如TCM_Symptom1）", required = true, dataType = "string", paramType = "path")
    public AjaxResult getDetail(@PathVariable("symptomId") String symptomId) {
        TcmSymptom symptom = symptomService.selectSymptomById(symptomId);
        if (symptom == null) {
            return AjaxResult.error("症状不存在");
        }
        return AjaxResult.success(symptom);
    }

    /**
     * 3. 获取症状的相关中药（分页）
     */
    @Anonymous
    @GetMapping("/{symptomId}/herbs")
    @ApiOperation(value = "获取症状的相关中药", notes = "查询可治疗该症状的中药")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symptomId", value = "症状ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getHerbs(
            @PathVariable("symptomId") String symptomId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        List<coreTcmHerbs> list = symptomService.selectHerbsBySymptomId(symptomId);
        return getDataTable(list);
    }

    /**
     * 4. 获取症状的相关方剂（分页）
     */
    @Anonymous
    @GetMapping("/{symptomId}/prescriptions")
    @ApiOperation(value = "获取症状的相关方剂", notes = "查询可治疗该症状的方剂")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symptomId", value = "症状ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getPrescriptions(
            @PathVariable("symptomId") String symptomId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        List<TcmPrescription> list = symptomService.selectPrescriptionsBySymptomId(symptomId);
        return getDataTable(list);
    }

    /**
     * 5. 获取症状的关联证候（分页）
     */
    @Anonymous
    @GetMapping("/{symptomId}/syndromes")
    @ApiOperation(value = "获取症状的关联证候", notes = "查询与该症状相关的证候")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symptomId", value = "症状ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getSyndromes(
            @PathVariable("symptomId") String symptomId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        List<com.tcmseek.pojo.entity.TcmSyndrome> list = symptomService.selectSyndromesBySymptomId(symptomId);
        return getDataTable(list);
    }

    /**
     * 6. 获取中医症状的关联西医症状（分页）
     */
    @Anonymous
    @GetMapping("/{symptomId}/wm-symptoms")
    @ApiOperation(value = "获取中医症状的关联西医症状", notes = "查询与该中医症状相关的西医症状")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symptomId", value = "中医症状ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getWmSymptoms(
            @PathVariable("symptomId") String symptomId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        List<com.tcmseek.pojo.entity.WmSymptom> list = symptomService.selectWmSymptomsBySymptomId(symptomId);
        return getDataTable(list);
    }
}

