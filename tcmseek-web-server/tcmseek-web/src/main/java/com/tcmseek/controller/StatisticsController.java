package com.tcmseek.controller;

import com.tcmseek.common.annotation.Anonymous;
import com.tcmseek.common.core.controller.BaseController;
import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.pojo.vo.DatabaseStatisticsVO;
import com.tcmseek.service.StatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 统计信息Controller
 */
@RestController
@RequestMapping("/tcmseek")
@Slf4j
@Api(tags = "统计信息")
public class StatisticsController extends BaseController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 1. 获取数据库统计信息
     */
    @Anonymous
    @GetMapping("/statistics")
    @ApiOperation(value = "获取数据库统计信息", notes = "返回数据库各类数据的总数统计")
    public AjaxResult getStatistics() {
        DatabaseStatisticsVO statistics = statisticsService.getDatabaseStatistics();
        return AjaxResult.success(statistics);
    }

    /**
     * 2. 获取中药分类统计
     */
    @Anonymous
    @GetMapping("/herbs/categories")
    @ApiOperation(value = "获取中药分类统计", notes = "返回各类型中药的数量统计")
    public AjaxResult getHerbCategories() {
        List<Map<String, Object>> categories = statisticsService.getHerbCategories();
        return AjaxResult.success(categories);
    }

    /**
     * 3. 获取方剂来源统计
     */
    @Anonymous
    @GetMapping("/prescriptions/sources")
    @ApiOperation(value = "获取方剂来源统计", notes = "返回各来源典籍的方剂数量统计（Top 50）")
    public AjaxResult getPrescriptionSources() {
        List<Map<String, Object>> sources = statisticsService.getPrescriptionSources();
        return AjaxResult.success(sources);
    }
}

