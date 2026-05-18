package com.tcmseek.dao;

import com.tcmseek.pojo.entity.TcmPrescription;
import com.tcmseek.pojo.entity.TcmSymptom;
import com.tcmseek.pojo.entity.coreTcmHerbs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 中医症状Mapper接口
 */
public interface SymptomMapper {

    /**
     * 查询中医症状列表
     * @param keyword 搜索关键词
     * @param locus 症状部位
     * @param type 症状类型
     * @return 症状列表
     */
    List<TcmSymptom> selectSymptomList(
            @Param("keyword") String keyword,
            @Param("locus") String locus,
            @Param("type") String type
    );

    /**
     * 根据ID查询症状详情
     * @param tcmSymptomId 症状ID
     * @return 症状信息
     */
    TcmSymptom selectSymptomById(@Param("tcmSymptomId") String tcmSymptomId);

    /**
     * 查询症状的相关中药
     * @param tcmSymptomId 症状ID
     * @return 中药列表
     */
    List<coreTcmHerbs> selectHerbsBySymptomId(@Param("tcmSymptomId") String tcmSymptomId);

    /**
     * 查询症状的相关方剂
     * @param tcmSymptomId 症状ID
     * @return 方剂列表
     */
    List<TcmPrescription> selectPrescriptionsBySymptomId(@Param("tcmSymptomId") String tcmSymptomId);

    /**
     * 查询症状的关联证候
     * @param tcmSymptomId 症状ID
     * @return 证候列表
     */
    List<com.tcmseek.pojo.entity.TcmSyndrome> selectSyndromesBySymptomId(@Param("tcmSymptomId") String tcmSymptomId);

    /**
     * 根据中药ID查询相关症状
     * @param tcmHerbId 中药ID
     * @return 症状列表
     */
    List<TcmSymptom> selectSymptomsByHerbId(@Param("tcmHerbId") String tcmHerbId);

    /**
     * 查询中医症状的关联西医症状
     * @param tcmSymptomId 中医症状ID
     * @return 西医症状列表
     */
    List<com.tcmseek.pojo.entity.WmSymptom> selectWmSymptomsBySymptomId(@Param("tcmSymptomId") String tcmSymptomId);
}

