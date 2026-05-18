package com.tcmseek.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "TCM处方信息实体类")
public class tcmPrescriptions {

    @ApiModelProperty(value = "主键ID")
    private Integer id;
    @ApiModelProperty(value = "处方ID", example = "TCM_1")
    private String tcmPrescriptionId;
    @ApiModelProperty(value = "处方名称", example = "TCM_1")
    private String nameZh;
    @ApiModelProperty(value = "拼音名称")
    private String pinyinName;
    @ApiModelProperty(value = "来源")
    private String source;
    @ApiModelProperty(value = "症状中文")
    private String indicationsZh;
    @ApiModelProperty(value = "症状英文")
    private String indicationsEn;
    @ApiModelProperty(value = "功效")
    private String effects;
    @ApiModelProperty(value = "功效中文")
    private String effectsZh;
}
