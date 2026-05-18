package com.tcmseek.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "中药材核心信息实体类")
public class coreTcmHerbs {

    @ApiModelProperty(value = "主键ID", example = "1")
    private int id;

    @ApiModelProperty(value = "中药ID", example = "HERB_1")
    private String tcmHerbId;

    @ApiModelProperty(value = "中文名称", example = "制大黄")
    private String herbNameZh;

    @ApiModelProperty(value = "拼音名称", example = "ZHI DA HUANG")
    private String pinyinName;

    @ApiModelProperty(value = "拉丁学名", example = "Radix et Rhizoma Rhei Praeparata")
    private String latinName;

    @ApiModelProperty(value = "英文名称", example = "Prepared root and rhziome of Sorrel Rhubarb")
    private String englishName;

    @ApiModelProperty(value = "类型", example = "Plant medicine")
    private String type;

    @ApiModelProperty(value = "中文功效", example = "大补元气")
    private String efficacyZh;

    @ApiModelProperty(value = "英文功效", example = "Treatment of fever with constipation, retention of the feces and abdominal pain, dysentery with inadequate discharge from the bowels, jaundice caused by damp-heat, haematemesis, epistaxis, inflammation of eye and swelling of the throat due to heat in the blood, appendicitis with abdominal pain, boils, sores and abscess, amenorrhea due to blood stasis, traumatic injuries, hemorrhage from the upper gastrointestinal tract. external use for scalds and burns.radixet rhizoma rhei (stir-fried with wine) inflammation of the eye, swelling of the throat and painful swelling of the gums. radix et rhizoma rhei (prepared) boils, sores and abscess. radix et rhizoma rhei (carbonized) hemorrhage with blood stasis due to heat in the blood.")
    private String functionEn;

    @ApiModelProperty(value = "功效分类", example = "泻下药")
    private String efficacyCategory;

    @ApiModelProperty(value = "中文毒性", example = "无毒")
    private String toxicityZh;

    @ApiModelProperty(value = "英文毒性", example = "Non-toxic")
    private String toxicEn;

    @ApiModelProperty(value = "中文毒性描述", example = "无明显毒性")
    private String toxicDescriptionZh;

    @ApiModelProperty(value = "毒性效应（英文）", example = "Safe for long-term use")
    private String toxicEffectEn;

    @ApiModelProperty(value = "药典收录情况", example = "中国药典2020版")
    private String pharmacopoeiaRecord;

    @ApiModelProperty(value = "生物学分类英文（科名）", example = "植物药")
    private String classification;

    @ApiModelProperty(value = "生物学分类中文（科名）", example = "植物药")
    private String classificationZh;

    @ApiModelProperty(value = "用药部位", example = "根")
    private String usePart;

    @ApiModelProperty(value = "性味（中文）", example = "甘、微苦，微温")
    private String natureTasteZh;

    @ApiModelProperty(value = "性味（英文）", example = "Warming, Tonifying")
    private String propertyEn;

    @ApiModelProperty(value = "归经（中文）", example = "脾、肺、心经")
    private String meridianZh;

    @ApiModelProperty(value = "归经（英文）", example = "Spleen, Lung, Heart Meridians")
    private String meridianTropismEn;

    @ApiModelProperty(value = "主治（中文）", example = "体虚欲脱，肢冷脉微")
    private String indicationsZh;

    @ApiModelProperty(value = "主治（英文）", example = "Collapse from deficiency, Cold limbs")
    private String indicationEn;
}

//    private int id;
//    private String tcmHerbId;
//    private String herbNameZh;
//    private String pinyinName;
//    private String latinName;
//    private String englishName;
//    private String type;
//    private String efficacyZh;
//    private String efficacyEn;
//    private String efficacyCategory;
//    private String toxicityZh;
//    private String toxicEn;
//    private String toxicDescriptionZh;
//    private String toxicEffectEh;
//    private String pharmacopoeiaRecord;
//    private String classification;
//    private String usePart;
//    private String natureTasteZh;
//    private String propertyEh;
//    private String meridianZh;
//    private String meridianTropismEn;
//    private String indicationsZh;
//    private String indicationEn;

