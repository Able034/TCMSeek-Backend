package com.tcmseek.controller;

import com.tcmseek.common.annotation.Anonymous;
import com.tcmseek.common.core.controller.BaseController;
import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.common.core.page.TableDataInfo;
import com.tcmseek.pojo.entity.TcmSymptom;
import com.tcmseek.pojo.entity.WmSymptom;
import com.tcmseek.service.WmSymptomService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 西医症状Controller
 */
@RestController
@RequestMapping("/tcmseek/wm-symptoms")
@Slf4j
@Api(tags = "西医症状")
public class WmSymptomController extends BaseController {

    @Autowired
    private WmSymptomService wmSymptomService;

    /**
     * 1. 获取西医症状列表（分页）
     */
    @Anonymous
    @GetMapping("/list")
    @ApiOperation(value = "获取西医症状列表", notes = "根据关键词查询西医症状信息，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageNum", value = "页码（框架标准）", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词", required = false, dataType = "string", paramType = "query")
    })
    public TableDataInfo list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        List<WmSymptom> list = wmSymptomService.selectWmSymptomList(keyword);
        return getDataTable(list);
    }

    /**
     * 2. 获取西医症状详情
     */
    @Anonymous
    @GetMapping("/{wmSymptomId}")
    @ApiOperation(value = "获取西医症状详情", notes = "根据西医症状ID获取详细信息")
    @ApiImplicitParam(name = "wmSymptomId", value = "西医症状ID（如WM_Symptom_ID01）", required = true, dataType = "string", paramType = "path")
    public AjaxResult getDetail(@PathVariable(value = "wmSymptomId") String wmSymptomId) {
        WmSymptom wmSymptom = wmSymptomService.selectWmSymptomById(wmSymptomId);
        if (wmSymptom == null) {
            return AjaxResult.error("西医症状不存在");
        }
        return AjaxResult.success(wmSymptom);
    }

    /**
     * 3. 获取西医症状对应的中医症状（分页）
     */
    @Anonymous
    @GetMapping("/{wmSymptomId}/tcm-symptoms")
    @ApiOperation(value = "获取西医症状对应的中医症状", notes = "查询与该西医症状相关的中医症状")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "wmSymptomId", value = "西医症状ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getTcmSymptoms(
            @PathVariable(value = "wmSymptomId") String wmSymptomId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        List<TcmSymptom> list = wmSymptomService.selectTcmSymptomsByWmSymptomId(wmSymptomId);
        return getDataTable(list);
    }

    /**
     * 4. 获取西医症状关联的基因（分页）
     */
    @Anonymous
    @GetMapping("/{wmSymptomId}/genes")
    @ApiOperation(value = "获取西医症状关联的基因", notes = "查询与该西医症状相关的基因/靶标")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "wmSymptomId", value = "西医症状ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getGenes(
            @PathVariable(value = "wmSymptomId") String wmSymptomId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        List<com.tcmseek.pojo.entity.Target> list = wmSymptomService.selectGenesByWmSymptomId(wmSymptomId);
        return getDataTable(list);
    }
}



