package com.tcmseek.service;

import com.tcmseek.pojo.vo.DatabaseStatisticsVO;

import java.util.List;
import java.util.Map;

/**
 * 统计信息Service接口
 */
public interface StatisticsService {

    DatabaseStatisticsVO getDatabaseStatistics();

    List<Map<String, Object>> getHerbCategories();

    List<Map<String, Object>> getPrescriptionSources();
}

