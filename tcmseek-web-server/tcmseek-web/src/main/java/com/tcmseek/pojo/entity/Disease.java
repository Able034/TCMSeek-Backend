package com.tcmseek.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 疾病信息实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "疾病信息实体类")
public class Disease {

    @ApiModelProperty(value = "主键ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "疾病ID", example = "DOID:11832")
    private String diseaseId;

    @ApiModelProperty(value = "疾病名称（英文）")
    private String diseaseName;

    @ApiModelProperty(value = "数据来源")
    private String source;
}

