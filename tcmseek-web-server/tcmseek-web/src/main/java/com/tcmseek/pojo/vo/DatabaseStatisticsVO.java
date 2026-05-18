package com.tcmseek.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 数据库统计信息VO
 */
@Data
@ApiModel(description = "数据库统计信息")
public class DatabaseStatisticsVO {

    @ApiModelProperty(value = "中药总数（核心+其他）")
    private Long herbsCount;

    @ApiModelProperty(value = "方剂总数")
    private Long formulasCount;

    @ApiModelProperty(value = "症状总数")
    private Long symptomsCount;

    @ApiModelProperty(value = "证候总数")
    private Long syndromesCount;

    @ApiModelProperty(value = "化合物总数")
    private Long compoundsCount;

    @ApiModelProperty(value = "靶标总数")
    private Long targetsCount;

    @ApiModelProperty(value = "疾病总数")
    private Long diseasesCount;

    @ApiModelProperty(value = "医案总数")
    private Long casesCount;
}

