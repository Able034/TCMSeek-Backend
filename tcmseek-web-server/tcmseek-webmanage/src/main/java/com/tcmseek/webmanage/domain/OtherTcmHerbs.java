package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 其他中药信息对象 other_tcm_herbs
 * 
 * @author Able
 * @date 2025-11-13
 */
public class OtherTcmHerbs extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 序号 */
    @Excel(name = "序号")
    private Long id;

    /** 其他中药ID (HERB2_1) */
    @Excel(name = "其他中药ID (HERB2_1)")
    private String tcmHerb2Id;

    /** 药材名称 */
    @Excel(name = "药材名称")
    private String herbNameZh;

    /** 拼音名称 */
    @Excel(name = "拼音名称")
    private String pinyinName;

    /** 拉丁名称 */
    @Excel(name = "拉丁名称")
    private String latinName;

    /** 英文名称 */
    @Excel(name = "英文名称")
    private String englishName;

    /** 药材类型 */
    @Excel(name = "药材类型")
    private String type;

    /** 功效（中文） */
    @Excel(name = "功效", readConverterExp = "中=文")
    private String efficacyZh;

    /** 功效（英文） */
    @Excel(name = "功效", readConverterExp = "英=文")
    private String functionEn;

    /** 功效分类 */
    @Excel(name = "功效分类")
    private String efficacyCategory;

    /** 毒性（中文） */
    @Excel(name = "毒性", readConverterExp = "中=文")
    private String toxicityZh;

    /** 毒性（英文） */
    @Excel(name = "毒性", readConverterExp = "英=文")
    private String toxicEn;

    /** 毒性描述 */
    @Excel(name = "毒性描述")
    private String toxicDescriptionZh;

    /** 毒性效应 */
    @Excel(name = "毒性效应")
    private String toxicEffectEn;

    /** 药典收录 */
    @Excel(name = "药典收录")
    private String pharmacopoeiaRecord;

    /** 生物学分类（中文） */
    @Excel(name = "生物学分类", readConverterExp = "中=文")
    private String classificationZh;

    /** 生物学分类（英文） */
    @Excel(name = "生物学分类", readConverterExp = "英=文")
    private String classificationEn;

    /** 使用部位 */
    @Excel(name = "使用部位")
    private String usePartEn;

    /** 性味（中文） */
    @Excel(name = "性味", readConverterExp = "中=文")
    private String propertyZh;

    /** 性味（英文） */
    @Excel(name = "性味", readConverterExp = "英=文")
    private String propertyEn;

    /** 归经（中文） */
    @Excel(name = "归经", readConverterExp = "中=文")
    private String meridianTropismZh;

    /** 归经（英文） */
    @Excel(name = "归经", readConverterExp = "英=文")
    private String meridianTropismEn;

    /** 主治（中文） */
    @Excel(name = "主治", readConverterExp = "中=文")
    private String indicationZh;

    /** 主治（英文） */
    @Excel(name = "主治", readConverterExp = "英=文")
    private String indicationEn;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date updatedAt;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setTcmHerb2Id(String tcmHerb2Id) 
    {
        this.tcmHerb2Id = tcmHerb2Id;
    }

    public String getTcmHerb2Id() 
    {
        return tcmHerb2Id;
    }

    public void setHerbNameZh(String herbNameZh) 
    {
        this.herbNameZh = herbNameZh;
    }

    public String getHerbNameZh() 
    {
        return herbNameZh;
    }

    public void setPinyinName(String pinyinName) 
    {
        this.pinyinName = pinyinName;
    }

    public String getPinyinName() 
    {
        return pinyinName;
    }

    public void setLatinName(String latinName) 
    {
        this.latinName = latinName;
    }

    public String getLatinName() 
    {
        return latinName;
    }

    public void setEnglishName(String englishName) 
    {
        this.englishName = englishName;
    }

    public String getEnglishName() 
    {
        return englishName;
    }

    public void setType(String type) 
    {
        this.type = type;
    }

    public String getType() 
    {
        return type;
    }

    public void setEfficacyZh(String efficacyZh) 
    {
        this.efficacyZh = efficacyZh;
    }

    public String getEfficacyZh() 
    {
        return efficacyZh;
    }

    public void setFunctionEn(String functionEn) 
    {
        this.functionEn = functionEn;
    }

    public String getFunctionEn() 
    {
        return functionEn;
    }

    public void setEfficacyCategory(String efficacyCategory) 
    {
        this.efficacyCategory = efficacyCategory;
    }

    public String getEfficacyCategory() 
    {
        return efficacyCategory;
    }

    public void setToxicityZh(String toxicityZh) 
    {
        this.toxicityZh = toxicityZh;
    }

    public String getToxicityZh() 
    {
        return toxicityZh;
    }

    public void setToxicEn(String toxicEn) 
    {
        this.toxicEn = toxicEn;
    }

    public String getToxicEn() 
    {
        return toxicEn;
    }

    public void setToxicDescriptionZh(String toxicDescriptionZh) 
    {
        this.toxicDescriptionZh = toxicDescriptionZh;
    }

    public String getToxicDescriptionZh() 
    {
        return toxicDescriptionZh;
    }

    public void setToxicEffectEn(String toxicEffectEn) 
    {
        this.toxicEffectEn = toxicEffectEn;
    }

    public String getToxicEffectEn() 
    {
        return toxicEffectEn;
    }

    public void setPharmacopoeiaRecord(String pharmacopoeiaRecord) 
    {
        this.pharmacopoeiaRecord = pharmacopoeiaRecord;
    }

    public String getPharmacopoeiaRecord() 
    {
        return pharmacopoeiaRecord;
    }

    public void setClassificationZh(String classificationZh) 
    {
        this.classificationZh = classificationZh;
    }

    public String getClassificationZh() 
    {
        return classificationZh;
    }

    public void setClassificationEn(String classificationEn) 
    {
        this.classificationEn = classificationEn;
    }

    public String getClassificationEn() 
    {
        return classificationEn;
    }

    public void setUsePartEn(String usePartEn) 
    {
        this.usePartEn = usePartEn;
    }

    public String getUsePartEn() 
    {
        return usePartEn;
    }

    public void setPropertyZh(String propertyZh) 
    {
        this.propertyZh = propertyZh;
    }

    public String getPropertyZh() 
    {
        return propertyZh;
    }

    public void setPropertyEn(String propertyEn) 
    {
        this.propertyEn = propertyEn;
    }

    public String getPropertyEn() 
    {
        return propertyEn;
    }

    public void setMeridianTropismZh(String meridianTropismZh) 
    {
        this.meridianTropismZh = meridianTropismZh;
    }

    public String getMeridianTropismZh() 
    {
        return meridianTropismZh;
    }

    public void setMeridianTropismEn(String meridianTropismEn) 
    {
        this.meridianTropismEn = meridianTropismEn;
    }

    public String getMeridianTropismEn() 
    {
        return meridianTropismEn;
    }

    public void setIndicationZh(String indicationZh) 
    {
        this.indicationZh = indicationZh;
    }

    public String getIndicationZh() 
    {
        return indicationZh;
    }

    public void setIndicationEn(String indicationEn) 
    {
        this.indicationEn = indicationEn;
    }

    public String getIndicationEn() 
    {
        return indicationEn;
    }

    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() 
    {
        return createdAt;
    }

    public void setUpdatedAt(Date updatedAt) 
    {
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAt() 
    {
        return updatedAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("tcmHerb2Id", getTcmHerb2Id())
            .append("herbNameZh", getHerbNameZh())
            .append("pinyinName", getPinyinName())
            .append("latinName", getLatinName())
            .append("englishName", getEnglishName())
            .append("type", getType())
            .append("efficacyZh", getEfficacyZh())
            .append("functionEn", getFunctionEn())
            .append("efficacyCategory", getEfficacyCategory())
            .append("toxicityZh", getToxicityZh())
            .append("toxicEn", getToxicEn())
            .append("toxicDescriptionZh", getToxicDescriptionZh())
            .append("toxicEffectEn", getToxicEffectEn())
            .append("pharmacopoeiaRecord", getPharmacopoeiaRecord())
            .append("classificationZh", getClassificationZh())
            .append("classificationEn", getClassificationEn())
            .append("usePartEn", getUsePartEn())
            .append("propertyZh", getPropertyZh())
            .append("propertyEn", getPropertyEn())
            .append("meridianTropismZh", getMeridianTropismZh())
            .append("meridianTropismEn", getMeridianTropismEn())
            .append("indicationZh", getIndicationZh())
            .append("indicationEn", getIndicationEn())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .toString();
    }
}
