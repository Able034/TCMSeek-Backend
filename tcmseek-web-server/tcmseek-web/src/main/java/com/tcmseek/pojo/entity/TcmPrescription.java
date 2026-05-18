package com.tcmseek.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TCM方剂实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "TCM方剂信息实体类")
public class TcmPrescription {

    @ApiModelProperty(value = "主键ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "方剂ID", example = "TCMSSD59734")
    private String tcmPrescriptionId;

    @ApiModelProperty(value = "方剂名称（中文）", example = "四君子汤")
    private String nameZh;

    @ApiModelProperty(value = "拼音名称", example = "Sì Jūn Zǐ Tāng")
    private String pinyinName;

    @ApiModelProperty(value = "来源典籍", example = "《太平惠民和剂局方》")
    private String source;

    @ApiModelProperty(value = "主治（中文）")
    private String indicationsZh;

    @ApiModelProperty(value = "主治（英文）")
    private String indicationsEn;

    @ApiModelProperty(value = "功效（英文）")
    private String effects;

    @ApiModelProperty(value = "功效（中文）")
    private String effectsZh;
}

