package com.tcmseek.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 中医证候实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "中医证候信息实体类")
public class TcmSyndrome {

    @ApiModelProperty(value = "主键ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "证候ID", example = "TCM_Syndrome1")
    private String tcmSyndromeId;

    @ApiModelProperty(value = "证候名称（中文）", example = "肝郁气滞证")
    private String syndromeNameZh;

    @ApiModelProperty(value = "证候名称（英文）")
    private String syndromeEnglish;

    @ApiModelProperty(value = "拼音")
    private String syndromePinyin;

    @ApiModelProperty(value = "证候定义（中文）")
    private String syndromeDefinitionZh;

    @ApiModelProperty(value = "证候描述（英文）")
    private String syndromeDescriptionEn;

    @ApiModelProperty(value = "分类（中文）")
    private String categoryZh;

    @ApiModelProperty(value = "分类（英文）")
    private String categoryEn;

    @ApiModelProperty(value = "数据来源")
    private String source;
}

