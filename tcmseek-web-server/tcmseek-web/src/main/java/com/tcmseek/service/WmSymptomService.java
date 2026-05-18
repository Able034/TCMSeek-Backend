package com.tcmseek.service;

import com.tcmseek.pojo.entity.TcmSymptom;
import com.tcmseek.pojo.entity.Target;
import com.tcmseek.pojo.entity.WmSymptom;

import java.util.List;

/**
 * 西医症状Service接口
 */
public interface WmSymptomService {

    /**
     * 根据关键词查询西医症状列表
     * @param keyword 搜索关键词
     * @return 西医症状列表
     */
    List<WmSymptom> selectWmSymptomList(String keyword);

    /**
     * 根据ID查询西医症状详情
     * @param wmSymptomId 西医症状ID
     * @return 西医症状信息
     */
    WmSymptom selectWmSymptomById(String wmSymptomId);

    /**
     * 根据西医症状ID查询关联的中医症状
     * @param wmSymptomId 西医症状ID
     * @return 中医症状列表
     */
    List<TcmSymptom> selectTcmSymptomsByWmSymptomId(String wmSymptomId);

    /**
     * 根据西医症状ID查询关联的基因/靶标
     * @param wmSymptomId 西医症状ID
     * @return 基因列表
     */
    List<Target> selectGenesByWmSymptomId(String wmSymptomId);
}



