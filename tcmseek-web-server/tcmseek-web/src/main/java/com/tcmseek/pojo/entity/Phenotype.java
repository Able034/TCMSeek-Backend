package com.tcmseek.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表型信息实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "表型信息实体类")
public class Phenotype {

    @ApiModelProperty(value = "主键ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "表型ID", example = "HP:0000006")
    private String phenotypeId;

    @ApiModelProperty(value = "表型名称")
    private String phenotypeName;

    @ApiModelProperty(value = "数据来源", example = "HPO")
    private String source;
}



