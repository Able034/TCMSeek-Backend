package com.tcmseek.dao;

import com.tcmseek.pojo.entity.TcmSymptom;
import com.tcmseek.pojo.entity.TcmSyndrome;
import com.tcmseek.pojo.entity.coreTcmHerbs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 证候信息Mapper接口
 */
public interface SyndromeMapper {

    /**
     * 查询证候列表
     * @param keyword 搜索关键词
     * @param category 证候分类
     * @return 证候列表
     */
    List<TcmSyndrome> selectSyndromeList(
            @Param("keyword") String keyword,
            @Param("category") String category
    );

    /**
     * 根据ID查询证候详情
     * @param tcmSyndromeId 证候ID
     * @return 证候信息
     */
    TcmSyndrome selectSyndromeById(@Param("tcmSyndromeId") String tcmSyndromeId);

    /**
     * 查询证候的相关症状
     * @param tcmSyndromeId 证候ID
     * @return 症状列表
     */
    List<TcmSymptom> selectSymptomsBySyndromeId(@Param("tcmSyndromeId") String tcmSyndromeId);

    /**
     * 查询证候的相关中药
     * @param tcmSyndromeId 证候ID
     * @return 中药列表
     */
    List<coreTcmHerbs> selectHerbsBySyndromeId(@Param("tcmSyndromeId") String tcmSyndromeId);

    /**
     * 查询证候的关联方剂
     * @param tcmSyndromeId 证候ID
     * @return 方剂列表
     */
    List<com.tcmseek.pojo.entity.TcmPrescription> selectPrescriptionsBySyndromeId(@Param("tcmSyndromeId") String tcmSyndromeId);

    /**
     * 根据中药ID查询相关证候
     * @param tcmHerbId 中药ID
     * @return 证候列表
     */
    List<TcmSyndrome> selectSyndromesByHerbId(@Param("tcmHerbId") String tcmHerbId);
}

