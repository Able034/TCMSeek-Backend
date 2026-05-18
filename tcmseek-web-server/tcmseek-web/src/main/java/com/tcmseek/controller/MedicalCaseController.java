package com.tcmseek.controller;

import com.tcmseek.common.annotation.Anonymous;
import com.tcmseek.common.core.controller.BaseController;
import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.common.core.page.TableDataInfo;
import com.tcmseek.pojo.entity.MedicalCase;
import com.tcmseek.pojo.entity.TcmPrescription;
import com.tcmseek.pojo.entity.coreTcmHerbs;
import com.tcmseek.service.MedicalCaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;


import javax.annotation.Resource;
import java.util.List;

/**
 * 中医医案控制器
 * Medical Case Controller
 */
@Slf4j
@RestController
@RequestMapping("/tcmseek/medical-cases")
@Api(tags = "中医医案接口")
public class MedicalCaseController extends BaseController {

    @Resource
    private MedicalCaseService medicalCaseService;

    /**
     * 1. 获取医案详情
     */
    @Anonymous
    @GetMapping("/{caseId}")
    @ApiOperation(value = "获取医案详情", notes = "根据医案ID查询详细信息")
    @ApiImplicitParam(name = "caseId", value = "医案ID", required = true, dataType = "string", paramType = "path")
    @Cacheable(value = "medicalCase:detail", key = "'medicalCase:' + #caseId", unless = "#result == null")
    public AjaxResult getDetail(@PathVariable("caseId") String caseId) {
        log.info("获取医案详情, caseId: {}", caseId);

        MedicalCase medicalCase = medicalCaseService.selectMedicalCaseById(caseId);

        if (medicalCase == null) {
            log.warn("未找到医案, caseId: {}", caseId);
            return error("未找到该医案");
        }

        log.info("医案详情查询成功: {}", medicalCase.getMedCaseId());
        return success(medicalCase);
    }
//    public Result<MedicalCase> getDetail(@PathVariable("caseId") String caseId) {
//        log.info("获取医案详情, caseId: {}", caseId);
//
//        MedicalCase medicalCase = medicalCaseService.selectMedicalCaseById(caseId);
//
//        if (medicalCase == null) {
//            log.warn("未找到医案, caseId: {}", caseId);
//            return Result.error("未找到该医案");
//        }
//
//        log.info("医案详情查询成功: {}", medicalCase.getMedCaseId());
//        return Result.success(medicalCase);
//    }


    /**
     * 2. 获取医案的关联方剂（分页）
     */
    @Anonymous
    @GetMapping("/{caseId}/prescriptions")
    @ApiOperation(value = "获取医案的关联方剂", notes = "查询医案使用的方剂列表，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "caseId", value = "医案ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getPrescriptions(
            @PathVariable("caseId") String caseId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        log.info("获取医案的关联方剂, caseId: {}, page: {}, pageSize: {}", caseId, currentPage, pageSize);
        
        List<TcmPrescription> list = medicalCaseService.selectPrescriptionsByCaseId(caseId);
        
        log.info("查询到方剂数量: {}", list != null ? list.size() : 0);
        return getDataTable(list);
    }

    /**
     * 3. 获取医案的关联中药（分页）
     */
    @Anonymous
    @GetMapping("/{caseId}/herbs")
    @ApiOperation(value = "获取医案的关联中药", notes = "查询医案使用的中药列表，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "caseId", value = "医案ID", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query")
    })
    public TableDataInfo getHerbs(
            @PathVariable("caseId") String caseId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);
        
        log.info("获取医案的关联中药, caseId: {}, page: {}, pageSize: {}", caseId, currentPage, pageSize);
        
        List<coreTcmHerbs> list = medicalCaseService.selectHerbsByCaseId(caseId);
        
        log.info("查询到中药数量: {}", list != null ? list.size() : 0);
        return getDataTable(list);
    }

    /**
     * 4. 获取医案分页）
     */
    @Anonymous
    @GetMapping("/list")
    @ApiOperation(value = "获取医案列表", notes = "查询医案列表，支持分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageNum", value = "页码", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = false, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "搜索医家、中医病名、西医病名、二边、脉象、舌象" , required = false, dataType = "string", paramType = "query")

    })
    public TableDataInfo list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword

    ) {
        Integer currentPage = (page != null) ? page : (pageNum != null ? pageNum : 1);
        com.github.pagehelper.PageHelper.startPage(currentPage, pageSize);

        List<MedicalCase> list = medicalCaseService.selectMedicalCaseList(keyword);

        return getDataTable(list);
    }
}








