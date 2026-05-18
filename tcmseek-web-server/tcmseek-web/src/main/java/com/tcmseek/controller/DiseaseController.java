package com.tcmseek.controller;

import com.tcmseek.common.annotation.Anonymous;
import com.tcmseek.common.core.controller.BaseController;
import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.common.core.page.TableDataInfo;
import com.tcmseek.pojo.entity.Disease;
import com.tcmseek.pojo.entity.TcmPrescription;
import com.tcmseek.pojo.entity.coreTcmHerbs;
import com.tcmseek.service.DiseaseService;
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
 * 疾病信息Controller
 */
@RestController
@RequestMapping("/tcmseek/diseases")
@Slf4j
@Api(tags = "疾病信息")
public class DiseaseController extends BaseController {

    @Autowired
    private DiseaseService diseaseService;

    /**
     * 获取疾病详情
     */
    @Anonymous
    @GetMapping("/{diseaseId}")
    @ApiOperation(value = "获取疾病详情", notes = "根据疾病ID获取详细信息")
    @ApiImplicitParam(name = "diseaseId", value = "疾病ID（如DOID:11832）", required = true, dataType = "string", paramType = "path")
    @Cacheable(value = "disease:detail" , key = "'disease' + #diseaseId" ,unless = "#result == null ")
    public AjaxResult getDetail(@PathVariable("diseaseId") String diseaseId) {
        log.info("获取疾病详情, diseaseId: {}", diseaseId);
        Disease disease = diseaseService.selectDiseaseById(diseaseId);
        if (disease == null) {
            log.warn("疾病不存在, diseaseId: {}", diseaseId);
            return AjaxResult.error("疾病不存在");
        }
        return AjaxResult.success(disease);
    }

    /**
     * 获取疾病的基因关联
     */
    @Anonymous
    @GetMapping("/{diseaseId}/genes")
    @ApiOperation(value = "获取疾病的基因关联", notes = "查询与该疾病相关的基因/靶标")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "diseaseId", value = "疾病ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getGenes(
            @PathVariable("diseaseId") String diseaseId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        log.info("获取疾病的基因关联, diseaseId: {}, page: {}, pageSize: {}", diseaseId, currentPage, pageSize);
        
        List<Map<String, Object>> list = diseaseService.selectGenesByDiseaseId(diseaseId);
        
        log.info("查询到基因数量: {}", list != null ? list.size() : 0);
        return getDataTable(list);
    }

    /**
     * 获取疾病的方剂关联
     */
    @Anonymous
    @GetMapping("/{diseaseId}/prescriptions")
    @ApiOperation(value = "获取疾病的方剂关联", notes = "查询可治疗该疾病的方剂")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "diseaseId", value = "疾病ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getPrescriptions(
            @PathVariable("diseaseId") String diseaseId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        log.info("获取疾病的方剂关联, diseaseId: {}, page: {}, pageSize: {}", diseaseId, currentPage, pageSize);
        
        List<TcmPrescription> list = diseaseService.selectPrescriptionsByDiseaseId(diseaseId);
        
        log.info("查询到方剂数量: {}", list != null ? list.size() : 0);
        return getDataTable(list);
    }

    /**
     * 获取疾病的中药关联
     */
    @Anonymous
    @GetMapping("/{diseaseId}/herbs")
    @ApiOperation(value = "获取疾病的中药关联", notes = "查询可治疗该疾病的中药")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "diseaseId", value = "疾病ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getHerbs(
            @PathVariable("diseaseId") String diseaseId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        log.info("获取疾病的中药关联, diseaseId: {}, page: {}, pageSize: {}", diseaseId, currentPage, pageSize);
        
        List<coreTcmHerbs> list = diseaseService.selectHerbsByDiseaseId(diseaseId);
        
        log.info("查询到中药数量: {}", list != null ? list.size() : 0);
        return getDataTable(list);
    }
}



