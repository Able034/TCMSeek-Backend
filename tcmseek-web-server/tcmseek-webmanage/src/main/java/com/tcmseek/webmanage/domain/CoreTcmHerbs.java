package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 核心中药信息对象 core_tcm_herbs
 * 
 * @author Able
 * @date 2025-11-13
 */
public class CoreTcmHerbs extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 自增主键 */
    private Long id;

    /** 中药唯一标识ID (HERB_1) */
    @Excel(name = "中药唯一标识ID (HERB_1)")
    private String tcmHerbId;

    /** 中药名称（中文） */
    @Excel(name = "中药名称", readConverterExp = "中=文")
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

    /** 毒性描述（中文） */
    @Excel(name = "毒性描述", readConverterExp = "中=文")
    private String toxicDescriptionZh;

    /** 毒性描述（英文） */
    @Excel(name = "毒性描述", readConverterExp = "英=文")
    private String toxicEffectEn;

    /** 药典收录情况 */
    @Excel(name = "药典收录情况")
    private String pharmacopoeiaRecord;

    /** 生物学分类（科名英文） */
    @Excel(name = "生物学分类", readConverterExp = "科=名英文")
    private String classification;

    /** 使用部位 */
    @Excel(name = "使用部位")
    private String usePart;

    /** 性味（中文） */
    @Excel(name = "性味", readConverterExp = "中=文")
    private String natureTasteZh;

    /** 性味（英文） */
    @Excel(name = "性味", readConverterExp = "英=文")
    private String propertyEn;

    /** 归经（中文） */
    @Excel(name = "归经", readConverterExp = "中=文")
    private String meridianZh;

    /** 归经（英文） */
    @Excel(name = "归经", readConverterExp = "英=文")
    private String meridianTropismEn;

    /** 主治（中文） */
    @Excel(name = "主治", readConverterExp = "中=文")
    private String indicationsZh;

    /** 主治（英文） */
    @Excel(name = "主治", readConverterExp = "英=文")
    private String indicationEn;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private Date createdAt;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private Date updatedAt;

    /** 生物学分类（科名中文） */
    @Excel(name = "生物学分类", readConverterExp = "科=名中文")
    private String classificationZh;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setTcmHerbId(String tcmHerbId) 
    {
        this.tcmHerbId = tcmHerbId;
    }

    public String getTcmHerbId() 
    {
        return tcmHerbId;
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

    public void setClassification(String classification) 
    {
        this.classification = classification;
    }

    public String getClassification() 
    {
        return classification;
    }

    public void setUsePart(String usePart) 
    {
        this.usePart = usePart;
    }

    public String getUsePart() 
    {
        return usePart;
    }

    public void setNatureTasteZh(String natureTasteZh) 
    {
        this.natureTasteZh = natureTasteZh;
    }

    public String getNatureTasteZh() 
    {
        return natureTasteZh;
    }

    public void setPropertyEn(String propertyEn) 
    {
        this.propertyEn = propertyEn;
    }

    public String getPropertyEn() 
    {
        return propertyEn;
    }

    public void setMeridianZh(String meridianZh) 
    {
        this.meridianZh = meridianZh;
    }

    public String getMeridianZh() 
    {
        return meridianZh;
    }

    public void setMeridianTropismEn(String meridianTropismEn) 
    {
        this.meridianTropismEn = meridianTropismEn;
    }

    public String getMeridianTropismEn() 
    {
        return meridianTropismEn;
    }

    public void setIndicationsZh(String indicationsZh) 
    {
        this.indicationsZh = indicationsZh;
    }

    public String getIndicationsZh() 
    {
        return indicationsZh;
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

    public void setClassificationZh(String classificationZh) 
    {
        this.classificationZh = classificationZh;
    }

    public String getClassificationZh() 
    {
        return classificationZh;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("tcmHerbId", getTcmHerbId())
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
            .append("classification", getClassification())
            .append("usePart", getUsePart())
            .append("natureTasteZh", getNatureTasteZh())
            .append("propertyEn", getPropertyEn())
            .append("meridianZh", getMeridianZh())
            .append("meridianTropismEn", getMeridianTropismEn())
            .append("indicationsZh", getIndicationsZh())
            .append("indicationEn", getIndicationEn())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .append("classificationZh", getClassificationZh())
            .toString();
    }
}
