package com.tcmseek.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "中医医案信息实体类")
public class medicalCases {
    @ApiModelProperty(value = "主键ID")
    private Integer id;
    @ApiModelProperty(value = "医案ID", example = "TCM_1")
    private String medCaseId;
    @ApiModelProperty(value = "医案文本")
    private String caseReport;
    @ApiModelProperty(value = "医家")
    private String physician;
    @ApiModelProperty(value = "中医病名")
    private String tcmDisease;
    @ApiModelProperty(value = "西医病名")
    private String wmDisease;
    @ApiModelProperty(value = "中医症状")
    private String tcmSymptoms;
    @ApiModelProperty(value = "西医症状")
    private String wmSymptoms;
    @ApiModelProperty(value = "二辩")
    private String urinationDefecation;
    @ApiModelProperty(value = "脉象")
    private String pulseCondition;
    @ApiModelProperty(value = "舌象")
    private String tongueAppearance;
    @ApiModelProperty(value = "中医证候")
    private String tcmSyndrome;
    @ApiModelProperty(value = "中医治法")
    private String tcmTreatment;
    @ApiModelProperty(value = "方剂")
    private String prescription;
    @ApiModelProperty(value = "中药组成")
    private String herbComposition;
}
