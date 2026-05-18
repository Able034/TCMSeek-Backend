package com.tcmseek.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 中医症状实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "中医症状信息实体类")
public class TcmSymptom {

    @ApiModelProperty(value = "主键ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "症状ID", example = "TCM_Symptom1")
    private String tcmSymptomId;

    @ApiModelProperty(value = "症状名称（中文）", example = "嗳气")
    private String symptomNameZh;

    @ApiModelProperty(value = "拼音", example = "Ài Qì")
    private String symptomPinyin;

    @ApiModelProperty(value = "症状定义")
    private String symptomDefinition;

    @ApiModelProperty(value = "症状部位", example = "胃脘")
    private String symptomLocus;

    @ApiModelProperty(value = "症状属性")
    private String symptomProperty;

    @ApiModelProperty(value = "类型", example = "Ontological terms")
    private String type;
}

