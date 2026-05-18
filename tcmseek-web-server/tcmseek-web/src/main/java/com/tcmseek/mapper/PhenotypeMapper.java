package com.tcmseek.mapper;

import com.tcmseek.pojo.entity.Phenotype;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 表型信息Mapper接口
 */
@Mapper
public interface PhenotypeMapper {

    /**
     * 查询表型列表
     * @param keyword 搜索关键词
     * @return 表型列表（包含关联基因数量）
     */
    List<Map<String, Object>> selectPhenotypeList(@Param("keyword") String keyword);

    /**
     * 根据表型ID查询表型详情
     * @param phenotypeId 表型ID
     * @return 表型详情
     */
    Phenotype selectPhenotypeById(@Param("phenotypeId") String phenotypeId);

    /**
     * 查询表型关联的靶标/基因列表
     * @param phenotypeId 表型ID
     * @param keyword 搜索关键词
     * @return 靶标/基因列表
     */
    List<Map<String, Object>> selectTargetsByPhenotypeId(
            @Param("phenotypeId") String phenotypeId,
            @Param("keyword") String keyword
    );

    /**
     * 统计表型关联的靶标数量
     * @param phenotypeId 表型ID
     * @return 靶标数量
     */
    Integer countTargetsByPhenotypeId(@Param("phenotypeId") String phenotypeId);

    /**
     * 获取基因类型分布
     * @param phenotypeId 表型ID
     * @return 基因类型分布列表
     */
    List<Map<String, Object>> getGeneTypeDistribution(@Param("phenotypeId") String phenotypeId);
}



