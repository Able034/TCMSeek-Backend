package com.tcmseek.controller;

import com.tcmseek.common.annotation.Anonymous;
import com.tcmseek.common.core.controller.BaseController;
import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.common.core.page.TableDataInfo;
import com.tcmseek.pojo.entity.TcmSymptom;
import com.tcmseek.pojo.entity.TcmSyndrome;
import com.tcmseek.pojo.entity.coreTcmHerbs;
import com.tcmseek.service.SyndromeService;
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
 * 证候信息Controller
 */
@RestController
@RequestMapping("/tcmseek/syndromes")
@Slf4j
@Api(tags = "中医证候")
public class SyndromeController extends BaseController {

    @Autowired
    private SyndromeService syndromeService;

    /**
     * 1. 获取证候列表（分页）
     */
    @Anonymous
    @GetMapping("/list")
    @ApiOperation(value = "获取证候列表", notes = "根据关键词、分类查询证候信息，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "category", value = "证候分类", required = false, dataType = "string", paramType = "query")
    })
    @Cacheable(
        value = "syndrome:list:page1",
        key = "'list:' + #pageSize + ':' + T(java.lang.String).valueOf(#keyword ?: '') + ':' + T(java.lang.String).valueOf(#category ?: '')",
        condition = "((#page != null && #page == 1) || (#page == null && #pageNum == null))",
        unless = "#result == null"
    )
    public TableDataInfo list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        List<TcmSyndrome> list = syndromeService.selectSyndromeList(keyword, category);
        return getDataTable(list);
    }

    /**
     * 2. 获取证候详情
     */
    @Anonymous
    @GetMapping("/{syndromeId}")
    @ApiOperation(value = "获取证候详情", notes = "根据证候ID获取详细信息")
    @ApiImplicitParam(name = "syndromeId", value = "证候ID（如TCM_Syndrome1）", required = true, dataType = "string", paramType = "path")
    public AjaxResult getDetail(@PathVariable("syndromeId") String syndromeId) {
        TcmSyndrome syndrome = syndromeService.selectSyndromeById(syndromeId);
        if (syndrome == null) {
            return AjaxResult.error("证候不存在");
        }
        return AjaxResult.success(syndrome);
    }

    /**
     * 3. 获取证候的相关症状（分页）
     */
    @Anonymous
    @GetMapping("/{syndromeId}/symptoms")
    @ApiOperation(value = "获取证候的相关症状", notes = "查询证候相关的症状列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "syndromeId", value = "证候ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getSymptoms(
            @PathVariable("syndromeId") String syndromeId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        List<TcmSymptom> list = syndromeService.selectSymptomsBySyndromeId(syndromeId);
        return getDataTable(list);
    }

    /**
     * 4. 获取证候的相关中药（分页）
     */
    @Anonymous
    @GetMapping("/{syndromeId}/herbs")
    @ApiOperation(value = "获取证候的相关中药", notes = "查询可治疗该证候的中药")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "syndromeId", value = "证候ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getHerbs(
            @PathVariable("syndromeId") String syndromeId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        List<coreTcmHerbs> list = syndromeService.selectHerbsBySyndromeId(syndromeId);
        return getDataTable(list);
    }

    /**
     * 5. 获取证候的关联方剂（分页）
     */
    @Anonymous
    @GetMapping("/{syndromeId}/prescriptions")
    @ApiOperation(value = "获取证候的关联方剂", notes = "查询可治疗该证候的方剂")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "syndromeId", value = "证候ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getPrescriptions(
            @PathVariable("syndromeId") String syndromeId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        List<com.tcmseek.pojo.entity.TcmPrescription> list = syndromeService.selectPrescriptionsBySyndromeId(syndromeId);
        return getDataTable(list);
    }
}

