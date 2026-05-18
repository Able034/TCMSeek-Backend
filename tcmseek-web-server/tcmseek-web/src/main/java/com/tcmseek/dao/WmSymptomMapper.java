package com.tcmseek.dao;

import com.tcmseek.pojo.entity.TcmSymptom;
import com.tcmseek.pojo.entity.Target;
import com.tcmseek.pojo.entity.WmSymptom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 西医症状Mapper接口
 */
@Mapper
public interface WmSymptomMapper {

    /**
     * 根据关键词查询西医症状列表
     * @param keyword 搜索关键词
     * @return 西医症状列表
     */
    List<WmSymptom> selectWmSymptomList(@Param("keyword") String keyword);

    /**
     * 根据ID查询西医症状详情
     * @param wmSymptomId 西医症状ID
     * @return 西医症状信息
     */
    WmSymptom selectWmSymptomById(@Param("wmSymptomId") String wmSymptomId);

    /**
     * 根据西医症状ID查询关联的中医症状
     * @param wmSymptomId 西医症状ID
     * @return 中医症状列表
     */
    List<TcmSymptom> selectTcmSymptomsByWmSymptomId(@Param("wmSymptomId") String wmSymptomId);

    /**
     * 根据西医症状ID查询关联的基因/靶标
     * @param wmSymptomId 西医症状ID
     * @return 基因列表
     */
    List<Target> selectGenesByWmSymptomId(@Param("wmSymptomId") String wmSymptomId);
}



