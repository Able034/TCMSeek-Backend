package com.tcmseek.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 化合物ADMET预测结果实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "化合物ADMET预测结果")
public class CompoundAdmet {

    @ApiModelProperty(value = "主键ID")
    private Integer id;

    @ApiModelProperty(value = "InChIKey")
    private String inchikey;

    @ApiModelProperty(value = "Ames致突变性")
    private Double ames;

    @ApiModelProperty(value = "血脑屏障通透性")
    private Double bbbp;

    @ApiModelProperty(value = "生物利用度")
    private Integer bioavailability;

    @ApiModelProperty(value = "Caco-2通透性")
    private Double caco2;

    @ApiModelProperty(value = "致癌性")
    private Integer carcinogens;

    @ApiModelProperty(value = "微粒体清除率")
    private Double clearanceMicrosome;

    @ApiModelProperty(value = "临床毒性")
    private Integer clintox;

    @ApiModelProperty(value = "CYP1A2抑制")
    private Double cyp1a2Inhibition;

    @ApiModelProperty(value = "CYP2C19抑制")
    private Double cyp2c19Inhibition;

    @ApiModelProperty(value = "CYP2C9抑制")
    private Integer cyp2c9Inhibition;

    @ApiModelProperty(value = "CYP2C9底物")
    private Double cyp2c9Substrate;

    @ApiModelProperty(value = "CYP2D6抑制")
    private Integer cyp2d6Inhibition;

    @ApiModelProperty(value = "CYP2D6底物")
    private Double cyp2d6Substrate;

    @ApiModelProperty(value = "CYP3A4抑制")
    private Double cyp3a4Inhibition;

    @ApiModelProperty(value = "CYP3A4底物")
    private Double cyp3a4Substrate;

    @ApiModelProperty(value = "药物性肝损伤")
    private Double dili;

    @ApiModelProperty(value = "自由溶解度")
    private Double freesolv;

    @ApiModelProperty(value = "hERG阻断")
    private Double hergBlockers;

    @ApiModelProperty(value = "hERG Karim")
    private Double hergKarim;

    @ApiModelProperty(value = "人体肠道吸收")
    private Integer hia;

    @ApiModelProperty(value = "半致死量")
    private Double ld50;

    @ApiModelProperty(value = "亲脂性")
    private Double lipophilicity;

    @ApiModelProperty(value = "PAMPA渗透性")
    private Double pampa;

    @ApiModelProperty(value = "P-糖蛋白底物")
    private Double pgp;

    @ApiModelProperty(value = "血浆蛋白结合率")
    private Double ppbr;

    @ApiModelProperty(value = "皮肤渗透性")
    private Double skin;

    @ApiModelProperty(value = "溶解度")
    private Double solubility;
}

