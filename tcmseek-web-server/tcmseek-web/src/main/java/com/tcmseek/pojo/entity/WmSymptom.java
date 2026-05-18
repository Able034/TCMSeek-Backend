package com.tcmseek.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 西医症状实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "西医症状信息实体类")
public class WmSymptom {

    @ApiModelProperty(value = "主键ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "西医症状ID", example = "WM_Symptom_ID01")
    private String wmSymptomId;

    @ApiModelProperty(value = "症状名称（英文）", example = "Fever")
    private String symptomName;

    @ApiModelProperty(value = "UMLS标识符", example = "C0015967")
    private String umlsId;
}



